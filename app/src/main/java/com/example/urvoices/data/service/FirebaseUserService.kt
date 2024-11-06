package com.example.urvoices.data.service

import android.net.Uri
import android.util.Log
import com.example.urvoices.data.model.User
import com.example.urvoices.utils.Auth.isPasswordStrong
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseUserService @Inject constructor(
    private val storage: StorageReference,
    private val firebaseFirestore: FirebaseFirestore,
    private val auth: FirebaseAuth
){
    val TAG = "FirebaseUserService"

    suspend fun getInfoUserByUserID(userId: String): User? {
        // get user info from firebase firestore
        try {
            val docRef = firebaseFirestore.collection("users").document(userId).get().await()

            val userID = docRef.data?.get("ID") as String
            val username = docRef.data?.get("username") as String
            val displayname = docRef.data?.get("displayname")?.toString() ?: username
            val email = docRef.data?.get("email") as String
            val country = docRef.data?.get("country") as String
            val avatarUrl = docRef.data?.get("avatarUrl") as String
            val bio = docRef.data?.get("bio") as String
            return User(userID, username, displayname, email, country, avatarUrl, bio)

        }catch (e: Exception){
            e.printStackTrace()
        }
        return null
    }

    suspend fun followUser(followingUserID: String, followStatus: Boolean): String {
        val user = auth.currentUser
        var resultID = ""
        if (user != null) {
            try {
                if (followStatus) {
                    // Follow user
                    val follow = hashMapOf(
                        "ID" to null,
                        "userID" to user.uid,
                        "followingUserID" to followingUserID,
                        "createdAt" to System.currentTimeMillis()
                    )
                    firebaseFirestore.collection("follows").add(follow)
                        .addOnCompleteListener {
                            firebaseFirestore.collection("follows").document(it.result?.id!!).update("ID", it.result?.id!!)
                            resultID = it.result?.id!!
                        }.await()
                } else {
                    // Unfollow user
                    val documents = firebaseFirestore.collection("follows")
                        .whereEqualTo("userID", user.uid)
                        .whereEqualTo("followingUserID", followingUserID)
                        .get()
                        .await()
                    for (document in documents) {
                        document.reference.delete().await()
                    }
                    resultID = ""
                }
                return resultID
            } catch (e: Exception) {
                // Handle exception
                e.printStackTrace()
                Log.e(TAG, "followUser: ${e.message}")
            }
        } else {
            // Inform the user that they are not signed in
            println("No user is currently signed in.")
        }
        return ""
    }

    suspend fun getPostCounts(userId: String): Int {
        // get post counts
        return try {
            val docRef = firebaseFirestore.collection("posts")
                .whereEqualTo("userID", userId)
                .count()
                .get(AggregateSource.SERVER)
                .await()
            docRef.count.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    suspend fun getFollowStatus(userId: String): Boolean {
        val user = auth.currentUser
        if (user != null) {
            return try {
                val countResult = firebaseFirestore.collection("follows")
                    .whereEqualTo("userID", user.uid)
                    .whereEqualTo("followingUserID", userId)
                    .count()
                    .get(AggregateSource.SERVER)
                    .await()
                countResult.count > 0
            } catch (e: Exception) {
                // Handle exception
                e.printStackTrace()
                Log.e(TAG, "getFollowStatus: ${e.message}")
                false
            }
        } else {
            // Inform the user that they are not signed in
            println("No user is currently signed in.")
            return false
        }
    }


    suspend fun getFollowingCounts(userId: String): Int {
        return try {
            val countResult = firebaseFirestore.collection("follows")
                .whereEqualTo("userID", userId)
                .count()
                .get(AggregateSource.SERVER)
                .await()
            countResult.count.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }


    suspend fun getFollowingDetail(userId: String): List<User> {
        // get following detail
        val followingList = mutableListOf<User>()
        try {
            val docRef = firebaseFirestore.collection("follows")
                .whereEqualTo("followingUserID", userId)
                .limit(10)
                .get()
                .await()
            docRef.documents.mapNotNull { doc ->
                val followingId = doc["userID"] as String
                val followingInfo = getInfoUserByUserID(followingId)
                followingInfo?.let {
                    followingList.add(it)
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        return followingList
    }

    suspend fun getFollowerCounts(userId:String): Int{
        return try {
            val countResult = firebaseFirestore.collection("follows")
                .whereEqualTo("followingUserID", userId)
                .count()
                .get(AggregateSource.SERVER)
                .await()
            countResult.count.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    suspend fun getFollowerDetail(userId: String): List<User> {
        // get follower detail
        val followerList = mutableListOf<User>()
        try {
            val docRef = firebaseFirestore.collection("follows")
                .whereEqualTo("userID", userId)
                .limit(10)
                .get()
                .await()
            docRef.documents.mapNotNull { doc ->
                val followerId = doc["followingUserID"] as String
                val followerInfo = getInfoUserByUserID(followerId)
                followerInfo?.let {
                    followerList.add(it)
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        return followerList
    }

    suspend fun resetPassword(email: String){
        // reset password
        auth.sendPasswordResetEmail(email).await()
    }

    suspend fun updateAvatar(avatarUri: Uri): Boolean {
        val user = auth.currentUser
        val imgName = "avatar.jpg"
        try {
            val imgRef = storage.child("imgs/${user?.uid}/$imgName")
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