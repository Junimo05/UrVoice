package com.example.urvoices.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Like(
    val id: String?,
    val commentID: String?,
    val postID: String,
    val userID: String,
    val createdAt: String,

) : Parcelable {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "ID" to id,
            "commentID" to commentID,
            "postID" to postID,
            "userID" to userID,
            "createdAt" to createdAt,
        )
    }
}
