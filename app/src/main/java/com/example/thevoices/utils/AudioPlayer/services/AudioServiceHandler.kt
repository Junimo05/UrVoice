package com.example.thevoices.utils.AudioPlayer.services

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
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
    private val exoPlayer: ExoPlayer
): Player.Listener {

    //state flow for the audio state
    private val _audioState: MutableStateFlow<AudioState> = MutableStateFlow(AudioState.Initial)
    val audioState: StateFlow<AudioState> = _audioState.asStateFlow()

    private var job: Job? = null

    init {
        exoPlayer.addListener(this)
    }

    fun addMediaItem(mediaItem: MediaItem){
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    fun setMediaItemList(mediaItems: List<MediaItem>){
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
    }

    suspend fun onPlayerEvents(
        playerEvent: PlayerEvent,
        selectAudioIndex: Int = -1,
        seekPosition: Long = 0,

        ){
        when(playerEvent){
            //seek forward the audio
            PlayerEvent.Forward -> exoPlayer.seekForward()
            //seek backward the audio
            PlayerEvent.Backward -> exoPlayer.seekBack()
            //play or pause the audio
            PlayerEvent.PlayPause -> playOrPause()
            //seek to the selected position
            PlayerEvent.SeekTo -> exoPlayer.seekTo(seekPosition)
            //change the audio to the selected audio
            PlayerEvent.SelectedAudioChange -> {
                when(selectAudioIndex){
                    exoPlayer.currentMediaItemIndex -> {
                        exoPlayer.seekToDefaultPosition()
                        playOrPause()
                    }
                    else -> {
                        exoPlayer.seekToDefaultPosition(selectAudioIndex)
                        _audioState.value = AudioState.Playing(true)
                        exoPlayer.playWhenReady = true
                        startProgressUpdate()
                    }
                }
            }
            PlayerEvent.Stop -> stopProgressUpdate()
            is PlayerEvent.UpdateProgress -> {
                exoPlayer.seekTo((exoPlayer.duration * playerEvent.newProgress).toLong())
            }
            PlayerEvent.SeekToNext -> exoPlayer.seekToNext()
        }
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

    @OptIn(DelicateCoroutinesApi::class)
    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        if(reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO){
            exoPlayer.seekToDefaultPosition(exoPlayer.currentMediaItemIndex)
            _audioState.value = AudioState.CurrentPlaying(exoPlayer.currentMediaItemIndex)
        }
    }



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

}

//sealed class for the player events
sealed class PlayerEvent {
    object PlayPause: PlayerEvent()
    object SelectedAudioChange: PlayerEvent()
    object Forward: PlayerEvent()
    object Backward: PlayerEvent()
    object SeekToNext: PlayerEvent()
    object SeekTo: PlayerEvent()
    object Stop: PlayerEvent()
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