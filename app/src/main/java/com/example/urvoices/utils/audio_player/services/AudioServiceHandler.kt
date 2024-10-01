package com.example.urvoices.utils.audio_player.services

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class AudioServiceHandler @Inject constructor(
    private val exoPlayer: ExoPlayer,
): Player.Listener {
    val TAG = "AudioServiceHandler"
    //state flow for the audio state
    private val _audioState: MutableStateFlow<AudioState> = MutableStateFlow(AudioState.Initial)
    val audioState: StateFlow<AudioState> = _audioState.asStateFlow()
    var isStop: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private var job: Job? = null

    init {
        exoPlayer.addListener(this)
    }

    fun addMediaItemFromUrl(url: String){
        val mediaItem = MediaItem.fromUri(url)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    fun addMediaItemToQueue(audioUrl: String){
        val mediaItem = MediaItem.fromUri(audioUrl)
        exoPlayer.addMediaItem(mediaItem)
    }

    fun addMediaItemLocal(mediaItem: MediaItem){
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

//    fun setMediaItemListLocal(mediaItems: List<MediaItem>){
//        exoPlayer.setMediaItems(mediaItems)
//        exoPlayer.prepare()
//    }

    @androidx.annotation.OptIn(UnstableApi::class)
    suspend fun onPlayerEvents(
        playerEvent: PlayerEvent,
        selectedAudioIndex: Int = -1,
        index : Int = -1,
        url: String = "",
        seekPosition: Long = 0,

        ){
        when(playerEvent){
            //seek forward the audio
            PlayerEvent.Forward -> exoPlayer.seekForward()
            //seek backward the audio
            PlayerEvent.Backward -> exoPlayer.seekBack()
            //play or pause the audio
            PlayerEvent.StartPlaying -> {
                addMediaItemFromUrl(url)
                _audioState.value = AudioState.Playing(true)
                isStop.value = false
                exoPlayer.playWhenReady = true
                startProgressUpdate()
            }
            PlayerEvent.PlayPause -> playOrPause()
            //seek to the selected position
            PlayerEvent.SeekTo -> exoPlayer.seekTo(seekPosition)
            //change the audio to the selected audio
            PlayerEvent.SelectedAudioChange -> {
                when(selectedAudioIndex){
                    exoPlayer.currentMediaItemIndex -> {
                        exoPlayer.seekToDefaultPosition()
                        playOrPause()
                    }
                    else -> {
                        exoPlayer.seekToDefaultPosition(selectedAudioIndex)
                        _audioState.value = AudioState.Playing(true)
                        exoPlayer.playWhenReady = true
                        startProgressUpdate()
                    }
                }
            }
            PlayerEvent.AddToQueue -> {
                if(url != ""){
                    addMediaItemToQueue(url)
                }
            }
            PlayerEvent.DeleteFromQueue -> {
                exoPlayer.removeMediaItem(index)
            }
            PlayerEvent.Stop -> {
                exoPlayer.stop()
                isStop.value = true
                stopProgressUpdate()
            }
            is PlayerEvent.UpdateProgress -> {
                exoPlayer.seekTo((exoPlayer.duration * playerEvent.newProgress).toLong())
            }
            PlayerEvent.SeekToNext -> exoPlayer.seekToNext()
            PlayerEvent.LoopModeChange -> {
                exoPlayer.repeatMode = when(exoPlayer.repeatMode) {
                    Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
                    Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
                    else -> Player.REPEAT_MODE_OFF
                }
            }
        }
    }

    fun getRepeatMode(): Int {
        return exoPlayer.repeatMode
    }

    //listen to the playback state of the audio
    override fun onPlaybackStateChanged(playbackState: Int) {
        when(playbackState){
            Player.STATE_READY -> {
                _audioState.value = AudioState.Ready(exoPlayer.duration)
            }
            Player.STATE_BUFFERING -> {
                _audioState.value = AudioState.Buffering(exoPlayer.currentPosition)
            }
        }
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        if(reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO){
            exoPlayer.seekToDefaultPosition(exoPlayer.currentMediaItemIndex)
            _audioState.value = AudioState.CurrentPlaying(exoPlayer.currentMediaItemIndex)
        }
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    @OptIn(DelicateCoroutinesApi::class)
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _audioState.value = AudioState.Playing(isPlaying = isPlaying)
        _audioState.value = AudioState.CurrentPlaying(exoPlayer.currentMediaItemIndex)
        if(isPlaying){
            GlobalScope.launch(Dispatchers.Main){
                startProgressUpdate()
            }
        }else{
            stopProgressUpdate()
        }
    }

    //play or pause the audio
    private suspend fun playOrPause(){
        if(exoPlayer.isPlaying){
            exoPlayer.pause()
            stopProgressUpdate()
        }else{
            exoPlayer.play()
            _audioState.value = AudioState.Playing(true)
            startProgressUpdate()
        }
    }

    //update the progress of the audio
    private suspend fun startProgressUpdate() = job.run {
        while (true){
            delay(500)
            _audioState.value = AudioState.Progress(exoPlayer.currentPosition)
        }
    }

    //stop the progress update of the audio
    private fun stopProgressUpdate(){
        job?.cancel()
        _audioState.value = AudioState.Playing(false)
    }

    //
}

//sealed class for the player events
sealed class PlayerEvent {
    object StartPlaying: PlayerEvent()
    object PlayPause: PlayerEvent()
    object SelectedAudioChange: PlayerEvent()
    object AddToQueue: PlayerEvent()
    object DeleteFromQueue: PlayerEvent()
    object Forward: PlayerEvent()
    object Backward: PlayerEvent()
    object SeekToNext: PlayerEvent()
    object SeekTo: PlayerEvent()
    object Stop: PlayerEvent()
    object LoopModeChange: PlayerEvent()
    data class UpdateProgress(val newProgress: Float): PlayerEvent()
}

//sealed class for the audio state
sealed class AudioState {
    object Initial: AudioState()
    data class Ready(val duration: Long): AudioState()
    data class Progress(val progress: Long): AudioState()
    data class Buffering(val progress: Long): AudioState()
    data class Playing(val isPlaying: Boolean): AudioState()
    data class CurrentPlaying(val mediaItemIndex: Int): AudioState()
}