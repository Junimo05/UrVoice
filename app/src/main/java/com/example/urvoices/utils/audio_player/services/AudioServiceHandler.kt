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
import com.example.urvoices.data.model.Audio
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

    //Playlist Management
    val playlist = mutableListOf<Audio>()
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    init {
        exoPlayer.addListener(this)
    }

    fun addToPlaylist(audio: Audio) {
        // Check for duplicates
        if (playlist.any { it.url == audio.url }) {
            return
        }
        playlist.add(audio)
        // Add to ExoPlayer's queue
        val mediaItem = MediaItem.fromUri(audio.url)
        exoPlayer.addMediaItem(mediaItem)
        _audioState.value = AudioState.PlaylistUpdated(playlist)
//        exoPlayer.prepare()
    }

    fun removeFromPlaylist(audio: Audio) {
        val index = playlist.indexOf(audio)
        if (index != -1) {
            playlist.removeAt(index)
            exoPlayer.removeMediaItem(index)
            _audioState.value = AudioState.PlaylistUpdated(playlist)
        }
    }

    fun addMediaItemFromUrl(audio: Audio){
        val mediaItem = MediaItem.fromUri(audio.url)
        playlist.add(audio)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    suspend fun onPlayerEvents(
        playerEvent: PlayerEvent,
        index : Int = -1,
        audio: Audio = Audio(
            id = "",
            title = "",
            url = "",
            author = "",
            duration = 0L
        ),
        seekPosition: Long = 0,

    ){
        when(playerEvent){
            //seek forward the audio
            PlayerEvent.Forward -> exoPlayer.seekForward()
            //seek backward the audio
            PlayerEvent.Backward -> exoPlayer.seekBack()
            //play or pauseRecording the audio
            PlayerEvent.StartPlaying -> {
                if(exoPlayer.isPlaying){
                    exoPlayer.stop()
                    stopProgressUpdate()
                }
                addMediaItemFromUrl(audio)
                //start playing first
                _audioState.value = AudioState.Playing(true)
                isStop.value = false
                exoPlayer.playWhenReady = true
                startProgressUpdate()
            }
            PlayerEvent.PlayPause -> playOrPause()
            //seek to the selected position
            PlayerEvent.SeekTo -> exoPlayer.seekTo(seekPosition)

            PlayerEvent.Stop -> {
                //Stop
                exoPlayer.stop()
                isStop.value = true
                stopProgressUpdate()
                //clear all
                exoPlayer.clearMediaItems()
                playlist.clear()
                _audioState.value = AudioState.PlaylistUpdated(playlist)
            }

            is PlayerEvent.UpdateProgress -> {
                exoPlayer.seekTo((exoPlayer.duration * playerEvent.newProgress).toLong())
            }

            PlayerEvent.LoopModeChange -> {
                exoPlayer.repeatMode = when(exoPlayer.repeatMode) {
                    Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
                    Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
                    else -> Player.REPEAT_MODE_OFF
                }
            }

            is PlayerEvent.AddToPlaylist -> {
                //check if no audio playing or audio is playing -> if no audio add to list and play
//                Log.e(TAG, "${isStop.value} && ${playlist}")
                if(isStop.value && playlist.isEmpty()){ //add to playlist and playing this audio
                    addMediaItemFromUrl(audio)
                    //start playing first
                    _audioState.value = AudioState.Playing(true)
                    isStop.value = false
                    exoPlayer.playWhenReady = true
                    startProgressUpdate()
                } else { //add to playlist only
                    addToPlaylist(audio)
                }
            }

            is PlayerEvent.RemoveFromPlayList -> {
                removeFromPlaylist(audio)
            }

            is PlayerEvent.PlayFromPlaylist -> {
                when(index){
                    exoPlayer.currentMediaItemIndex -> {
                        exoPlayer.seekToDefaultPosition()
                        playOrPause()
                    }
                    else -> {
                        exoPlayer.seekToDefaultPosition(index)
                        _audioState.value = AudioState.Playing(true)
                        exoPlayer.playWhenReady = true
                        startProgressUpdate()
                    }
                }
            }
            PlayerEvent.ClearPlaylist -> { //clear playlist and stop playing
                exoPlayer.stop()
                playlist.clear()
                exoPlayer.clearMediaItems()
                _audioState.value = AudioState.PlaylistUpdated(playlist)
                isStop.value = true
                stopProgressUpdate()
            }
            PlayerEvent.NextTrack -> {
                //seekToNext
                exoPlayer.seekToNext()
                //update Index
                _audioState.value = AudioState.PlaylistIndex(exoPlayer.currentMediaItemIndex)
            }
            PlayerEvent.PreviousTrack -> {
                exoPlayer.seekToPrevious()
                _audioState.value = AudioState.PlaylistIndex(exoPlayer.currentMediaItemIndex)
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
            _audioState.value = AudioState.PlaylistIndex(exoPlayer.currentMediaItemIndex)
            _audioState.value = AudioState.CurrentPlaying(playlist[exoPlayer.currentMediaItemIndex])
        }
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    @OptIn(DelicateCoroutinesApi::class)
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _audioState.value = AudioState.Playing(isPlaying = isPlaying)
        _audioState.value = AudioState.PlaylistIndex(exoPlayer.currentMediaItemIndex)
        _audioState.value = AudioState.CurrentPlaying(playlist[exoPlayer.currentMediaItemIndex])
        if(isPlaying){
            GlobalScope.launch(Dispatchers.Main){
                startProgressUpdate()
            }
        }else{
            stopProgressUpdate()
        }
    }

    //play or pauseRecording the audio
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

    //stopRecording the progress update of the audio
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
    object Forward: PlayerEvent()
    object Backward: PlayerEvent()
    object SeekTo: PlayerEvent()
    object Stop: PlayerEvent()
    object LoopModeChange: PlayerEvent()
    data class UpdateProgress(val newProgress: Float): PlayerEvent()

    //Playlist

    object NextTrack: PlayerEvent()
    object PreviousTrack: PlayerEvent()
    object AddToPlaylist: PlayerEvent()
    object RemoveFromPlayList: PlayerEvent()
    object PlayFromPlaylist: PlayerEvent()
    object ClearPlaylist: PlayerEvent()
}

//sealed class for the audio state
sealed class AudioState {
    object Initial: AudioState()
    data class PlaylistUpdated(val list: MutableList<Audio>): AudioState()
    data class PlaylistIndex(val index: Int): AudioState()
    data class Ready(val duration: Long): AudioState()
    data class Progress(val progress: Long): AudioState()
    data class Buffering(val progress: Long): AudioState()
    data class Playing(val isPlaying: Boolean): AudioState()
    data class CurrentPlaying(val audio: Audio): AudioState()
}