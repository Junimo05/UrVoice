package com.example.urvoices.data.db.Dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.urvoices.data.db.Entity.SavedPost

@Dao
interface SavedPostDao {
	@Query("SELECT * FROM saved_posts")
	fun getAll(): PagingSource<Int, SavedPost>


	@Insert
	fun insert(savedPost: SavedPost)

	//insert all
	@Insert
	fun insertAll(savedPosts: List<SavedPost>)

	@Delete
	fun delete(savedPost: SavedPost)

	@Query("DELETE FROM saved_posts")
	fun deleteAll()

}