package com.example.urvoices.data.service

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import com.example.urvoices.data.AudioManager
import com.example.urvoices.data.model.Comment
import com.example.urvoices.data.model.CommentData
import com.example.urvoices.data.model.Like
import com.example.urvoices.data.model.Post
import com.example.urvoices.utils.getDurationFromUrl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebasePostService @Inject constructor(
    private val audioManager: AudioManager,
    private val auth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore,
    private val storage: StorageReference
){
    val TAG = "FirebasePostService"
    val scope = CoroutineScope(Dispatchers.IO)
    suspend fun getNewFeed(page: Int, lastVisiblePost: MutableState<String>, lastvisiblePage: MutableState<Int>): List<Post>{
        val posts = mutableListOf<Post>()
        val limit = 4L
        try {
            // get posts
            var postQuery = firebaseFirestore.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)

            if (lastVisiblePost.value != "" && page > lastvisiblePage.value) {
                val lastVisiblePostRef = firebaseFirestore.collection("posts").document(lastVisiblePost.value).get().await()
                postQuery = postQuery.startAfter(lastVisiblePostRef)
                lastvisiblePage.value = page
            }

            val postRefs = postQuery.get().await()
            val lastVisible = postRefs.documents.lastOrNull()
            if(lastVisible != null){
                lastVisiblePost.value = lastVisible.id
            }else{
                lastVisiblePost.value = ""
            }

            posts.addAll(postRefs.documents.mapNotNull { document ->
                val deleteAt = document.getLong("deletedAt")
                if (deleteAt != null) {
                    return@mapNotNull null
                }
                val post = document.toObject<Post>()
                if (post != null) {
                    val likesDeferred = CoroutineScope(Dispatchers.IO).async { getCountLikesPosts(post.ID!!) }
                    val commentsDeferred = CoroutineScope(Dispatchers.IO).async { getCountCommentsPosts(post.ID!!) }
                    val likes = likesDeferred.await()
                    val comments = commentsDeferred.await()

                    post.likes = likes
                    post.comments = comments

                    post
                } else {
                    null
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getNewFeedError: ${e.message}")
        }
//        Log.e(TAG, "getNewFeed: $posts")
        return posts
    }

    suspend fun getUserInfoDisplayForPost(userID: String): Map<String, String> {
        var result = mapOf<String, String>()
        try {
            val userRef = firebaseFirestore.collection("users").document(userID).get().await()
            val username = userRef.getString("username") ?: ""
            val avatar = userRef.getString("avatarUrl") ?: ""
            result = mapOf(
                "username" to username,
                "avatarUrl" to avatar
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
            if(postRef.exists()){
                post = postRef.toObject<Post>()
                val likesDeferred = CoroutineScope(Dispatchers.IO).async { getCountLikesPosts(postID) }
                val commentsDeferred = CoroutineScope(Dispatchers.IO).async { getCountCommentsPosts(postID) }
                post!!.likes = likesDeferred.await()
                post.comments = commentsDeferred.await()
            } else {
                // Error
                Log.e(TAG, "getPostDetailByPostID: Post not found")
            }
        }catch (e: Exception){
            e.printStackTrace()
            Log.e(TAG, "getPostDetailByPostID: ${e.message}")
        }
        return post
    }

    suspend fun getAllPostFromUser(
        page: Int,
        userID: String,
        lastVisiblePost: MutableState<String>,
        lastvisiblePage: MutableState<Int>
    ): List<Post> {
        val limit = 3L
        val posts = mutableListOf<Post>()
        try {
            // get posts from posts collection
            var postQuery = firebaseFirestore.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .whereEqualTo("userId", userID)
                .limit(limit)

            if (lastVisiblePost.value != "" && page > lastvisiblePage.value) {
                val lastVisiblePostRef = firebaseFirestore.collection("posts").document(lastVisiblePost.value).get().await()
                postQuery = postQuery.startAfter(lastVisiblePostRef)
                lastvisiblePage.value = page
            }

            val postRefs = postQuery.get().await()
            val lastVisible = postRefs.documents.lastOrNull()
            if (lastVisible != null) {
                lastVisiblePost.value = lastVisible.id
            } else {
                lastVisiblePost.value = ""
            }

            // for each post, get the details
            postRefs.documents.forEach { document ->
                val deleteAt = document.getLong("deletedAt")
                if (deleteAt != null) {
                    return@forEach
                }
                val post = document.toObject(Post::class.java)
                if (post != null) {
                    val processedPost = withContext(Dispatchers.IO) {
                        val likesDeferred = async { getCountLikesPosts(post.ID!!) }
                        val commentsDeferred = async { getCountCommentsPosts(post.ID!!) }
                        // Đợi tất cả các deferred hoàn thành
                        post.apply {
                            likes = likesDeferred.await()
                            comments = commentsDeferred.await()
                        }
                    }
                    posts.add(processedPost)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getAllPostFromUser: ${e.message}")
        }
        return posts
    }

    /*
        Save Posts Feature
    */
    suspend fun savePosts(postID: String): Boolean? {
        val currentUser = auth.currentUser ?: return null
        try {
            //find if the post is saved
            val relaSavePosts = firebaseFirestore.collection("rela_savePosts").document(currentUser.uid)
                .get().await()
                //check array
            val savedPosts = relaSavePosts.get("savedPosts") as List<*>?
            if (savedPosts != null) {
                if(savedPosts.contains(postID)){ //remove Saved Post
                    val updatedSavedPosts = savedPosts.toMutableList().apply {
                        remove(postID)
                    }
                    firebaseFirestore.collection("rela_savePosts").document(currentUser.uid)
                        .update("savedPosts", updatedSavedPosts)
                        .await()
                    //Removed
                    return false
                } else { //add Saved Post
                    val updatedSavedPosts = savedPosts.toMutableList().apply {
                        add(postID)
                    }
                    firebaseFirestore.collection("rela_savePosts").document(currentUser.uid)
                        .update("savedPosts", updatedSavedPosts)
                        .await()
                    //Saved
                    return true
                }
            } else {
                //create new savedPosts for first time
                val newSavedPosts = listOf(postID)
                val rela = mapOf(
                    "ID" to currentUser.uid,
                    "savedPosts" to newSavedPosts
                )
                firebaseFirestore.collection("rela_savePosts")
                    .document(currentUser.uid)
                    .set(rela)
                //Saved
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "savePosts: ${e.message}")
        }
        return null
    }

    suspend fun getSaveStatus(postID :String): Boolean{
        val currentUser = auth.currentUser ?: return false
        try {
            val relaSavePosts = firebaseFirestore.collection("rela_savePosts").document(currentUser.uid)
                .get().await()
            val savedPosts = relaSavePosts.get("savedPosts") as List<*>?
            if (savedPosts != null) {
                return savedPosts.contains(postID)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getSaveStatus: ${e.message}")
        }
        return false
    }

    suspend fun getAllSavedPostFromUser(
        page: Int,
        userID: String,
        lastVisiblePost: MutableState<String>,
        lastvisiblePage: MutableState<Int>
    ): List<Post> {
        val limit = 5L
        val posts = mutableListOf<Post>()
        //get limit 5 posts from savedPosts Array
        try {
            val refSavedPosts = firebaseFirestore.collection("rela_savePosts").document(userID).get().await()
            val savedPosts = refSavedPosts.get("savedPosts") as List<*>?
//            Log.e(TAG, "getAllSavedPostFromUser Data: ${savedPosts!!.size}")
            if (savedPosts != null) {
                // get posts from posts collection
                var postQuery = firebaseFirestore.collection("posts")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .whereIn("ID", savedPosts)
                    .limit(limit)

                if (lastVisiblePost.value != "" && page > lastvisiblePage.value) {
                    val lastVisiblePostRef = firebaseFirestore.collection("posts").document(lastVisiblePost.value).get().await()
                    postQuery = postQuery.startAfter(lastVisiblePostRef)
                    lastvisiblePage.value = page
                }

                val postRefs = postQuery.get().await()
                val lastVisible = postRefs.documents.lastOrNull()
                if (lastVisible != null) {
                    lastVisiblePost.value = lastVisible.id
                } else {
                    lastVisiblePost.value = ""
                }

                // for each post, get the details
                postRefs.documents.forEach { document ->
                    val deleteAt = document.getLong("deletedAt")
                    if (deleteAt != null) {
                        return@forEach
                    }
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        posts.add(post)
                    } else {
                        // Error
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getAllSavedPostFromUser: ${e.message}")
        }
        return posts
    }

    suspend fun getAllSavedPost(): List<String>{
        val currentUser = auth.currentUser
        if(currentUser != null){
            try {
                val refSavedPosts = firebaseFirestore.collection("rela_savePosts").document(currentUser.uid).get().await()
                val savedPosts = refSavedPosts.get("savedPosts") as List<*>?
                if (savedPosts != null) {
                    Log.e(TAG, "getAllSavedPost: ${savedPosts.size}")
                    return savedPosts.mapNotNull { it as String }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "getAllSavedPost: ${e.message}")
            }
        }
        return emptyList()
    }

    suspend fun createPost(post: Post, audioUrl: Uri, imgUri: Uri): Boolean {
        return try {
            val amplitudes = audioManager.getAmplitudes(audioUrl)
            post.amplitudes = amplitudes
            val duration = getDurationFromUrl(audioUrl.toString())
            post.duration = duration
            val postToCreate = post.copy(ID = null).toMap()

            // Add the post to Firestore and get the auto-generated ID
            val newPostRef = firebaseFirestore.collection("posts").add(postToCreate).await()
            val newPostId = newPostRef.id

            // Update the 'id' field in the Firestore document
            newPostRef.update("ID", newPostId).await()

            //Prepare the storage reference
            val audioRef = storage.child("audios/${post.userId}/${newPostId}")
            val imgRef = storage.child("imgs/${post.userId}/posts/${newPostId}")

            // Upload the audio to the new location
            val uploadTask = audioRef.putFile(audioUrl).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

            // Upload the image to the new location
            val uploadImgTask = imgRef.putFile(imgUri).await()
            val downloadImgUrl = uploadImgTask.storage.downloadUrl.await().toString()

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
            // Update the 'imgUrl' field in the Firestore document
            firebaseFirestore.collection("posts").document(newPostId).update("imgUrl", downloadImgUrl).await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "createPost: ${e.message}")
            false
        }
    }

    suspend fun getAllDeletePost() : List<Post>{
        val posts = mutableListOf<Post>()
        val user = auth.currentUser
        try {
        	if(user != null){
                val deletePostRef = firebaseFirestore.collection("posts")
                    .whereEqualTo("userId", user.uid)
                    .whereNotEqualTo("deletedAt", null)
                    .get()
                    .await()
                if (!deletePostRef.isEmpty) {
                    deletePostRef.mapNotNull {
                        val post = it.toObject<Post>()
                        posts.add(post)
                    }
                }
            }
            return posts
        } catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG, "getAllDeletePost: ${e.message}")
        }
        return posts
    }

    suspend fun deletePost(postID: String): Boolean {
        // delete post
        try {
            firebaseFirestore.collection("posts").document(postID).update("deletedAt", System.currentTimeMillis())
                .await()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "deletePost: ${e.message}")
        }
        return false
    }

    suspend fun deletePermanentlyPost(postID: String): Boolean {
        return try {
            coroutineScope {
                val deletePost = async {
                    firebaseFirestore.collection("posts").document(postID).delete().await()
                }
                val deleteRelaPostsUsers = async {
                    firebaseFirestore.collection("rela_posts_users").document(postID).delete().await()
                }
                val deleteComments = async {
                    val comments = firebaseFirestore.collection("rela_comments_users_posts")
                        .whereEqualTo("postID", postID)
                        .get()
                        .await()
                    comments.documents.map { document ->
                        async {
                            firebaseFirestore.collection("rela_comments_users_posts").document(document.id).delete().await()
                        }
                    }.awaitAll()
                }
                awaitAll(deletePost, deleteRelaPostsUsers, deleteComments)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updatePost(newData: Map<String, Any?>, oldData: Post): Boolean {
        return try {
            val updatedMapData = newData.toMutableMap()
            val newImgUri = updatedMapData["imgUrl"] as? Uri

            if (newImgUri != null && newImgUri != Uri.EMPTY) {
                Log.d(TAG, "Updating post image: $newImgUri")

                // Create a reference to the storage location
                val storageRef = storage.child("imgs/${oldData.userId}/posts/${oldData.ID}")

                // Remove the existing file first
                storageRef.delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "Old image deleted successfully")
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, "Failed to delete old image: ${exception.message}")
                    }

                // Upload the new image
                val uploadTask = storageRef.putFile(newImgUri).await()
                val imgUrl = uploadTask.storage.downloadUrl.await().toString()

                updatedMapData["imgUrl"] = imgUrl
            }

            updatedMapData["updatedAt"] = System.currentTimeMillis()

            // Update Firestore document
            firebaseFirestore.collection("posts")
                .document(updatedMapData["ID"] as String)
                .set(updatedMapData, SetOptions.merge())
                .await()

            true
        } catch (e: Exception) {
            Log.e(TAG, "updatePost error: ${e.message}", e)
            false
        }
    }

    /*
     *   Post Interactions
     */

    private suspend fun getCountCommentsPosts(postId: String): Int {
        return try {
            // get the documents which belong to the post
            val querySnapshot = firebaseFirestore.collection("rela_comments_users_posts")
                .whereEqualTo("postID", postId)
                .get()
                .await()

            // count the documents where deletedAt is null or does not exist
            querySnapshot.documents.count {
                !it.contains("deletedAt") || it.get("deletedAt") == null
            }
        } catch (e: FirebaseFirestoreException) {
            e.printStackTrace()
            Log.e(TAG, "getCountCommentsPosts: ${e.message}")
            0
        }
    }


    //Post Interaction Events
    suspend fun likePost(postId: String, userID: String): String {
        var relaID = ""
        try {
            val like = Like(
                id = null,
                commentID = null,
                postID = postId,
                userID = userID,
                createdAt = System.currentTimeMillis().toString()
            )
            val likeRef = firebaseFirestore.collection("likes").add(like.toMap()).await()
            relaID = likeRef.id
            firebaseFirestore.collection("likes").document(relaID).update("ID", relaID).await()
//            Log.e(TAG, "likePost Done: $relaID")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "likePost: ${e.message}")
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
            val likeRef = firebaseFirestore.collection("likes").add(like.toMap()).await()
            relaID = likeRef.id
            firebaseFirestore.collection("likes").document(relaID).update("ID", relaID).await()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "likeComment: ${e.message}")
        }
        return relaID
    }

    suspend fun commentPost(actionUserID: String, postID: String, content: String): CommentData {
        var commentResultID = ""
        var relaCommentID = ""
        val comment = Comment(
            id = null,
            userId = actionUserID,
            parentId = null,
            postId = postID,
            content = content,
            createdAt = System.currentTimeMillis(),
            updatedAt = null,
            deletedAt = null
        )
        try {
            // Add comment and get the reference
            val commentRef = firebaseFirestore.collection("comments").add(comment.toCommentMap()).await()
            commentResultID = commentRef.id
            comment.id = commentRef.id

            // Update ID for comment
            firebaseFirestore.collection("comments").document(commentResultID)
                .update("ID", commentResultID).await()

            // Update ID for relation
            val relaRef = firebaseFirestore.collection("rela_comments_users_posts").add(comment.toRelaMap()).await()
            relaCommentID = relaRef.id
            firebaseFirestore.collection("rela_comments_users_posts").document(relaCommentID)
                .update("ID", relaCommentID, "commentID", commentResultID).await()

            return CommentData(
                comment = comment,
                relaCommentID = commentResultID
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "commentPost: ${e.message}")
        }
        return CommentData()
    }

    suspend fun replyComment(actionUserID: String, parentID: String, postID: String, content: String): CommentData {
        // reply comment
        var commentResultID = ""
        var relaCommentID = ""
        val comment = Comment(
            id = null,
            userId = actionUserID,
            parentId = parentID,
            postId = postID,
            content = content,
            createdAt = System.currentTimeMillis(),
            updatedAt = null,
            deletedAt = null
        )
        try {
            // Add comment and get the reference
            val commentRef = firebaseFirestore.collection("comments").add(comment.toCommentMap()).await()
            commentResultID = commentRef.id
            comment.id = commentRef.id

            // Update ID for comment
            firebaseFirestore.collection("comments").document(commentResultID)
                .update("ID", commentResultID).await()

            // Update ID for relation
            val relaRef = firebaseFirestore.collection("rela_comments_users_posts").add(comment.toRelaMap()).await()
            relaCommentID = relaRef.id
            firebaseFirestore.collection("rela_comments_users_posts").document(relaCommentID)
                .update("ID", relaCommentID, "commentID", commentResultID).await()

            return CommentData(
                comment = comment,
                relaCommentID = commentResultID
            )

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "replyComment: ${e.message}")
        }
        return CommentData()
    }

    suspend fun softDeleteComment(comment: Comment): Boolean {
        return try {
            // delete comment
            firebaseFirestore.collection("comments").document(comment.id!!).update("deletedAt", System.currentTimeMillis())
                .await()
            // delete relation
            firebaseFirestore.collection("rela_comments_users_posts")
                .whereEqualTo("commentID", comment.id)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.documents?.forEach { document ->
                            firebaseFirestore.collection("rela_comments_users_posts").document(document.id)
                                .set(
                                    mapOf("deletedAt" to System.currentTimeMillis()),
                                    SetOptions.merge()
                                )
                        }
                    }
                }.await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "deleteComment: ${e.message}")
            false
        }
    }

    suspend fun unDeleteComment(comment: Comment): Boolean {
        return try {
            // delete comment
            firebaseFirestore.collection("comments").document(comment.id!!).update("deletedAt", null)
                .await()
            // delete relation
            firebaseFirestore.collection("rela_comments_users_posts")
                .whereEqualTo("commentID", comment.id)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.documents?.forEach { document ->
                            firebaseFirestore.collection("rela_comments_users_posts").document(document.id)
                                .set(
                                    mapOf("deletedAt" to null),
                                    SetOptions.merge()
                                )
                        }
                    }
                }.await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "deleteComment: ${e.message}")
            false
        }
    }

    suspend fun deletePermanentlyComment(comment: Comment): Boolean {
        return try {
            coroutineScope {
                // Delete the main comment
                val deleteComment = async {
                    firebaseFirestore.collection("comments").document(comment.id!!).delete().await()
                }

                // Find and delete all relationships related to this comment
                val deleteRelationships = async {
                    val relationshipsQuery = firebaseFirestore.collection("rela_comments_users_posts")
                        .whereEqualTo("commentID", comment.id)
                        .get()
                        .await()

                    // Delete all related relationship documents
                    relationshipsQuery.documents.map { document ->
                        async {
                            firebaseFirestore.collection("rela_comments_users_posts")
                                .document(document.id)
                                .delete()
                                .await()
                        }
                    }.awaitAll()
                }

                // Optional: Delete child comments if they exist
                val deleteChildComments = async {
                    val childCommentsQuery = firebaseFirestore.collection("comments")
                        .whereEqualTo("parentID", comment.id)
                        .get()
                        .await()

                    // Recursively delete child comments
                    childCommentsQuery.documents.map { childDocument ->
                        async {
                            val childComment = childDocument.toObject(Comment::class.java)
                            childComment?.let { deletePermanentlyComment(it) }
                        }
                    }.awaitAll()
                }

                // Wait for all deletion operations to complete
                awaitAll(deleteComment, deleteRelationships, deleteChildComments)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getCommentsPosts(page: Int ,postId: String, lastCmt: MutableState<String>, lastPage: MutableState<Int>): List<Comment>{
        val result = mutableListOf<Comment>()
        val limit = 5L
        try {
            // get idComments which are belong to post
            var commentQuery = firebaseFirestore.collection("rela_comments_users_posts")
                .whereEqualTo("parentID", null)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .whereEqualTo("postID", postId)
                .limit(limit)

            if (lastCmt.value != "" && page > lastPage.value) {
                val lastVisibleCommentRef = firebaseFirestore.collection("rela_comments_users_posts").document(lastCmt.value).get().await()
                commentQuery = commentQuery.startAfter(lastVisibleCommentRef)
                lastPage.value = page
            }

            val commentRefs = commentQuery.get().await()
            if(commentRefs.documents.isEmpty()){
                lastCmt.value = ""
                return result
            }else {
                lastCmt.value = commentRefs.documents.last().id
            }

            commentRefs.documents.forEach { document ->
                val commentID = document.getString("commentID")
                if(commentID != null){
                    val likeDeferred = CoroutineScope(Dispatchers.IO).async { getCountLikesComments(commentID) }
                    val repliesDeferred = CoroutineScope(Dispatchers.IO).async { getCountReplyComments(commentId = commentID) }
                    //get comment
                    val commentRef = firebaseFirestore.collection("comments").document(commentID).get().await()
                    val deletedAt = commentRef.getLong("deletedAt")
                    if(deletedAt != null){
                        return@forEach
                    }
                    val userID = document.getString("userID") ?: return@forEach
                    val postID = document.getString("postID") ?: return@forEach
                    val content = commentRef.getString("content") ?: return@forEach
                    val createdAt = commentRef.getLong("createdAt") ?: return@forEach
                    val updatedAt = commentRef.getLong("updatedAt")

                    val likes = likeDeferred.await()
                    val replies = repliesDeferred.await()
//                Log.e(TAG, "getCommentsPosts : $replies")
                    val comment = Comment(
                        id = commentID,
                        userId = userID,
                        parentId = null,
                        postId = postID,
                        content = content,
                        likes = likes,
                        replyComments = replies,
                        createdAt = createdAt,
                        updatedAt = updatedAt,
                        deletedAt = deletedAt
                    )
                    result.add(comment)
                }

            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getCommentsPosts: ${e.message}")
        }
//        Log.e(TAG, "getCommentsPosts Result: $result")
        return result
    }

    private suspend fun getCountLikesPosts(postId: String): Int {
        return try {
            // get the count of likes which belong to the post
            val countResult = firebaseFirestore.collection("likes")
                .whereEqualTo("postID", postId)
                .count()
                .get(AggregateSource.SERVER)
                .await()
            countResult.count.toInt()
        } catch (e: FirebaseFirestoreException) {
            e.printStackTrace()
            Log.e(TAG, "getCountLikesPosts: ${e.message}")
            0
        }
    }

    suspend fun getCountLikesComments(commentId: String): Int{
        return try {
            // get the count of likes which belong to the comment
            val countResult = firebaseFirestore.collection("likes")
                .whereEqualTo("commentID", commentId)
                .count()
                .get(AggregateSource.SERVER)
                .await()
            countResult.count.toInt()
        }catch (e: FirebaseFirestoreException){
            e.printStackTrace()
            Log.e(TAG, "getCountLikesComments: ${e.message}")
            0
        }
    }


    @SuppressLint("SuspiciousIndentation")
    suspend fun getCountReplyComments(commentId: String): Int{
        return try {
            // get the count of reply comments which belong to the comment
            val querySnapshot = firebaseFirestore.collection("rela_comments_users_posts")
                .whereEqualTo("parentID", commentId)
                .get()
                .await()
                querySnapshot.documents.count {
                    !it.contains("deletedAt") || it.get("deletedAt") == null
                }
        }catch (e: FirebaseFirestoreException){
            e.printStackTrace()
            Log.e(TAG, "getCountReplyComments: ${e.message}")
            0
        }
    }

    suspend fun getRepliesComments(commentId: String, lastCommentReplyID: MutableState<String>, lastParentCommentID: MutableState<String>): List<Comment>{
        var result = emptyList<Comment>()
        val limit = 5L
        try {
            val commentQuery = firebaseFirestore.collection("rela_comments_users_posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .whereEqualTo("parentID", commentId)
                .limit(limit)

            if (lastCommentReplyID.value != "" && lastParentCommentID.value == commentId) {
                val lastVisibleCommentRef = firebaseFirestore.collection("rela_comments_users_posts").document(lastCommentReplyID.value).get().await()
                commentQuery.startAfter(lastVisibleCommentRef)
            }

            val commentRefs = commentQuery.get().await()
            val lastVisible = commentRefs.documents.lastOrNull()
            if (lastVisible != null) {
                lastCommentReplyID.value = lastVisible.id
            } else {
                lastCommentReplyID.value = ""
                lastParentCommentID.value = commentId
            }

            result = commentRefs.documents.mapNotNull { document ->
                val commentID = document.getString("commentID") ?: return@mapNotNull null
                val likeDeferred = CoroutineScope(Dispatchers.IO).async { getCountLikesComments(commentID) }
                val repliesDeferred = CoroutineScope(Dispatchers.IO).async { getCountReplyComments(commentId = commentID) }
                //get comment
                val commentRef = firebaseFirestore.collection("comments").document(commentID).get().await()
                val deletedAt = commentRef.getLong("deletedAt")
                if (deletedAt != null) return@mapNotNull null
                val userID = document.getString("userID") ?: return@mapNotNull null
                val postID = document.getString("postID") ?: return@mapNotNull null
                val content = commentRef.getString("content") ?: return@mapNotNull null
                val createdAt = commentRef.getLong("createdAt") ?: return@mapNotNull null
                val updatedAt = commentRef.getLong("updatedAt")
                val likes = likeDeferred.await()
                val replies = repliesDeferred.await()

                Comment(
                    id = commentID,
                    userId = userID,
                    parentId = commentId,
                    postId = postID,
                    content = content,
                    likes = likes,
                    replyComments = replies,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                    deletedAt = deletedAt
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getRepliesComments: ${e.message}")
        }
        return result
    }


}