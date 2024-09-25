package com.example.urvoices.viewmodel

import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urvoices.utils.SharedPreferencesHelper
import com.example.urvoices.utils.UserPreferences
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferencesHelper,
    private val firestore: FirebaseFirestore,
    private val userDataStore: UserPreferences,
    private val auth: FirebaseAuth
): ViewModel(){
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

    fun saveDataUserPreference(userid: String, username: String, email: String){
        viewModelScope.launch {
            userDataStore.saveUserInfo(userid, username, email)
        }
    }

    fun signInEmailPassword(email: String, password: String){
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                    sharedPreferences.setLoggedIn(true)
                    //
                    val user = auth.currentUser
                    if(user != null){
                        val userRef = firestore.collection("users").document(user.uid)
                        userRef.get().addOnCompleteListener {
                            if(it.isSuccessful){
                                val document = it.result
                                if(document != null){
                                    val username = document.getString("username")
                                    val email = document.getString("email")
                                    val id = document.getString("ID")
                                    requireNotNull(username) { "Username is null"}
                                    requireNotNull(email) { "Email is null"}
                                    requireNotNull(id) { "ID is null"}
                                    saveDataUserPreference(id, username, email)
                                }
                            }
                        }
                    }
                }else{
                    _authState.value = AuthState.Error("Email or Password is incorrect")
                }
            }
            .addOnFailureListener{
                _authState.value = AuthState.Error(it.message ?: "An error occurred")
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
                    val user = auth.currentUser
                    if(user != null){
                        val userRef = firestore.collection("users").document(user.uid)
                        userRef.set(mapOf(
                            "ID" to user.uid,
                            "bio" to "",
                            "country" to "",
                            "avatarUrl" to "",
                            "email" to user.email,
                            "username" to username,
                            "createAt" to user.metadata?.creationTimestamp,
                            "isDeleted" to false
                        )).addOnCompleteListener {
                            if(it.isSuccessful){
                                _authState.value = AuthState.Authenticated
                            }else{
                                _authState.value = AuthState.Error(it.exception?.message ?: "An error occurred")
                            }
                        }
                    }

                }else{
                    _authState.value = AuthState.Error(task.exception?.message ?: "An error occurred")
                }
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "An error occurred")
            }
    }


    //Before Pick Credential
    fun signInWithGoogle(credential: Credential){
        _authState.value = AuthState.Loading
        if(credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            signInWithFirebase(firebaseCredential)
        }
    }

    private fun signInWithFirebase(firebaseCredential: AuthCredential) {
        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val user = auth.currentUser
                    if(user != null){
                        val userRef = firestore.collection("users").document(user.uid)
                        userRef.get().addOnCompleteListener { getTask ->
                            if(getTask.isSuccessful){
                                val document = getTask.result
                                if(document != null && document.exists()){
                                    _authState.value = AuthState.Authenticated
                                    sharedPreferences.setLoggedIn(true)
                                    //save data to user preference
                                    val username = document.getString("username")
                                    val email = document.getString("email")
                                    val id = document.getString("ID")
                                    requireNotNull(username) { "Username is null"}
                                    requireNotNull(email) { "Email is null"}
                                    requireNotNull(id) { "ID is null"}
                                    saveDataUserPreference(id, username, email)
                                } else {
                                    // User does not exist in Firestore, create a new document
                                    userRef.set(mapOf(
                                        "ID" to user.uid,
                                        "bio" to "",
                                        "country" to "",
                                        "avatarUrl" to "",
                                        "email" to user.email,
                                        "username" to user.displayName,
                                        "createAt" to user.metadata?.creationTimestamp,
                                        "isDeleted" to false
                                    )).addOnCompleteListener {
                                        if(it.isSuccessful){
                                            _authState.value = AuthState.Authenticated
                                            sharedPreferences.setLoggedIn(true)
                                            //save data to user preference
                                            saveDataUserPreference(user.uid, user.displayName ?: "", user.email ?: "")
                                        }else{
                                            _authState.value = AuthState.Error(it.exception?.message ?: "An error occurred")
                                        }
                                    }
                                }
                            } else {
                                _authState.value = AuthState.Error(getTask.exception?.message ?: "An error occurred")
                            }
                        }
                    }
                }else{
                    _authState.value = AuthState.Error(task.exception?.message ?: "An error occurred")
                }
            }
    }

    suspend fun signOut(){
        auth.signOut()
        //delete data in user preference
        userDataStore.clearUserInfo()
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