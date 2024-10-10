package com.example.urvoices.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.example.urvoices.data.repository.NotificationRepository
import com.example.urvoices.data.repository.PostRepository
import com.example.urvoices.utils.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class InteractionRowViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val postRepository: PostRepository,
    private val auth: FirebaseAuth,
    private val userRef: UserPreferences,
    private val firebaseFirestore: FirebaseFirestore,
    savedStateHandle: SavedStateHandle
): ViewModel(){
    val TAG = "InteractionRowViewModel"
    private val _uiState: MutableStateFlow<InteractionRowState> = MutableStateFlow(InteractionRowState.Initial)
    val uiState: StateFlow<InteractionRowState> = _uiState.asStateFlow()

    @OptIn(SavedStateHandleSaveableApi::class)
    var currentUserID by savedStateHandle.saveable { mutableStateOf("") }
    @OptIn(SavedStateHandleSaveableApi::class)
    var currentUsername by savedStateHandle.saveable { mutableStateOf("") }
    @OptIn(SavedStateHandleSaveableApi::class)
    var isLove by savedStateHandle.saveable { mutableStateOf(false) }

    init {
        viewModelScope.launch {
            currentUserID = auth.currentUser?.uid.toString()
            currentUsername = userRef.userNameFlow.first().toString()
        }
    }

    fun getLoveStatus(postID: String = "", commentID: String = ""){
        try {
            _uiState.value = InteractionRowState.Loading
            viewModelScope.launch {
                if (commentID.isEmpty()) {
                    isLove = firebaseFirestore.collection("likes")
                        .whereEqualTo("postID", postID)
                        .whereEqualTo("userID", currentUserID
                        ).get().isSuccessful
                } else {
                    isLove = firebaseFirestore.collection("likes")
                        .whereEqualTo("postID", postID)
                        .whereEqualTo("userID", currentUserID)
                        .whereEqualTo("commentID", commentID)
                        .get().isSuccessful
                }
                _uiState.value = InteractionRowState.Success
            }
        }catch (e: Exception){
            _uiState.value = InteractionRowState.Error
        }
    }

    fun loveAction(targetUserID: String, isLove: Boolean, postID: String = "", commentID: String = ""){
        var result = false
        try {
            _uiState.value = InteractionRowState.Working
            viewModelScope.launch {
                if(postID.isNotEmpty()) {
                    if(commentID.isNotEmpty()){
                        //Like Comment
                        if(isLove) {
                            if(postRepository.likeComment(currentUserID, commentID, postID) != ""){
                                result = notificationRepository.likeComment(targetUserID, currentUsername , commentID)
                            }
                        } else {
                             firebaseFirestore.collection("likes")
                                .whereEqualTo("postID", postID)
                                .whereEqualTo("userID", currentUserID)
                                .whereEqualTo("commentID", commentID)
                                .get()
                                .addOnSuccessListener { documents ->
                                    for (document in documents) {
                                        document.reference.delete()
                                    }
                                    result = true
                                }
                                .addOnFailureListener { exception ->
                                    // Handle any errors here
                                    result = false
                                }
                        }
                    } else {
                        //Like Post
                        if(isLove) {
                            if(postRepository.likePost(currentUserID, postID) != ""){
                                result = notificationRepository.likePost(targetUserID, currentUsername, postID)
                            }
                        } else {
                            firebaseFirestore.collection("likes")
                                .whereEqualTo("postID", postID)
                                .whereEqualTo("userID", currentUserID)
                                .get()
                                .addOnSuccessListener { documents ->
                                    for (document in documents) {
                                        document.reference.delete()
                                    }
                                    result = true
                                }
                                .addOnFailureListener { exception ->
                                    // Handle any errors here
                                    result = false
                                }
                        }
                    }
                }
                if(result) {
                    _uiState.value = InteractionRowState.Success
                    this@InteractionRowViewModel.isLove = isLove
                }
            }
        }catch (e: Exception){
            _uiState.value = InteractionRowState.Error
        }
    }

    fun commentAction(targetUserID: String, postID: String = "", commentID: String = "", content: String){
        var addResult = false
        try {
            _uiState.value = InteractionRowState.Working
            viewModelScope.launch {
                if(postID.isNotEmpty()) {
                    if(commentID.isNotEmpty()){
                        //Reply Comment
                        if(postRepository.replyComment(currentUserID, commentID, postID, content) != ""){
                            addResult = notificationRepository.replyComment(targetUserID, currentUsername, commentID)
                        }
                    }else {
                        //Comment Post
                        if(postRepository.commentPost(currentUserID, postID, content) != ""){
                            addResult = notificationRepository.commentPost(targetUserID, currentUsername, postID)
                        }
                    }
                }
                if(addResult) {
                    _uiState.value = InteractionRowState.Success
                }
            }
        }catch (e: Exception){
            _uiState.value = InteractionRowState.Error
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