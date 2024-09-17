package com.example.urvoices.viewmodel

import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.urvoices.utils.SharedPreferencesHelper
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferencesHelper
): ViewModel(){
    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    init {
        checkStatus()
    }

    fun checkStatus(){
        if(auth.currentUser == null){
            _authState.value = AuthState.Unauthenticated
        }else{
            _authState.value = AuthState.Authenticated
        }
    }

    fun signInEmailPassword(email: String, password: String){
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                    sharedPreferences.setLoggedIn(true)
                }else{
                    _authState.value = AuthState.Error("Email or Password is incorrect")
                }
        }
    }

    fun signUpEmailPassword(email: String, password: String, username: String, retypedPassword: String){
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                    sharedPreferences.setLoggedIn(true)

                    // Add user to Firestore

                }else{
                    _authState.value = AuthState.Error(task.exception?.message ?: "An error occurred")
                }
        }
    }

    fun signInWithGoogle(credential: Credential){
        _authState.value = AuthState.Loading
        if(credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            signInWithFirebase(firebaseCredential)
        }
    }

    private fun signInWithFirebase(firebaseCredential: AuthCredential) {
        scope.launch {
            try {
                auth.signInWithCredential(firebaseCredential).await()
                _authState.value = AuthState.Authenticated
                sharedPreferences.setLoggedIn(true)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun signOut(){
        auth.signOut()
        sharedPreferences.setLoggedIn(false)
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Authenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}