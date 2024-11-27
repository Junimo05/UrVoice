package com.example.urvoices.data.repository

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.urvoices.data.db.AppDatabase
import com.example.urvoices.data.db.Dao.SavedPostDao
import com.example.urvoices.data.db.Entity.SavedPost
import com.example.urvoices.data.model.Comment
import com.example.urvoices.data.model.Post
import com.example.urvoices.data.service.FirebaseNotificationService
import com.example.urvoices.data.service.FirebasePostService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firestorePostService: FirebasePostService,
    private val savedPostDao: SavedPostDao,
    private val database: AppDatabase,
    private val firestoreNotiService: FirebaseNotificationService,
){
    val TAG = "PostRepository"
    val scope = CoroutineScope(Dispatchers.Main)

    suspend fun createPost(post: Post, audioUri: Uri, imgUri: Uri): Boolean{
        try {
            val result = firestorePostService.createPost(post, audioUri, imgUri)
            if (result) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()

        }
        return false
    }

    suspend fun updatePost(mapData: Map<String, Any?>, oldData: Post): Boolean {
        try {
            val result = firestorePostService.updatePost(mapData, oldData)
            if(result) {
                return true
            }else {
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "updatePostRepo Error: ${e.message}")
        }
        return false
    }

    suspend fun deletePost(postID: String): Boolean {
        try {
            val result = firestorePostService.deletePost(postID)
            if(result) {
                return true
            }else {
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "deletePostRepo Error: ${e.message}")
        }
        return false
    }

    suspend fun getAllDeletePost(): List<Post> {
        try {
            val result = firestorePostService.getAllDeletePost()
            return result
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getAllDeletePostRepo Error: ${e.message}")
        }
        return emptyList()
    }

    fun getNewFeed(lastVisiblePage: MutableState<Int>, lastVisiblePost: MutableState<String>): PagingSource<Int, Post> {
        return object : PagingSource<Int, Post>() {
            private val NETWORK_PAGE_SIZE = 4

            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
                return try {
                    val nextPage = params.key ?: 1
                    val postList = firestorePostService.getNewFeed(nextPage, lastVisiblePost, lastVisiblePage)
                    val nextKey = if (postList.isEmpty()) null else {
                        nextPage + (params.loadSize / NETWORK_PAGE_SIZE)
                    }
                    // Check if the new page is the same as the last page
                    LoadResult.Page(
                        data = postList,
                        prevKey = if (nextPage == 1) null else nextPage - 1,
                        nextKey = if(postList.isEmpty()) null else nextKey
                    )
                } catch (e: Exception) {
                    LoadResult.Error(e)
                }
            }
            override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
                return state.anchorPosition?.let { anchorPosition ->
                    state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                        ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
                }
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
    suspend fun savePost(postID: String): Boolean? {
        try {
            val result = firestorePostService.savePosts(postID)
            if(result != null) {
                syncSavedPosts()
            }
            return result
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    suspend fun getSaveStatus(postID: String): Boolean {
        try {
            val result = firestorePostService.getSaveStatus(postID)
            return result
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

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

    suspend fun commentPost(actionUserID: String, postID: String, content: String): Comment {
        val result = firestorePostService.commentPost(actionUserID, postID, content)
        return result
    }

    suspend fun replyComment(actionUserID: String, parentID: String, postID: String, content: String): Comment {
        val result = firestorePostService.replyComment(actionUserID, parentID, postID, content)
        return result
    }

    suspend fun softDeleteComment(comment: Comment): Boolean{
        try {
            val result = firestorePostService.softDeleteComment(comment)
            return result
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
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

    fun getAllSavedPostFromUser(userID: String, lastVisiblePost: MutableState<String>, lastVisiblePage: MutableState<Int>): PagingSource<Int,Post> {
        return object : PagingSource<Int, Post>() {
            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
                return try {
                    val nextPage = params.key ?: 1
                    val postList = firestorePostService.getAllSavedPostFromUser(nextPage, userID, lastVisiblePost, lastVisiblePage)

                    val nextKey = if (postList.isEmpty()) null else {
                        nextPage + (params.loadSize / 5)
                    }

                    LoadResult.Page(
                        data = postList,
                        prevKey = if (nextPage == 1) null else nextPage - 1,
                        nextKey = if (postList.isEmpty()) null else nextKey
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

    suspend fun syncSavedPosts() {
        withContext(Dispatchers.IO) {
            try {
                val snapshot = firestorePostService.getAllSavedPost()
                savedPostDao.deleteAll()
                snapshot.forEach { id ->
                    val document = firestore.collection("posts").document(id).get().await()
                    val userDoc = firestore.collection("users").document(document.getString("userId")!!).get().await()
                    val userid = document.getString("userId") ?: ""
                    val username = userDoc.getString("username") ?: ""
                    val avatarUrl = userDoc.getString("avatarUrl") ?: ""
                    val audioName = document.getString("audioName") ?: ""
                    val audioUrl = document.getString("url") ?: ""
                    val imgUrl = document.getString("imgUrl") ?: ""
                    savedPostDao.insert(
                        SavedPost(
                            id = id,
                            userID = userid,
                            username = username,
                            avatarUrl = avatarUrl,
                            imgUrl = imgUrl,
                            audioName = audioName,
                            audioUrl = audioUrl
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "syncSavedPosts: ${e.message}")
            }
        }
    }


    fun getSavedPostDataFromLocal(): Flow<PagingData<SavedPost>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
        ){
            savedPostDao.getAll()
        }.flow
    }


    suspend fun getUserBaseInfo(userID: String): Map<String, String> {
        val result = firestorePostService.getUserInfoDisplayForPost(userID)
        return result
    }

    //Get Comment's Detail
     fun getCommentsPosts(postID: String, lastCmt: MutableState<String>, lastPage: MutableState<Int>): PagingSource<Int, Comment> {
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

    suspend fun getReplyComments(commentID: String, lastCommentReplyID: MutableState<String>, lastParentCommentID: MutableState<String>): List<Comment>{
        try {
            val result = firestorePostService.getRepliesComments(commentID, lastCommentReplyID, lastParentCommentID)
            return result
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }
}