package com.example.urvoices.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.urvoices.data.db.Entity.Notification
import com.example.urvoices.data.repository.DeletedPostRepository
import com.example.urvoices.data.repository.NotificationRepository
import com.example.urvoices.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
	@ApplicationContext private val context: Context,
	private val notificationRepository: NotificationRepository,
) : ViewModel() {

	val TAG = "NotificationViewModel"

	private val _hasNewNotification = MutableStateFlow(false)
	val hasNewNotification: StateFlow<Boolean> = _hasNewNotification
	val isRefreshing = MutableStateFlow(false)

	private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
	val navigationEvents = _navigationEvents.asSharedFlow()

	init {
		EventBus.getDefault().register(this)
		refreshNotifications()
	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	fun onNewNotificationEvent(event: NewNotificationEvent) {
		// Handle new notifications
		_hasNewNotification.value = event.notificationCount > 0
		// Update UI or perform other actions
	}

	fun onNotificationClick(event: NotificationEvent) = viewModelScope.launch {
		try {
			when (event) {
				is NotificationEvent.CommentPost -> {
//				val data = notificationRepository.getInfoForCommentPostNoti(event.relaId)
//				val userID = data["userID"]!!
//				val postID = data["postID"]!!
//				_navigationEvents.emit(NavigationEvent.NavigateToPost(userID.toString(), postID.toString()))
				}
				is NotificationEvent.ReplyComment -> {

				}
				is NotificationEvent.FollowRequest -> {
					val data = notificationRepository.getInfoForFollowUserNoti(event.relaId)
					val userID = data["userID"]!!
					_navigationEvents.emit(NavigationEvent.NavigateToUser(userID.toString()))
				}
				is NotificationEvent.Following -> {
					val data = notificationRepository.getInfoForFollowUserNoti(event.relaId)
					val userID = data["userID"]!!
					_navigationEvents.emit(NavigationEvent.NavigateToUser(userID.toString()))
					markNotificationAsRead(event.notiID)
				}
				is NotificationEvent.LikePost -> {
					val data = notificationRepository.getInfoForLikePostNoti(event.relaId)
					Log.e(TAG, "onNotificationClick: ${data.values}")
					val userID = data["userID"]!!
					val postID = data["postID"]!!
					_navigationEvents.emit(NavigationEvent.NavigateToPost(userID, postID))
					markNotificationAsRead(event.notiID)
				}
				is NotificationEvent.LikeComment -> {
//				val data = notificationRepository.getInfoForLikeCommentNoti(event.relaId)
//				val userID = data["userID"]!!
//				val postID = data["postID"]!!
//				val commentID = data["commentID"]!!
//				_navigationEvents.emit(NavigationEvent.NavigateToComment(userID, postID, commentID))
				}
			}
		} catch (e: Exception) {
			Log.e(TAG, "onNotificationClick: ", e)
			Toast.makeText(context, "Unknown Error", Toast.LENGTH_SHORT).show()
		}
	}

	val notifications: Flow<PagingData<Notification>> =
		notificationRepository.getNotificationsFlow()
			.cachedIn(viewModelScope)

	fun refreshNotifications() {
		viewModelScope.launch {
			notificationRepository.fetchNewNotifications()
			_hasNewNotification.value = false
		}
	}

	fun markNotificationAsRead(notificationId: String) {
		viewModelScope.launch {
			notificationRepository.markNotificationAsRead(notificationId)
		}
	}

	fun cleanupOldNotifications() {
		viewModelScope.launch {
			notificationRepository.cleanupOldNotifications()
		}
	}

	fun acceptFollowRequest(notiID: String, relaId: String): Boolean {
		var result = false
		viewModelScope.launch {
			markNotificationAsRead(notiID)
			result = notificationRepository.acceptFollowRequest(
				notiID = notiID,
				relaID = relaId
			)
		}
		return result
	}

	fun rejectFollowRequest(notiID: String, relaId: String) {
		viewModelScope.launch {
			markNotificationAsRead(notiID)
			notificationRepository.rejectFollowRequest(
				notiID = notiID,
				relaID = relaId
			)
		}
	}

	fun setIsRefreshing(value: Boolean) {
		isRefreshing.value = value
	}

	override fun onCleared() {
		EventBus.getDefault().unregister(this)
		super.onCleared()
	}

}

sealed class NotificationEvent {
	data class Following(val notiID: String, val relaId: String) : NotificationEvent()
	data class CommentPost(val notiID: String, val relaId: String) : NotificationEvent()
	data class ReplyComment(val notiID: String, val relaId: String) : NotificationEvent()
	data class LikePost(val notiID: String, val relaId: String) : NotificationEvent()
	data class LikeComment(val notiID: String, val relaId: String) : NotificationEvent()
	data class FollowRequest(val notiID: String,val relaId: String) : NotificationEvent()
}

sealed class NavigationEvent {
	data class NavigateToComment(val userID: String, val postID: String, val commentID: String, val parentID:String = "") : NavigationEvent()
	data class NavigateToUser(val userID: String): NavigationEvent()
	data class NavigateToPost(val userID: String, val postID: String) : NavigationEvent()
}

data class NewNotificationEvent(val notificationCount: Int)