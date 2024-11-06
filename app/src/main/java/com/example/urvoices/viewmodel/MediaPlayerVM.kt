package com.example.urvoices.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MediaPlayerVM @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioService: AudioServiceHandler,
    saveStateHandle: SavedStateHandle
): ViewModel(){
    val TAG = "MediaPlayerViewModel"

    @OptIn(SavedStateHandleSaveableApi::class)
    var duration by saveStateHandle.saveable { mutableLongStateOf(0L) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var progress by saveStateHandle.saveable { mutableFloatStateOf(0F) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var progressString by saveStateHandle.saveable { mutableStateOf("00:00") }
    val isStop: StateFlow<Boolean> = audioService.isStop.asStateFlow()
    @OptIn(SavedStateHandleSaveableApi::class)
    var isPlaying by saveStateHandle.saveable { mutableStateOf(false) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var currentPlayingAudio by saveStateHandle.saveable { mutableIntStateOf(0) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var currentPlayingPost by saveStateHandle.saveable { mutableStateOf("") }
    @OptIn(SavedStateHandleSaveableApi::class)
    var repeatMode by saveStateHandle.saveable { mutableIntStateOf(Player.REPEAT_MODE_OFF) }
    var isServiceRunning = false

    //UI STATE
    val _uiState: MutableStateFlow<UIStates> = MutableStateFlow(UIStates.Initial)
    val uiState: StateFlow<UIStates> = _uiState.asStateFlow()

    init{
        viewModelScope.launch {
            audioService.audioState.collectLatest {state ->
                when(state){
                    AudioState.Initial -> _uiState.value = UIStates.Initial
                    is AudioState.Buffering -> setProgressValue(state.progress)
                    is AudioState.Playing -> isPlaying = state.isPlaying
                    is AudioState.Progress -> setProgressValue(state.progress)
                    is AudioState.CurrentPlaying -> {
                        currentPlayingAudio = state.mediaItemIndex
                    }
                    is AudioState.Ready -> {
                        duration = state.duration
                        _uiState.value = UIStates.Ready
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
                audioService.onPlayerEvents(
                    PlayerEvent.StartPlaying,
                    url = uiEvents.url
                )
            }
            UIEvents.Stop -> {
                audioService.onPlayerEvents(
                    PlayerEvent.Stop
                )
            }
            is UIEvents.PlayingAudioChange -> {
                audioService.onPlayerEvents(
                    PlayerEvent.SelectedAudioChange,
                    selectedAudioIndex = uiEvents.index
                )
            }
            is UIEvents.AddMediaItemToQueue -> {
                audioService.onPlayerEvents(
                    PlayerEvent.AddToQueue,
                    url = uiEvents.uri
                )
            }
            is UIEvents.DeleteMediaItemFromQueue -> {
                audioService.onPlayerEvents(
                    PlayerEvent.DeleteFromQueue,
                    index = uiEvents.index
                )
            }
            is UIEvents.SeekTo -> {
                audioService.onPlayerEvents(
                    PlayerEvent.SeekTo,
                    seekPosition = (duration * uiEvents.position).toLong()
                )
            }
            UIEvents.SeekToNext -> audioService.onPlayerEvents(PlayerEvent.SeekToNext)
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
        }
    }

    fun updateCurrentPlayingPost(post: String){
        currentPlayingPost = post
    }

    private fun startBackGroundService() {
        if (!isServiceRunning) {
            val intent = Intent(context, AudioService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            isServiceRunning = true
        }
    }

    private fun setProgressValue(currentProgress: Long){
        progress =
            if (currentProgress > 0) ((currentProgress.toFloat() / duration.toFloat()) * 1f)
            else 0f
        progressString = formatDurationString(currentProgress)
    }
    @SuppressLint("DefaultLocale")
    fun formatDurationString(duration: Long): String {
        val minute = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
        val seconds = (minute) - minute * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES)
        return String.format("%02d:%02d", minute, seconds)
    }
}

sealed class UIEvents {
    data class PlayingAudio(val url: String): UIEvents()
    data class PlayingAudioChange(val index: Int): UIEvents()
    object Forward: UIEvents()
    object Backward: UIEvents()
    data class SeekTo(val position: Float): UIEvents()
    object SeekToNext: UIEvents()
    object PlayPause: UIEvents()
    object Stop: UIEvents()
    object LoopModeChange: UIEvents()
    data class AddMediaItemToQueue(val uri: String): UIEvents()
    data class DeleteMediaItemFromQueue(val index: Int): UIEvents()
    data class UpdateProgress(val newProgress: Float): UIEvents()
}

sealed class UIStates {
    object Initial: UIStates()
    object Ready: UIStates()
    object Error: UIStates()
}