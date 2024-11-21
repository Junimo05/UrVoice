package com.example.urvoices.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.example.urvoices.data.model.Post
import com.example.urvoices.data.repository.BlockRepository
import com.example.urvoices.data.repository.PostRepository
import com.example.urvoices.data.repository.UserRepository
import com.example.urvoices.utils.SharedPreferencesHelper
import com.example.urvoices.utils.SharedPreferencesKeys
import com.example.urvoices.utils.SharedPreferencesKeys.isFirstTime
import com.example.urvoices.utils.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@SuppressLint("MutableCollectionMutableState")
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val postRepository: PostRepository,
    private val blockRepository: BlockRepository,
    private val userRepository: UserRepository,
    private val sharedPrefs: SharedPreferencesHelper,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
): ViewModel(){

    val TAG = "HomeViewModel"
    private val _homeState = MutableStateFlow<HomeState>(HomeState.Initial)
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()
    val isRefreshing = MutableStateFlow(false)
    private val _scrollToTopEvent = MutableStateFlow(false)
    val scrollToTopEvent: StateFlow<Boolean> = _scrollToTopEvent
    fun triggerScrollToTop() { _scrollToTopEvent.value = true }
    fun resetScrollToTopEvent() { _scrollToTopEvent.value = false }

    private val _postList = MutableStateFlow<PagingData<Post>>(PagingData.empty())
    val postList: StateFlow<PagingData<Post>> = _postList

    val currentUser = mutableStateOf(auth.currentUser)

    init {
        loadData()
    }

    private fun loadData(){
        viewModelScope.launch {
           fetchPosts()
        }
    }

    fun refreshHomeScreen(){
        setIsRefreshing(true)
        viewModelScope.launch {
            fetchPosts()
        }
    }


    private suspend fun fetchPosts(){
        val lastVisiblePost = mutableStateOf<String>("")
        val lastVisiblePage = mutableIntStateOf(1)
        val postsPaging3 = Pager(PagingConfig(
            pageSize = 4,
            prefetchDistance = 2,
            enablePlaceholders = false,
        )){
            postRepository.getNewFeed(lastVisiblePage, lastVisiblePost)
        }.flow
            .map {pagingData -> //filter out blocked users
                pagingData.filter {
                    post -> !blockRepository.getBlockStatus(post.userId)
                }
            }
            .cachedIn(viewModelScope)
            .collect{
                _postList.value = it
            }
    }

    fun setIsRefreshing(value: Boolean){
        isRefreshing.value = value
    }

    fun clearData(){
        _postList.value = PagingData.empty()
    }

    suspend fun getUserInfo(userID: String): Map<String, String> {
        val result = postRepository.getUserBaseInfo(userID)
        return result
    }

    /*
        Check First Login in App and Generate SharedPrefs Base
    */
    private fun sharedBaseGenerate(){
        //shareLoving
        sharedPrefs.save(SharedPreferencesKeys.shareLoving, true, userId = currentUser.value!!.uid)
        //private Account
        sharedPrefs.save(SharedPreferencesKeys.privateAccount, false, currentUser.value!!.uid)

        //Done -> Save To Firebase
        viewModelScope.launch {
            userRepository.saveUserSettings()

        }
    }

    fun checkFirstLogin(){
        val user = auth.currentUser
        if(user != null){
            currentUser.value = user
        }
        //Check First Login/ Fetch Database Setting From Account if exists
        val isFirstTime = sharedPrefs.getBoolean(SharedPreferencesKeys.isFirstTime, true, currentUser.value!!.uid)
        try {
            if(isFirstTime){
                viewModelScope.launch {
                    val result = userRepository.getUserSettingsByID(currentUser.value!!.uid)
                    if (result != null) { //exist
//                        Log.e(TAG, "Exist Settings")
                        sharedPrefs.save(SharedPreferencesKeys.isFirstTime, false, currentUser.value!!.uid)
                        if(result.isNotEmpty()){
                            val mapSettings = result
                            mapSettings.forEach { setting ->
                                sharedPrefs.save(setting.key, setting.value, currentUser.value!!.uid)
                            }
                        }
                    } else { //not exist
//                        Log.e(TAG, "Not Exist Settings")
                        sharedPrefs.save(SharedPreferencesKeys.isFirstTime, false, currentUser.value!!.uid)
                        sharedBaseGenerate()
                    }
                }
            } else {
                //nothing
            }
        }catch (
            ex: Exception
        ){
            ex.printStackTrace()
        }
    }

}

sealed class HomeState {
    object Initial : HomeState()
    object Working : HomeState()
    object Successful : HomeState()
    data class Error(val message: String) : HomeState()
}