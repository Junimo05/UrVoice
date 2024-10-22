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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    //Reply Comment Control
    val lastCommentReplyID = mutableStateOf("")
    val lastParentCommentID = mutableStateOf("")
    private val _replyLists = MutableStateFlow<List<Comment>>(emptyList())
    val replyLists = _replyLists.asStateFlow()


    init {

    }

    suspend fun getUserInfo(userID: String): Map<String, String> {
        val result = postRepository.getUserBaseInfo(userID)
        return result
    }

    fun loadMoreReplyComments(commentID: String) {
        if (_uiState.value ==  CommentState.Loading) return // Prevent multiple calls
        _uiState.value = CommentState.Loading
        try {
            viewModelScope.launch {
                val result = postRepository.getReply_Comments(commentID, lastCommentReplyID, lastParentCommentID)
                _replyLists.value += result // Append new replies to the existing list
                if(replyLists.value.isNotEmpty()){
                    _uiState.value = CommentState.Success("Success")
                    Log.e(TAG, "Reply Comment Size: ${replyLists.value.size}")
                }else {
                    _uiState.value = CommentState.Success("No more comments")
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = CommentState.Error("Error")
        }
    }
}

sealed class CommentState {
    object Initial: CommentState()
    object Loading: CommentState()
    data class Success(val message: String): CommentState()
    data class Error(val message: String): CommentState()
}