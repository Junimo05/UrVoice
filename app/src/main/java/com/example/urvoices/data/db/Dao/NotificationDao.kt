package com.example.urvoices.data.db.Dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.urvoices.data.db.Entity.Notification
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
	@Query("SELECT * FROM notifications ORDER BY createdAt DESC")
	fun getPagingSource(): PagingSource<Int, Notification>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertAll(notifications: List<Notification>)

	@Query("SELECT MAX(createdAt) FROM notifications")
	suspend fun getLatestTimestamp(): Long?

	@Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
	suspend fun markAsRead(notificationId: String)

	@Query("DELETE FROM notifications WHERE id = :notificationId")
	suspend fun deleteNotification(notificationId: String)

	@Query("DELETE FROM notifications WHERE createdAt < :threshold")
	suspend fun deleteOldNotifications(threshold: Long)

	@Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
	fun getUnreadNotificationsCount(): Flow<Int>

	@Query("SELECT message FROM notifications WHERE id = :notificationId")
	suspend fun getMessage(notificationId: String): String
	@Query("UPDATE notifications SET message = :message WHERE id = :notificationId")
	suspend fun updateMessage(notificationId: String, message: String)

	@Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
	suspend fun updateReadStatus(notificationId: String)
}