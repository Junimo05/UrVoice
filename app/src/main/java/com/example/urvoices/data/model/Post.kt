package com.example.urvoices.data.model

import com.example.urvoices.data.db.Entity.PostEntity

data class Post(
    val id: String?,
    val userId: String,
    val url: String?,
    val audioName: String?,
    val description: String,
    val likes: Int?,
    val comments: Int?,
    val tag: List<String>?,
    val createdAt: Long,
    val updateAt: Long?,
    val deleteAt: Long?,
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "description" to description,
            "tag" to tag,
            "createdAt" to createdAt,
            "updateAt" to updateAt,
            "deleteAt" to deleteAt,
        )
    }

    fun toEntity(): PostEntity {
        return PostEntity(
            id = id!!,
            userId = userId,
            description = description,
            audioName = audioName,
            audioUrl = url,
            likes = likes,
            comments = comments,
            tags = tag,
            createdAt = createdAt,
            updatedAt = updateAt,
            deletedAt = deleteAt,
        )
    }
}