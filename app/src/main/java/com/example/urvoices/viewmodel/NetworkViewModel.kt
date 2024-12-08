package com.example.urvoices.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urvoices.utils.broadcasts.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NetworkViewModel @Inject constructor(
	@ApplicationContext private val context: Context
) : ViewModel() {
	private val networkMonitor = NetworkMonitor(context)

	private val _isNetworkConnected = MutableStateFlow(true)
	val isNetworkConnected: StateFlow<Boolean> get() = _isNetworkConnected.asStateFlow()

	init {
		networkMonitor.registerCallback { isConnected ->
			viewModelScope.launch {
				_isNetworkConnected.emit(isConnected)
			}
		}
	}



	override fun onCleared() {
		super.onCleared()
		networkMonitor.unregisterCallback()
	}

}