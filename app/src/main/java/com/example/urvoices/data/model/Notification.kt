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
    val imgUrl: String,
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
            "imgUrl" to imgUrl,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "deletedAt" to deleteAt
        )
    }
}

fun replaceMessage(message: String, replacePart: String, newPart: String): String{
    return message.replace(replacePart, newPart)
}