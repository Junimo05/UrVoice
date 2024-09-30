package com.example.urvoices.data.service

import android.net.Uri
import android.util.Log
import com.example.urvoices.data.AudioManager
import com.example.urvoices.data.model.Comment
import com.example.urvoices.data.model.Post
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Dictionary
import javax.inject.Inject

class FirebasePostService @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val storage: StorageReference,
    private val firebaseAudioService: FirebaseAudioService
){
    val TAG = "FirebasePostService"
    private var lastVisiblePost: DocumentSnapshot? = null
    suspend fun getNewFeed(limit: Long = 10): List<Post>{
        val posts = mutableListOf<Post>()
        try {
            // get posts
            val postQuery = firebaseFirestore.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)

            // If there is a last visible post, start after it
            if (lastVisiblePost != null) {
                postQuery.startAfter(lastVisiblePost)
            }

            val postRefs = postQuery.get().await()
            lastVisiblePost = postRefs.documents.lastOrNull()
            posts.addAll(postRefs.documents.mapNotNull { document ->
                val deleteAt = document.getLong("deletedAt")
                if(deleteAt != null){
                    return@mapNotNull null
                }
                val postID = document.id
                val userID = getUserIDByPostID(postID)
                val audioUrl = document.getString("url") ?: return@mapNotNull null
                val description = document.getString("description") ?: return@mapNotNull null
                val createdAt = document.getLong("createdAt") ?: return@mapNotNull null
                val tag = document.get("tag") as List<*>?
                val updateAt = document.getLong("updatedAt")
                val likes = getLikes_Posts(postID)
                val comments = getComments_Posts(postID).size

                Post(
                    id = postID,
                    userId = userID,
                    url = audioUrl,
                    audioName = audioUrl.split("/").last(),
                    description = description,
                    createdAt = createdAt,
                    updateAt = updateAt,
                    deleteAt = deleteAt,
                    likes = likes,
                    comments = comments,
                    tag = tag?.map { it as String }
                )
            })

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return posts
    }

    suspend fun getUserInfo(userID: String): Map<String, String> {
        var result = mapOf<String, String>()
        try {
            val userRef = firebaseFirestore.collection("users").document(userID).get().await()
            val username = userRef.getString("username") ?: ""
            val avatar = userRef.getString("avatar") ?: ""
            result = mapOf(
                "username" to username,
                "avatar" to avatar
            )
        }catch (e: Exception){
            e.printStackTrace()
        }
        return result
    }

    private suspend fun getUserIDByPostID(postID: String): String{
        var result = ""
        try {
            val postRef = firebaseFirestore.collection("rela_posts_users")
                .whereEqualTo("postID", postID)
                .get()
                .await()
            result = postRef.documents[0].getString("userID") ?: ""
        }catch (e: Exception){
            e.printStackTrace()
        }
        return result
    }

    suspend fun getAllPostFromUser(userID: String): List<Post>{
        val posts = mutableListOf<Post>()
        try {
            // get postIDs from rela_posts_users
            val postIDRefs = firebaseFirestore.collection("rela_posts_users")
                .whereEqualTo("userID", userID)
                .get()
                .await()
            // for each postID, get the post from posts
            postIDRefs.documents.forEach { document ->
                val postID = document.getString("postID")
                if (postID != null) {
                    val postRef = firebaseFirestore.collection("posts").document(postID).get().await()
                    val deleteAt = postRef.getLong("deletedAt")
                    if(deleteAt != null){
                        return@forEach
                    }
                    val audioUrl = postRef.getString("url") ?: return@forEach
                    val description = postRef.getString("description") ?: return@forEach
                    val createdAt = postRef.getLong("createdAt") ?: return@forEach
                    val tag = postRef.get("tag") as List<*>?
                    val updateAt = postRef.getLong("updatedAt")

                    val likes = getLikes_Posts(postID)
                    val comments = getComments_Posts(postID).size

                    val post = Post(
                        id = postID,
                        userId = userID,
                        url = audioUrl,
                        audioName = audioUrl.split("/").last(),
                        description = description,
                        createdAt = createdAt,
                        updateAt = updateAt,
                        deleteAt = deleteAt,
                        likes = likes,
                        comments = comments,
                        tag = tag?.map { it as String }
                    )
                    posts.add(post)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
//            Log.e(TAG, "getAllPostFromUser: ${e.message}")
        }
//        Log.e(TAG, "getAllPostFromUser: $posts")
        return posts
    }


    suspend fun createPost(post: Post, audioUrl: Uri): Boolean {
        // create post
        val postToCreate = post.copy(id = null).toMap()
        val newFileRef = storage.child("audios/${post.userId}/${post.audioName}")
        var downloadUrl = ""
        return try {
            // Upload the file to the new location
            CoroutineScope(Dispatchers.IO).launch {
                val uploadTask = newFileRef.putFile(audioUrl).await()
                downloadUrl = uploadTask.storage.downloadUrl.await().toString()
            }

            // Add the post to Firestore and get the auto-generated ID
            val newPostRef = firebaseFirestore.collection("posts").add(postToCreate).await()
            val newPostId = newPostRef.id

            // Update the 'id' field in the Firestore document
            newPostRef.update("id", newPostId).await()

            // create rela_posts_users
            val relaPostUser = mapOf(
                "ID" to "${post.userId}_$newPostId",
                "postID" to newPostId,
                "userID" to post.userId
            )
            firebaseFirestore.collection("rela_posts_users")
                .document("${post.userId}_$newPostId").set(relaPostUser).await()

            // Update the 'url' field in the Firestore document
            firebaseFirestore.collection("posts").document(newPostId).update("url", downloadUrl).await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "createPost: ${e.message}")
            false
        }
    }
    suspend fun updatePost(post: Post): Boolean {
        // update post
        try {
            post.id?.let {
                firebaseFirestore.collection("posts").document(it).update(post.toMap())
                    .await()
            }
            return true

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    suspend fun deletePost(post: Post): Boolean {
        // delete post
        try {
            post.id?.let {
                firebaseFirestore.collection("posts").document(it).update("deletedAt", System.currentTimeMillis())
                    .await()
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    suspend fun deletePermanentlyPost(post: Post): Boolean {
        // delete post
        try {
            post.id?.let { firebaseFirestore.collection("posts").document(it).delete().await() }
            //delete rela_posts_users
            firebaseFirestore.collection("rela_posts_users").document("${post.userId}_${post.id}").delete().await()
            return true
        } catch (e: Exception) {
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
            return true
        }catch (e: Exception){
            e.printStackTrace()
        }
        return false
    }


    //Comments, Likes Get

    suspend fun getComments_Posts(postId: String): List<Comment>{
        var result: List<Comment> = mutableListOf()
        try {
            // get comments which are belong to post
            val commentRefs = firebaseFirestore.collection("comments_posts_users")
                .whereEqualTo("postID", postId)
                .whereEqualTo("parentCommentID", null)
                .get()
                .await()
            result = commentRefs.documents.mapNotNull { document ->
                // get comment information
                val commentID = document.getString("commentID") ?: return@mapNotNull null
                val userID = document.getString("userID") ?: return@mapNotNull null
                //get comment document
                val commentDoc = firebaseFirestore.collection("comments").document(commentID).get().await()
                val deleteAt = commentDoc.getLong("deleteAt")
                if(deleteAt != null){
                    return@mapNotNull null
                }
                val parentCommentID = document.getString("parentCommentID")
                val content = commentDoc.getString("content")
                val createdAt = commentDoc.getLong("createdAt")
                val updateAt = commentDoc.getLong("updateAt")




                Comment(
                    id = commentID,
                    userId = userID,
                    postId = postId,
                    parentCommentId = parentCommentID,
                    content = content ?: return@mapNotNull null,
                    createdAt = createdAt ?: return@mapNotNull null,
                    updateAt = updateAt,
                    deleteAt = deleteAt
                )
            }
        }catch (
            e: Exception
        ) {
            e.printStackTrace()
//            Log.e(TAG, "getComments_Posts: ${e.message}")
        }

        return result
    }

    suspend fun getLikes_Posts(postId: String): Int{
        var result = 0
        try {
            // get likes
            val likeRefs = firebaseFirestore.collection("likes")
                .whereEqualTo("postID", postId)
                .get()
                .await()
            result = likeRefs.documents.size
        }catch (e: Exception){
            e.printStackTrace()
            Log.e(TAG, "getLikes_Posts: ${e.message}")
        }
        return result
    }

    suspend fun getReplies_Comments(commentId: String): List<Comment>{
        var result = mutableListOf<Comment>()
        try {
            // get comments which are belong to post
            val commentRefs = firebaseFirestore.collection("comments_posts_users")
                .whereEqualTo("parentCommentID", commentId)
                .get()
                .await()

            result = commentRefs.documents.mapNotNull { document ->
                // get comment information
                val replyId = document.getString("commentID") ?: return@mapNotNull null
                val userId = document.getString("userID") ?: return@mapNotNull null
                val postId = document.getString("postID") ?: return@mapNotNull null

                //get comment document
                val commentDoc =
                    firebaseFirestore.collection("comments").document(replyId).get().await()

                Comment(
                    id = replyId,
                    userId = userId,
                    postId = postId,
                    parentCommentId = commentId,
                    content = commentDoc.getString("content") ?: return@mapNotNull null,
                    createdAt = commentDoc.getLong("createdAt") ?: return@mapNotNull null,
                    updateAt = commentDoc.getLong("updateAt"),
                    deleteAt = commentDoc.getLong("deleteAt")
                )
            }.toMutableList()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getReplies_Comments: ${e.message}")
        }
        return result
    }

    suspend fun getLikes_Comments(commentId: String): Int{
        var result = 0
        try {
            // get likes
            val likeRefs = firebaseFirestore.collection("likes")
                .whereEqualTo("commentID", commentId)
                .get()
                .await()
            result = likeRefs.documents.size
        }catch (e: Exception){
            e.printStackTrace()
            Log.e(TAG, "getLikes_Comments: ${e.message}")
        }
        return result
    }


}