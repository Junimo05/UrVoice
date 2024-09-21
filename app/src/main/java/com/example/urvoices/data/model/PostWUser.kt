package com.example.urvoices.data.model

data class PostWithUser(
    val post: Post,
    val username: String,
    val avatarUrl: String
)