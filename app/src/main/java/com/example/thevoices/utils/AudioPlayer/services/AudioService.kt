package com.example.thevoices.utils.AudioPlayer.services

import android.content.Intent
import android.os.Build
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.thevoices.utils.AudioPlayer.notifications.Notification_Manager
import javax.inject.Inject

class AudioService: MediaSessionService() {
    @Inject
    lateinit var mediaSession: MediaSession

    @Inject
    lateinit var notificationManager: Notification_Manager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // Start the notification service
            notificationManager.startNotificationService(this, mediaSession)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release the media session
        mediaSession.apply {
            release()
            if(player.playbackState != Player.STATE_IDLE){
                player.seekTo(0)
                player.playWhenReady = false
                player.stop()
            }
        }
    }
}