package com.example.urvoices.data.service

import android.util.Log
import com.example.urvoices.data.model.BlockList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseBlockService @Inject constructor(
    private val storage: StorageReference,
    private val firebaseFirestore: FirebaseFirestore,
    private val auth: FirebaseAuth
){
    val TAG = "FirebaseBlockService"

    suspend fun getAllBlockList(): BlockList? {
        val user = auth.currentUser
        if(user != null){
            try {
                val query = firebaseFirestore.collection("rela_blocks")
                    .whereEqualTo("actionID", user.uid)
                    .get().await()
                if (!query.isEmpty) {
                    for (doc in query) {
                        return doc.toObject(BlockList::class.java)
                    }
                }
            } catch (e: Exception){
                e.printStackTrace()
                Log.e(TAG, "getAllBlockList: ${e.message}")
            }
        }
        return null
    }

    suspend fun getBlockStatus(targetID: String): Boolean {
        val user = auth.currentUser
        var isBlocked = false
        if(user != null){
            try {
                val query = firebaseFirestore.collection("rela_blocks")
                    .whereEqualTo("actionID", user.uid)
                    .get().await()
                if (!query.isEmpty) {
                    for (doc in query) {
                        val blockList = doc.toObject(BlockList::class.java)
                        if (blockList.targetID.contains(targetID)) {
                            isBlocked = true
                            break
                        }
                    }
                }
            } catch (e: Exception){
                e.printStackTrace()
                Log.e(TAG, "getBlockStatus: ${e.message}")
            }
        }
        return isBlocked
    }

    suspend fun blockUser(targetID: String): String {
        val user = auth.currentUser
        var resultID = ""
        if(user != null){
            try {
                val documentRef = firebaseFirestore.collection("rela_blocks").document()
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
                            actionID = user.uid,
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
                    .whereEqualTo("actionID", user.uid)
                    .get().await()
                if (!query.isEmpty) {
                    for (doc in query) {
                        val blockList = doc.toObject(BlockList::class.java)
                        val targetIDList = blockList.targetID.toMutableList()
                        if (targetIDList.contains(targetID)) {
                            targetIDList.remove(targetID)
                            firebaseFirestore.collection("rela_blocks").document(doc.id)
                                .update("targetID", targetIDList).await()
                            resultID = user.uid
                            break
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