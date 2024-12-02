package com.example.urvoices.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertHeaderItem
import com.example.urvoices.data.model.Comment
import com.example.urvoices.data.model.Post
import com.example.urvoices.data.repository.NotificationRepository
import com.example.urvoices.data.repository.PostRepository
import com.example.urvoices.data.repository.UserRepository
import com.example.urvoices.utils.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
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
    _tags = listOf(),
    createdAt = 0,
    updatedAt = 0,
    deletedAt = 0
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val userRef: UserPreferences,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    val TAG = "PostDetailViewModel"

    private val _uiState = MutableStateFlow<PostDetailState>(PostDetailState.Initial)
    val uiState = _uiState.asStateFlow()

    private val _isLoadingCmt = MutableStateFlow(false)
    val isLoadingCmt: StateFlow<Boolean> = _isLoadingCmt.asStateFlow()

    private val _refreshRequire = MutableStateFlow(false)
    val refreshRequire: StateFlow<Boolean> = _refreshRequire.asStateFlow()
    fun triggerRefresh() { _refreshRequire.value = true }
    fun resetRefreshRequire() { _refreshRequire.value = false }

    private var postID = ""
    var currentPost = mutableStateOf(emptyPost)
    @OptIn(SavedStateHandleSaveableApi::class)
    var userPost by savedStateHandle.saveable { mutableStateOf(userTemp) }
    var currentUser = auth.currentUser
    var currentUsername by savedStateHandle.saveable { mutableStateOf("") }
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
    fun resetCommentFlow() {
        _commentFlow.value = PagingData.empty()
        lastPage.intValue = 1
        lastCmt.value = ""
    }

    val postFlow: MutableSharedFlow<Post> = MutableSharedFlow(replay = 1)

    fun configureObservers() = viewModelScope.launch {
        postFlow
            .distinctUntilChanged() // ignores multiple identical events
            .collect { postUpdated ->
                Log.e(TAG, "configureObservers: ${postUpdated.audioName}")
            }
    }

    init {
        viewModelScope.launch {
            currentUsername = userRef.userNameFlow.first().toString()
        }
    }

    val postFlow: MutableSharedFlow<Post> = MutableSharedFlow(replay = 1)

    fun configureObservers() = viewModelScope.launch {
        postFlow
            .distinctUntilChanged() // ignores multiple identical events
            .collect { postUpdated ->
                Log.e(TAG, "configureObservers: ${postUpdated.audioName}")
            }
    }

    fun loadData(postID: String, userID: String) {
        resetCommentFlow()
        this.postID = postID
        currentPost.value = emptyPost
        userPost = userTemp
        reloadComment()

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

    private suspend fun fetchComments(){
        val lastVisiblePost = mutableStateOf<String>("")
        val lastVisiblePage = mutableIntStateOf(1)
        viewModelScope.launch {
            _isLoadingCmt.value = true
            val comments = Pager(pagingConfig) {
                postRepository.getCommentsPosts(
                    postID = postID,
                    lastPage = lastPage,
                    lastCmt = lastCmt
                )
            }.flow.cachedIn(viewModelScope)
                .collect{
                    _commentFlow.value = it
                    _isLoadingCmt.value = false
                }
        }
    }

    suspend fun sendComment(message: String, parentID: String = "") {
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

            if (result.comment.id != null) {
                _uiState.value = PostDetailState.SendCommentSuccess
                if (replyCheck) {
                    //add Noti
                    notificationRepository.replyComment(
                        targetUserID = currentPost.value.userId,
                        actionUsername = currentUsername,
                        relaID = result.relaCommentID
                    )
                    Toast.makeText(
                        context,
                        "Reply sent successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    _refreshRequire.value = true
                } else {
                    notificationRepository.commentPost(
                        targetUserID = currentPost.value.userId,
                        actionUsername = currentUsername,
                        relaID = result.relaCommentID
                    )
                    Toast.makeText(
                        context,
                        "Comment sent successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    _commentFlow.value = _commentFlow.value.insertHeaderItem(item = result.comment)
                }
                _uiState.value = PostDetailState.Success
            } else {
                _uiState.value = PostDetailState.Failed
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "sendComment: ${e.message}")
            _uiState.value = PostDetailState.Error
        }
    }



    fun reloadComment() {
        viewModelScope.launch {
            fetchComments()
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
            } else {
                _uiState.value = PostDetailState.Failed
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = PostDetailState.Error
        }
    }

    override fun onCleared() {
        super.onCleared()
        resetCommentFlow()
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