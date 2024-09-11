package com.example.thevoices.ViewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.saveable
import com.example.thevoices.utils.SharedPreferencesHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferencesHelper,
) : ViewModel() {
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var loginStatus by mutableStateOf(false)
    fun onLoginClicked(): Boolean {
        if(username == "admin" && password == "admin") {
            loginStatus = true
            Log.i("LoginScreen", "Login successful")
            sharedPreferences.setLoggedIn(true)
            return true
        }
        return false
    }

    fun onUsernameChange(newUsername: String) {
        username = newUsername
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
    }
}
