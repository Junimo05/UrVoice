package com.example.urvoices.utils

import android.os.Handler
import android.os.Looper

class TimerHandler {
    var milliseconds = 0L
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            milliseconds += 10
            handler.postDelayed(this, 10)
        }
    }

    fun start() {
        handler.postDelayed(runnable, 10)
    }

    fun pause() {
        handler.removeCallbacks(runnable)
    }

    fun resume() {
        handler.postDelayed(runnable, 10)
    }

    fun stop() {
        handler.removeCallbacks(runnable)
    }

    fun clear(){
        milliseconds = 0L
    }
}