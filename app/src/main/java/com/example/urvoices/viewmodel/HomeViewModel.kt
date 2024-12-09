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
import com.example.urvoices.data.service.FirebaseBlockService
import com.example.urvoices.utils.FollowState
import com.example.urvoices.utils.MessagingService
import com.example.urvoices.utils.SharedPreferencesHelper
import com.example.urvoices.utils.SharedPreferencesKeys
import com.example.urvoices.utils.SharedPreferencesKeys.isFirstTime
import com.example.urvoices.utils.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
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
import kotlinx.coroutines.tasks.await
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
    val postList: StateFlow<PagingData<Post>> = _postList.asStateFlow()

    val currentUser = mutableStateOf(auth.currentUser)

    init {
        viewModelScope.launch {
            sendRegistrationToServer()
        }
        loadData()
    }

    private fun loadData(){
        viewModelScope.launch {
           fetchPosts()
        }
    }

    fun refreshHomeScreen(){
        clearData()
        setIsRefreshing(true)
        loadData()
    }


    private suspend fun fetchPosts() {
        val lastVisiblePost = mutableStateOf<String>("")
        val lastVisiblePage = mutableIntStateOf(1)
        val postsPaging3 = Pager(PagingConfig(
            pageSize = 4,
            prefetchDistance = 2,
            enablePlaceholders = false,
        )) {
            postRepository.getNewFeed(lastVisiblePage, lastVisiblePost)
        }.flow
            .map { pagingData -> // filter out blocked users and apply other conditions
                pagingData.filter { post ->
                    val isCurrentUser = post.userId == currentUser.value!!.uid
                    val blockStatus = blockRepository.getBlockStatusFromFirebase(post.userId) == FirebaseBlockService.BlockInfo.NO_BLOCK //no block
                    val privateStatus = userRepository.getUserPrivateAccountStatus(post.userId) == false // if user is not private
                    val followStatus = userRepository.getFollowStatus(post.userId) == FollowState.FOLLOW // if user is private, only show posts from users that current user follows

                    //if no block or isCurrentUser or not private or followStatus = true
                    blockStatus && (isCurrentUser || privateStatus || followStatus)
                }
            }
            .cachedIn(viewModelScope)
        postsPaging3.collect {
            _postList.value = it
        }
    }


    fun setIsRefreshing(value: Boolean){
        isRefreshing.value = value
    }

    private fun clearData(){
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
//                checkTokenSaved()
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

    suspend fun sendRegistrationToServer() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()

//            Log.e(TAG, "sendRegistrationToServer: $token")
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val userDocRef = FirebaseFirestore.getInstance().collection("userTokens").document(user.uid)
                val userDoc = userDocRef.get().await()
//                Log.e(TAG, "sendRegistrationToServer: ${userDoc.exists()}")
                if (userDoc.exists()) {
                    val existingTokens = userDoc.get("token") as? List<String> ?: emptyList()
                    if (!existingTokens.contains(token)) {
                        val updatedTokens = existingTokens.toMutableList().apply { add(token) }
                        userDocRef.set(mapOf("token" to updatedTokens), SetOptions.merge())
                            .addOnSuccessListener {
                                Log.e(TAG, "sendRegistrationToServer: Token added successfully")
                            }
                            .addOnFailureListener {
                                Log.e(TAG, "sendRegistrationToServer: ${it.message}")
                            }
                    } else {
                        Log.e(TAG, "sendRegistrationToServer: Token already exists")
                    }
                } else {
                    userDocRef.set(mapOf("token" to listOf(token)), SetOptions.merge())
                        .addOnSuccessListener {
                            Log.e(TAG, "sendRegistrationToServer: Token added successfully")
                        }
                        .addOnFailureListener {
                            Log.e(TAG, "sendRegistrationToServer: ${it.message}")
                        }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendRegistrationToServer: ${e.message}")
        }
    }

}

sealed class HomeState {
    object Initial : HomeState()
    object Working : HomeState()
    object Successful : HomeState()
    data class Error(val message: String) : HomeState()
}