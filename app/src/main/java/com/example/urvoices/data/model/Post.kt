package com.example.urvoices.data.model

import android.os.Parcelable
import com.algolia.search.model.ObjectID
import com.algolia.search.model.indexing.Indexable
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
    var duration: Long = 0L,
    val description: String,
    var likes: Int? = 0,
    var comments: Int? = 0,
    val _tags: List<String>?,
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
        duration = 0,
        description = "",
        likes = 0,
        comments = 0,
        _tags = listOf(),
        createdAt = 0,
        updatedAt = null,
        deletedAt = null
    )

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "audioName" to audioName,
            "description" to description,
            "_tags" to _tags,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "deletedAt" to deletedAt,
        )
    }
}