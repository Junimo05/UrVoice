package com.example.urvoices.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.urvoices.data.model.Post
import com.example.urvoices.data.service.FirebasePostService
import com.example.urvoices.utils.SharedPreferencesHelper
import com.example.urvoices.utils.UserPreferences
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val userData: UserPreferences,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val firestore: FirebaseFirestore,
    private val storage: StorageReference,
    private val firebasePostService: FirebasePostService
): ViewModel(){
    private val _uploadState = MutableLiveData<UploadState>()
    val uploadState: LiveData<UploadState> = _uploadState

    suspend fun createPost(audioUri: Uri, audioName:String, description: String, tags: List<String>){
        try {
            Log.e("UploadViewModel", "createPost: $audioUri")
            Log.e("UploadViewModel", "createPost: $description")
            Log.e("UploadViewModel", "createPost: $tags")

            _uploadState.value = UploadState.Loading
            val userID = userData.userIdFlow.first()

//            Log.e("UploadViewModel", "createPost: $userID")

            val post = Post(
                id = null,
                userId = userID ?: return,
                audioName = audioName,
                description = description,
                createdAt = System.currentTimeMillis(),
                tag = tags,
                url = null,
                comments = null,
                likes = null,
                deleteAt = null,
                updateAt = null
            )


            val result = firebasePostService.createPost(post, audioUri)
            if (result) {
                _uploadState.value = UploadState.Success
            }
        } catch (e: Exception) {
            _uploadState.value = UploadState.Error(e.message ?: "An error occurred")
        }
    }
}

sealed class UploadState {
    object Loading: UploadState()
    object Success: UploadState() //confirm
    data class Error(val message: String): UploadState()
}