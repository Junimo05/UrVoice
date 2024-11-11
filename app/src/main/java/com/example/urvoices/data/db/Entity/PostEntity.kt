package com.example.urvoices.data.db.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.urvoices.data.model.Post

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val description: String,
    val audioName: String ?= "",
    val audioUrl: String? = "",
    val amplitudes: List<Int>?,
    val likes: Int = 0,
    val comments: Int = 0,
    val tags : List<String>?,
    val createdAt: Long,
    val updatedAt: Long?,
    val deletedAt: Long?,
){
    fun toPost() = Post(
        ID = id,
        userId = userId,
        description = description,
        audioName = audioName,
        url = audioUrl,
        amplitudes = amplitudes,
        likes = likes,
        comments = comments,
        _tags = tags,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
}
