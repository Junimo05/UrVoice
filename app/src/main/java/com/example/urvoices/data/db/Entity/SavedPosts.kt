package com.example.urvoices.data.db.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_posts")
data class SavedPost(
	@PrimaryKey val id: String = "",
	val userID : String = "",
	val username: String = "",
	val imgUrl : String = "",
	val avatarUrl: String = "",
	val audioName: String = "",
	val audioUrl: String = "",
	val isDeleted: Boolean = false
)
