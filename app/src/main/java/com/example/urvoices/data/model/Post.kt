package com.example.urvoices.data.model

import android.os.Parcelable
import com.example.urvoices.data.db.Entity.PostEntity
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


@Serializable
@Parcelize
data class Post(
    val id: String?,
    val userId: String,
    val url: String?,
    val amplitudes: List<Int>? = null,
    val audioName: String?,
    val description: String,
    val likes: Int = 0,
    val comments: Int = 0,
    val tag: List<String>?,
    val createdAt: Long,
    val updateAt: Long?,
    val deleteAt: Long?,
) : Parcelable {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "audioName" to audioName,
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
            amplitudes = amplitudes,
            likes = likes,
            comments = comments,
            tags = tag,
            createdAt = createdAt,
            updatedAt = updateAt,
            deletedAt = deleteAt,
        )
    }
}