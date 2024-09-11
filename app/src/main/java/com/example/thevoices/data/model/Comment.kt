package com.example.thevoices.data.model

class Comment {
    var id: Int = 0
    var postId: Int = 0
    var userId: Int = 0
    var post: Post? = null
    var like: Int = 0
    var content: String = ""
    var createdAt: String = ""
    var updatedAt: String = ""
}