package com.example.urvoices.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.urvoices.data.model.Post
import com.example.urvoices.data.repository.PostRepository
import com.example.urvoices.utils.UserPreferences
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val userData: UserPreferences,
    private val firestore: FirebaseFirestore,
    private val storage: StorageReference,
    private val postRepository: PostRepository
): ViewModel(){
    private val _uploadState = MutableLiveData<UploadState>(UploadState.Initial)
    val uploadState: LiveData<UploadState> = _uploadState


    suspend fun createPost(audioUri: Uri, audioName:String, description: String, tags: List<String>): Boolean {
        _uploadState.value = UploadState.Loading
        try {
            val userID = userData.userIdFlow.first()
            val post = Post(
                ID = null,
                userId = userID ?: return false,
                audioName = audioName,
                description = description,
                createdAt = System.currentTimeMillis(),
                tag = tags,
                url = null,
                comments = 0,
                likes = 0,
                deletedAt = null,
                updatedAt = null
            )
            val result = postRepository.createPost(post, audioUri)
            if (result) {
                _uploadState.value = UploadState.Success
            }
            return result
        } catch (e: Exception) {
            _uploadState.value = UploadState.Error(e.message ?: "An error occurred")
        }
        return false
    }

    fun setFileSelected() {
        _uploadState.value = UploadState.FileSelected
    }

    fun resetUploadState () {
        _uploadState.value = UploadState.Initial
    }
}

sealed class UploadState {
    data object Initial: UploadState()
    data object FileSelected: UploadState()
    data object Loading: UploadState()
    data object Success: UploadState() //confirm
    data class Error(val message: String): UploadState()
}