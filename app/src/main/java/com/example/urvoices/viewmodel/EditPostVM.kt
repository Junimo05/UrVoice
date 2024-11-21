package com.example.urvoices.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urvoices.data.model.Post
import com.example.urvoices.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EditPostVM @Inject constructor(

	private val postRepository: PostRepository,
	private val savedStateHandle: SavedStateHandle
): ViewModel() {
	val TAG = "EditPostVM"
	private val _editPostState = MutableLiveData<EditPostState>(EditPostState.Idle)
	val editPostState: LiveData<EditPostState> get() = _editPostState

	init{

	}

	suspend fun updatePost(mapData: Map<String, Any?>, oldData: Post): Boolean {
		_editPostState.value = EditPostState.Loading
		return withContext(viewModelScope.coroutineContext) {
			try {
				val result = postRepository.updatePost(mapData, oldData)
				if(result){
					_editPostState.postValue(EditPostState.Success)
				}else{
					_editPostState.postValue(EditPostState.Error)
				}
				result
			} catch (e: Exception) {
				_editPostState.postValue(EditPostState.Error)
				e.printStackTrace()
				false
			}
		}
	}

}

sealed class EditPostState{
	object Idle: EditPostState()
	object Loading: EditPostState()
	object Success: EditPostState()
	object Error: EditPostState()
}