package com.example.urvoices.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.urvoices.data.model.Post
import com.example.urvoices.data.model.User
import com.example.urvoices.data.repository.BlockRepository
import com.example.urvoices.data.repository.NotificationRepository
import com.example.urvoices.data.repository.PostRepository
import com.example.urvoices.data.repository.UserRepository
import com.example.urvoices.data.service.FirebaseBlockService
import com.example.urvoices.utils.FollowState
import com.example.urvoices.utils.SharedPreferencesHelper
import com.example.urvoices.utils.SharedPreferencesKeys
import com.example.urvoices.utils.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

val userTemp = User(
    ID = "empty",
    username = "empty",
    email = "empty",
    country = "empty",
    avatarUrl = "empty",
    bio = "empty",
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseFireStore: FirebaseFirestore,
    private val postRepository: PostRepository,
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    val blockRepository: BlockRepository,
    private val auth: FirebaseAuth,
    private val userDataStore: UserPreferences,
    private val sharedPrefs: SharedPreferencesHelper,
    savedStateHandle: SavedStateHandle
): ViewModel(){
    val TAG = "ProfileViewModel"

    private val _uiState = MutableStateFlow<ProfileState>(ProfileState.Initial)
    val uiState: StateFlow<ProfileState> = _uiState.asStateFlow()

    private val _displayUser = MutableStateFlow<User>(userTemp)
    val displayUser: StateFlow<User> = _displayUser.asStateFlow()

    @OptIn(SavedStateHandleSaveableApi::class)
    var isFollowed by savedStateHandle.saveable { mutableStateOf(false) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var followState by savedStateHandle.saveable { mutableStateOf(FollowState.UNFOLLOW) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var followers by savedStateHandle.saveable { mutableIntStateOf(0) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var followings by savedStateHandle.saveable { mutableIntStateOf(0) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var postCounts by savedStateHandle.saveable { mutableIntStateOf(0) }


    @OptIn(SavedStateHandleSaveableApi::class)
    var displayuserID by savedStateHandle.saveable { mutableStateOf("") }

    @OptIn(SavedStateHandleSaveableApi::class)
    var isCurrentUser by savedStateHandle.saveable { mutableStateOf(false) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var currentUserID by savedStateHandle.saveable { mutableStateOf("") }
    var authCurrentUser = auth.currentUser
    @OptIn(SavedStateHandleSaveableApi::class)
    var shareLoving by savedStateHandle.saveable { mutableStateOf(false) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var isPrivate by savedStateHandle.saveable { mutableStateOf(false) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var isBlocked by savedStateHandle.saveable { mutableStateOf(false) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var blockInfo by savedStateHandle.saveable { mutableStateOf("") }
    private var userListenerRegistration: ListenerRegistration? = null

    //Posts
    private val _posts = MutableStateFlow<PagingData<Post>>(PagingData.empty())
    val posts: StateFlow<PagingData<Post>> = _posts.asStateFlow()

    //SavedPosts
    private val _savedPosts = MutableStateFlow<PagingData<Post>>(PagingData.empty())
    val savedPosts: StateFlow<PagingData<Post>> = _savedPosts.asStateFlow()


    fun loadData(userID: String){
        if(displayuserID != userID){
            this.displayuserID = userID
            reloadPost()
            reloadSavedPost()
        }
        currentUserID = authCurrentUser?.uid ?: ""
        if (currentUserID == this.displayuserID){
            isCurrentUser = true
        } else {
            isCurrentUser = false
        }

        _uiState.value = ProfileState.Loading
        viewModelScope.launch {
            loadbaseUserData(userID)
            getBlockStatus(userID)
            getUserPrivacy(userID)
            getFollowStatus(userID)
            getPostCounts(userID)
            getFollowerCount(userID)
            getFollowingCount(userID)
            //listen Change
            listenToUserChanges(userID)
            _uiState.value = ProfileState.Successful
        }
    }

    fun pullToRefresh(){
        clearPostData()
        reloadPost()
        reloadSavedPost()
    }

    private fun reloadPost(){
        viewModelScope.launch {
            fetchPost()
        }
    }

    private fun reloadSavedPost(){
        viewModelScope.launch {
            fetchSavedPost()
        }
    }

    private fun clearPostData(){
        _posts.value = PagingData.empty()
        _savedPosts.value = PagingData.empty()
    }

    private suspend fun fetchPost(){
        val lastVisiblePost = mutableStateOf<String>("")
        val lastVisiblePage = mutableIntStateOf(1)
        val data = Pager(PagingConfig(pageSize = 4)){
            postRepository.getAllPostFromUser(displayuserID, lastVisiblePost, lastVisiblePage)
        }.flow
            .cachedIn(viewModelScope)

        data.collect{
            _posts.value = it
        }
    }

    private suspend fun fetchSavedPost(){
        val lastVisiblePost = mutableStateOf<String>("")
        val lastVisiblePage = mutableIntStateOf(1)
        val data = Pager(PagingConfig(pageSize = 4)){
            postRepository.getAllSavedPostFromUser(displayuserID, lastVisiblePost, lastVisiblePage)
        }.flow
            .cachedIn(viewModelScope)

        data.collect{
            _savedPosts.value = it
        }
    }

    private suspend fun loadbaseUserData(userID: String){
        try {
            val user = userRepository.getInfoUserByUserID(userID)
            if(user != null){
                _displayUser.value = user
            }
        } catch (e: Exception) {
            _uiState.value = ProfileState.Error("Error when loading user data")
            Log.e(TAG, "loadbaseUserData: Error")
        }
    }

    suspend fun loadUserBaseInfo(userID: String): Map<String, String>{
        _uiState.value = ProfileState.Loading
        return withContext(Dispatchers.IO){
            try {
                val result = postRepository.getUserBaseInfo(userID)
                _uiState.value = ProfileState.Successful
                result
            } catch (e: Exception) {
                _uiState.value = ProfileState.Error("Error when loading user base info")
                Log.e(TAG, "loadUserBaseInfo: Error")
                emptyMap()
            }
        }
    }

    fun followUser(){
        try {
            val privateStatus = this.isPrivate
            val targetUserID = this.displayuserID
            val actionUserID = this.currentUserID
            val followSend = when(followState){
                FollowState.FOLLOW -> false //unfollow
                FollowState.UNFOLLOW -> true //follow send
                FollowState.REQUEST_FOLLOW -> false //undo request follow
                else -> false
            }
            _uiState.value = ProfileState.Working
            viewModelScope.launch {
                val actionUsername = userDataStore.userNameFlow.first()!!
                val relaFollowID = userRepository.followUser(userId = targetUserID, followStatus = followSend, isPrivate = privateStatus) //follow Info ID
                if(relaFollowID != ""){ //Success
                    if(followSend){ // true -> do a follow request
                        if(targetUserID != actionUserID){
                            if(isPrivate){ //follow user private
                                isFollowed = false
                                followState = FollowState.REQUEST_FOLLOW
                            }else { //follow user public
                                followers++
                                isFollowed = true
                                followState = FollowState.FOLLOW
                            }
                        }
                        val result = notificationRepository.followUser(targetUserID = targetUserID, actionUsername = actionUsername, followInfoID = relaFollowID, isPrivate = privateStatus)
                        if(result){
                            _uiState.value = ProfileState.Successful
//                        Log.e(TAG, "followUser: Success")
                        }
                    } else { //false -> unfollow or undo request follow
                        if(targetUserID != actionUserID){
                            if(followState == FollowState.FOLLOW){
                                followers--
                            }
                            if(followState == FollowState.REQUEST_FOLLOW){
                                //delete request follow
                               notificationRepository.deleteRequestFollow(
                                    followID = relaFollowID,
                                    targetUserID = targetUserID,
                                    actionUserID = actionUserID
                                )
                            }
                            isFollowed = false
                            followState = FollowState.UNFOLLOW
                        }
                        _uiState.value = ProfileState.Successful
                    }
                } else { //Failed
                    _uiState.value = ProfileState.Error("Error when following user")
                    Log.e(TAG, "followUser: Error")
                }
            }
        } catch (e: Exception) {
            _uiState.value = ProfileState.Error("Error when following user")
            Log.e(TAG, "followUser: Error")
        }
    }

    fun acceptFollowRequest(){

    }

    //Update DATA
    suspend fun updateProfile(
        username: String,
        bio: String,
        country: String,
        avatarUri: Uri = Uri.EMPTY,
    ): Boolean {
        _uiState.value = ProfileState.Working
        val oldUser = displayUser.value.copy()

        return withContext(Dispatchers.IO) {
            try {
                val result = userRepository.updateUser(username, bio, country, avatarUri, oldUser)
                if (result) {
                    _uiState.value = ProfileState.Successful
                    // Update user data
                    _displayUser.value = _displayUser.value.copy(
                        username = username,
                        bio = bio,
                        country = country,
                    )
                    true
                } else {
                    _uiState.value = ProfileState.Error("Error when updating profile")
                    false
                }
            } catch (e: Exception) {
                _uiState.value = ProfileState.Error("Error when updating profile")
                Log.e(TAG, "updateProfile: Error", e)
                false
            }
        }
    }



    //Utils

    private fun listenToUserChanges(userId: String) {
        val userDocumentRef = firebaseFireStore.collection("users").document(userId)
        userListenerRegistration = userDocumentRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listen failed.", error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                if (user != null) {
                    _displayUser.value = user
                    // Update UI here or do something with the updated user
                }
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }

    private fun stopListeningToUserChanges() {
        userListenerRegistration?.remove()
    }

    private fun getFollowStatus(userId: String) {
        viewModelScope.launch {
            try {
                val followStatus = userRepository.getFollowStatus(userId)
                followState = followStatus
                when(followState){
                    FollowState.FOLLOW -> {
                        isFollowed = true
                    }
                    FollowState.UNFOLLOW -> {
                        isFollowed = false
                    }
                    FollowState.REQUEST_FOLLOW -> {
                        isFollowed = false
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ProfileState.Error("Error when loading follow status")
                Log.e(TAG, "getFollowStatus: Error")
            }
        }
    }

    private suspend fun getPostCounts(userId: String) {
        try {
            val postCounts = userRepository.getPostCount(userId)
            this.postCounts = postCounts
        } catch (e: Exception) {
            _uiState.value = ProfileState.Error("Error when loading post counts")
            Log.e(TAG, "getPostCounts: Error")
        }
    }

    private suspend fun getFollowerCount(userId: String) {
        try {
            followers = userRepository.getFollowerCount(userId)
        } catch (e: Exception) {
            _uiState.value = ProfileState.Error("Error when loading follower count")
            Log.e(TAG, "getFollowerCount: Error")
        }
    }

    private suspend fun getFollowingCount(userId: String) {
        try {
            followings = userRepository.getFollowingCount(userId)
        } catch (e: Exception) {
            _uiState.value = ProfileState.Error("Error when loading following count")
            Log.e(TAG, "getFollowingCount: Error")
        }
    }

    /*
        SharedPref
    */
    private suspend fun getUserPrivacy(userID: String){
        try {
                //get from Firebase
            val result = userRepository.getUserSettingsByID(userID)
            if(result != null){
                //load from Map
                isPrivate = result[SharedPreferencesKeys.privateAccount] as Boolean
                shareLoving = result[SharedPreferencesKeys.shareLoving] as Boolean
            }
        } catch (e: Exception) {
            _uiState.value = ProfileState.Error("Error when loading user privacy")
            Log.e(TAG, "getUserPrivacy: Error")
        }
    }

    private suspend fun getBlockStatus(userID: String){
        try {
            //get from Firebase
            val result = blockRepository.getBlockStatusFromFirebase(userID) //local get
            if (result == FirebaseBlockService.BlockInfo.BLOCK) {
                isBlocked = true
                blockInfo = result
            } else if (result == FirebaseBlockService.BlockInfo.BLOCKED) {
                isBlocked = true
                blockInfo = result
            } else {
                isBlocked = false
                blockInfo = result
            }
        } catch (e: Exception) {
            _uiState.value = ProfileState.Error("Error when loading user privacy")
            Log.e(TAG, "getUserPrivacy: Error")
        }
    }

    //override onCleared
    override fun onCleared() {
        super.onCleared()
        stopListeningToUserChanges()
    }
}

sealed class ProfileState {
    object Initial : ProfileState()
    object Loading : ProfileState()
    object Working: ProfileState()
    object Successful : ProfileState()
    data class Error(val message: String) : ProfileState()
}