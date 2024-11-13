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
import com.example.urvoices.data.repository.BlockRepository
import com.example.urvoices.data.repository.PostRepository
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
    private val  blockRepository: BlockRepository,
    savedStateHandle: SavedStateHandle
): ViewModel(){

    val TAG = "HomeViewModel"
    private val _homeState = MutableStateFlow<HomeState>(HomeState.Initial)
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    private val lastVisiblePost = mutableStateOf<String>("")
    private val lastVisiblePage = mutableIntStateOf(1)

    val postsPaging3 = Pager(PagingConfig(pageSize = 2)){
//        Log.e(TAG, "PagingConfig")
        postRepository.getNewFeed(lastVisiblePage, lastVisiblePost)
    }.flow.cachedIn(viewModelScope)


    suspend fun getUserInfo(userID: String): Map<String, String> {
        val result = postRepository.getUserBaseInfo(userID)
        return result
    }

    fun syncBlockData(){
        viewModelScope.launch {
            blockRepository.syncBlockUsers()
        }
    }
}

sealed class HomeState {
    object Initial : HomeState()
    object Working : HomeState()
    object Successful : HomeState()
    data class Error(val message: String) : HomeState()
}