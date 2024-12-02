package com.example.urvoices.data.db.Dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.example.urvoices.data.db.Entity.DeletedPost

@Dao
interface DeletedPostDao {

	@Query("SELECT * FROM deleted_posts")
	fun getAll(): List<DeletedPost>

	@Query("SELECT * FROM deleted_posts WHERE id = :id")
	fun getById(id: String): DeletedPost

	@Query("SELECT * FROM deleted_posts ORDER BY deletedAt DESC")
	fun getPagingSource(): PagingSource<Int, DeletedPost>

	@Query("SELECT MAX(deletedAt) FROM deleted_posts")
	fun getLastTimestamp(): Long?

	@Delete
	fun delete(deletedPost: DeletedPost)

	@Query("DELETE FROM deleted_posts")
	fun deleteAll()

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(deletedPost: DeletedPost)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertAll(deletedPosts: List<DeletedPost>)

}