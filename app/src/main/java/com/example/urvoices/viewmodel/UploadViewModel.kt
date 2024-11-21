package com.example.urvoices.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.example.urvoices.data.model.Post
import com.example.urvoices.data.repository.PostRepository
import com.example.urvoices.utils.UserPreferences
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val userData: UserPreferences,
    private val firestore: FirebaseFirestore,
    private val storage: StorageReference,
    private val postRepository: PostRepository,
    private val workManager: WorkManager
) : ViewModel() {
    private val _uploadState = MutableLiveData<UploadState>(UploadState.Initial)
    val uploadState: LiveData<UploadState> = _uploadState

    val snackBarUploading = SnackbarHostState()

    fun showSnackBar(message: String) {
        viewModelScope.launch {
            snackBarUploading.showSnackbar(
                message = message,
                actionLabel = null
            )
        }
    }

    fun hideSnackBar() {
        viewModelScope.launch {
            snackBarUploading.currentSnackbarData?.dismiss()
        }
    }

    fun createPost(audioUri: Uri, imgUri: Uri = Uri.EMPTY, audioName: String, description: String, tags: List<String>) {
        _uploadState.value = UploadState.Loading

        val inputData = UploadWorker.createInputData(
            audioUri.toString(),
            imgUri.toString(),
            audioName,
            description,
            tags
        )

        val uploadWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<UploadWorker>()
            .setInputData(inputData)
            .build()

        workManager.enqueue(uploadWorkRequest)

        workManager.getWorkInfoByIdLiveData(uploadWorkRequest.id).observeForever { workInfo ->
            if (workInfo != null && workInfo.state.isFinished) {
                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                    _uploadState.value = UploadState.Success
                } else {
                    _uploadState.value = UploadState.Error("Upload failed")
                }
            }
        }
    }

    fun setFileSelected() {
        _uploadState.value = UploadState.FileSelected
    }

    fun resetUploadState() {
        _uploadState.value = UploadState.Initial
    }
}

class UploadWorker @AssistedInject constructor (
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val userData: UserPreferences,
    private val postRepository: PostRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        fun createInputData(
            audioUri: String,
            imgUri: String,
            audioName: String,
            description: String,
            tags: List<String>
        ): Data {
            return Data.Builder()
                .putString("audioUri", audioUri)
                .putString("imgUri", imgUri)
                .putString("audioName", audioName)
                .putString("description", description)
                .putStringArray("tags", tags.toTypedArray())
                .build()
        }
    }

    override suspend fun doWork(): Result {
        val audioUri = inputData.getString("audioUri")?.let { Uri.parse(it) }
        val imgUri = inputData.getString("imgUri")?.let { Uri.parse(it) }
        val audioName = inputData.getString("audioName") ?: return Result.failure()
        val description = inputData.getString("description") ?: return Result.failure()
        val tags = inputData.getStringArray("tags")?.toList() ?: return Result.failure()

        return try {
            val userID = userData.userIdFlow.first()
            val post = Post(
                ID = null,
                userId = userID ?: return Result.failure(),
                audioName = audioName,
                description = description,
                createdAt = System.currentTimeMillis(),
                _tags = tags,
                url = null,
                imgUrl = null,
                comments = 0,
                likes = 0,
                deletedAt = null,
                updatedAt = null
            )
            val result = postRepository.createPost(post, audioUri!!, imgUri!!)
            if (result) {
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}


sealed class UploadState {
    data object Initial: UploadState()
    data object FileSelected: UploadState()
    data object Loading: UploadState()
    data object Success: UploadState() //confirm
    data class Error(val message: String): UploadState()
}