package com.example.urvoices.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.urvoices.data.db.Entity.BlockedUser
import com.example.urvoices.data.db.Entity.DeletedPost
import com.example.urvoices.data.repository.BlockRepository
import com.example.urvoices.data.repository.DeletedPostRepository
import com.example.urvoices.data.repository.PostRepository
import com.example.urvoices.data.repository.UserRepository
import com.example.urvoices.utils.SharedPreferencesHelper
import com.example.urvoices.utils.SharedPreferencesKeys
import com.example.urvoices.utils.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
	@ApplicationContext context: Context,
	private val blockRepository: BlockRepository,
	private val postRepository: PostRepository,
	private val deletedPostRepository: DeletedPostRepository,
	private val userRepository: UserRepository,
	private val sharedPrefHelper: SharedPreferencesHelper,
	private val auth: FirebaseAuth,
	savedStateHandle: SavedStateHandle
): ViewModel(){
	val TAG = "SettingViewModel"
	private val _state = MutableStateFlow<SettingState>(SettingState.Initial)
	val state: StateFlow<SettingState> = _state.asStateFlow()

	val blockedUsers = blockRepository.getBlockDataFromLocal().cachedIn(viewModelScope)
	var savedPosts = postRepository.getSavedPostDataFromLocal().cachedIn(viewModelScope)
	val deletedPosts: Flow<PagingData<DeletedPost>> = deletedPostRepository.getDeletedPostsFlow().cachedIn(viewModelScope)

	//load blocks list from dao

	@OptIn(SavedStateHandleSaveableApi::class)
	var isShareLoving by savedStateHandle.saveable { mutableStateOf(false) }
	@OptIn(SavedStateHandleSaveableApi::class)
	var isPrivate by savedStateHandle.saveable { mutableStateOf(false) }
	val userPref = UserPreferences(context)
	val userID = runBlocking {
		userPref.userIdFlow.first()
	}
	val username = runBlocking {
		userPref.userNameFlow.first()
	}

	fun savePost(postID: String, callback: (Boolean?) -> Unit){
		try {
			viewModelScope.launch {
				val result = postRepository.savePost(postID)
				callback(result)
			}
		} catch (e: Exception) {
			Log.e(TAG, "savePost Func Failed: ${e.message}")
		}
	}

	fun unblockUser(blockedUser: BlockedUser){
		viewModelScope.launch {
			_state.value = SettingState.Loading
			try {
				val result = blockRepository.unblockUser(blockedUser.userID)
				if(result.isNotEmpty()){
					_state.value = SettingState.Loaded
				}else{
					_state.value = SettingState.Error
				}
			} catch (e: Exception) {
				_state.value = SettingState.Error
			}
		}
	}

	fun syncBlockData(){
		viewModelScope.launch {
			_state.value = SettingState.Loading
			try {
				blockRepository.syncBlockUsers()
				_state.value = SettingState.Loaded
			} catch (e: Exception) {
				_state.value = SettingState.Error
			}
		}
	}

	fun syncSavedPostData(){
		viewModelScope.launch {
			_state.value = SettingState.Loading
			try {
				postRepository.syncSavedPosts()
				_state.value = SettingState.Loaded
			} catch (e: Exception) {
				_state.value = SettingState.Error
			}
		}
	}

	fun getSetting(){
		getShareLoving()
		getPrivateAccount()
	}

	//SharedPreferencesHelper Action
	fun privateAccountChange(allow: Boolean){
		sharedPrefHelper.save(SharedPreferencesKeys.privateAccount, allow, userID)
		isPrivate = allow
		saveSettingToFirebase()
	}

	private fun getPrivateAccount(){
		isPrivate = sharedPrefHelper.getBoolean(SharedPreferencesKeys.privateAccount, userId = userID)
	}

	fun shareLovingChange(allow: Boolean){
		sharedPrefHelper.save(SharedPreferencesKeys.shareLoving, allow, userID)
		isShareLoving = allow
		saveSettingToFirebase()
	}
	private fun getShareLoving(){
		isShareLoving = sharedPrefHelper.getBoolean(SharedPreferencesKeys.shareLoving, userId = userID)
	}

	private fun saveSettingToFirebase(){
		viewModelScope.launch {
			//save setting to firebase
			userRepository.saveUserSettings()
		}
	}

	/*
	   DeletedPost

	 */
	fun refreshDeletedPosts(){
		viewModelScope.launch {
			_state.value = SettingState.Loading
			try {
				deletedPostRepository.fetchNewDeletedPosts()
				_state.value = SettingState.Loaded
			} catch (e: Exception) {
				_state.value = SettingState.Error
			}
		}
	}

	fun restoreDeletedPost(deletedPost: DeletedPost){
		viewModelScope.launch {
			_state.value = SettingState.Loading
			try {
				deletedPostRepository.restorePost(deletedPost)
				_state.value = SettingState.Loaded
			} catch (e: Exception) {
				_state.value = SettingState.Error
			}
		}
	}

	//Check Login Provider
	fun checkIsEmailPasswordLogin(): Boolean{
		val currentUser = auth.currentUser
		var isEmailPassword = false
		if(currentUser != null){
			val providerData = currentUser.providerData
			for (profile in providerData) {
				val providerId = profile.providerId
				if(providerId == "password"){
					isEmailPassword = true
				}
			}
		}
		return isEmailPassword
	}

}

sealed class SettingState{
	object Initial: SettingState()
	object Loading: SettingState()
	object Loaded: SettingState()
	object Error: SettingState()
}