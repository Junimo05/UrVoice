package com.example.urvoices.utils.audio_record

import com.example.urvoices.data.db.Entity.AudioDes

interface AudioRecorder {
    fun startRecording()
    fun stopRecording()
    fun cancelRecording()
    fun pauseRecording()
    fun resumeRecording()

    fun releaseRecording()

    fun toItem(): AudioDes
}