package com.example.urvoices.data.model

data class Comments(
    val id: String,
    val userId: String,
    val postId: String,
    val parentCommentId: String,
    val comment: String,
    val createdAt: Long,
    val updateAt: Long,
    val deleteAt: Long,
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userID" to userId,
            "postID" to postId,
            "comment" to comment,
            "parentCommentID" to parentCommentId,
            "createdAt" to createdAt,
            "updateAt" to updateAt,
            "deleteAt" to deleteAt,
        )
    }
}
