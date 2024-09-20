package com.example.urvoices.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.urvoices.utils.SharedPreferencesHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val firestore: FirebaseFirestore,
    private val storage: StorageReference
): ViewModel(){
    private val _uploadState = MutableLiveData<UploadState>()
    val uploadState: LiveData<UploadState> = _uploadState


    fun uploadTempAudio(uri: Uri, onProgress: (Int) -> Unit, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        _uploadState.value = UploadState.Loading
        val storageRef = FirebaseStorage.getInstance().reference.child("temp_audio/${uri.lastPathSegment}")
        val uploadTask = storageRef.putFile(uri)

        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            onProgress(progress)
        }.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                onSuccess(downloadUri.toString())
                _uploadState.value = UploadState.Loaded
            }
        }.addOnFailureListener { exception ->
            onFailure(exception)
            _uploadState.value = UploadState.Error(exception.message ?: "An error occurred")
        }
    }


}

sealed class UploadState {
    object Loading: UploadState() //dang tai file
    object Loaded: UploadState() //tai xong file
    object Success: UploadState() //confirm
    data class Error(val message: String): UploadState()
}