package com.example.urvoices.data.model
data class Likes(
    val id: String,
    val commentID: String = "",
    val postID: String = "",
    val userID: String,
    val createdAt: String,

) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "commentID" to commentID,
            "postID" to postID,
            "userID" to userID,
            "createdAt" to createdAt,
        )
    }
}
