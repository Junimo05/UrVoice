package com.example.urvoices.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Comment(
    val id: String?,
    val userId: String,
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
}
