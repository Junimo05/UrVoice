package com.example.urvoices.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.urvoices.data.db.Dao.BlockedUserDao
import com.example.urvoices.data.db.Entity.BlockedUser
import com.example.urvoices.data.service.FirebaseBlockService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BlockRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val blockFirebase: FirebaseBlockService,
    private val blockedUserDao: BlockedUserDao
) {
    val scope = CoroutineScope(Dispatchers.IO)
    suspend fun blockUser(targetID: String): String {
        val result = blockFirebase.blockUser(targetID)
        if(result != ""){
            val document = firestore.collection("users").document(targetID).get().await()
            val username = document.getString("username") ?: ""
            val avatarUrl = document.getString("avatarUrl") ?: ""
            blockedUserDao.insertBlockedUser(
                BlockedUser(
                    targetID,
                    username,
                    avatarUrl
                ))
        }
        return result
    }

    suspend fun unblockUser(targetID: String): String {
        try {
            val result = blockFirebase.unblockUser(targetID)
            if(result != ""){
                blockedUserDao.delete(targetID)
            }
            return result
        } catch (e: Exception) {
            return ""
        }
    }

    suspend fun getBlockStatus(targetID: String): Boolean {
        return blockedUserDao.getBlockedUser(targetID) != null
    }

    suspend fun getBlockStatusFromFirebase(targetID: String): String {
        return blockFirebase.getBlockStatus(targetID)
    }

    suspend fun syncBlockUsers(){
        val snapshot = blockFirebase.getAllBlockList()
        val blockedList = snapshot!!.targetID.map { it }
        blockedUserDao.deleteAll()
        blockedList.forEach {id ->
            val document = firestore.collection("users").document(id).get().await()
            val username = document.getString("username") ?: ""
            val avatarUrl = document.getString("avatarUrl") ?: ""
            blockedUserDao.insertBlockedUser(BlockedUser(id, username, avatarUrl))
        }
    }

    fun getBlockDataFromLocal(): Flow<PagingData<BlockedUser>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
        ){
            blockedUserDao.getAllBlockedUsers()
        }.flow
    }
}