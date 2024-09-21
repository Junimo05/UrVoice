package com.example.urvoices.data.service

import android.net.Uri
import android.util.Log
import com.example.urvoices.data.AudioManager
import com.example.urvoices.data.model.Comments
import com.example.urvoices.data.model.Likes
import com.example.urvoices.data.model.Post
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebasePostService @Inject constructor(
    private val audioManager: AudioManager,
    private val storage: StorageReference,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAudioService: FirebaseAudioService
){
    val TAG = "FirebasePostService"
    private var lastVisiblePost: DocumentSnapshot? = null
    suspend fun getNewFeed(limit: Long = 10): List<Post>{
        val posts = mutableListOf<Post>()
        try {
            var query = firebaseFirestore.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)

            if (lastVisiblePost != null) {
                query = query.startAfter(lastVisiblePost!!)
            }

            val querySnapshot = query.get().await()

            for (document in querySnapshot.documents) {
                val post = document.toObject(Post::class.java)
                if (post != null) {
                    posts.add(post)
                }
            }

            lastVisiblePost = querySnapshot.documents.lastOrNull()

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getNewFeed: ${e.message}")
        }
        return posts
    }
    suspend fun getAllPostFromUser(userID: String){
        // get posts
    }


    suspend fun createPost(post: Post, audioUrl: Uri): Boolean{
        // create post
        val postWithoutUserId = post.toMap()
        try {
            firebaseFirestore.collection("posts").document(post.id).set(postWithoutUserId).await()
            return true
        }catch (e: Exception){
            e.printStackTrace()
        }
        return false
    }

    suspend fun updateDescription(postId: String, description: String): Boolean{
        // update post description
        try {
            firebaseFirestore.collection("posts").document(postId).update("description", description).await()
            //update time for updateAt
            firebaseFirestore.collection("posts").document(postId).update("updateAt", System.currentTimeMillis()).await()
            return true
        }catch (e: Exception){
            e.printStackTrace()
        }
        return false
    }

    suspend fun deletePost(postId: String): Boolean{
        // delete post
        try {
            firebaseFirestore.collection("posts").document(postId).update("deleteAt", System.currentTimeMillis()).await()
            firebaseFirestore.collection("")
            return true
        }catch (e: Exception){
            e.printStackTrace()
        }
        return false
    }


    //Comments, Likes Get

    suspend fun getComments_Posts(postId: String): List<Comments>{
        // get comments
        return listOf()
    }

    suspend fun getLikes_Posts(postId: String): List<Likes>{
        // get likes
        return listOf()
    }

    suspend fun getComments_Comments(commentId: String): List<Comments>{
        // get comments
        return listOf()
    }

    suspend fun getLikes_Comments(commentId: String): List<Likes>{
        // get likes
        return listOf()
    }


}