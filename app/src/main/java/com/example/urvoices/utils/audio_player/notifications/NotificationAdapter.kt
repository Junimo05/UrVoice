package com.example.urvoices.utils.audio_player.notifications

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerNotificationManager
import coil.ImageLoader
import coil.request.ImageRequest

@UnstableApi
class Notification_Adapter(
    private val context: Context,
    private val pendingIntent: PendingIntent?
): PlayerNotificationManager.MediaDescriptionAdapter {
    override fun getCurrentContentTitle(player: Player): CharSequence {
        return player.mediaMetadata.displayTitle?: "Unknown"
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        return pendingIntent
    }

    override fun getCurrentContentText(player: Player): CharSequence? {
        return player.mediaMetadata.displayTitle?: "Unknown"
    }

    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {
        val request = ImageRequest.Builder(context)
            .data(player.mediaMetadata.artworkUri)
            .target(
                onSuccess = { result ->
                    callback.onBitmap(result.toBitmap())
                },
                onError = { _ ->
                    // Handle error here
                }
            )
            .build()

        val imageLoader = ImageLoader(context)
        imageLoader.enqueue(request)

        return null
    }

}