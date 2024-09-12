package com.example.urvoices.data.db.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.urvoices.data.db.Entity.UserLogin
import kotlinx.coroutines.flow.Flow

@Dao
interface UserLoginDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: UserLogin)
    @Update
    suspend fun updateUser(user: UserLogin)
    @Delete
    suspend fun deleteUser(user: UserLogin)

    @Query("SELECT * FROM userLogin ORDER BY id ASC")
    fun getAllUsers(): Flow<List<UserLogin>>

    @Query("SELECT * FROM userLogin WHERE username = :username")
    fun getUserByUsername(username: String): Flow<UserLogin>

}