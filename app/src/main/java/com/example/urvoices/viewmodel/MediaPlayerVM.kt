package com.example.urvoices.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.media3.common.Player
import com.example.urvoices.data.model.Audio
import com.example.urvoices.utils.audio_player.services.AudioService
import com.example.urvoices.utils.audio_player.services.AudioServiceHandler
import com.example.urvoices.utils.audio_player.services.AudioState
import com.example.urvoices.utils.audio_player.services.PlayerEvent
import com.example.urvoices.viewmodel.State.AppGlobalState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@SuppressLint("MutableCollectionMutableState")
@HiltViewModel
class MediaPlayerVM @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioService: AudioServiceHandler,
    saveStateHandle: SavedStateHandle
): ViewModel(){
    val TAG = "MediaPlayerViewModel"

    val currentAudio = mutableStateOf(
        Audio(
            id = "", //post id
            title = "", //audio name
            url = "",  //audio url
            author = "", //username
            duration = 0L //audio duration
    ))


    @OptIn(SavedStateHandleSaveableApi::class)
    var playlist by saveStateHandle.saveable{mutableStateOf(listOf<Audio>())}
    @OptIn(SavedStateHandleSaveableApi::class)
    var currentPlayingIndex by saveStateHandle.saveable{mutableIntStateOf(-1)}
    @OptIn(SavedStateHandleSaveableApi::class)
    var durationPlayer by saveStateHandle.saveable { mutableLongStateOf(0L) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var progress by saveStateHandle.saveable { mutableFloatStateOf(0F) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var progressString by saveStateHandle.saveable { mutableStateOf("00:00") }
    val isStop: StateFlow<Boolean> = audioService.isStop.asStateFlow()
    @OptIn(SavedStateHandleSaveableApi::class)
    var isPlaying by saveStateHandle.saveable { mutableStateOf(false) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var isEnd by saveStateHandle.saveable { mutableStateOf(false) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var repeatMode by saveStateHandle.saveable { mutableIntStateOf(Player.REPEAT_MODE_OFF) }
    var isServiceRunning = false

    //UI STATE
    val _uiState: MutableStateFlow<UIStates> = MutableStateFlow(UIStates.Initial)
    val uiState: StateFlow<UIStates> = _uiState.asStateFlow()

    init{
        viewModelScope.launch {
            audioService.audioState
                .collect {state ->
                when(state){
                    AudioState.Initial -> _uiState.value = UIStates.Initial
                    is AudioState.Buffering -> setProgressValue(state.progress)
                    is AudioState.Playing -> {
                        isPlaying = state.isPlaying
                    }
                    is AudioState.Ending -> {
                        isEnd = state.isEnd
                    }
                    is AudioState.Progress -> setProgressValue(state.progress)
                    is AudioState.CurrentPlaying -> {
//                        Log.e(TAG, "Current Playing: ${state.audio}")
                        currentAudio.value = state.audio
                    }
                    is AudioState.Ready -> {
                        durationPlayer = state.duration
                        isEnd = false
                        _uiState.value = UIStates.Ready
                    }

                    is AudioState.PlaylistIndex -> {
                        currentPlayingIndex = state.index
                    }
                    is AudioState.PlaylistUpdated -> {
//                        Log.e(TAG, "Playlist Updated Run: ${state.list}")
//                        Log.e(TAG, "Playlist Updated: ${state.list}")
                        val oldList = playlist
                        updatePlaylist(state.list.toMutableList())
                        if (oldList.isEmpty() && playlist.isNotEmpty()){ //First Play
                            Toast.makeText(context, "Start Playing", Toast.LENGTH_SHORT).show()
                        } else if(oldList.isNotEmpty() && playlist.isEmpty()) {
                            Toast.makeText(context, "Clear Playlist", Toast.LENGTH_SHORT).show()
                        } else if (oldList.size < playlist.size){ //Playlist is available : Added
                            Toast.makeText(context, "Added to playlist", Toast.LENGTH_SHORT).show()
                        } else if(oldList.size > playlist.size){ //Playlist is available : Removed
                            Toast.makeText(context, "Removed from playlist", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                AppGlobalState.mediaState.value = _uiState.value
                AppGlobalState.isPlaying.value = isPlaying
                AppGlobalState.isStop.value = isStop.value
            }
        }
    }

    fun onUIEvents(uiEvents: UIEvents) = viewModelScope.launch {
        when(uiEvents){
            UIEvents.PlayPause -> {
                audioService.onPlayerEvents(
                    PlayerEvent.PlayPause
                )
            }
            is UIEvents.PlayingAudio -> {
                startBackGroundService()
                val audio = uiEvents.audio
                audioService.onPlayerEvents(
                    PlayerEvent.StartPlaying,
                    audio = audio
                )
            }
            UIEvents.Stop -> {
                audioService.onPlayerEvents(
                    PlayerEvent.Stop
                )
                //reset all data to initial
                clearAllVMData()
            }
            is UIEvents.SeekTo -> {
                audioService.onPlayerEvents(
                    PlayerEvent.SeekTo,
                    seekPosition = (durationPlayer * uiEvents.position).toLong()
                )
            }
            is UIEvents.UpdateProgress -> {
                audioService.onPlayerEvents(
                    PlayerEvent.UpdateProgress(
                        uiEvents.newProgress
                    )
                )
                progress = uiEvents.newProgress
            }
            UIEvents.LoopModeChange -> {
                audioService.onPlayerEvents(PlayerEvent.LoopModeChange)
                repeatMode = audioService.getRepeatMode()
            }
            UIEvents.Backward -> {
                audioService.onPlayerEvents(PlayerEvent.Backward)
            }
            UIEvents.Forward -> {
                audioService.onPlayerEvents(PlayerEvent.Forward)
            }
            is UIEvents.PlaySelectedFromList -> {
                val index = uiEvents.index
                audioService.onPlayerEvents(
                    PlayerEvent.PlayFromPlaylist,
                    index = index
                )
            }
            is UIEvents.AddToPlaylist -> {
                audioService.onPlayerEvents(
                    PlayerEvent.AddToPlaylist,
                    audio = uiEvents.audio,
                    index = uiEvents.index
                )
            }
            is UIEvents.RemoveFromPlaylist -> {
                audioService.onPlayerEvents(
                    PlayerEvent.RemoveFromPlayList,
                    index = uiEvents.index
                )
            }
            UIEvents.NextAudio -> {
                audioService.onPlayerEvents(PlayerEvent.NextTrack)

            }
            UIEvents.PreviousAudio -> {
                audioService.onPlayerEvents(PlayerEvent.PreviousTrack)
            }

            is UIEvents.ReorderPlaylist -> { //After Change Position of A Audio in List
                val oldPosition = uiEvents.oldPosition
                val newPosition = uiEvents.newPosition
                audioService.onPlayerEvents(
                    PlayerEvent.ReorderPlaylist,
                    oldPosition = oldPosition,
                    newPosition = newPosition
                )
            }
        }
    }
    private fun updatePlaylist(list: MutableList<Audio>) {
        playlist = list
    }


    private fun startBackGroundService() {
        if (!isServiceRunning) {
            val intent = Intent(context, AudioService::class.java)
            context.startForegroundService(intent)
            isServiceRunning = true
        }
    }

    private fun setProgressValue(currentProgress: Long){
        progress =
            if (currentProgress > 0) ((currentProgress.toFloat() / durationPlayer.toFloat()) * 1f)
            else 0f
        progressString = formatDurationString(currentProgress)
    }

    fun clearAllVMData(){
        currentAudio.value = Audio(
            id = "", //post id
            title = "", //audio name
            url = "",  //audio url
            author = "", //username
            duration = 0L //audio duration
        )
        currentPlayingIndex = -1
        durationPlayer = 0L
        progress = 0f
        progressString = "00:00"
        isPlaying = false
        repeatMode = Player.REPEAT_MODE_OFF
        isServiceRunning = false
    }

    @SuppressLint("DefaultLocale")
    fun formatDurationString(duration: Long): String {
        val minute = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
        val seconds = (minute) - minute * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES)
        return String.format("%02d:%02d", minute, seconds)
    }
}

sealed class UIEvents {
    data class PlayingAudio(val audio: Audio): UIEvents()
    data class PlaySelectedFromList(val index: Int): UIEvents()
    data class AddToPlaylist(val audio: Audio, val index: Int = -1): UIEvents()
    data class RemoveFromPlaylist(val index: Int): UIEvents()
    data class ReorderPlaylist(val oldPosition: Int, val newPosition: Int): UIEvents()
    object NextAudio: UIEvents()
    object PreviousAudio: UIEvents()
    object Forward: UIEvents()
    object Backward: UIEvents()
    data class SeekTo(val position: Float): UIEvents()
    object PlayPause: UIEvents()
    object Stop: UIEvents()
    object LoopModeChange: UIEvents()
    data class UpdateProgress(val newProgress: Float): UIEvents()
}

sealed class UIStates {
    object Initial: UIStates()
    object Ready: UIStates()
    object Error: UIStates()
}