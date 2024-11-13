package com.example.urvoices.data.repository

import com.example.urvoices.data.db.Dao.BlockedUserDao
import com.example.urvoices.data.db.Entity.BlockedUser
import com.example.urvoices.data.service.FirebaseBlockService
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class BlockRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val blockFirebase: FirebaseBlockService,
    private val blockedUserDao: BlockedUserDao
) {

    suspend fun blockUser(targetID: String): String {
        val result = blockFirebase.blockUser(targetID)
        if(result != ""){
            blockedUserDao.insertBlockedUser(BlockedUser(targetID))
        }
        return result
    }

    suspend fun unblockUser(targetID: String): String {
        val result = blockFirebase.unblockUser(targetID)
        if(result != ""){
            blockedUserDao.delete(targetID)
        }
        return result
    }

    suspend fun getBlockStatus(targetID: String): Boolean {
        return blockedUserDao.getAllBlockedUsers().any { it.userID == targetID }
    }

    suspend fun syncBlockUsers(){
        val snapshot = blockFirebase.getAllBlockList()
        val blockedList = snapshot!!.targetID.map { it }
        blockedUserDao.deleteAll()
        blockedList.forEach {
            blockedUserDao.insertBlockedUser(BlockedUser(it))
        }
    }


}