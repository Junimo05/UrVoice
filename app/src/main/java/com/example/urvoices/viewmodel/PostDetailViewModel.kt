package com.example.urvoices.viewmodel

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
import com.example.urvoices.data.model.Comment
import com.example.urvoices.data.model.Post
import com.example.urvoices.data.repository.PostRepository
import com.example.urvoices.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

val emptyPost = Post(
    id = "",
    userId = "",
    url = "",
    amplitudes = listOf(),
    audioName = "",
    description = "",
    likes = 0,
    comments = 0,
    tag = listOf(),
    createdAt = 0,
    updateAt = 0,
    deleteAt = 0
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    val TAG = "PostDetailViewModel"

    val _uiState = MutableStateFlow<PostDetailState>(PostDetailState.Initial)
    val uiState = _uiState.asStateFlow()

    private val postID: String by lazy {
        checkNotNull(savedStateHandle["postID"])
    }
    @OptIn(SavedStateHandleSaveableApi::class)
    var currentPost by savedStateHandle.saveable { mutableStateOf(emptyPost) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var userPost by savedStateHandle.saveable { mutableStateOf(userTemp) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var isFollowed by savedStateHandle.saveable { mutableStateOf(false) }

    val commentLists : Flow<PagingData<Comment>> by lazy {
        Pager(PagingConfig(pageSize = 10)){
            postRepository.getComments_Posts(postID)
        }.flow.cachedIn(viewModelScope)
    }

    fun loadData(postID: String, userID: String) {
        try {
            _uiState.value = PostDetailState.Working
            //get post data and user data
            viewModelScope.launch {
                if(userID != ""){
                    loadUserPostData(userID)
                }
                loadPostData(postID)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = PostDetailState.Error
        }
    }

    private suspend fun loadPostData(postID: String) {
        try {
            _uiState.value = PostDetailState.Working
            val post = postRepository.getPostDetail(postID)
            if (post != null) {
                currentPost = post
                if(userPost == userTemp){
                    loadUserPostData(post.userId)
                }
            } else {
                _uiState.value = PostDetailState.Failed
            }
            _uiState.value = PostDetailState.Success
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = PostDetailState.Error
        }
    }

    private suspend fun loadUserPostData(userID: String) {
        try {
            _uiState.value = PostDetailState.Working
            val user = userRepository.getInfoUserByUserID(userID)
            if (user != null) {
                userPost = user
                checkFollowed()
            } else {
                _uiState.value = PostDetailState.Failed
            }
            _uiState.value = PostDetailState.Success
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = PostDetailState.Error
        }
    }

    private suspend fun checkFollowed() {
        try {
            _uiState.value = PostDetailState.Working
            val result = userRepository.getFollowStatus(currentPost.userId)
            isFollowed = result
            _uiState.value = PostDetailState.Success
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = PostDetailState.Error
        }
    }
}

sealed class PostDetailState{
    object Initial: PostDetailState()
    object Working: PostDetailState()
    object Success: PostDetailState()
    object Failed: PostDetailState()
    object Error: PostDetailState()
}