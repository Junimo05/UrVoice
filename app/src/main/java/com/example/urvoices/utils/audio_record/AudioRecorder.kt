package com.example.urvoices.utils.audio_record

import com.example.urvoices.data.db.Entity.AudioDes

interface AudioRecorder {
    fun start()
    suspend fun stop(filename: String)
    fun cancel()
    fun pause()
    fun resume()

    fun toItem(): AudioDes

    fun getAmplitude(): Int
}