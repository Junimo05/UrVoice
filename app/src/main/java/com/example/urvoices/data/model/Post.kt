package com.example.urvoices.data.model

data class Post(
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val tag: List<String>,
    val createdAt: Long,
    val updateAt: Long,
    val deleteAt: Long,
    val url: String,
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userID" to userId,
            "title" to title,
            "description" to description,
            "tag" to tag,
            "createdAt" to createdAt,
            "updateAt" to updateAt,
            "deleteAt" to deleteAt,
            "url" to url,
        )
    }
}