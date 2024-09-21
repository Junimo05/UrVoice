package com.example.urvoices.data.service

import android.net.Uri
import android.util.Log
import com.example.urvoices.data.AudioManager
import com.example.urvoices.data.model.User
import com.example.urvoices.utils.Auth.isPasswordStrong
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseUserService @Inject constructor(
    private val audioManager: AudioManager,
    private val storage: StorageReference,
    private val firebaseFirestore: FirebaseFirestore,
    private val auth: FirebaseAuth
){
    val TAG = "FirebaseUserService"

    suspend fun resetPassword(email: String){
        // reset password
        auth.sendPasswordResetEmail(email).await()

    }
    suspend fun getInfoUserByUserID(userId: String): User {
        // get user info from firebase firestore
        var userInfo = mutableMapOf<String, Any>()
        try {
            val docRef = firebaseFirestore.collection("users").document(userId)
            val doc = docRef.get().await()
            userInfo = doc.data as MutableMap<String, Any>
        }catch (e: Exception){
            e.printStackTrace()
        }
        return User(
            id = userId,
            username = userInfo["username"] as String,
            email = userInfo["email"] as String,
            country = userInfo["country"] as String,
            avatarUrl = userInfo["avatarUrl"] as String,
            bio = userInfo["bio"] as String,
        )
    }

    suspend fun updateAvatar(avatarUri: Uri): Boolean {
        val user = auth.currentUser
        val imgName = user?.uid + ".jpg"
        try {
            val imgRef = storage.child("${user?.uid}/img/$imgName")
            imgRef.putFile(avatarUri).await()
            val url = imgRef.downloadUrl.await().toString()
            firebaseFirestore.collection("users").document(user?.uid!!).update("avatarUrl", url).await()
            return true
        } catch (e: Exception) {
            // Handle exception
            e.printStackTrace()
            Log.e(TAG, "updateAvatar: ${e.message}")
        }
        return false
    }

    suspend fun updateUsername(username: String): Boolean {
        val user = auth.currentUser
        if (user != null) {
            try {
                firebaseFirestore.collection("users").document(user.uid).update("username", username).await()
                return true
            } catch (e: Exception) {
                // Handle exception
                e.printStackTrace()
                Log.e(TAG, "updateUsername: ${e.message}")
            }
        } else {
            // Inform the user that they are not signed in
            println("No user is currently signed in.")
        }
        return false
    }

    suspend fun updateCountry(country: String): Boolean {
        val user = auth.currentUser
        if (user != null) {
            try {
                firebaseFirestore.collection("users").document(user.uid).update("country", country).await()
                return true
            } catch (e: Exception) {
                // Handle exception
                e.printStackTrace()
                Log.e(TAG, "updateCountry: ${e.message}")
            }
        } else {
            // Inform the user that they are not signed in
            println("No user is currently signed in.")
        }
        return false
    }

    suspend fun updateBio(bio: String): Boolean {
        val user = auth.currentUser
        if (user != null) {
            try {
                firebaseFirestore.collection("users").document(user.uid).update("bio", bio).await()
                return true
            } catch (e: Exception) {
                // Handle exception
                e.printStackTrace()
                Log.e(TAG, "updateBio: ${e.message}")
            }
        } else {
            // Inform the user that they are not signed in
            println("No user is currently signed in.")
        }
        return false
    }

    suspend fun updatePassword(password: String): Boolean {
        val user = auth.currentUser
        if (user != null) {
            if (isPasswordStrong(password)) {
                try {
                    user.updatePassword(password).await()
                    return true
                } catch (e: Exception) {
                    // Handle exception
                    e.printStackTrace()
                    Log.e(TAG, "updatePassword: ${e.message}")
                }
            } else {
                // Inform the user that the password is not strong enough
                println("The password is not strong enough.")
            }
        } else {
            // Inform the user that they are not signed in
            println("No user is currently signed in.")
        }
        return false
    }

    suspend fun updateEmail(email: String): Boolean {
        val user = auth.currentUser
        if (user != null) {
            try {
                firebaseFirestore.collection("users").document(user.uid).update("email", email).await()
                user.updateEmail(email).await()
                return true
            } catch (e: Exception) {
                // Handle exception
                e.printStackTrace()
                Log.e(TAG, "updateEmail: ${e.message}")
            }
        } else {
            // Inform the user that they are not signed in
            println("No user is currently signed in.")
        }
        return false
    }

    suspend fun deleteAccount(): Boolean {
        val user = auth.currentUser
        return if (user != null) {
            try {
                firebaseFirestore.collection("users").document(user.uid).update("isDeleted", true).await()
                true
            } catch (e: Exception) {
                // Handle exception
                e.printStackTrace()
                Log.e(TAG, "deleteAccount: ${e.message}")
                false
            }
        } else {
            // Inform the user that they are not signed in
            println("No user is currently signed in.")
            false
        }
    }

}