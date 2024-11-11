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
import com.example.urvoices.data.repository.NotificationRepository
import com.example.urvoices.data.repository.PostRepository
import com.example.urvoices.data.repository.UserRepository
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
    private val auth: FirebaseAuth,
    private val userDataStore: UserPreferences,
    savedStateHandle: SavedStateHandle
): ViewModel(){
    val TAG = "ProfileViewModel"

    private val _uiState = MutableStateFlow<ProfileState>(ProfileState.Initial)
    val uiState: StateFlow<ProfileState> = _uiState.asStateFlow()

    @OptIn(SavedStateHandleSaveableApi::class)
    var isFollowed by savedStateHandle.saveable { mutableStateOf(false) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var followers by savedStateHandle.saveable { mutableIntStateOf(0) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var followings by savedStateHandle.saveable { mutableIntStateOf(0) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var postCounts by savedStateHandle.saveable { mutableIntStateOf(0) }


    @OptIn(SavedStateHandleSaveableApi::class)
    var displayuserID by savedStateHandle.saveable { mutableStateOf("") }
    @OptIn(SavedStateHandleSaveableApi::class)
    var displayuser by savedStateHandle.saveable { mutableStateOf<User>(userTemp) }

    @OptIn(SavedStateHandleSaveableApi::class)
    var isCurrentUser by savedStateHandle.saveable { mutableStateOf(false) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var currentUserID by savedStateHandle.saveable { mutableStateOf("") }
    var authCurrentUser = auth.currentUser

    private var userListenerRegistration: ListenerRegistration? = null

    //Posts
    val lastVisiblePost = mutableStateOf("")
    val lastVisiblePage = mutableIntStateOf(1)
    var posts : Flow<PagingData<Post>> = Pager(PagingConfig(pageSize = 3)) {
        if (displayuserID != currentUserID) {
            lastVisiblePost.value = lastVisiblePost.value
            lastVisiblePage.intValue = lastVisiblePage.intValue
        }
        postRepository.getAllPostFromUser(displayuserID, lastVisiblePost, lastVisiblePage)
    }.flow.cachedIn(viewModelScope)

    //SavedPosts
    val lastVisibleSavedPost = mutableStateOf("")
    val lastVisibleSavedPage = mutableIntStateOf(1)
    var savedPosts : Flow<PagingData<Post>> = Pager(PagingConfig(pageSize = 3)) {
        if (displayuserID != currentUserID) {
            lastVisibleSavedPost.value = lastVisibleSavedPost.value
            lastVisibleSavedPage.intValue = lastVisibleSavedPage.intValue
        }
        postRepository.getAllSavedPostFromUser(displayuserID, lastVisibleSavedPost, lastVisibleSavedPage)
    }.flow.cachedIn(viewModelScope)


    fun loadData(userID: String){
        if(displayuserID != userID){
            this.displayuserID = userID
            reloadPost()
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
            getFollowStatus(userID)
            getPostCounts(userID)
            getFollowerCount(userID)
            getFollowingCount(userID)
            //listen Change
            listenToUserChanges(userID)
            _uiState.value = ProfileState.Successful
        }
    }

    private fun reloadPost(){
        lastVisiblePost.value = ""
        lastVisiblePage.intValue = 1
        posts = Pager(PagingConfig(pageSize = 3)) {
            postRepository.getAllPostFromUser(displayuserID, lastVisiblePost, lastVisiblePage)
        }.flow.cachedIn(viewModelScope)
    }

    private suspend fun loadbaseUserData(userID: String){
        try {
            val user = userRepository.getInfoUserByUserID(userID)
            if(user != null){
                this.displayuser = user
            }
        } catch (e: Exception) {
            _uiState.value = ProfileState.Error("Error when loading user data")
            Log.e(TAG, "loadbaseUserData: Error")
        }
    }

    suspend fun loadUserBaseInfo(userID: String): Map<String, String>{
        val result = postRepository.getUserBaseInfo(userID)
        return result
    }

    fun followUser(){
        try {
            val targetUserID = this.displayuserID
            val actionUserID = this.currentUserID
            _uiState.value = ProfileState.Working
            viewModelScope.launch {
                val actionUsername = userDataStore.userNameFlow.first()!!
                val relaFollowID = userRepository.followUser(userId = targetUserID, followStatus = !isFollowed)
                if(relaFollowID != ""){ //Follow
                    if(targetUserID != actionUserID){
                        followers++
                        isFollowed = !isFollowed
                    }
                    val result = notificationRepository.followUser(targetUserID = targetUserID, actionUsername = actionUsername, followInfoID = relaFollowID)
                    if(result){
                        _uiState.value = ProfileState.Successful
//                        Log.e(TAG, "followUser: Success")
                    }
                } else { //Unfollow
                    followers--
                    isFollowed = !isFollowed
                }
            }
        } catch (e: Exception) {
            _uiState.value = ProfileState.Error("Error when following user")
            Log.e(TAG, "followUser: Error")
        }
    }


    //Update DATA
    suspend fun updateProfile(
        username: String,
        bio: String,
        country: String,
        email: String,
        avatarUri: Uri = Uri.EMPTY,
    ): Boolean {
        _uiState.value = ProfileState.Working
        val oldUser = displayuser.copy()

        return withContext(Dispatchers.IO) {
            try {
                val result = userRepository.updateUser(username, bio, country, email, avatarUri, oldUser)
                if (result) {
                    _uiState.value = ProfileState.Successful
                    // Update user data
                    displayuser = displayuser.copy(
                        username = username,
                        bio = bio,
                        country = country,
                        email = email,
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
                    displayuser = user
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
                isFollowed = followStatus
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