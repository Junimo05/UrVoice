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
    fun toCommentMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "content" to content,
            "createdAt" to createdAt,
            "updateAt" to updateAt,
            "deleteAt" to deleteAt,
        )
    }
    fun toRelationMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "postId" to postId,
            "parentCommentId" to parentCommentId,
        )
    }
}
