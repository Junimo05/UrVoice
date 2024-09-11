package com.example.thevoices.utils.record

interface AudioRecorder {
    fun start()
    fun stop(filename: String)
}