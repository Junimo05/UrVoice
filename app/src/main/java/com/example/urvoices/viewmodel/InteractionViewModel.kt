package com.example.urvoices.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.example.urvoices.data.repository.NotificationRepository
import com.example.urvoices.data.repository.PostRepository
import com.example.urvoices.data.service.FirebaseBlockService
import com.example.urvoices.utils.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class InteractionViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val postRepository: PostRepository,
    private val auth: FirebaseAuth,
    private val userRef: UserPreferences,
    private val blockService: FirebaseBlockService,
    private val firebaseFirestore: FirebaseFirestore,
    savedStateHandle: SavedStateHandle
): ViewModel(){
    val TAG = "InteractionRowViewModel"


    //STATE
    private val _uiState: MutableStateFlow<InteractionRowState> = MutableStateFlow(InteractionRowState.Initial)
    val uiState: StateFlow<InteractionRowState> = _uiState.asStateFlow()

    //DATA STATE
    @OptIn(SavedStateHandleSaveableApi::class)
    var currentUserID by savedStateHandle.saveable { mutableStateOf("") }
    @OptIn(SavedStateHandleSaveableApi::class)
    var currentUsername by savedStateHandle.saveable { mutableStateOf("") }
    init {
        viewModelScope.launch {
            currentUserID = auth.currentUser?.uid.toString()
            currentUsername = userRef.userNameFlow.first().toString()
        }
    }


    //Callback
    fun getLoveStatus(postID: String = "", commentID: String = "", callback: (Boolean) -> Unit){
        viewModelScope.launch {
            val result = getLoveStatusSuspend(postID, commentID)
            callback(result)
        }
    }

    fun getBlockStatus(targetUserID: String, callback: (Boolean) -> Unit){
        try {
            viewModelScope.launch {
                val result = blockService.getBlockStatus(
                    targetID = targetUserID,
                )
                callback(result)
            }
        } catch (e: Exception) {
            Log.e(TAG, "getBlockStatus Func Failed: ${e.message}")
        }
    }

    fun getSaveStatus(postID: String, callback: (Boolean) -> Unit){
        try {
            viewModelScope.launch {
                val result = postRepository.getSaveStatus(postID)
                callback(result)
            }
        } catch (e: Exception) {
            Log.e(TAG, "getSaveStatus Func Failed: ${e.message}")
        }
    }
    /*
        Block or Unblock a user
    */
    fun blockUser(targetID: String): String {
        _uiState.value = InteractionRowState.Loading
        var resultMsg = ""
        try {
            viewModelScope.launch {
                val result = blockService.blockUser(targetID)
                if (result.isNotEmpty()) {
                    _uiState.value = InteractionRowState.Success
                    resultMsg = "User blocked successfully"
                } else {
                    _uiState.value = InteractionRowState.Error
                    resultMsg = "Something went wrong"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = InteractionRowState.Error
            resultMsg = "Something went wrong"
        }
        return resultMsg
    }

    fun unblockUser(targetID: String): String {
        _uiState.value = InteractionRowState.Loading
        var resultMsg = ""
        try {
            viewModelScope.launch {
                val result = blockService.unblockUser(targetID)
                if (result.isNotEmpty()) {
                    _uiState.value = InteractionRowState.Success
                    resultMsg = "User unblocked successfully"
                } else {
                    _uiState.value = InteractionRowState.Error
                    resultMsg = "Something went wrong"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = InteractionRowState.Error
            resultMsg = "Something went wrong"
        }
        return resultMsg
    }

    /*
        SavePost
    */
    fun savePost(postID: String, callback: (Boolean?) -> Unit) {
        try {
            viewModelScope.launch {
                val result = postRepository.savePost(postID)
                callback(result)
            }
        } catch (e: Exception) {
            Log.e(TAG, "savePost Func Failed: ${e.message}")
        }
    }

    /*
        Loves Action
    */
    fun loveAction(targetUserID: String, isLove: Boolean, postID: String = "", commentID: String = "", callback: (Boolean) -> Unit){
        try {
            viewModelScope.launch {
                val result = loveActionSuspend(targetUserID, isLove, postID, commentID)
                callback(result)
            }
        } catch (e: Exception) {
            Log.e(TAG, "loveAction Func Failed: ${e.message}")
        }
    }

    //Suspend
    private suspend fun getLoveStatusSuspend(postID: String = "", commentID: String = ""): Boolean {
        return try {
            _uiState.value = InteractionRowState.Loading
            withContext(Dispatchers.IO) {
                val query = if (commentID.isEmpty()) {
                    firebaseFirestore.collection("likes")
                        .whereEqualTo("postID", postID)
                        .whereEqualTo("userID", currentUserID)
                } else {
                    firebaseFirestore.collection("likes")
                        .whereEqualTo("postID", postID)
                        .whereEqualTo("userID", currentUserID)
                        .whereEqualTo("commentID", commentID)
                }

                val snapshot = query.get().await()
                val isLove = !snapshot.isEmpty
                _uiState.value = InteractionRowState.Success
                isLove
            }
        } catch (e: Exception) {
            _uiState.value = InteractionRowState.Error
            false
        }
    }



    private suspend fun loveActionSuspend(targetUserID: String, isLove: Boolean, postID: String = "", commentID: String = ""): Boolean {
        return try {
            _uiState.value = InteractionRowState.Working
            withContext(Dispatchers.IO) {
                var result = false
                if (postID.isNotEmpty()) {
                    if (commentID.isNotEmpty()) { //Comment like
                        if (isLove) {
                            val likeAction = postRepository.likeComment(
                                actionUserID = currentUserID,
                                postID = postID,
                                commentID = commentID
                            )
                            if (likeAction != "") {
                                result = notificationRepository.likeComment(
                                    targetUserID = targetUserID,
                                    actionUsername = currentUsername,
                                    relaID = commentID
                                )
                            }
                        } else {
                            val documents = firebaseFirestore.collection("likes")
                                .whereEqualTo("postID", postID)
                                .whereEqualTo("userID", currentUserID)
                                .whereEqualTo("commentID", commentID)
                                .get()
                                .await()
                            for (document in documents) {
                                document.reference.delete().await()
                            }
                            result = true
                        }
                    } else { //Post like
                        if (isLove) {
                            val likeAction = postRepository.likePost(
                                actionUserID = currentUserID,
                                postID = postID
                            )
                            if (likeAction != "") {
                                result = notificationRepository.likePost(
                                    targetUserID = targetUserID,
                                    actionUsername = currentUsername,
                                    relaID = postID
                                )
                            }
                        } else {
                            val documents = firebaseFirestore.collection("likes")
                                .whereEqualTo("postID", postID)
                                .whereEqualTo("userID", currentUserID)
                                .get()
                                .await()
                            for (document in documents) {
                                document.reference.delete().await()
                            }
                            result = true
                        }
                    }
                    if (result) {
                        _uiState.value = InteractionRowState.Success
                    }
                } else {
                    _uiState.value = InteractionRowState.Error
                    Log.e(TAG, "loveActionSuspend: postID is empty")
                }
                result
            }
        } catch (e: Exception) {
            _uiState.value = InteractionRowState.Error
            Log.e(TAG, "loveAction InteractionRow Failed: ${e.message}")
            false
        }
    }
}

sealed class InteractionRowState{
    object Initial: InteractionRowState()
    object Working: InteractionRowState()
    object Loading: InteractionRowState()
    object Success: InteractionRowState()
    object Error: InteractionRowState()
}