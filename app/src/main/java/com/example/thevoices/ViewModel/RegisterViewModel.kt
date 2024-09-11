package com.example.thevoices.ViewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
): ViewModel() {
    var username by savedStateHandle.saveable { mutableStateOf("")}
    var password by savedStateHandle.saveable{ mutableStateOf("")}
    var retypePassword by savedStateHandle.saveable{ mutableStateOf("")}
    var email by savedStateHandle.saveable{ mutableStateOf("")}

    fun onRegisterClicked(): Boolean {
        return true
    }
}
