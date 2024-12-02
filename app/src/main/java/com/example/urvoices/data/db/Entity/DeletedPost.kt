package com.example.urvoices.data.db.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deleted_posts")
data class DeletedPost(
	@PrimaryKey val id: String = "",
	val userID : String = "",
	val username: String = "",
	val imgUrl : String = "",
	val avatarUrl: String = "",
	val audioName: String = "",
	val deletedAt: Long = 0L
)