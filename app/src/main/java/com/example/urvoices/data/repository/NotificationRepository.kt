package com.example.urvoices.data.repository

import com.example.urvoices.data.service.FirebaseNotificationService
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val notificationService: FirebaseNotificationService
){
    val TAG = "NotificationRepository"

    suspend fun followUser(targetUserID: String, actionUsername: String, followInfoID: String): Boolean {
        return notificationService.followUser(targetUserID, actionUsername, followInfoID)
    }

    suspend fun likePost(targetUserID: String, actionUsername: String, relaID: String): Boolean {
        return notificationService.likePost(targetUserID, actionUsername, relaID)
    }

    suspend fun commentPost(targetUserID: String, actionUsername: String, relaID: String): Boolean {
        return notificationService.commentPost(targetUserID, actionUsername, relaID)
    }

    suspend fun likeComment(targetUserID: String, actionUsername: String, relaID: String): Boolean {
        return notificationService.likeComment(targetUserID, actionUsername , relaID)
    }

    suspend fun replyComment(targetUserID: String, actionUsername: String, relaID: String): Boolean {
        return notificationService.replyComment(targetUserID, actionUsername ,relaID)
    }

}