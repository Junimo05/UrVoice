package com.example.urvoices.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.urvoices.data.model.Comment
import com.example.urvoices.data.model.MessageNotification
import com.example.urvoices.data.model.Notification
import com.example.urvoices.data.model.TypeNotification
import com.example.urvoices.data.repository.PostRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@SuppressLint("MutableCollectionMutableState")
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val postRepository: PostRepository,
    savedStateHandle: SavedStateHandle
): ViewModel(){

    val TAG = "HomeViewModel"
    private val _homeState = MutableStateFlow<HomeState>(HomeState.Initial)
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    val lastVisiblePost = mutableStateOf<String>("")
    val lastVisiblePage = mutableStateOf<Int>(1)

    val postsPaging3 = Pager(PagingConfig(pageSize = 3)){
        postRepository.getNewFeed(lastVisiblePage, lastVisiblePost)
    }.flow.cachedIn(viewModelScope)

    init {
       viewModelScope.launch {
//           loadingData()
       }
    }

//    suspend fun loadingData() {
//        _homeState.value = HomeState.LoadingData
//        val newPostList = postRepository.getNewFeed()
//        _posts.value = newPostList.value
////        Log.e(TAG, "loadingData: ${newPostList.value}")
//        _homeState.value = HomeState.LoadedData
//    }


    suspend fun getUserInfo(userID: String): Map<String, String> {
        val result = postRepository.getUserInfoDisplayForPost(userID)
        return result
    }

}

sealed class HomeState {
    object Initial : HomeState()
    object LoadingData : HomeState()
    object LoadedData : HomeState()
    data class Error(val message: String) : HomeState()
}