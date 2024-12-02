package com.example.urvoices.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urvoices.data.db.AppDatabase
import com.example.urvoices.utils.SharedPreferencesHelper
import com.example.urvoices.utils.UserPreferences
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferences: SharedPreferencesHelper,
    private val firestore: FirebaseFirestore,
    private val userDataStore: UserPreferences,
    private val appDatabase: AppDatabase,
    private val auth: FirebaseAuth
): ViewModel(){

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _signUpState = MutableLiveData<SignupState>(SignupState.Idle)
    val signUpState: LiveData<SignupState> = _signUpState

    val emailVerificationStatus = MutableLiveData<Boolean?>(null)
    val emailSent = MutableLiveData<Boolean?>(null)



    init {
        checkStatus()
    }

    fun resetSignUpState(){
        _signUpState.value = SignupState.Idle
    }

    fun resetEmailState(){
        emailVerificationStatus.value = null
        emailSent.value = null
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

    fun signInEmailPassword(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        _authState.value = AuthState.Authenticated
                        val userRef = firestore.collection("users").document(user.uid)
                        userRef.get().addOnCompleteListener {
                            if (it.isSuccessful) {
                                val document = it.result
                                if (document != null) {
                                    val username = document.getString("username")
                                    val email = document.getString("email")
                                    val id = document.getString("ID")
                                    requireNotNull(username) { "Username is null" }
                                    requireNotNull(email) { "Email is null" }
                                    requireNotNull(id) { "ID is null" }
                                    saveDataUserPreference(id, username, email)
                                }
                            }
                        }
                    }
                } else {
                    _authState.value = AuthState.Error("Email or Password is incorrect")
                }
            }
            .addOnFailureListener { exception ->
                when (exception) {
                    is FirebaseAuthInvalidUserException -> {
                        _authState.value = AuthState.Error("No account found with this email")
                    }
                    is FirebaseAuthInvalidCredentialsException -> {
                        _authState.value = AuthState.Error("Invalid password")
                    }
                    else -> {
                        Log.e("AuthViewModel", exception.message ?: "An error occurred")
                        _authState.value = AuthState.Error("An unexpected error occurred")
                    }
                }
            }
    }

    fun cancelSignUp() {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                try {
                    // Delete user from Firestore
                    firestore.collection("users").document(user.uid).delete().await()

                    // Delete user from Firebase Authentication
                    user.delete().await()

                    // Clear user data from shared preferences or data store
                    userDataStore.clearUserInfo()

                    // Reset sign-up state
                    resetSignUpState()
                    resetEmailState()
                    _authState.value = AuthState.Unauthenticated

                    Toast.makeText(context, "Sign-up process cancelled and user data deleted.", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Error cancelling sign-up: ${e.message}")
                    _signUpState.value = SignupState.Error("Error cancelling sign-up: ${e.message}")
                }
            }
        } else {
            resetEmailState()
            resetSignUpState()
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun signUpEmailPassword(email: String, password: String, retypePassword: String){
        _signUpState.value = SignupState.Loading
        if(password != retypePassword){
            Toast.makeText(context, "Password and retype password are not the same", Toast.LENGTH_SHORT).show()
            _signUpState.value = SignupState.Error("Password and retype password are not the same")
            return
        }
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

    fun createInfoAfterVerify(username: String){
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
                }else{
                    _authState.value = AuthState.Error("Cannot create user info")
                   cancelSignUp()
                }
            }.addOnFailureListener {
                cancelSignUp()
                _authState.value = AuthState.Error(it.message ?: "An error occurred")
                Log.e("AuthViewModel", it.message ?: "An error occurred")
            }
        }
    }

    fun sendEmailVerification(){
        _signUpState.value = SignupState.Loading
        viewModelScope.launch {
            reloadUser()
            val user = auth.currentUser
            user?.sendEmailVerification()
                ?.addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        Toast.makeText(context, "Email Verification Sent", Toast.LENGTH_SHORT).show()
                        emailSent.value = true
                        _signUpState.value = SignupState.SendEmail
                    } else {
                        emailSent.value = false
                        Toast.makeText(context, "An error occurred. Please try again", Toast.LENGTH_SHORT).show()
                        _signUpState.value = SignupState.Error(task.exception?.message ?: "An error occurred")
                    }
                }
        }
    }

    fun updateEmail(email: String){
        val user = auth.currentUser
        user?.verifyBeforeUpdateEmail(email)
            ?.addOnCompleteListener { task ->
                if(task.isSuccessful){
                    emailSent.value = true
                    emailVerificationStatus.value = false
                    firestore.collection("users").document(user.uid).update("email", email)
                }else{
                    emailSent.value = false
                    Toast.makeText(context, "An error occurred. Please try again", Toast.LENGTH_SHORT).show()
                    _signUpState.value = SignupState.Error(task.exception?.message ?: "An error occurred")
                }
            }
    }

    fun checkVerifyEmail(){
        _signUpState.value = SignupState.Loading
        viewModelScope.launch {
            //reload
            reloadUser()
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
            .addOnFailureListener {
                resetSignUpState()
                when(it){
                    is FirebaseAuthInvalidUserException -> {
                        _authState.value = AuthState.Error("No account found with this email")
                    }
                    is FirebaseAuthInvalidCredentialsException -> {
                        _authState.value = AuthState.Error("Invalid password")
                    }
                    else -> {
                        Log.e("AuthViewModel", it.message ?: "An error occurred")
                        _authState.value = AuthState.Error("An unexpected error occurred")
                    }
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

    fun updatePassword(newPassword: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.updatePassword(newPassword)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.e("AuthViewModel", "User password updated.")
                    onSuccess()
                } else {
                    val errorMessage = task.exception?.message ?: "Failed to update user password."
                    Log.e("AuthViewModel", errorMessage)
                    onFailure(errorMessage)
                }
            }
    }

    fun reAuthenticateUser(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.providerData?.forEach { userInfo ->
            val credential = when (userInfo.providerId) {
                GoogleAuthProvider.PROVIDER_ID -> GoogleAuthProvider.getCredential(email, password)
                EmailAuthProvider.PROVIDER_ID -> EmailAuthProvider.getCredential(email, password)
                // Add other providers as needed
                else -> null
            }
            credential?.let {
                user.reauthenticate(it)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("AuthViewModel", "User re-authenticated with provider: ${userInfo.providerId}.")
                            onSuccess()
                        } else {
                            val exception = task.exception
                            val errorMessage = if (exception is FirebaseAuthInvalidUserException) {
                                "The user's credential is no longer valid. Please sign in again."
                            } else {
                                exception?.message ?: "Failed to re-authenticate user with provider: ${userInfo.providerId}."
                            }
                            Log.e("AuthViewModel", errorMessage)
                            onFailure(errorMessage)
                        }
                    }
            }
        }
    }

    suspend fun reloadUser(){
        auth.currentUser?.reload()?.await()
    }

    suspend fun signOut(){
        auth.signOut()
        //delete data in user preference
        userDataStore.clearUserInfo()
        //clear local Room
        appDatabase.clearAllTables()
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