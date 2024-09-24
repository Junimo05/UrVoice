package com.example.urvoices.data.model

data class Post(
    val id: String,
    val userId: String,
    val url: String?,
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
            "id" to id,
            "description" to description,
            "tag" to tag,
            "createdAt" to createdAt,
            "updateAt" to updateAt,
            "deleteAt" to deleteAt,
        )
    }
}