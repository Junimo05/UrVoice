package com.example.urvoices.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.example.urvoices.data.model.Comment
import com.example.urvoices.data.repository.PostRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val auth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore,
    savedStateHandle: SavedStateHandle
): ViewModel(){
    val TAG = "CommentViewModel"

    val _uiState = MutableStateFlow<CommentState>(CommentState.Initial)
    val uiState = _uiState.asStateFlow()

    private val _userInfo = MutableStateFlow<Map<String, String>>(emptyMap())

    //replyLists
    private val lastCommentReplyID = mutableStateOf("")
    private val lastParentCommentID = mutableStateOf("")
    private val _replyLists = MutableStateFlow<List<Comment>>(emptyList())
    val replyLists = _replyLists.asStateFlow()

    private suspend fun loadMoreReplyCommentsAsync(commentID: String): List<Comment> = withContext(Dispatchers.IO) {
        try {
            _uiState.value = CommentState.Working("GET_DATA: REPLY_COMMENTS")
            val resultFetch = postRepository.getReplyComments(commentID, lastCommentReplyID, lastParentCommentID)
            _replyLists.value = resultFetch
            if (replyLists.value.isNotEmpty()) {
                _uiState.value = CommentState.Success("GET_DATA_SUCCESS: REPLY_COMMENTS")
                resultFetch
            } else {
                _uiState.value = CommentState.Error("ReplyComments Error")
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = CommentState.Error("ReplyComments Error")
            emptyList()
        }
    }

    fun loadMoreReplyComments(commentID: String, callback: (List<Comment>) -> Unit) {
//        Log.e(TAG, "loadMoreReplyComments: $commentID")
        viewModelScope.launch {
            val result = loadMoreReplyCommentsAsync(commentID)
            callback(result)
        }
    }

    suspend fun getUserInfo(userID: String): Map<String, String> {
        _uiState.value = CommentState.Working("GET_DATA: USER_INFO")
        return withContext(Dispatchers.IO) {
            val result = postRepository.getUserBaseInfo(userID)
            _userInfo.value = result
            _uiState.value = CommentState.Success("GET_DATA_SUCCESS: USER_INFO")
//            Log.e(TAG, "UserInfo: ${userInfo.value}")
            result
        }
    }
}

sealed class CommentState {
    object Initial: CommentState()
    data class Working(val message: String): CommentState()
    data class Success(val message: String): CommentState()
    data class Error(val message: String): CommentState()
}