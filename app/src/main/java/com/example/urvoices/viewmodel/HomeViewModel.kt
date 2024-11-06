package com.example.urvoices.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.urvoices.data.repository.PostRepository
import com.example.urvoices.data.service.FirebaseBlockService
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("MutableCollectionMutableState")
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val postRepository: PostRepository,
    private val blockService: FirebaseBlockService,
    savedStateHandle: SavedStateHandle
): ViewModel(){

    val TAG = "HomeViewModel"
    private val _homeState = MutableStateFlow<HomeState>(HomeState.Initial)
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    private val lastVisiblePost = mutableStateOf<String>("")
    private val lastVisiblePage = mutableIntStateOf(1)

    val postsPaging3 = Pager(PagingConfig(pageSize = 3)){
//        Log.e(TAG, "PagingConfig")
        postRepository.getNewFeed(lastVisiblePage, lastVisiblePost)
    }.flow.cachedIn(viewModelScope)


    init {
       viewModelScope.launch {
//           loadingData()
       }
    }

    fun blockUser(targetID: String): String {
        _homeState.value = HomeState.Working
        var resultMsg = ""
        try {
            viewModelScope.launch {
                val result = blockService.blockUser(targetID)
                if (result.isNotEmpty()) {
                    _homeState.value = HomeState.Successful
                    resultMsg = "User blocked successfully"
                } else {
                    _homeState.value = HomeState.Error("Failed to block user")
                    resultMsg = "Something went wrong"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _homeState.value = HomeState.Error("Error blocking user")
            resultMsg = "Something went wrong"
        }
        return resultMsg
    }

    fun unblockUser(targetID: String): String {
        _homeState.value = HomeState.Working
        var resultMsg = ""
        try {
            viewModelScope.launch {
                val result = blockService.unblockUser(targetID)
                if (result.isNotEmpty()) {
                    _homeState.value = HomeState.Successful
                    resultMsg = "User unblocked successfully"
                } else {
                    _homeState.value = HomeState.Error("Failed to unblock user")
                    resultMsg = "Something went wrong"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _homeState.value = HomeState.Error("Error unblocking user")
            resultMsg = "Something went wrong"
        }
        return resultMsg
    }

    suspend fun getUserInfo(userID: String): Map<String, String> {
        val result = postRepository.getUserBaseInfo(userID)
        return result
    }

}

sealed class HomeState {
    object Initial : HomeState()
    object Working : HomeState()
    object Successful : HomeState()
    data class Error(val message: String) : HomeState()
}