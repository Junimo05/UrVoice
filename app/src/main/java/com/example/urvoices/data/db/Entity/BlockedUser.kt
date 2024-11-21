package com.example.urvoices.data.db.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_users")
data class BlockedUser(
    @PrimaryKey val userID: String,
    val username: String = "",
    val avatarUrl: String = ""
)