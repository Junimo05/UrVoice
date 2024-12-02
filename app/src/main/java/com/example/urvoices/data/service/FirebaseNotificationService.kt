package com.example.urvoices.data.service

import android.util.Log

import com.example.urvoices.data.model.Notification
import com.example.urvoices.utils.MessageNotification
import com.example.urvoices.utils.TypeNotification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


/*
    TODO: Add Url to Notification for redirect to the specific page
 */

class FirebaseNotificationService @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val auth: FirebaseAuth

){
    val TAG = "FirebaseNotificationService"

    suspend fun updateMessageText(notiID: String, message: String){
        try {
            firebaseFirestore.collection("notifications").document(notiID).update("message", message).await()
        } catch (e: Exception) {
            Log.e(TAG, "updateMessageText: ${e.message}")
        }
    }

    suspend fun followUser(targetUserID: String, actionUsername: String, followInfoID: String, isPrivate: Boolean): Boolean {
        return try {
            val noti = Notification(
                id = null,
                targetUserID = targetUserID,
                infoID = followInfoID,
                message = actionUsername + " " + if (isPrivate) MessageNotification.REQUEST_FOLLOW else MessageNotification.FOLLOW_USER,
                typeNotification = if (isPrivate) TypeNotification.REQUEST_FOLLOW else TypeNotification.FOLLOW_USER,
                isRead = false,
                imgUrl = "",
                createdAt = System.currentTimeMillis(),
                updatedAt = null,
                deleteAt = null
            )

            val result = firebaseFirestore.collection("notifications").add(noti.toMap())
                .addOnCompleteListener {
                    firebaseFirestore.collection("notifications").document(it.result?.id!!).update("ID", it.result?.id!!)
                }
                .addOnFailureListener {
                    Log.e(TAG, "followUser: ${it.message}")
                }
                .await()
            result != null
        } catch (e: Exception) {
            Log.e(TAG, "followUser: ${e.message}")
            false
        }
    }

    suspend fun likePost(targetUserID: String, actionUsername: String, likeID: String): Boolean {
        val noti = Notification(
            id = null,
            targetUserID = targetUserID,
            infoID = likeID,
            message = actionUsername + " " + MessageNotification.LIKE_POST,
            typeNotification = TypeNotification.LIKE_POST,
            isRead = false,
            imgUrl = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = null,
            deleteAt = null
        )

        val result = firebaseFirestore.collection("notifications").add(noti.toMap())
            .addOnCompleteListener {
                firebaseFirestore.collection("notifications").document(it.result?.id!!).update("ID", it.result?.id!!)
            }
            .addOnFailureListener {
               Log.e(TAG, "likePost: ${it.message}")
            }
            .await()
        Log.e(TAG, "likePost Check: $result")
        return result != null
    }

    suspend fun likeComment(targetUserID: String, actionUsername: String, relaID: String): Boolean {
        val noti = Notification(
            id = null,
            targetUserID = targetUserID,
            infoID = relaID,
            message = actionUsername + " " + MessageNotification.LIKE_COMMENT,
            typeNotification = TypeNotification.LIKE_COMMENT,
            isRead = false,
            imgUrl = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = null,
            deleteAt = null
        )

        val createNoti = firebaseFirestore.collection("notifications").add(noti.toMap())
            .addOnCompleteListener {
                firebaseFirestore.collection("notifications").document(it.result?.id!!).update("ID", it.result?.id!!)
            }
            .addOnFailureListener {
                Log.e(TAG, "likeComment: ${it.message}")
            }
            .await()
        return createNoti != null
    }

    suspend fun replyComment(targetUserID: String, actionUsername: String, relaID: String): Boolean {
        val noti = Notification(
            id = null,
            targetUserID = targetUserID,
            infoID = relaID,
            message = actionUsername + " " + MessageNotification.REPLY_COMMENT,
            typeNotification = TypeNotification.REPLY_COMMENT,
            isRead = false,
            imgUrl = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = null,
            deleteAt = null
        )

        val createNoti = firebaseFirestore.collection("notifications").add(noti.toMap())
            .addOnCompleteListener {
                firebaseFirestore.collection("notifications").document(it.result?.id!!).update("ID", it.result?.id!!)
            }
            .addOnFailureListener {
                Log.e(TAG, "replyComment: ${it.message}")
            }
            .await()
        return createNoti != null
    }

    suspend fun commentPost(targetUserID: String, actionUsername: String, relaID: String): Boolean {
        val noti = Notification(
            id = null,
            targetUserID = targetUserID,
            infoID = relaID,
            message = actionUsername + " " + MessageNotification.COMMENT_POST,
            typeNotification = TypeNotification.COMMENT_POST,
            isRead = false,
            imgUrl = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = null,
            deleteAt = null
        )

        val createNoti = firebaseFirestore.collection("notifications").add(noti.toMap())
            .addOnCompleteListener {
                firebaseFirestore.collection("notifications").document(it.result?.id!!).update("ID", it.result?.id!!)
            }
            .addOnFailureListener {
                Log.e(TAG, "commentPost: ${it.message}")
            }
            .await()
        return createNoti != null
    }

    suspend fun acceptFollowRequest(notiID: String, relaID: String, newMessage: String): Boolean {
        val user = auth.currentUser
        if (user != null) {
            try {
                val notiRef = firebaseFirestore.collection("notifications").document(notiID)
                notiRef.update("isRead", true).await()
                notiRef.update("message", newMessage).await()
                val documents = firebaseFirestore.collection("follows")
                    .document(relaID)
                    .get()
                    .await()
                documents.reference.update("accepted", true).await()
                //change message

                return true
            } catch (e: Exception) {
                // Handle exception
                e.printStackTrace()
                Log.e(TAG, "acceptFollowRequest: ${e.message}")
            }
        } else {
            // Inform the user that they are not signed in
            println("No user is currently signed in.")
        }
        return false
    }

    suspend fun rejectFollowRequest(notiID: String, relaID: String): Boolean {
        val user = auth.currentUser
        if (user != null) {
            try {
                firebaseFirestore.collection("follows")
                    .document(relaID)
                    .delete()
                    .await()
                firebaseFirestore.collection("notifications").document(notiID).delete().await()
                return true
            } catch (e: Exception) {
                // Handle exception
                e.printStackTrace()
                Log.e(TAG, "rejectFollowRequest: ${e.message}")
            }
        } else {
            // Inform the user that they are not signed in
            println("No user is currently signed in.")
        }
        return false
    }

}