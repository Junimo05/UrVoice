package com.example.urvoices.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Notification(
    val id: String?,
    val targetUserID: String,
    val infoID: String,
    val message: String, // message notification
    val typeNotification: String, // type notification
    val isRead: Boolean,
    val url: String,
    val createdAt: Long,
    val updatedAt: Long?,
    val deleteAt: Long?,
) : Parcelable {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "ID" to id,
            "targetUserID" to targetUserID,
            "infoID" to infoID,
            "message" to message,
            "typeNotification" to typeNotification,
            "isRead" to isRead,
            "url" to url,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "deleteAt" to deleteAt
        )
    }
}

data object TypeNotification {
    const val FOLLOW_USER = "FOLLOW_USER"
    const val LIKE_POST = "LIKE_POST"
    const val LIKE_COMMENT = "LIKE_COMMENT"
    const val COMMENT_POST = "COMMENT_POST"
    const val REPLY_COMMENT = "REPLY_COMMENT"
}

data object MessageNotification {
    const val FOLLOW_USER = "followed you"
    const val LIKE_POST = "liked your post"
    const val LIKE_COMMENT = "liked your comment"
    const val COMMENT_POST = "commented on your post"
    const val REPLY_COMMENT = "replied to your comment"
}