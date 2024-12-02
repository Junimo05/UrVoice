package com.example.urvoices.data.remotemediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.urvoices.data.db.Dao.NotificationDao
import com.example.urvoices.data.db.Entity.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalPagingApi::class)
class NotificationRM(
	private val firestore: FirebaseFirestore,
	private val notificationDao: NotificationDao,
	private val auth: FirebaseAuth
) : RemoteMediator<Int, Notification>() {
	override suspend fun load(
		loadType: LoadType,
		state: PagingState<Int, Notification>
	): MediatorResult {
		return try {
			val lastTimestamp = when (loadType) {
				LoadType.REFRESH -> null
				LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
				LoadType.APPEND -> {
					state.lastItemOrNull()?.createdAt
				}
			}

			val query = firestore.collection("notifications")
				.whereEqualTo("targetUserID", auth.currentUser?.uid)
				.whereLessThan("createdAt", lastTimestamp ?: System.currentTimeMillis())
				.orderBy("createdAt", Query.Direction.DESCENDING)
				.limit(state.config.pageSize.toLong())

			val snapshot = query.get().await()
			val notifications = snapshot.documents.map { document ->
				Notification(
					id = document.id,
					type = document.getString("typeNotification") ?: "",
					message = document.getString("message") ?: "",
					infoID = document.getString("infoID") ?: "",
					imgUrl = document.getString("imgUrl") ?: "",
					createdAt = document.getLong("createdAt") ?: System.currentTimeMillis(),
					isRead = document.getBoolean("isRead") ?: false
				)
			}

			notificationDao.insertAll(notifications)

			MediatorResult.Success(
				endOfPaginationReached = notifications.isEmpty()
			)
		} catch (e: Exception) {
			MediatorResult.Error(e)
		}
	}
}