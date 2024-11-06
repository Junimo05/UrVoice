package com.example.urvoices.data.model

import android.os.Parcelable
import com.example.urvoices.data.db.Entity.PostEntity
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


@Serializable
@Parcelize
data class Post(
    val ID: String?,
    val userId: String,
    val url: String? = "",
    var amplitudes: List<Int>? = null,
    val audioName: String? = "",
    val description: String,
    var likes: Int? = 0,
    var comments: Int? = 0,
    val tag: List<String>?,
    val createdAt: Long,
    val updatedAt: Long?,
    val deletedAt: Long?,
) : Parcelable {
    constructor() : this(
        ID = "",
        userId = "",
        url = "",
        amplitudes = listOf(),
        audioName = "No Name",
        description = "",
        likes = 0,
        comments = 0,
        tag = listOf(),
        createdAt = 0,
        updatedAt = null,
        deletedAt = null
    )
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "audioName" to audioName,
            "description" to description,
            "tag" to tag,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "deletedAt" to deletedAt,
        )
    }
}