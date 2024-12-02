package com.example.urvoices.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.urvoices.R
import com.example.urvoices.app.host.MainActivity
import com.example.urvoices.data.repository.NotificationRepository
import com.example.urvoices.viewmodel.NotificationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


val channel = "UrVoices"

@AndroidEntryPoint
class MessagingService : FirebaseMessagingService() {
    val TAG = "FirebaseInstanceIDService"

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    @Inject
    lateinit var notificationRepository: NotificationRepository


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sharedPreferencesHelper.save("Token", token, "UrVoices")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "default_channel_id"
            val name = "Default Notifications"
            val descriptionText = "Channel for default notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }



    @SuppressLint("ServiceCast", "WrongThread")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
//        Log.e(TAG, "From: ${remoteMessage.from}")

        val intent = Intent("NewNotification")
        sendBroadcast(intent)

        val title = remoteMessage.notification?.title
        val body = remoteMessage.notification?.body
//        Log.e(TAG, "onMessageReceived: $title $body")

        notificationRepository.fetchNewNotifications()

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

}