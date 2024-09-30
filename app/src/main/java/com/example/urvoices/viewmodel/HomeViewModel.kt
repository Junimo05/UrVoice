package com.example.urvoices.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.example.urvoices.data.model.Comment
import com.example.urvoices.data.model.MessageNotification
import com.example.urvoices.data.model.Notification
import com.example.urvoices.data.model.Post
import com.example.urvoices.data.model.TypeNotification
import com.example.urvoices.data.repository.PostRepository
import com.example.urvoices.data.service.FirebasePostService
import com.example.urvoices.utils.SharedPreferencesHelper
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@SuppressLint("MutableCollectionMutableState")
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val firestore: FirebaseFirestore,
    private val firebasePostService: FirebasePostService,
    private val postRepository: PostRepository,
    savedStateHandle: SavedStateHandle
): ViewModel(){

    val TAG = "HomeViewModel"
    private val _homeState = MutableLiveData<HomeState>()
    val homeState: LiveData<HomeState> = _homeState

    private var _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> get() = _posts

    init {
       viewModelScope.launch {
           loadingData()
       }
    }

    suspend fun loadingData() {
        _homeState.value = HomeState.LoadingData
        val newPostList = postRepository.getNewFeed()
        _posts.value = newPostList.value
        Log.e(TAG, "loadingData: ${newPostList.value}")
        _homeState.value = HomeState.LoadedData
    }

    //User Like -> Noti
    suspend fun likePost(userID: String, postID: String = ""): Boolean {
        val noti = Notification(
            id = null,
            userID = userID,
            message = MessageNotification.LIKE_POST,
            typeNotification = TypeNotification.LIKE_POST,
            isRead = false,
            url = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = null,
            deleteAt = null
        )

        val result = firestore.collection("notifications").add(noti)
            .addOnCompleteListener {
                    firestore.collection("notifications").document(it.result?.id!!).update("id", it.result?.id!!)
            }
            .addOnFailureListener {
                Log.e("HomeViewModel", "likePost: ${it.message}")
            }
            .await()
        return result != null
    }

    suspend fun likeComment(userID: String, commentID: String = ""): Boolean {
        val noti: Notification = Notification(
            id = null,
            userID = userID,
            message = MessageNotification.LIKE_COMMENT,
            typeNotification = TypeNotification.LIKE_COMMENT,
            isRead = false,
            url = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = null,
            deleteAt = null
        )

        val result = firestore.collection("notifications").add(noti)
            .addOnCompleteListener {
                firestore.collection("notifications").document(it.result?.id!!).update("id", it.result?.id!!)
            }
            .addOnFailureListener {
                Log.e("HomeViewModel", "likeComment: ${it.message}")
            }
            .await()
        return result != null
    }

    //User Comment -> Noti
    suspend fun commentPost(userID: String, postID: String = "", comment: Comment): Boolean {
        try {
            //create comment
            val resultComment = firestore.collection("comments").add(comment.toCommentMap())
                .addOnCompleteListener {
                    firestore.collection("comments").document(it.result?.id!!)
                        .update("id", it.result?.id!!)
                }
                .addOnFailureListener {
                    Log.e("HomeViewModel", "commentPost: ${it.message}")
                }
                .await()

            //create relation comment
            if (resultComment.id.isNotEmpty()) {
                val resultRelation =
                    firestore.collection("comments_posts_users").add(comment.toRelationMap())
                        .addOnCompleteListener {
                            firestore.collection("relation_comments").document(it.result?.id!!)
                                .update("id", it.result?.id!!)
                        }
                        .addOnFailureListener {
                            Log.e("HomeViewModel", "commentPost: ${it.message}")
                        }
                        .await()
            }

            //send Noti
            if (resultComment.id.isNotEmpty()) {
                val noti = Notification(
                    id = null,
                    userID = userID,
                    message = MessageNotification.COMMENT_POST,
                    typeNotification = TypeNotification.COMMENT_POST,
                    isRead = false,
                    url = "",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = null,
                    deleteAt = null
                )

                val resultNoti = firestore.collection("notifications").add(noti)
                    .addOnCompleteListener {
                        firestore.collection("notifications").document(it.result?.id!!)
                            .update("id", it.result?.id!!)
                    }
                    .addOnFailureListener {
                        Log.e("HomeViewModel", "commentPost: ${it.message}")
                    }
                    .await()
            }
            return true
        } catch (e: Exception) {
            Log.e("HomeViewModel", "commentPost: ${e.message}")
        }
        return false
    }

    //Post Loader
    suspend fun getNewFeed(): List<Post> {
        val result = firebasePostService.getNewFeed()
        return result
    }

    suspend fun getUserInfo(userID: String): Map<String, String> {
        val result = firebasePostService.getUserInfo(userID)
        return result
    }
}

sealed class HomeState {
    object LoadingData : HomeState()
    object LoadedData : HomeState()
    data class ShowToast(val message: String) : HomeState()
    data class ShowError(val message: String) : HomeState()
    data class ShowSuccess(val message: String) : HomeState()
    data class ShowLoading(val message: String) : HomeState()
    data class ShowPost(val message: String) : HomeState()
    data class ShowProfile(val message: String) : HomeState()
    data class ShowSettings(val message: String) : HomeState()
    data class ShowSearch(val message: String) : HomeState()
    data class ShowNotification(val message: String) : HomeState()
    data class ShowNewPost(val message: String) : HomeState()
    data class ShowNewFeed(val message: String) : HomeState()
    data class ShowNewProfile(val message: String) : HomeState()
    data class ShowNewSettings(val message: String) : HomeState()
    data class ShowNewSearch(val message: String) : HomeState()
    data class ShowNewNotification(val message: String) : HomeState()
    data class ShowNewPostDetail(val message: String) : HomeState()
    data class ShowNewFeedDetail(val message: String) : HomeState()
    data class ShowNewProfileDetail(val message: String) : HomeState()
    data class ShowNewSettingsDetail(val message: String) : HomeState()
    data class ShowNewSearchDetail(val message: String) : HomeState()
    data class ShowNewNotificationDetail(val message: String) : HomeState()
    data class ShowNewPostDetailComment(val message: String) : HomeState()
}