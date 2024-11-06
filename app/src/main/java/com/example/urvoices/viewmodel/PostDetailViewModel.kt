package com.example.urvoices.viewmodel

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
import androidx.paging.insertHeaderItem
import com.example.urvoices.data.model.Comment
import com.example.urvoices.data.model.Post
import com.example.urvoices.data.repository.PostRepository
import com.example.urvoices.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

val emptyPost = Post(
    ID = "",
    userId = "",
    url = "",
    amplitudes = listOf(),
    audioName = "",
    description = "",
    likes = 0,
    comments = 0,
    tag = listOf(),
    createdAt = 0,
    updatedAt = 0,
    deletedAt = 0
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
    var currentPost = mutableStateOf(emptyPost)
    @OptIn(SavedStateHandleSaveableApi::class)
    var userPost by savedStateHandle.saveable { mutableStateOf(userTemp) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var isFollowed by savedStateHandle.saveable { mutableStateOf(false) }

    //Comment Control
    val lastPage = mutableIntStateOf(1)
    val lastCmt = mutableStateOf("")

    private val pagingConfig = PagingConfig(
        pageSize = 10,
        enablePlaceholders = false,
        initialLoadSize = 20, // Load more items initially
        prefetchDistance = 5, // prefetch distance
        maxSize = 100 // Limit cache size
    )

    private val _commentFlow = MutableStateFlow<PagingData<Comment>>(PagingData.empty())
    val commentFlow = _commentFlow.asStateFlow()


    //Reply Comment Control
    private val lastCommentReplyID = mutableStateOf("")
    val lastParentCommentID = mutableStateOf("")
    private val _replyLists = MutableStateFlow<List<Comment>>(emptyList())
    val replyLists = _replyLists.asStateFlow()

    private val loadedCommentIds = mutableSetOf<String>()
    private val loadedReplyIds = mutableSetOf<String>()

    fun loadData(postID: String, userID: String) {
        this.postID = postID
        currentPost.value = emptyPost
        userPost = userTemp
        loadedCommentIds.clear()
        loadedReplyIds.clear()
        initializeCommentPager(true)

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
                    if(currentPost.value.ID == "" || currentPost.value.ID != postID){
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

    private fun initializeCommentPager(reload: Boolean = false) {
        if(reload){
            lastPage.intValue = 1
            lastCmt.value = ""
            _commentFlow.value = PagingData.empty()
        }
        viewModelScope.launch {
            Pager(pagingConfig) {
                postRepository.getCommentsPosts(
                    postID = postID,
                    lastPage = lastPage,
                    lastCmt = lastCmt
                )
            }.flow.cachedIn(viewModelScope)
                .collect{
                    _commentFlow.value = it
                }
        }
    }
    suspend fun sendComment(message: String, parentID: String = "") {
        val currentUser = auth.currentUser
        if (message.isBlank()) return
        var replyCheck = false
//        Log.e(TAG, "sendComment: $parentID")
        _uiState.value = PostDetailState.SendingComment

        try {
            val result = withContext(Dispatchers.IO) {
                if (parentID.isBlank()) {
                    postRepository.commentPost(
                        actionUserID = currentUser!!.uid,
                        postID = currentPost.value.ID!!,
                        content = message
                    )
                    
                } else {
                    replyCheck = true
                    postRepository.replyComment(
                        actionUserID = currentUser!!.uid,
                        parentID = parentID,
                        postID = currentPost.value.ID!!,
                        content = message
                    )
                }
            }

            currentPost.value = currentPost.value.copy(
                comments = currentPost.value.comments?.plus(1)
            )

            if (result.id != null) {
                _uiState.value = PostDetailState.SendCommentSuccess
                if (replyCheck) {
                    initializeCommentPager()
                } else {
                    _commentFlow.value = _commentFlow.value.insertHeaderItem(item = result)
                }
            } else {
                _uiState.value = PostDetailState.Failed
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "sendComment: ${e.message}")
            _uiState.value = PostDetailState.Error
        }
    }



    fun loadMoreReplyComments(commentID: String) {
        try {
            _uiState.value = PostDetailState.Working
            viewModelScope.launch {
                val result = postRepository.getReplyComments(commentID, lastCommentReplyID, lastParentCommentID)
                _replyLists.value = result
                if(replyLists.value.isNotEmpty()){
                    _uiState.value = PostDetailState.Success
                    Log.e(TAG, "loadMoreReplyComments: ${replyLists.value.size}")
                }else {
                    _uiState.value = PostDetailState.Failed
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = PostDetailState.Error
        }
    }

    private suspend fun loadPostData(postID: String) {
        try {
            val post = postRepository.getPostDetail(postID)
            if (post != null) {
                currentPost.value = post
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
            val result = userRepository.getFollowStatus(currentPost.value.userId)
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
    object SendingComment: PostDetailState()
    object SendCommentSuccess: PostDetailState()
    object Success: PostDetailState()
    object Failed: PostDetailState()
    object Error: PostDetailState()
}