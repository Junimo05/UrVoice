package com.example.urvoices.utils.audio_player.services

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.urvoices.data.model.Audio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val playlistScope = CoroutineScope(SupervisorJob()  + Dispatchers.Main)

    //Playlist Management
    val playlist = mutableListOf<Audio>()
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()
    val mutex = Mutex()
    init {
        exoPlayer.addListener(this)
    }

    private suspend fun safeUpdateState(state: AudioState){
//        Log.e(TAG, "safeUpdateState: $state")
        mutex.withLock {
            _audioState.update { state }
        }
    }

    private suspend fun addToPlaylist(audio: Audio, index: Int = -1) {
        //check if have in playlist
        if(playlist.contains(audio)){
            return
        }
        playlistScope.launch {
            if (index == -1) {
                playlist.add(audio)
                exoPlayer.addMediaItem(MediaItem.fromUri(audio.url))
            } else {
                playlist[index] = audio
                exoPlayer.replaceMediaItem(index, MediaItem.fromUri(audio.url))
            }
            safeUpdateState(AudioState.PlaylistUpdated(playlist))
        }
    }

    private suspend fun removeFromPlaylist(index: Int) {
        playlistScope.launch {
            playlist.removeAt(index)
            exoPlayer.removeMediaItem(index)
            safeUpdateState(AudioState.PlaylistUpdated(playlist))
        }
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    suspend fun addMediaItemToStart(audio: Audio) {
        val mediaItem = MediaItem.fromUri(audio.url)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

        playlistScope.launch {
            playlist.add(0, audio)
            safeUpdateState(AudioState.PlaylistUpdated(playlist))
        }
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
        oldPosition: Int = 0,
        newPosition: Int = 0,
    ){
        when(playerEvent){
            //seek forward the audio
            PlayerEvent.Forward -> exoPlayer.seekForward()
            //seek backward the audio
            PlayerEvent.Backward -> exoPlayer.seekBack()
            //play or pauseRecording the audio
            PlayerEvent.StartPlaying -> {
                  if(playlist.isEmpty()){
                      //Case1: First Time Playing
                        addMediaItemToStart(audio)
                        //start playing first
                        safeUpdateState(AudioState.Playing(true))
                        isStop.value = false
                        exoPlayer.playWhenReady = true
                        startProgressUpdate()
                  } else {
                      val existingIndex = playlist.indexOfFirst { it.url == audio.url }
                      val currentPlaying = exoPlayer.currentMediaItemIndex
                      if(existingIndex != -1){
                            //Case2 : Audio is already in the playlist
                            exoPlayer.seekToDefaultPosition(existingIndex)
                            safeUpdateState(AudioState.PlaylistIndex(existingIndex))
                            exoPlayer.playWhenReady = true
                            startProgressUpdate()
                      } else {
                            //Case3: Audio is not in the playlist || Replace Current Playing Audio in Playlist
                            addToPlaylist(audio, currentPlaying)
                            exoPlayer.seekToDefaultPosition(currentPlaying)
                            safeUpdateState(AudioState.Playing(true))
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = true
                            startProgressUpdate()
                      }
                  }
            }
            PlayerEvent.PlayPause -> playOrPause()
            //seek to the selected position
            PlayerEvent.SeekTo -> exoPlayer.seekTo(seekPosition)

            PlayerEvent.Stop -> {
                //Stop
                exoPlayer.clearMediaItems()
                exoPlayer.stop()
                isStop.value = true
                stopProgressUpdate()
                //clear all
                playlist.clear()

                safeUpdateState(AudioState.Playing(false))
                playlistScope.launch {
                    safeUpdateState(AudioState.PlaylistUpdated(playlist))
                }
            }

            is PlayerEvent.UpdateProgress -> {
                exoPlayer.seekTo((exoPlayer.duration * playerEvent.newProgress).toLong())
            }

            PlayerEvent.PlayModeChange -> {
                val currentShuffleMode = exoPlayer.shuffleModeEnabled
                exoPlayer.repeatMode = when {
                    exoPlayer.repeatMode == Player.REPEAT_MODE_OFF -> {
                        //  REPEAT_ALL
                        safeUpdateState(AudioState.PlayMode(PlayMode.REPEAT_ALL))
                        exoPlayer.shuffleModeEnabled = false
                        Player.REPEAT_MODE_ALL
                    }
                    exoPlayer.repeatMode == Player.REPEAT_MODE_ALL && !currentShuffleMode -> {
                        //  REPEAT_ONE
                        safeUpdateState(AudioState.PlayMode(PlayMode.REPEAT_ONE))
                        Player.REPEAT_MODE_ONE
                    }
                    exoPlayer.repeatMode == Player.REPEAT_MODE_ONE && !currentShuffleMode -> {
                        // SHUFFLE ( repeat ALL)
                        safeUpdateState(AudioState.PlayMode(PlayMode.SHUFFLE))
                        exoPlayer.shuffleModeEnabled = true
                        Player.REPEAT_MODE_ALL
                    }
                    else -> {
                        //  OFF_MODE
                        safeUpdateState(AudioState.PlayMode(PlayMode.OFF_MODE))
                        exoPlayer.shuffleModeEnabled = false
                        Player.REPEAT_MODE_OFF
                    }
                }
            }


            is PlayerEvent.AddToPlaylist -> {
                //check if no audio playing or audio is playing -> if no audio add to list and play
                if(isStop.value && playlist.isEmpty()){ //add to playlist and playing this audio
                    addMediaItemToStart(audio)
                    //start playing first
                    safeUpdateState(AudioState.Playing(true))
                    isStop.value = false
                    exoPlayer.playWhenReady = true
                    startProgressUpdate()
                } else { //add to playlist only
                    addToPlaylist(audio, index)
                }
            }

            is PlayerEvent.RemoveFromPlayList -> {
                removeFromPlaylist(index)
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

            PlayerEvent.ReorderPlaylist -> {
                //change in ExoPlayer and update the playlist
                exoPlayer.moveMediaItem(oldPosition, newPosition) //move media item in exoPlayer
                //remember to update playing state
                safeUpdateState(AudioState.PlaylistIndex(exoPlayer.currentMediaItemIndex))
                playlistScope.launch {
                    playlist.add(newPosition, playlist.removeAt(oldPosition))
                    safeUpdateState(AudioState.PlaylistUpdated(playlist))
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
            Player.STATE_ENDED -> {
                _audioState.value = AudioState.Ending(true)
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
    object PlayModeChange: PlayerEvent()
    object ReorderPlaylist:PlayerEvent()
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
    data class PlaylistUpdated(val list: List<Audio>): AudioState()
    data class PlaylistIndex(val index: Int): AudioState()
    data class PlayMode(val mode: String) : AudioState()
    data class Ready(val duration: Long): AudioState()
    data class Progress(val progress: Long): AudioState()
    data class Buffering(val progress: Long): AudioState()
    data class Playing(val isPlaying: Boolean): AudioState()
    data class Ending(val isEnd: Boolean): AudioState()
    data class CurrentPlaying(val audio: Audio): AudioState()
}

object PlayMode{
    val OFF_MODE = "OFF_MODE"
    val REPEAT_ONE = "REPEAT_ONE"
    val REPEAT_ALL = "REPEAT_ALL"
    val SHUFFLE = "SHUFFLE"
}