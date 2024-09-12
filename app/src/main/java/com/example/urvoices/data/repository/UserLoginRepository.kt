package com.example.urvoices.data.repository

import com.example.urvoices.data.db.Dao.UserLoginDAO
import com.example.urvoices.data.db.Entity.UserLogin
import com.example.urvoices.di.IODispatcher
import okhttp3.Dispatcher
import javax.inject.Inject

class UserLoginRepository @Inject constructor(
    private val userLoginDao: UserLoginDAO, // offline service
    @IODispatcher val dispatcher: Dispatcher
) {
    suspend fun insertUser(user: UserLogin) {
        userLoginDao.insertUser(user)
    }

    suspend fun updateUser(user: UserLogin) {
        userLoginDao.updateUser(user)
    }

    suspend fun deleteUser(user: UserLogin) {
        userLoginDao.deleteUser(user)
    }

    fun getAllUsers() = userLoginDao.getAllUsers()

    fun getUserByUsername(username: String) = userLoginDao.getUserByUsername(username)
}