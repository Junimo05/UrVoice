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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

val userTemp = User(
    id = "empty",
    username = "empty",
    displayname = "empty",
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
    @OptIn(SavedStateHandleSaveableApi::class)
    var currentUsername by savedStateHandle.saveable { mutableStateOf("") }
    var authCurrentUser = auth.currentUser

    private var userListenerRegistration: ListenerRegistration? = null

    val lastVisiblePost = mutableStateOf("")
    val lastVisiblePage = mutableStateOf(1)
    var posts : Flow<PagingData<Post>> = Pager(PagingConfig(pageSize = 3)) {
        if (displayuserID != currentUserID) {
            lastVisiblePost.value = ""
            lastVisiblePage.value = 1
        }
        postRepository.getAllPostFromUser(displayuserID, lastVisiblePost, lastVisiblePage)
    }.flow.cachedIn(viewModelScope)


    fun loadData(userID: String){
        currentUserID = authCurrentUser?.uid ?: ""
        this.displayuserID = userID
        if (currentUserID == this.displayuserID){
            isCurrentUser = true
        }
        _uiState.value = ProfileState.Loading
        viewModelScope.launch {
            loadbaseUserData(userID)
            reloadPost()
            getFollowStatus(userID)
            getPostCounts(userID)
            getFollowerCount(userID)
            getFollowingCount(userID)
            //listen Change
            listenToUserChanges(userID)
            _uiState.value = ProfileState.Successful
        }
    }

    fun reloadPost(){
        lastVisiblePost.value = ""
        lastVisiblePage.value = 1
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

    fun followUser(){
        try {
            val targetUserID = this.displayuserID
            val actionUserID = this.currentUserID
            val actionUsername = this.currentUsername
            _uiState.value = ProfileState.Working
            viewModelScope.launch {
                val relaFollowID = userRepository.followUser(targetUserID)
                if(relaFollowID != ""){
                    if(targetUserID != actionUserID){
                        followers++
                        isFollowed = true
                    }
                    val result = notificationRepository.followUser(targetUserID, actionUsername, relaFollowID)
                    if(result){
                        _uiState.value = ProfileState.Successful
//                        Log.e(TAG, "followUser: Success")
                    }
                }else{
                    _uiState.value = ProfileState.Error("Error when following user")
//                    Log.e(TAG, "followUser: Error")
                }
            }
        } catch (e: Exception) {
            _uiState.value = ProfileState.Error("Error when following user")
            Log.e(TAG, "followUser: Error")
        }
    }


    //Update DATA
    fun updateProfile(
        username: String,
        bio: String,
        country: String,
        email: String,
        avatarUri: Uri = Uri.EMPTY,
    ){
        _uiState.value = ProfileState.Working
        val oldUser = displayuser.copy()
        try {
            viewModelScope.launch {
                val result = userRepository.updateUser(username, bio, country, email, avatarUri, oldUser)
                if(result){
                    _uiState.value = ProfileState.Successful
                    //update user data
                    displayuser = displayuser.copy(
                        username = username,
                        bio = bio,
                        country = country,
                        email = email,
                    )
                }else {
                    _uiState.value = ProfileState.Error("Error when updating profile")
                }
            }
        } catch (e: Exception) {
            _uiState.value = ProfileState.Error("Error when updating profile")
            Log.e(TAG, "updateProfile: Error")
        }
    }



    //Utils

    fun listenToUserChanges(userId: String) {
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

    fun stopListeningToUserChanges() {
        userListenerRegistration?.remove()
    }

    private suspend fun getFollowStatus(userId: String) {
        try {
            val followStatus = userRepository.getFollowStatus(userId)
            isFollowed = followStatus
        } catch (e: Exception) {
            _uiState.value = ProfileState.Error("Error when loading follow status")
            Log.e(TAG, "getFollowStatus: Error")
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