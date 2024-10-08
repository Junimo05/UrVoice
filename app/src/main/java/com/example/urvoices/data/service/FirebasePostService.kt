package com.example.urvoices.data.service

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import com.example.urvoices.data.AudioManager
import com.example.urvoices.data.model.Comment
import com.example.urvoices.data.model.Like
import com.example.urvoices.data.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebasePostService @Inject constructor(
    private val audioManager: AudioManager,
    private val firebaseFirestore: FirebaseFirestore,
    private val storage: StorageReference
){
    val TAG = "FirebasePostService"


    suspend fun getNewFeed(page: Int, lastVisiblePost: MutableState<String>, lastvisiblePage: MutableState<Int>): List<Post>{
        val posts = mutableListOf<Post>()
        val limit = 3L
        try {
            // get posts
            var postQuery = firebaseFirestore.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
//            Log.e(TAG, "getNewFeed: $lastVisiblePost")
//            Log.e(TAG, "getNewFeed: $lastvisiblePage")

            if (lastVisiblePost.value != "" && page > lastvisiblePage.value) {
                val lastVisiblePostRef = firebaseFirestore.collection("posts").document(lastVisiblePost.value).get().await()
                postQuery = postQuery.startAfter(lastVisiblePostRef)
                lastvisiblePage.value = page
            }

            val postRefs = postQuery.get().await()
            val lastVisible = postRefs.documents.lastOrNull()

            if (lastVisible != null) {
                lastVisiblePost.value = lastVisible.id
            }else {
                lastVisiblePost.value = ""
            }


            posts.addAll(postRefs.documents.mapNotNull { document ->
                val deleteAt = document.getLong("deletedAt")
                if(deleteAt != null){
                    return@mapNotNull null
                }
                val postID = document.id
                val likesDeferred = CoroutineScope(Dispatchers.IO).async { getCountLikesPosts(postID) }
                val commentsDeferred = CoroutineScope(Dispatchers.IO).async { getCountCommentsPosts(postID) }


                val userID = getUserIDByPostID(postID)

                val audioUrl = document.getString("url") ?: return@mapNotNull null
                val amplitudesDeferred = CoroutineScope(Dispatchers.IO).async {
                    audioManager.getAmplitudes(audioUrl)
                }

                val audioName = document.getString("audioName")
                val description = document.getString("description") ?: return@mapNotNull null
                val createdAt = document.getLong("createdAt") ?: return@mapNotNull null
                val tag = document.get("tag") as List<*>?
                val updateAt = document.getLong("updatedAt")

                val likes = likesDeferred.await()
                val comments = commentsDeferred.await()
                val amplitudes = amplitudesDeferred.await()

                Post(
                    id = postID,
                    userId = userID,
                    url = audioUrl,
                    amplitudes = amplitudes,
                    audioName = audioName ?: "No Name",
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

    suspend fun getUserInfoDisplayForPost(userID: String): Map<String, String> {
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

    suspend fun getPostDetailByPostID(postID: String): Post?{
        var post: Post? = null
        try {
            val postRef = firebaseFirestore.collection("posts").document(postID).get().await()
            val deleteAt = postRef.getLong("deletedAt")
            if(deleteAt != null){
                return null
            }

            val likesDeferred = CoroutineScope(Dispatchers.IO).async { getCountLikesPosts(postID) }
            val commentsDeferred = CoroutineScope(Dispatchers.IO).async { getCountCommentsPosts(postID) }
            val audioUrl = postRef.getString("url") ?: return null
            val amplitudesDeferred = CoroutineScope(Dispatchers.IO).async {
                audioManager.getAmplitudes(audioUrl)
            }

            val audioName = postRef.getString("audioName")
            val description = postRef.getString("description") ?: return null
            val createdAt = postRef.getLong("createdAt") ?: return null
            val tag = postRef.get("tag") as List<*>?
            val updateAt = postRef.getLong("updatedAt")

            val likes = likesDeferred.await()
            val comments = commentsDeferred.await()
            val amplitudes = amplitudesDeferred.await()

            post = Post(
                id = postID,
                userId = getUserIDByPostID(postID),
                url = audioUrl,
                audioName = audioName ?: "No Name",
                description = description,
                createdAt = createdAt,
                updateAt = updateAt,
                deleteAt = deleteAt,
                likes = likes,
                comments = comments,
                tag = tag?.map { it as String },
                amplitudes = amplitudes
            )
        }catch (e: Exception){
            e.printStackTrace()
        }
        return post
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

    suspend fun getAllPostFromUser(page: Int, userID: String, lastVisiblePost: MutableState<String>, lastvisiblePage: MutableState<Int>): List<Post>{
        val limit = 3L
        val posts = mutableListOf<Post>()
        try {
            // get postIDs from rela_posts_users
            var postIDQuery = firebaseFirestore.collection("rela_posts_users")
                .whereEqualTo("userID", userID)
                .limit(limit)

            if (lastVisiblePost.value != "" && page > lastvisiblePage.value) {
                val lastVisiblePostRef = firebaseFirestore.collection("rela_posts_users").document(lastVisiblePost.value).get().await()
                postIDQuery = postIDQuery.startAfter(lastVisiblePostRef)
                lastvisiblePage.value = page
            }

            val postIDRefs = postIDQuery.get().await()
            Log.e(TAG, "getAllPostFromUser: ${postIDRefs.documents}")
            val lastVisible = postIDRefs.documents.lastOrNull()
            if (lastVisible != null) {
                lastVisiblePost.value = lastVisible.id
            }else { lastVisiblePost.value = "" }


            // for each postID, get the post from posts
            postIDRefs.documents.forEach { document ->
                val postID = document.getString("postID")
                if (postID != null) {
                    val postRef = firebaseFirestore.collection("posts").document(postID).get().await()
                    val deleteAt = postRef.getLong("deletedAt")
                    if(deleteAt != null){
                        return@forEach
                    }
                    val likesDeferred = CoroutineScope(Dispatchers.IO).async { getCountLikesPosts(postID) }
                    val commentsDeferred = CoroutineScope(Dispatchers.IO).async { getCountCommentsPosts(postID) }
                    val audioUrl = postRef.getString("url") ?: return@forEach
                    val amplitudesDeferred = CoroutineScope(Dispatchers.IO).async {
                        audioManager.getAmplitudes(audioUrl)
                    }
                    val audioName = postRef.getString("audioName")
                    val description = postRef.getString("description") ?: return@forEach
                    val createdAt = postRef.getLong("createdAt") ?: return@forEach
                    val tag = postRef.get("tag") as List<*>?
                    val updateAt = postRef.getLong("updatedAt")

                    val likes = likesDeferred.await()
                    val comments = commentsDeferred.await()
                    val amplitudes = amplitudesDeferred.await()

                    val post = Post(
                        id = postID,
                        userId = userID,
                        url = audioUrl,
                        audioName = audioName,
                        description = description,
                        createdAt = createdAt,
                        updateAt = updateAt,
                        deleteAt = deleteAt,
                        likes = likes,
                        comments = comments,
                        tag = tag?.map { it as String },
                        amplitudes = amplitudes
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
        return try {
            val postToCreate = post.copy(id = null).toMap()
            val newFileRef = storage.child("audios/${post.userId}/${post.audioName}")

            // Upload the file to the new location
            val uploadTask = newFileRef.putFile(audioUrl).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

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

    /*
     *   Post Interactions
    */

    private suspend fun getCountCommentsPosts(postId: String): Int{
        var result = 0
        try {
            // get comments
            val commentRefs = firebaseFirestore.collection("comments_posts_users")
                .whereEqualTo("postID", postId)
                .whereEqualTo("parentCommentID", null)
                .get()
                .await()
            result = commentRefs.documents.size
        }catch (e: Exception){
            e.printStackTrace()
            Log.e(TAG, "getCountCommentsPosts: ${e.message}")
        }
        return result
    }


    //Post Interaction Events
    suspend fun likePost(postId: String, userID: String): String{
        // like post
        var relaID = ""
        try {
            val like = Like(
                id = null,
                commentID = null,
                postID = postId,
                userID = userID,
                createdAt = System.currentTimeMillis().toString()
            )
            firebaseFirestore.collection("likes").add(like.toMap()).addOnCompleteListener {
                firebaseFirestore.collection("likes").document(it.result?.id!!).update("ID", it.result?.id!!)
                relaID = it.result?.id!!
            }.await()

            return relaID
        }catch (e: Exception){
            e.printStackTrace()
//            Log.e(TAG, "likePost: ${e.message}")
        }
        return relaID
    }

    suspend fun likeComment(commentId: String, postID: String, userID: String): String{
        // like comment
        var relaID = ""
        try {
            val like = Like(
                id = null,
                commentID = commentId,
                postID = postID,
                userID = userID,
                createdAt = System.currentTimeMillis().toString()
            )
            firebaseFirestore.collection("likes").add(like.toMap()).addOnCompleteListener {
                firebaseFirestore.collection("likes").document(it.result?.id!!).update("ID", it.result?.id!!)
                relaID = it.result?.id!!
            }.await()
            return relaID
        }catch (e: Exception){
            e.printStackTrace()
//            Log.e(TAG, "likeComment: ${e.message}")
        }
        return relaID
    }

    suspend fun commentPost(actionUserID: String, postID: String, content: String): String {
        // comment post
        var relaID = ""
        try {
            val comment = Comment(
                id = null,
                userId = actionUserID,
                postId = postID,
                parentCommentId = null,
                content = content,
                createdAt = System.currentTimeMillis(),
                updatedAt = null,
                deletedAt = null
            )
            firebaseFirestore.collection("comments").add(comment.toCommentMap()).addOnCompleteListener {
                //update ID for comment
                cmt ->
                firebaseFirestore.collection("comments").document(cmt.result?.id!!)
                    .update("ID", cmt.result?.id!!)
                //update Relation
                firebaseFirestore.collection("comments_posts_users").add(comment.toRelationMap()).addOnCompleteListener {
                    rela ->
                    firebaseFirestore.collection("comments_posts_users").document(rela.result?.id!!)
                        .update("ID", rela.result?.id!!)
                    relaID = rela.result?.id!!
                }
            }.await()
            return relaID
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return relaID
    }

    suspend fun replyComment(actionUserID: String, commentID: String, postID: String, content: String): String {
        // reply comment
        var relaID = ""
        try {
            val comment = Comment(
                id = null,
                userId = actionUserID,
                postId = postID,
                parentCommentId = commentID,
                content = content,
                createdAt = System.currentTimeMillis(),
                updatedAt = null,
                deletedAt = null
            )
            firebaseFirestore.collection("comments").add(comment.toCommentMap()).addOnCompleteListener {
                //update ID for comment
                cmt ->
                firebaseFirestore.collection("comments").document(cmt.result?.id!!)
                    .update("ID", cmt.result?.id!!)
                //update Relation
                firebaseFirestore.collection("comments_posts_users").add(comment.toRelationMap()).addOnCompleteListener {
                    rela ->
                    firebaseFirestore.collection("comments_posts_users").document(rela.result?.id!!)
                        .update("ID", rela.result?.id!!)
                    relaID = rela.result?.id!!
                }
            }.await()
            return relaID
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return relaID
    }

    suspend fun getCommentsPosts(page: Int ,postId: String): List<Comment>{
        var result: List<Comment> = mutableListOf()
        try {
            // get comments which are belong to post
            val commentRefs = firebaseFirestore.collection("comments_posts_users")
                .whereEqualTo("postID", postId)
                .whereEqualTo("parentCommentID", null)
                .limit(10)
                .get()
                .await()
            result = commentRefs.documents.mapNotNull { document ->
                // get comment information
                val commentID = document.getString("commentID") ?: return@mapNotNull null
                val userID = document.getString("userID") ?: return@mapNotNull null
                //get comment document
                val commentDoc = firebaseFirestore.collection("comments").document(commentID).get().await()
                val deletedAt = commentDoc.getLong("deleteAt")
                if(deletedAt != null){
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
                    updatedAt = updateAt,
                    deletedAt = deletedAt
                )
            }
        }catch (
            e: Exception
        ) {
            e.printStackTrace()
//            Log.e(TAG, "getCommentsPosts: ${e.message}")
        }

        return result
    }

    private suspend fun getCountLikesPosts(postId: String): Int{
        return try {
            // get likes
            val likeRefs = firebaseFirestore.collection("likes")
                .whereEqualTo("postID", postId)
                .get()
                .await()
            likeRefs.documents.size
        } catch (e: FirebaseFirestoreException) {
            e.printStackTrace()
            Log.e(TAG, "getCountLikesPosts: ${e.message}")
            0
        }
    }

    suspend fun getCountLikesComments(commentId: String): Int{
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
            Log.e(TAG, "getCountLikesComments: ${e.message}")
        }
        return result
    }

    suspend fun getCountReplyComments(commentId: String): Int{
        var result = 0
        try {
            // get comments which are belong to post
            val commentRefs = firebaseFirestore.collection("comments_posts_users")
                .whereEqualTo("parentCommentID", commentId)
                .get()
                .await()
            result = commentRefs.documents.size
        }catch (e: Exception){
            e.printStackTrace()
            Log.e(TAG, "getCountReplyComments: ${e.message}")
        }
        return result
    }

    suspend fun getRepliesComments(commentId: String): List<Comment>{
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
                    updatedAt = commentDoc.getLong("updatedAt"),
                    deletedAt = commentDoc.getLong("deletedAt")
                )
            }.toMutableList()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getRepliesComments: ${e.message}")
        }
        return result
    }




}