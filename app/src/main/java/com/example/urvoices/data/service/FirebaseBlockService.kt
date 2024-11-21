package com.example.urvoices.data.service

import android.util.Log
import com.example.urvoices.data.model.BlockList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseBlockService @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val auth: FirebaseAuth
){
    val TAG = "FirebaseBlockService"

    suspend fun getAllBlockList(): BlockList? {
        val user = auth.currentUser
        if(user != null){
            try {
                val query = firebaseFirestore.collection("rela_blocks")
                    .document(user.uid)
                    .get().await()
                if(query.exists()){
                    return query.toObject(BlockList::class.java)
                }
            } catch (e: Exception){
                e.printStackTrace()
                Log.e(TAG, "getAllBlockList: ${e.message}")
            }
        }
        return BlockList()
    }

    object BlockInfo{
        const val BLOCK = "BLOCK"
        const val BLOCKED = "BLOCKED"
        const val NO_BLOCK = "NO_BLOCK"
    }

    suspend fun getBlockStatus(targetID: String): String {
        val user = auth.currentUser
        var blockInfo = BlockInfo.NO_BLOCK
        if (user != null) {
            try {
                // Check if the current user has blocked the target user
                val currentUserQuery = firebaseFirestore.collection("rela_blocks")
                    .whereEqualTo("id", user.uid)
                    .get().await()
                if (!currentUserQuery.isEmpty) {
                    for (doc in currentUserQuery) {
                        val blockList = doc.toObject(BlockList::class.java)
                        if (blockList.targetID.contains(targetID)) {
                            blockInfo = BlockInfo.BLOCK
                            break
                        }
                    }
                }

                // Check if the target user has blocked the current user
                if (blockInfo == BlockInfo.NO_BLOCK) {
                    val targetUserQuery = firebaseFirestore.collection("rela_blocks")
                        .whereEqualTo("id", targetID)
                        .get().await()
                    if (!targetUserQuery.isEmpty) {
                        for (doc in targetUserQuery) {
                            val blockList = doc.toObject(BlockList::class.java)
                            if (blockList.targetID.contains(user.uid)) {
                                blockInfo = BlockInfo.BLOCKED
                                break
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "getBlockStatus: ${e.message}")
            }
        }
        return blockInfo
    }

    suspend fun blockUser(targetID: String): String {
        val user = auth.currentUser
        var resultID = ""
        if(user != null){
            try {
                val documentRef = firebaseFirestore.collection("rela_blocks").document(user.uid)
                firebaseFirestore.runTransaction{transaction ->
                    val snapshot = transaction.get(documentRef)
                    if(snapshot.exists()){
                        val targetIDList = snapshot.get("targetID") as? MutableList<String> ?: mutableListOf()
                        if(!targetIDList.contains(targetID)){
                            targetIDList.add(targetID)
                            transaction.update(documentRef, "targetID", targetIDList)
                        } else {
                            // Already blocked
                        }
                    } else {
                        val blockList = BlockList(
                            id = user.uid,
                            targetID = listOf(targetID)
                        )
                        transaction.set(documentRef, blockList)
                    }
                }.await()
                resultID = user.uid
            } catch (e: Exception){
                e.printStackTrace()
                Log.e(TAG, "blockUser: ${e.message}")
            }
        }
        return resultID
    }

    suspend fun unblockUser(targetID: String): String {
        val user = auth.currentUser
        var resultID = ""
        if(user != null){
            try {
                val query = firebaseFirestore.collection("rela_blocks")
                    .document(user.uid)
                    .get().await()
                if(query.exists()){
                    val blockList = query.toObject(BlockList::class.java)
                    if(blockList != null){
                        val targetIDList = blockList.targetID.toMutableList()
                        if(targetIDList.contains(targetID)){
                            targetIDList.remove(targetID)
                            firebaseFirestore.collection("rela_blocks")
                                .document(user.uid)
                                .update("targetID", targetIDList)
                                .await()
                            resultID = user.uid
                        }
                    }
                }
            } catch (e: Exception){
                e.printStackTrace()
                Log.e(TAG, "unblockUser: ${e.message}")
            }
        }
        return resultID
    }
}