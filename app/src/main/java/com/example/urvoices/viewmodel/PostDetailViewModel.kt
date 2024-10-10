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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private val _uiState = MutableStateFlow<PostDetailState>(PostDetailState.Initial)
    val uiState = _uiState.asStateFlow()

    private var postID = ""
    @OptIn(SavedStateHandleSaveableApi::class)
    var currentPost by savedStateHandle.saveable { mutableStateOf(emptyPost) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var userPost by savedStateHandle.saveable { mutableStateOf(userTemp) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var isFollowed by savedStateHandle.saveable { mutableStateOf(false) }


    //Comment Control
    val lastPage = mutableStateOf(1)
    val lastCmt = mutableStateOf("")
    val commentLists : Flow<PagingData<Comment>> by lazy {
        Pager(PagingConfig(pageSize = 10)){
            postRepository.getComments_Posts(postID, lastCmt, lastPage)
        }.flow.cachedIn(viewModelScope)
    }


    //Reply Comment Control
    val lastCommentReplyID = mutableStateOf("")
    val lastParentCommentID = mutableStateOf("")
    private val _replyLists = MutableStateFlow<List<Comment>>(emptyList())
    val replyLists: StateFlow<List<Comment>> = _replyLists

    fun loadData(postID: String, userID: String) {
        this.postID = postID
        viewModelScope.launch {
            try {
                _uiState.value = PostDetailState.Working
                //get post data and user data
                val job1 = launch {
                    if(userID != ""){
                        loadUserPostData(userID)
                    }
                }
                val job2 = launch {
                    if(currentPost.id == "" || currentPost.id != postID){
                        loadPostData(postID)
                    }
                }
                job1.join()
                job2.join()

                withContext(Dispatchers.Main) {
                    _uiState.value = PostDetailState.Success
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _uiState.value = PostDetailState.Error
                }
            }
        }
    }

    fun loadMoreReplyComments(commentID: String) {
        try {
            _uiState.value = PostDetailState.Working
            viewModelScope.launch {
                val result = postRepository.getReply_Comments(commentID, lastCommentReplyID, lastParentCommentID)
                _replyLists.value = result
            }
            _uiState.value = PostDetailState.Success
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = PostDetailState.Error
        }
    }

    private suspend fun loadPostData(postID: String) {
        try {
            val post = postRepository.getPostDetail(postID)
            if (post != null) {
                currentPost = post
                if(userPost == userTemp){
                    loadUserPostData(post.userId)
                }
            } else {
                _uiState.value = PostDetailState.Failed
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = PostDetailState.Error
        }
    }

    private suspend fun loadUserPostData(userID: String) {
        try {
            val user = userRepository.getInfoUserByUserID(userID)
            if (user != null) {
                userPost = user
                checkFollowed()
            } else {
                _uiState.value = PostDetailState.Failed
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = PostDetailState.Error
        }
    }

    private suspend fun checkFollowed() {
        try {
            val result = userRepository.getFollowStatus(currentPost.userId)
            isFollowed = result
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