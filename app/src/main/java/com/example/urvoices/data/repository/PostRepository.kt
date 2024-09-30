package com.example.urvoices.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.example.urvoices.data.AudioManager
import com.example.urvoices.data.db.Dao.PostDao
import com.example.urvoices.data.db.Entity.PostEntity
import com.example.urvoices.data.model.Comment
import com.example.urvoices.data.model.Post
import com.example.urvoices.data.service.FirebasePostService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val manager: AudioManager,
    private val firestorePostService: FirebasePostService,
    private val postDao: PostDao,
){
    val TAG = "PostRepository"
    val scope = CoroutineScope(Dispatchers.Main)
    private val MAX_POSTS = 50
    suspend fun getNewFeed(): LiveData<List<Post>> = withContext(Dispatchers.IO) {
        val newPosts = firestorePostService.getNewFeed()
        val newPostEntities = newPosts.map { it.toEntity() }
        // Insert new posts into the database
        postDao.insertAll(newPostEntities)

        // Get all posts from the database
        val allPosts = postDao.getAllPosts()

        // If the number of posts exceeds MAX_POSTS, delete the oldest posts
        if (allPosts.size > MAX_POSTS) {
            val postsToDelete = allPosts.subList(0, allPosts.size - MAX_POSTS)
            postDao.deleteAll(postsToDelete)
        }

        // Get the updated list of posts from the database
        val updatedPosts = postDao.getAllPosts().map { it.toPost() }
        MutableLiveData(updatedPosts)
    }


    //Firebase Only
    suspend fun getAllPostFromUser(userID: String): List<Post> {
        val result = firestorePostService.getAllPostFromUser(userID)
        return result
    }

    suspend fun getComments_Posts(postID: String): List<Comment> {
        val result = firestorePostService.getComments_Posts(postID)
        return result
    }

    suspend fun getReply_Comments(commentID: String): List<Comment>{
        val result = firestorePostService.getReplies_Comments(commentID)
        return result
    }


}