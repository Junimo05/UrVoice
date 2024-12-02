package com.example.urvoices.data.db.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notification(
	@PrimaryKey val id: String = "",
	val type: String = "",
	val message: String = "",
	val createdAt: Long = System.currentTimeMillis(),
	val imgUrl: String = "",
	val infoID: String = "",
	val isRead: Boolean = false
)