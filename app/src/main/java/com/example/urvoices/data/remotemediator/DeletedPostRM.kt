package com.example.urvoices.data.remotemediator

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.urvoices.data.db.Dao.DeletedPostDao
import com.example.urvoices.data.db.Entity.DeletedPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalPagingApi::class)
class DeletedPostRM(
	private val firestore: FirebaseFirestore,
	private val auth: FirebaseAuth,
	private val deletedPostDao: DeletedPostDao
) : RemoteMediator<Int, DeletedPost>(){
	@OptIn(ExperimentalPagingApi::class)
	override suspend fun load(
		loadType: LoadType,
		state: PagingState<Int, DeletedPost>
	): MediatorResult {
		return try {
			val lastTimestamp = when (loadType) {
				LoadType.REFRESH -> null
				LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
				LoadType.APPEND -> {
					state.lastItemOrNull()?.deletedAt
				}
			}

			val query = firestore.collection("posts")
				.whereEqualTo("userId", auth.currentUser?.uid)
				.whereLessThan("deletedAt", lastTimestamp ?: System.currentTimeMillis())
				.orderBy("deletedAt")
				.limit(state.config.pageSize.toLong())

			val userQuery = firestore.collection("users")
				.whereEqualTo("ID", auth.currentUser?.uid)
				.limit(1)

			val username = userQuery.get().await().documents[0].getString("username") ?: ""
			val avatarUrl = userQuery.get().await().documents[0].getString("avatarUrl") ?: ""

			val snapshot = query.get().await()
			val deletedPosts = snapshot.documents.map { document ->
				DeletedPost(
					id = document.id,
					userID = document.getString("userId") ?: "",
					username = username,
					imgUrl = document.getString("imgUrl") ?: "",
					avatarUrl = avatarUrl,
					audioName = document.getString("audioName") ?: "",
					deletedAt = document.getLong("deletedAt") ?: System.currentTimeMillis()
				)
			}

			deletedPostDao.insertAll(deletedPosts)

			MediatorResult.Success(
				endOfPaginationReached = deletedPosts.isEmpty()
			)
		} catch (e: Exception) {
			Log.e("DeletedPostRM", "Error: ${e.message}")
			MediatorResult.Error(e)
		}
	}
}
