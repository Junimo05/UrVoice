package com.example.urvoices.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Comment(
    val id: String,
    val userId: String,
    val postId: String,
    val parentCommentId: String?,
    val content: String,
    val createdAt: Long,
    val updateAt: Long?,
    val deleteAt: Long?,
):Parcelable {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "userID" to userId,
            "postID" to postId,
            "content" to content,
            "parentCommentID" to parentCommentId,
            "createdAt" to createdAt,
            "updateAt" to updateAt,
            "deleteAt" to deleteAt,
        )
    }
}
