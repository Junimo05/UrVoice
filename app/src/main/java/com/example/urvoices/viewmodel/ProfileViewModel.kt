package com.example.urvoices.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import com.example.urvoices.data.model.Post
import com.example.urvoices.data.model.User
import com.example.urvoices.data.repository.PostRepository
import com.example.urvoices.data.repository.UserRepository
import com.example.urvoices.utils.SharedPreferencesHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val firestore: FirebaseFirestore,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
): ViewModel(){
    val TAG = "ProfileViewModel"

    val _uiState = MutableStateFlow<ProfileState>(ProfileState.Initial)
    val uiState: StateFlow<ProfileState> = _uiState.asStateFlow()

    @OptIn(SavedStateHandleSaveableApi::class)
    var followers by savedStateHandle.saveable { mutableIntStateOf(0) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var followings by savedStateHandle.saveable { mutableIntStateOf(0) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var postCounts by savedStateHandle.saveable { mutableIntStateOf(0) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var isCurrentUser by savedStateHandle.saveable { mutableStateOf(false) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var user by savedStateHandle.saveable { mutableStateOf<User>(userTemp) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var isFollowed by savedStateHandle.saveable { mutableStateOf(false) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var userID by savedStateHandle.saveable { mutableStateOf("") }

    val posts : Flow<PagingData<Post>> by lazy {
        Pager(PagingConfig(pageSize = 3)){
            postRepository.getAllPostFromUser(userID)
        }.flow.cachedIn(viewModelScope)
    }


    fun loadData(userID: String){
        val currentUser = auth.currentUser
        this.userID = userID
        isCurrentUser = currentUser?.uid == userID
        _uiState.value = ProfileState.Loading
        viewModelScope.launch {
            load_baseUserData(userID)
            getFollowStatus(userID)
            getPostCounts(userID)
            getFollowerCount(userID)
            getFollowingCount(userID)
            _uiState.value = ProfileState.Successful
        }
    }



    private suspend fun load_baseUserData(userID: String){
        val result = userRepository.getInfoUserByUserID(userID)
        if(result != null){
            user = result
        }else {
            _uiState.value = ProfileState.Error("Error when loading user data")
        }
    }

    private suspend fun getFollowStatus(userId: String) {
        isFollowed = userRepository.getFollowStatus(userId)
    }

    private suspend fun getPostCounts(userId: String) {
        postCounts = userRepository.getPostCount(userId)
    }

    private suspend fun getFollowerCount(userId: String) {
        followers = userRepository.getFollowerCount(userId)
    }

    private suspend fun getFollowingCount(userId: String) {
        followings = userRepository.getFollowingCount(userId)
    }
}

sealed class ProfileState {
    object Initial : ProfileState()
    object Loading : ProfileState()
    object Successful : ProfileState()
    data class Error(val message: String) : ProfileState()
}