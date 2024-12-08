package com.example.urvoices.data.repository

import android.content.Context
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.urvoices.data.db.Dao.NotificationDao
import com.example.urvoices.data.db.Entity.Notification
import com.example.urvoices.data.model.replaceMessage
import com.example.urvoices.data.remotemediator.NotificationRM
import com.example.urvoices.data.service.FirebaseNotificationService
import com.example.urvoices.utils.MessageNotification
import com.example.urvoices.utils.MessageNotificationAfterAction
import com.example.urvoices.viewmodel.NewNotificationEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val context: Context,
    private val notificationService: FirebaseNotificationService,
    private val firebaseFirestore: FirebaseFirestore,
    private val notificationDao: NotificationDao,
    private val auth : FirebaseAuth
){
    val TAG = "NotificationRepository"
    val scope = CoroutineScope(Dispatchers.IO)
    @OptIn(ExperimentalPagingApi::class)
    fun getNotificationsFlow(): Flow<PagingData<Notification>> = Pager(
        config = PagingConfig(pageSize = 20),
        remoteMediator = NotificationRM(firebaseFirestore, notificationDao, auth = FirebaseAuth.getInstance())
    ) {
        notificationDao.getPagingSource()
    }.flow

    private fun mapFirestoreToNotification(document: DocumentSnapshot): Notification {
        return Notification(
            id = document.id,
            type = document.getString("typeNotification") ?: "",
            message = document.getString("message") ?: "",
            imgUrl = document.getString("imgUrl") ?: "",
            infoID = document.getString("infoID") ?: "",
            createdAt = document.getLong("createdAt") ?: System.currentTimeMillis(),
            isRead = document.getBoolean("isRead") ?: false
        )
    }

    fun fetchNewNotifications() {
        try {
            scope.launch {
                val currentUser = auth.currentUser ?: return@launch
//                Log.e(TAG, "fetchNewNotifications userid: ${currentUser.uid}")
                val userId = currentUser.uid
                val lastTimestamp = notificationDao.getLatestTimestamp() ?: 0
//                Log.e(TAG, "fetchNewNotifications time: $lastTimestamp")
                firebaseFirestore.collection("notifications")
                    .whereEqualTo("targetUserID", userId)
                    .whereGreaterThan("createdAt", lastTimestamp)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val newNotifications = querySnapshot.documents
                            .map {
                                mapFirestoreToNotification(it)
                            }
//                        Log.e(TAG, "fetchNewNotifications: ${newNotifications.size}")
                        scope.launch {
//                            Log.e(TAG, "fetchNewNotifications: ${newNotifications.size}")
                            if(newNotifications.isNotEmpty()){
                                EventBus.getDefault().post(NewNotificationEvent(newNotifications.size))
                                notificationDao.insertAll(newNotifications)
                            }

                        }
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "fetchNewNotifications: ${it.message}")
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchNewNotifications: ${e.message}")
        }
    }

    fun updateMessageText(notiID: String, message: String){
        scope.launch {
            notificationService.updateMessageText(notiID, message)
        }
    }

    suspend fun markNotificationAsRead(notificationId: String) {
        notificationDao.markAsRead(notificationId)
    }

    suspend fun cleanupOldNotifications(daysToKeep: Int = 30) {
        val threshold = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        notificationDao.deleteOldNotifications(threshold)
    }

    suspend fun followUser(targetUserID: String, actionUsername: String, followInfoID: String, isPrivate: Boolean): Boolean {
        return notificationService.followUser(
            targetUserID = targetUserID,
            actionUsername = actionUsername,
            followInfoID = followInfoID,
            isPrivate = isPrivate
        )
    }

    suspend fun getInfoForFollowUserNoti(followInfoID: String): Map<String, Any> {
        try {
            val result = firebaseFirestore.collection("follows").document(followInfoID).get().await()
            val userID = result.getString("userID")
            val accepted = result.getBoolean("accepted")
            return mapOf(
                "userID" to userID!!,
                "accepted" to accepted!!
            )
        }catch (e: Exception) {
            Log.e(TAG, "getInfoForFollowUserNoti: ${e.message}")
        }
        return mapOf()
    }

    suspend fun likePost(targetUserID: String, actionUsername: String, relaID: String): Boolean {
        return notificationService.likePost(
            targetUserID = targetUserID,
            actionUsername = actionUsername,
            likeID = relaID
        )
    }

    suspend fun getInfoForLikePostNoti(relaID: String): Map<String, String> {
        try {
            Log.e(TAG, "RelaID get: $relaID")
            val result = firebaseFirestore.collection("likes").document(relaID).get().await()
            Log.e(TAG, "getLikeByRelaID result: ${result.data}")
            val userID = result.getString("userID")
            val postID = result.getString("postID")

            return mapOf(
                "userID" to userID!!,
                "postID" to postID!!
            )
        }catch (e: Exception) {
            Log.e(TAG, "getLikeByRelaID: ${e.message}")
        }
        return mapOf()
    }

    suspend fun likeComment(targetUserID: String, actionUsername: String, relaID: String): Boolean {
        return notificationService.likeComment(
            targetUserID = targetUserID,
            actionUsername = actionUsername,
            relaID = relaID
        )
    }

    suspend fun getInfoForLikeCommentNoti(rela: String): Map<String,String>{
        try {
            val result = firebaseFirestore.collection("likes").document(rela).get().await()
            val userID = result.getString("userID")
            val postID = result.getString("postID")
            val commentID = result.getString("commentID")
            return mapOf(
                "userID" to userID!!,
                "postID" to postID!!,
                "commentID" to commentID!!
            )
        }catch (e: Exception){
            Log.e(TAG, "getInfoForLikeCommentNoti: ${e.message}")
        }
        return mapOf()
    }

    suspend fun commentPost(targetUserID: String, actionUsername: String, relaID: String): Boolean {
        return notificationService.commentPost(
            targetUserID = targetUserID,
            actionUsername = actionUsername,
            relaID = relaID
        )
    }

    suspend fun getInfoForCommentPostNoti(relaID: String): Map<String, String> {
        try {
            val result = firebaseFirestore.collection("rela_comments_users_posts").document(relaID).get().await()
            val userID = result.getString("userID")
            val postID = result.getString("postID")
            val commentID = result.getString("commentID")
            return mapOf(
                "userID" to userID!!,
                "postID" to postID!!,
                "commentID" to commentID!!
            )
        }catch (e: Exception) {
            Log.e(TAG, "getInfoForCommentPostNoti: ${e.message}")
        }
        return mapOf()
    }

    suspend fun replyComment(targetUserID: String, actionUsername: String, relaID: String): Boolean {
        return notificationService.replyComment(
            targetUserID = targetUserID,
            actionUsername = actionUsername,
            relaID = relaID
        )
    }

    suspend fun getInfoForReplyCommentNoti(relaID: String): Map<String, String> {
        try {
            val result = firebaseFirestore.collection("rela_comments_users_posts").document(relaID).get().await()
            val userID = result.getString("userID")
            val postID = result.getString("postID")
            val commentID = result.getString("commentID")
            val parentID = result.getString("parentID")
            return mapOf(
                "userID" to userID!!,
                "postID" to postID!!,
                "commentID" to commentID!!,
                "parentID" to parentID!!
            )
        }catch (e: Exception) {
            Log.e(TAG, "getInfoForReplyCommentNoti: ${e.message}")
        }
        return mapOf()
    }

    suspend fun acceptFollowRequest(notiID: String, relaID: String): Boolean {
        try {
            //change data in Dao
            notificationDao.updateReadStatus(relaID)
            val oldMessage = notificationDao.getMessage(notiID)
            val newMessage = replaceMessage(oldMessage, MessageNotification.REQUEST_FOLLOW, MessageNotificationAfterAction.ACCEPT_FOLLOW_USER)
            notificationDao.updateMessage(notiID, newMessage)

            return notificationService.acceptFollowRequest(
                notiID = notiID,
                relaID = relaID,
                newMessage = newMessage
            )
        } catch (e: Exception) {
            Log.e(TAG, "acceptFollowRequest: ", e)
        }
        return false
    }

    suspend fun rejectFollowRequest(notiID: String, relaID: String): Boolean {
        try {
            //delete data in Dao
            notificationDao.deleteNotification(notiID)
            return notificationService.rejectFollowRequest(
                notiID = notiID,
                relaID = relaID
            )
        } catch (e: Exception) {
            Log.e(TAG, "rejectFollowRequest: ", e)
        }
        return false
    }

    suspend fun deleteRequestFollow(followID: String, actionUserID: String, targetUserID: String): Boolean {
        try {
            //delete Dao
            val notiRef = firebaseFirestore.collection("notifications")
                .whereEqualTo("infoID", followID)
                .whereEqualTo("targetUserID", targetUserID)
                .get().await()
            //delete Noti on Firebase
            notiRef.documents.forEach {
                it.reference.delete().await()
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "deleteRequestFollow: ", e)
        }
        return false
    }

}