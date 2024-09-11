package com.example.thevoices.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import com.example.thevoices.utils.AudioPlayer.notifications.Notification_Manager
import com.example.thevoices.utils.AudioPlayer.services.AudioServiceHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun provideAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .setUsage(C.USAGE_MEDIA)
            .build()
    }

    @OptIn(UnstableApi::class)
    @Provides
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
    ): ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(audioAttributes, true)
        .setHandleAudioBecomingNoisy(true)
        .setTrackSelector(DefaultTrackSelector(context))
        .apply {
            setUseLazyPreparation(true)
        }
        .build()

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideMediaSession(
        @ApplicationContext context: Context,
        player: ExoPlayer
    ): MediaSession {
        return MediaSession.Builder(context, player)
            .build()
    }

    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context,
        exoPlayer: ExoPlayer
    ): Notification_Manager = Notification_Manager(context,exoPlayer)

    @Provides
    @Singleton
    fun provideServiceHandler(
        exoPlayer: ExoPlayer
    ): AudioServiceHandler = AudioServiceHandler(exoPlayer)
}