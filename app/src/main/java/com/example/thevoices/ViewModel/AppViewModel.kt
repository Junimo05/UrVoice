package com.example.thevoices.ViewModel

import androidx.lifecycle.ViewModel
import com.example.thevoices.ViewModel.State.AppGlobalState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
):ViewModel(){
    private val _appStateFlow = MutableStateFlow<AppState>(AppState.Loading)
    val appStateFlow: StateFlow<AppState> = _appStateFlow

    private val _appGlobalState = MutableStateFlow(AppGlobalState())
    val appGlobalState: StateFlow<AppGlobalState> = _appGlobalState

    fun setAppState(appState: AppState){
        _appStateFlow.value = appState
    }

    fun setLoginState(isLogin: Boolean){
        _appGlobalState.value = _appGlobalState.value.copy(isLogin = isLogin)
    }
}

sealed class AppState {
    object Loading: AppState()
    object Success: AppState()
    object Error: AppState()
}