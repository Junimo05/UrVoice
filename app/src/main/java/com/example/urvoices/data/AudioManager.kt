package com.example.urvoices.data

import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import linc.com.amplituda.Amplituda
import linc.com.amplituda.Cache
import linc.com.amplituda.callback.AmplitudaErrorListener
import javax.inject.Inject


class AudioManager @Inject constructor(
    private val amplituda: Amplituda
){
    suspend fun getAmplitudes(path: String, id: String): List<Int> = withContext(Dispatchers.IO) {
        return@withContext amplituda.processAudio(
            path,
            Cache.withParams(Cache.REUSE, id)
        )
            .get(
                AmplitudaErrorListener {
                    it.printStackTrace()
//                    Log.e("AudioManager", "AudioID: $id Error: ${it.message}")
                }
            )
            .amplitudesAsList()
    }
}