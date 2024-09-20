package com.example.urvoices.data.service

import com.example.urvoices.data.AudioManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAudioService @Inject constructor(
    private val audioManager: AudioManager,
    private val storage: StorageReference,
    private val firebaseFirestore: FirebaseFirestore
){

    suspend fun getAllUsersInfo(): List<Map<String, Any>> {
        // get all users info from firebase firestore
        val querySnapshot: QuerySnapshot = firebaseFirestore.collection("users").get().await()
        val users = mutableListOf<Map<String, Any>>()
        for (document in querySnapshot.documents) {
            val user = document.data as Map<String, Any>
            users.add(user)
        }
        return users
    }

    suspend fun getAllUrlFromUserID(userId: String): List<String> {
        // get all audio files from firebase firestore
        var urls = mutableListOf<String>()
        try {
            val querySnapshot: QuerySnapshot = firebaseFirestore.collection("rela_posts_users")
                .whereEqualTo("userID", userId)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                val url = document.getString("url")
                if (url != null) {
                    urls.add(url)
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        return urls
    }
    suspend fun getUrlFromPostId(postId: String): String {
        // get audio file from firebase storage
        val docRef = firebaseFirestore.collection("posts").document(postId)
        val url = docRef.get().await().getString("url")
        return url!!
    }
    suspend fun getAllAudioFilesByUserID(userId: String): List<String> {
        // get all audio files from firebase storage
        val dirRef = storage.child("$userId/audio")

        val result = mutableListOf<String>()

        val items = dirRef.listAll().await().items
        for (item in items) {
            val url = item.downloadUrl.await()
            val amplitudes = audioManager.getAmplitudes(url.toString())
            result.add(url.toString())
        }
        return result
    }
}