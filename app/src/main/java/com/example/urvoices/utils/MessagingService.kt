package com.example.urvoices.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.urvoices.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.tasks.await


val channel = "UrVoices"


class MessagingService : FirebaseMessagingService() {
    val TAG = "FirebaseInstanceIDService"


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

    }

    private fun createNotificationChannel() {
        val name = "Default Channel"
        val descriptionText = "Channel for default notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("default_channel_id", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }


    @SuppressLint("ServiceCast")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
//        Log.e(TAG, "From: ${remoteMessage.from}")

        val title = remoteMessage.notification?.title
        val body = remoteMessage.notification?.body
//        Log.e(TAG, "onMessageReceived: $title $body")
        try {
            if(title != null && body != null){
                if(checkNotificationPermission()){
                    val notificationManager = NotificationManagerCompat.from(this)
                    val notificationId = System.currentTimeMillis().toInt()
                    val notificationBuilder = NotificationCompat.Builder(this, "default_channel_id")
                        .setSmallIcon(R.drawable.urvoice_circle)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                    notificationManager.notify(notificationId, notificationBuilder.build())
                }else {
                    Toast.makeText(this, "Please enable notification permission", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "onMessageReceived: ${e.message}")
        }
    }


    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        }
        else { true }
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

    suspend fun sendRegistrationToServer() {
        val token = FirebaseMessaging.getInstance().token.await()
//        Log.e(TAG, "sendRegistrationToServer: $token")
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userDocRef = FirebaseFirestore.getInstance().collection("userTokens").document(user.uid)
            val userDoc = userDocRef.get().await()

            if (userDoc.exists()) {
                val existingTokens = userDoc.get("token") as? List<String> ?: emptyList()
                if (!existingTokens.contains(token)) {
                    val updatedTokens = existingTokens.toMutableList().apply { add(token) }
                    userDocRef.set(mapOf("token" to updatedTokens), SetOptions.merge())
                        .addOnSuccessListener {
                            Log.e(TAG, "sendRegistrationToServer: Token added successfully")
                        }
                        .addOnFailureListener {
                            Log.e(TAG, "sendRegistrationToServer: ${it.message}")
                        }
                } else {
                    Log.e(TAG, "sendRegistrationToServer: Token already exists")
                }
            } else {
                userDocRef.set(mapOf("token" to listOf(token)), SetOptions.merge())
                    .addOnSuccessListener {
                        Log.e(TAG, "sendRegistrationToServer: Token added successfully")
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "sendRegistrationToServer: ${it.message}")
                    }
            }
        }
    }


}