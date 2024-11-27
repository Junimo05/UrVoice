package com.example.urvoices.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.urvoices.data.db.Entity.Notification
import com.example.urvoices.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
	private val notificationRepository: NotificationRepository
) : ViewModel() {

	val notifications: Flow<PagingData<Notification>> =
		notificationRepository.getNotificationsFlow()
			.cachedIn(viewModelScope)

	fun refreshNotifications() {
		viewModelScope.launch {
			notificationRepository.fetchNewNotifications()
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
}