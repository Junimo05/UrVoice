package com.example.urvoices.data.repository

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.urvoices.data.AudioManager
import com.example.urvoices.data.db.Dao.PostDao
import com.example.urvoices.data.model.Comment
import com.example.urvoices.data.model.Post
import com.example.urvoices.data.service.FirebaseNotificationService
import com.example.urvoices.data.service.FirebasePostService
import com.example.urvoices.viewmodel.UploadState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val manager: AudioManager,
    private val firestorePostService: FirebasePostService,
    private val firestoreNotiService: FirebaseNotificationService,
    private val postDao: PostDao,
){
    val TAG = "PostRepository"
    val scope = CoroutineScope(Dispatchers.Main)
    private val MAX_POSTS = 50
//    suspend fun getNewFeed(): LiveData<List<Post>> = withContext(Dispatchers.IO) {
//        val newPosts = firestorePostService.getNewFeed(1)
//        val newPostEntities = newPosts.map { it.toEntity() }
//        // Insert new posts into the database
//        postDao.insertAll(newPostEntities)
//
//        // Get all posts from the database
//        val allPosts = postDao.getAllPosts()
//
//        // If the number of posts exceeds MAX_POSTS, delete the oldest posts
//        if (allPosts.size > MAX_POSTS) {
//            val postsToDelete = allPosts.subList(0, allPosts.size - MAX_POSTS)
//            postDao.deleteAll(postsToDelete)
//        }
//
//        // Get the updated list of posts from the database
//        val updatedPosts = postDao.getAllPosts().map { it.toPost() }
//        MutableLiveData(updatedPosts)
//    }

    suspend fun createPost(post: Post, audioUri: Uri): Boolean{
        try {
            val result = firestorePostService.createPost(post, audioUri)
            if (result) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()

        }
        return false
    }

    fun getNewFeed(lastVisiblePage: MutableState<Int>, lastVisiblePost: MutableState<String>): PagingSource<Int, Post> {
        return object : PagingSource<Int, Post>() {

            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
                return try {
                    val nextPage = params.key ?: 1
                    val postList = firestorePostService.getNewFeed(nextPage, lastVisiblePost, lastVisiblePage)
                    // Check if the new page is the same as the last page
                    LoadResult.Page(
                        data = postList,
                        prevKey = if (nextPage == 1) null else nextPage - 1,
                        nextKey = if(postList.isEmpty()) null else nextPage + 1
                    )
                } catch (e: Exception) {
                    LoadResult.Error(e)
                }
            }
            override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
                return state.anchorPosition
            }
        }
    }

    fun getAllPostFromUser(userID: String): PagingSource<Int,Post> {
        return object : PagingSource<Int, Post>() {
            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
                return try {
                    val nextPage = params.key ?: 1
                    val postList = firestorePostService.getAllPostFromUser(nextPage, userID)
                    LoadResult.Page(
                        data = postList,
                        prevKey = if (nextPage == 1) null else nextPage - 1,
                        nextKey = if (postList.isEmpty()) null else nextPage + 1
                    )
                } catch (e: Exception) {
                    LoadResult.Error(e)
                }
            }

            override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
                return state.anchorPosition
            }
        }
    }

    suspend fun getUserInfoDisplayForPost(userID: String): Map<String, String> {
        val result = firestorePostService.getUserInfoDisplayForPost(userID)
        return result
    }

    //Get Comment's Detail
    suspend fun getComments_Posts(postID: String): List<Comment> {
        val result = firestorePostService.getComments_Posts(postID)
        return result
    }

    suspend fun getReply_Comments(commentID: String): List<Comment>{
        val result = firestorePostService.getReplies_Comments(commentID)
        return result
    }



}