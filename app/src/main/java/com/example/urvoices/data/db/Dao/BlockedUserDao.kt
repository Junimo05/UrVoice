package com.example.urvoices.data.db.Dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.urvoices.data.db.Entity.BlockedUser

@Dao
interface BlockedUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedUser(blockedUser: BlockedUser)

    @Query("DELETE FROM blocked_users WHERE userID = :userID")
    suspend fun delete(userID: String)

    @Query("DELETE FROM blocked_users")
    suspend fun deleteAll()

    @Query("SELECT * FROM blocked_users")
    fun getAllBlockedUsers(): PagingSource<Int, BlockedUser>

    @Query("SELECT * FROM blocked_users WHERE userID = :userID")
    suspend fun getBlockedUser(userID: String): BlockedUser?
}
