package com.example.urvoices.data.db.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.urvoices.data.db.Entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM posts")
    fun getAllPosts(): List<PostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(posts: List<PostEntity>)

    @Delete
    fun delete(post: PostEntity)

    @Delete
    fun deleteAll(posts: List<PostEntity>)
}