package com.example.urvoices.data.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.urvoices.data.db.Dao.DeletedPostDao
import com.example.urvoices.data.db.Entity.DeletedPost
import com.example.urvoices.data.remotemediator.DeletedPostRM
import com.example.urvoices.data.service.FirebasePostService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DeletedPostRepository @Inject constructor(
	private val postService: FirebasePostService,
	private val deletedPostDao: DeletedPostDao,
	private val auth: FirebaseAuth,
	private val firebaseFirestore: FirebaseFirestore
){

	val TAG = "DeletedPostRepository"
	val scope = CoroutineScope(Dispatchers.IO)

	@OptIn(ExperimentalPagingApi::class)
	fun getDeletedPostsFlow(): Flow<PagingData<DeletedPost>> = Pager(
		config = PagingConfig(pageSize = 20),
		remoteMediator = DeletedPostRM(
			firebaseFirestore,
			auth,
			deletedPostDao
		)
	) {
		deletedPostDao.getPagingSource()
	}.flow

	fun mapToDeletedPost(document: DocumentSnapshot, username: String, avatarUrl: String): DeletedPost {
		return DeletedPost(
			id = document.id,
			userID = document.getString("userId") ?: "",
			username = username,
			avatarUrl = avatarUrl,
			imgUrl = document.getString("imgUrl") ?: "",
			audioName = document.getString("audioName") ?: "",
			deletedAt = document.getLong("deletedAt") ?: System.currentTimeMillis()
		)
	}

	fun fetchNewDeletedPosts() {
		try {
			scope.launch {
				val currentUser = auth.currentUser ?: return@launch
				val userId = currentUser.uid
				val lastTimestamp = deletedPostDao.getLastTimestamp() ?: 0

				val querySnapshot = firebaseFirestore.collection("posts")
					.whereEqualTo("userId", userId)
					.whereGreaterThan("deletedAt", lastTimestamp)
					.get()
					.await()

				val newDeletedPosts = querySnapshot.documents.map { document ->
					// Get User Info
					val userDoc = firebaseFirestore.collection("users").document(document.getString("userId") ?: "")
					val user = userDoc.get().await()
					val username = user.getString("username") ?: ""
					val avatarUrl = user.getString("avatarUrl") ?: ""
					mapToDeletedPost(document, username, avatarUrl)
				}

				deletedPostDao.insertAll(newDeletedPosts)
			}
		} catch (e: Exception) {
			e.printStackTrace()
			Log.e(TAG, "fetchNewDeletedPosts Func Failed: ${e.message}")
		}
	}

	fun restorePost(deletedPost: DeletedPost) {
		try {
			scope.launch {
				val currentUser = auth.currentUser?: return@launch
				val postDoc = firebaseFirestore.collection("posts").document(deletedPost.id)
				postDoc.update("deletedAt", null)
				deletedPostDao.delete(deletedPost)
			}
		} catch (e: Exception) {
			e.printStackTrace()
			Log.e(TAG, "restorePost Func Failed: ${e.message}")
		}
	}


}