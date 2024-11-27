package com.example.urvoices.utils.audio_player.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.example.urvoices.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val NOTIFICATION_ID = 2
private const val NOTIFICATION_CHANNEL_ID = "noti_channel_music"
private const val NOTIFICATION_CHANNEL_NAME = "Notification Channel Music"

class Notification_Manager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exoPlayer: ExoPlayer
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
    }

    fun startNotificationService(
        mediaSessionService: MediaSessionService,
        mediaSession: MediaSession
    ) {
        buildNotification(mediaSession)
        startForeGroundService(mediaSessionService)
    }

    // Start Foreground Service
    private fun startForeGroundService(mediaSessionService: MediaSessionService){
        val notification = Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        mediaSessionService.startForeground(NOTIFICATION_ID, notification)
    }


    @OptIn(UnstableApi::class)
    private fun buildNotification(mediaSession: MediaSession){
        Log.e("NotificationManager", "buildNotification called")
        PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID,
        )
            .setMediaDescriptionAdapter(Notification_Adapter(context, mediaSession.sessionActivity))
            .setSmallIconResourceId(R.drawable.urvoice_circle)
            .build()
            .also {
                it.setMediaSessionToken(mediaSession.sessionCompatToken)
                it.setUseNextActionInCompactView(true)
                it.setUseFastForwardActionInCompactView(true)
                it.setUseRewindActionInCompactView(true)
                it.setPriority(NotificationCompat.PRIORITY_LOW)
                it.setPlayer(exoPlayer)
            }
    }

    private fun createNotificationChannel(){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}