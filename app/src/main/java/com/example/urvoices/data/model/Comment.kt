package com.example.urvoices.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Comment(
    val id: String?,
    val userId: String,
    val parentId : String?,
    val postId: String,
    val content: String,
    val likes: Int = 0,
    val replyComments: Int = 0,
    val createdAt: Long,
    val updatedAt: Long?,
    val deletedAt: Long?,
):Parcelable {
    fun toCommentMap(): Map<String, Any?> {
        return mapOf(
            "ID" to id,
            "content" to content,
            "createdAt" to createdAt,
            "updateAt" to updatedAt,
            "deleteAt" to deletedAt,
        )
    }

    fun toRelaMap(): Map<String, Any?> {
        return mapOf(
            "ID" to null,
            "commentID" to id,
            "userID" to userId,
            "postID" to postId,
            "parentID" to parentId,
            "createdAt" to System.currentTimeMillis(),
        )
    }
}
