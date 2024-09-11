package com.example.thevoices.data.db.Entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "userLogin")
data class UserLogin(
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    @ColumnInfo(name = "username")
    val username:String,
    @ColumnInfo(name = "password")
    val password:String
)
