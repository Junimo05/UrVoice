package com.example.urvoices.viewmodel

import android.util.Log
import android.widget.Toast
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
import kotlinx.coroutines.delay
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

    private val _signUpState = MutableLiveData<SignupState>()
    val signUpState: LiveData<SignupState> = _signUpState

    val emailVerificationStatus = MutableLiveData<Boolean?>()

    init {
        checkStatus()
    }

    fun getCurrentUser() = auth.currentUser

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
                    val user = auth.currentUser
                    if(user != null){
                        _authState.value = AuthState.Authenticated
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

    fun signUpEmailPassword(email: String, password: String){
        _signUpState.value = SignupState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    _signUpState.value = SignupState.SignUp
                }else{
                    _signUpState.value = SignupState.Error("Please check your email is valid")
                    Log.e("AuthViewModel", task.exception?.message ?: "An error occurred")
                }
            }
            .addOnFailureListener {
                _signUpState.value= SignupState.Error("Something went wrong, please try again later")
                Log.e("AuthViewModel Failure", it.message ?: "An error occurred")
            }
    }

    fun createInfo(username: String){
        val user = auth.currentUser
        _signUpState.value = SignupState.Loading
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
                    _signUpState.value = SignupState.Complete
                    _authState.value = AuthState.Authenticated
                }else{
                    _authState.value = AuthState.Error("Cannot create user info")
                    deleteAccountWhenSignError()
                }
            }.addOnFailureListener {
                deleteAccountWhenSignError()
                _authState.value = AuthState.Error(it.message ?: "An error occurred")
                Log.e("AuthViewModel", it.message ?: "An error occurred")
            }
        }
    }

    private fun deleteAccountWhenSignError(){
        val user = auth.currentUser
        if(user != null){
            viewModelScope.launch {
                try {
                    user.delete().await()
                    _authState.value = AuthState.Unauthenticated
                } catch (e: Exception) {
                    _authState.value = AuthState.Error(e.message ?: "An error occurred")
                }
            }
        }
    }

    fun sendEmailVerification(){
        _signUpState.value = SignupState.Loading
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if(task.isSuccessful){
                    _signUpState.value = SignupState.SendEmail
                }else{
                    Toast.makeText(null, "An error occurred. Please try again", Toast.LENGTH_SHORT).show()
                    _signUpState.value = SignupState.Error(task.exception?.message ?: "An error occurred")
                }
            }
    }

    fun checkVerifyEmail(){
        _signUpState.value = SignupState.Loading
        viewModelScope.launch {
            //reload
            auth.currentUser?.reload()?.await()
            val user = auth.currentUser
            if (user != null) {
                if (user.isEmailVerified) {
                    _signUpState.value = SignupState.SuccessAuth
                    emailVerificationStatus.value = true
                } else {
                    _signUpState.value = SignupState.Error("Please verify your email")
                    emailVerificationStatus.value = false
                }
            }
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

    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("AuthViewModel", "Password reset email sent.")
                    onSuccess()
                } else {
                    val errorMessage = task.exception?.message ?: "Failed to send password reset email."
                    Log.e("AuthViewModel", errorMessage)
                    onFailure(errorMessage)
                }
            }
    }

    suspend fun signOut(){
        auth.signOut()
        //delete data in user preference
        userDataStore.clearUserInfo()
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class SignupState{
    object Idle: SignupState()
    object Complete: SignupState()
    object SuccessAuth: SignupState()
    object SendEmail: SignupState()
    object Loading: SignupState()
    object SignUp: SignupState()
    data class Error(val message: String): SignupState()
}

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Authenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}