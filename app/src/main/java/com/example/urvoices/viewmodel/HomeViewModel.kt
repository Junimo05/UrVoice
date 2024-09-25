package com.example.urvoices.viewmodel

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.example.urvoices.data.model.Comment
import com.example.urvoices.data.model.MessageNotification
import com.example.urvoices.data.model.Notification
import com.example.urvoices.data.model.Post
import com.example.urvoices.data.model.TypeNotification
import com.example.urvoices.data.service.FirebasePostService
import com.example.urvoices.utils.SharedPreferencesHelper
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val firestore: FirebaseFirestore,
    private val firebasePostService: FirebasePostService
): ViewModel(){


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

    suspend fun getAllPostFromUser(userID: String): List<Post> {
        val result = firebasePostService.getAllPostFromUser(userID)
//        Log.e("HomeViewModel", "getAllPostFromUser: $result")
        return result
    }

    suspend fun getComments_Posts(postID: String): List<Comment> {
        val result = firebasePostService.getComments_Posts(postID)
        return result
    }

    suspend fun getReply_Comments(commentID: String): List<Comment>{
        val result = firebasePostService.getReplies_Comments(commentID)
        return result
    }
}

sealed class HomeEvent {
    data class ShowToast(val message: String) : HomeEvent()
    data class ShowError(val message: String) : HomeEvent()
    data class ShowSuccess(val message: String) : HomeEvent()
    data class ShowLoading(val message: String) : HomeEvent()
    data class ShowPost(val message: String) : HomeEvent()
    data class ShowProfile(val message: String) : HomeEvent()
    data class ShowSettings(val message: String) : HomeEvent()
    data class ShowSearch(val message: String) : HomeEvent()
    data class ShowNotification(val message: String) : HomeEvent()
    data class ShowNewPost(val message: String) : HomeEvent()
    data class ShowNewFeed(val message: String) : HomeEvent()
    data class ShowNewProfile(val message: String) : HomeEvent()
    data class ShowNewSettings(val message: String) : HomeEvent()
    data class ShowNewSearch(val message: String) : HomeEvent()
    data class ShowNewNotification(val message: String) : HomeEvent()
    data class ShowNewPostDetail(val message: String) : HomeEvent()
    data class ShowNewFeedDetail(val message: String) : HomeEvent()
    data class ShowNewProfileDetail(val message: String) : HomeEvent()
    data class ShowNewSettingsDetail(val message: String) : HomeEvent()
    data class ShowNewSearchDetail(val message: String) : HomeEvent()
    data class ShowNewNotificationDetail(val message: String) : HomeEvent()
    data class ShowNewPostDetailComment(val message: String) : HomeEvent()
}