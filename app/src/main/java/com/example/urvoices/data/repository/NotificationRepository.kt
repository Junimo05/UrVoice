package com.example.urvoices.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.urvoices.data.db.Dao.NotificationDao
import com.example.urvoices.data.db.Entity.Notification
import com.example.urvoices.data.remotemediator.NotificationRM
import com.example.urvoices.data.service.FirebaseNotificationService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationRepository @Inject constructor(
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
            createdAt = document.getLong("createdAt") ?: System.currentTimeMillis(),
            isRead = document.getBoolean("isRead") ?: false
        )
    }

    suspend fun fetchNewNotifications() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid
        val lastTimestamp = notificationDao.getLatestTimestamp() ?: 0
        firebaseFirestore.collection("notifications")
            .whereEqualTo("targetUserID", userId)
            .whereGreaterThan("createdAt", lastTimestamp)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val newNotifications = querySnapshot.documents
                    .map { mapFirestoreToNotification(it) }

                scope.launch {
                    notificationDao.insertAll(newNotifications)
                }
            }
    }

    suspend fun markNotificationAsRead(notificationId: String) {
        notificationDao.markAsRead(notificationId)
    }

    suspend fun cleanupOldNotifications(daysToKeep: Int = 30) {
        val threshold = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        notificationDao.deleteOldNotifications(threshold)
    }

    suspend fun followUser(targetUserID: String, actionUsername: String, followInfoID: String): Boolean {
        return notificationService.followUser(
            targetUserID = targetUserID,
            actionUsername = actionUsername,
            followInfoID = followInfoID
        )
    }

    suspend fun likePost(targetUserID: String, actionUsername: String, relaID: String): Boolean {
        return notificationService.likePost(
            targetUserID = targetUserID,
            actionUsername = actionUsername,
            likeID = relaID
        )
    }

    suspend fun commentPost(targetUserID: String, actionUsername: String, relaID: String): Boolean {
        return notificationService.commentPost(
            targetUserID = targetUserID,
            actionUsername = actionUsername,
            relaID = relaID
        )
    }

    suspend fun likeComment(targetUserID: String, actionUsername: String, relaID: String): Boolean {
        return notificationService.likeComment(
            targetUserID = targetUserID,
            actionUsername = actionUsername,
            relaID = relaID
        )
    }

    suspend fun replyComment(targetUserID: String, actionUsername: String, relaID: String): Boolean {
        return notificationService.replyComment(
            targetUserID = targetUserID,
            actionUsername = actionUsername,
            relaID = relaID
        )
    }

}