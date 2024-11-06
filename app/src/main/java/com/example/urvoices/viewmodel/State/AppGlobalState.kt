package com.example.urvoices.viewmodel.State

import androidx.compose.runtime.mutableStateOf
import com.example.urvoices.viewmodel.RecorderState
import com.example.urvoices.viewmodel.UIStates

object AppGlobalState {
    //Recording States
    var recorderState = mutableStateOf<RecorderState>(RecorderState.Idle)
    var isRecording = mutableStateOf(false)


    //Media States
    var mediaState = mutableStateOf<UIStates>(UIStates.Initial)
    var isPlaying = mutableStateOf(false)
    var isStop = mutableStateOf(false)
}