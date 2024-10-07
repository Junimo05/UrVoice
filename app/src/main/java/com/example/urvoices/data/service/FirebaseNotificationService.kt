package com.example.urvoices.data.service

import android.util.Log
import com.example.urvoices.data.model.MessageNotification
import com.example.urvoices.data.model.Notification
import com.example.urvoices.data.model.TypeNotification
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


/*
    TODO: Add Url to Notification for redirect to the specific page
 */

class FirebaseNotificationService @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,

    ){
    val TAG = "FirebaseNotificationService"
    suspend fun followUser(targetUserID: String, actionUsername: String , followInfoID: String): Boolean {
        val noti = Notification(
            id = null,
            targetUserID = targetUserID,
            infoID = followInfoID,
            message = actionUsername + " " + MessageNotification.FOLLOW_USER,
            typeNotification = TypeNotification.FOLLOW_USER,
            isRead = false,
            url = "",
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
        return result != null
    }

    suspend fun likePost(targetUserID: String, actionUsername: String, likeID: String): Boolean {
        val noti = Notification(
            id = null,
            targetUserID = targetUserID,
            infoID = likeID,
            message = actionUsername + " " + MessageNotification.LIKE_POST,
            typeNotification = TypeNotification.LIKE_POST,
            isRead = false,
            url = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = null,
            deleteAt = null
        )

        val result = firebaseFirestore.collection("notifications").add(noti)
            .addOnCompleteListener {
                firebaseFirestore.collection("notifications").document(it.result?.id!!).update("ID", it.result?.id!!)
            }
            .addOnFailureListener {
               Log.e(TAG, "likePost: ${it.message}")
            }
            .await()
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
            url = "",
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
            url = "",
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
            url = "",
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


}