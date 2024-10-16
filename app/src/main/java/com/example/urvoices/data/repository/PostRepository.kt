package com.example.urvoices.data.repository

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.urvoices.data.AudioManager
import com.example.urvoices.data.db.Dao.PostDao
import com.example.urvoices.data.model.Comment
import com.example.urvoices.data.model.Post
import com.example.urvoices.data.service.FirebaseNotificationService
import com.example.urvoices.data.service.FirebasePostService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
                    //add to local database
//                    postDao.insertAll(postList.map { it.toEntity() })
//                    val allPosts = postDao.getAllPosts()
//                    // If the number of posts exceeds MAX_POSTS, delete the oldest posts
//                    if (allPosts.size > MAX_POSTS) {
//                        val postsToDelete = allPosts.subList(0, allPosts.size - MAX_POSTS)
//                        postDao.deleteAll(postsToDelete)
//                    }
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

    suspend fun getPostDetail(postID: String): Post? {
        try {
            val post = firestorePostService.getPostDetailByPostID(postID)
            return post
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    //Post Interaction Events
    suspend fun likePost(actionUserID: String, postID: String): String {
        val result = firestorePostService.likePost(
            postId = postID,
            userID = actionUserID
        )
        return result
    }

    suspend fun likeComment(actionUserID: String, commentID: String, postID: String): String {
        val result = firestorePostService.likeComment(actionUserID, commentID, postID)
        return result
    }

    suspend fun commentPost(actionUserID: String, postID: String, content: String): String {
        val result = firestorePostService.commentPost(actionUserID, postID, content)
        return result
    }

    suspend fun replyComment(actionUserID: String, commentID: String, postID: String, content: String): String {
        val result = firestorePostService.replyComment(actionUserID, commentID, postID, content)
        return result
    }

    //
    fun getAllPostFromUser(userID: String, lastVisiblePost: MutableState<String>, lastVisiblePage: MutableState<Int>): PagingSource<Int,Post> {
        return object : PagingSource<Int, Post>() {
            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
                return try {
                    val nextPage = params.key ?: 1
                    val postList = firestorePostService.getAllPostFromUser(nextPage, userID, lastVisiblePost, lastVisiblePage)
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

    suspend fun getUserBaseInfo(userID: String): Map<String, String> {
        val result = firestorePostService.getUserInfoDisplayForPost(userID)
        return result
    }

    //Get Comment's Detail
     fun getComments_Posts(postID: String, lastCmt: MutableState<String>, lastPage: MutableState<Int>): PagingSource<Int, Comment> {
        return object : PagingSource<Int, Comment>() {
            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Comment> {
                return try {
                    val nextPage = params.key ?: 1
                    val commentList = firestorePostService.getCommentsPosts(nextPage, postID, lastCmt, lastPage)
                    LoadResult.Page(
                        data = commentList,
                        prevKey = if (nextPage == 1) null else nextPage - 1,
                        nextKey = if (commentList.isEmpty()) null else nextPage + 1
                    )
                } catch (e: Exception) {
                    LoadResult.Error(e)
                }
            }

            override fun getRefreshKey(state: PagingState<Int, Comment>): Int? {
                return state.anchorPosition
            }
        }
    }

    suspend fun getReply_Comments(commentID: String, lastCommentReplyID: MutableState<String>, lastParentCommentID: MutableState<String>): List<Comment>{
        try {
            val result = firestorePostService.getRepliesComments(commentID, lastCommentReplyID, lastParentCommentID)
            return result
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }
}