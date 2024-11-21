package com.example.urvoices.viewmodel

import android.Manifest
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urvoices.utils.audio_record.AndroidAudioRecorder
import com.example.urvoices.viewmodel.State.AppGlobalState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaRecorderVM @Inject constructor(
    @ApplicationContext context: Context,
    private val audioRecorder: AndroidAudioRecorder,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val permission = arrayOf(Manifest.permission.RECORD_AUDIO)
    private val _recorderState = MutableStateFlow<RecorderState>(RecorderState.Idle)
    val recorderState = _recorderState.asStateFlow()

    var recordingTime = mutableLongStateOf(0L)

    val amplitudesLive = mutableStateListOf<Int>()
    val maxAmplitude = 100


    private val handler = Handler(Looper.getMainLooper())

    //Amplitudes
    fun clearAmplitudes() = amplitudesLive.clear()

    //File
    fun getFileUri() = audioRecorder.getAudioFileUri()
    fun getFilePath() = audioRecorder.getAudioFilePath()
    fun deleteFile() = audioRecorder.tempFile.delete()


    init {
        viewModelScope.launch {
            _recorderState.collect{ state ->
                AppGlobalState.recorderState.value = state
                AppGlobalState.isRecording.value = state is RecorderState.Recording
            }
        }
    }


    //Recorder
    fun startRecording() {
        audioRecorder.startRecording()
        _recorderState.value = RecorderState.Recording

        //
        handler.postDelayed(this::updateTime, 1000)
        handler.postDelayed(this::updateAmplitude, 100)
    }

    fun pauseRecording() {
        audioRecorder.pauseRecording()
        _recorderState.value = RecorderState.Paused
    }

    fun resumeRecording() {
        audioRecorder.resumeRecording()
        _recorderState.value = RecorderState.Recording
        handler.postDelayed(this::updateTime, 1000)
        handler.postDelayed(this::updateAmplitude, 100)
    }

    fun stopRecording() {
        //Stop Recording
        audioRecorder.stopRecording()
        recordingTime.longValue = 0L
        clearAmplitudes()
        _recorderState.value = RecorderState.Uploading
        viewModelScope.launch {
            delay(3000)
            _recorderState.value = RecorderState.Idle
        }
    }

    fun cancelRecording() {
        audioRecorder.cancelRecording()
        recordingTime.longValue = 0L
        clearAmplitudes()
        _recorderState.value = RecorderState.Idle
    }

    private fun updateAmplitude(){
        if(recorderState.value != RecorderState.Recording) return
        audioRecorder.getMaxAmplitude().let {
            if(amplitudesLive.size > maxAmplitude) amplitudesLive.removeFirst()
            amplitudesLive.add(it)
        }
        handler.postDelayed(this::updateAmplitude, 100)
    }

    private fun updateTime(){
        if(recorderState.value != RecorderState.Recording) return

        recordingTime.longValue += 1000
        handler.postDelayed(this::updateTime, 1000)
    }

    override fun onCleared(){
        super.onCleared()
        audioRecorder.cancelRecording()
    }

    fun resetState(){
        _recorderState.value = RecorderState.Idle
    }
    //MediaPlayer After Recording Actions


}


fun Int.toPx(displayMetrics: DisplayMetrics): Float { return this * displayMetrics.density }
fun Float.toPx(displayMetrics: DisplayMetrics): Float { return this * displayMetrics.density }

sealed class RecorderState {
    object Idle : RecorderState()
    object Recording : RecorderState()
    object Paused : RecorderState()
    object Uploading : RecorderState()
    data class Error(val message: String) : RecorderState()

}