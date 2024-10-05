package com.example.urvoices.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Comment(
    val id: String?,
    val userId: String,
    val postId: String,
    val parentCommentId: String?,
    val content: String,
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
    fun toRelationMap(): Map<String, Any?> {
        return mapOf(
            "ID" to id,
            "userId" to userId,
            "postId" to postId,
            "parentCommentId" to parentCommentId,
            "createdAt" to createdAt,
        )
    }
}
