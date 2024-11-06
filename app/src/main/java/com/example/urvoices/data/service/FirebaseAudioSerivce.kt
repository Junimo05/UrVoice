package com.example.urvoices.data.service

import android.media.MediaMetadataRetriever
import com.example.urvoices.data.AudioManager
import com.example.urvoices.data.model.Audio
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAudioService @Inject constructor(
    private val audioManager: AudioManager,
    private val storage: StorageReference,
    private val firebaseFirestore: FirebaseFirestore,
){
//    suspend fun getAllUrlFromUserID(userId: String): List<String> {
//        // get all audio files from firebase firestore
//        var urls = mutableListOf<String>()
//        try {
//            val querySnapshot: QuerySnapshot = firebaseFirestore.collection("rela_posts_users")
//                .whereEqualTo("userID", userId)
//                .get()
//                .await()
//
//            for (document in querySnapshot.documents) {
//                val url = document.getString("url")
//                if (url != null) {
//                    urls.add(url)
//                }
//            }
//        }catch (e: Exception){
//            e.printStackTrace()
//        }
//        return urls
//    }
//    suspend fun getUrlFromPostId(postId: String): String {
//        // get audio file from firebase storage
//        val docRef = firebaseFirestore.collection("posts").document(postId)
//        val url = docRef.get().await().getString("url")
//        return url!!
//    }

//    suspend fun getFileFromUrl(url: String): Audio {
//        // get audio file from firebase storage
//        val fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(url)
//        val retriever = MediaMetadataRetriever()
//        retriever.setDataSource(url, HashMap())
//        val metadata = fileRef.metadata.await()
//
//        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
//        val durationMillis = duration?.toLong() ?: 0
//
//        retriever.release()
//
//        val amplitudes = audioManager.getAmplitudes(url)
//        return Audio(
//            id = metadata.path,
//            url = url,
//            displayName = metadata.name ?: "",
//            type = metadata.contentType ?: "",
//            duration = durationMillis,
//            audioCreated = metadata.creationTimeMillis,
//            audioSize = metadata.sizeBytes,
//            amplitudes = amplitudes
//        )
//    }

//    suspend fun getAllAudioFilesByUserID(userId: String): List<String> {
//        // get all audio files from firebase storage
//        val dirRef = storage.child("audios/$userId")
//
//        val result = mutableListOf<String>()
//
//        val items = dirRef.listAll().await().items
//        for (item in items) {
//            val url = item.downloadUrl.await()
//            val amplitudes = audioManager.getAmplitudes(url.toString())
//            result.add(url.toString())
//        }
//        return result
//    }
}