package com.example.urvoices.data

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
    suspend fun getAmplitudes(path: String): List<Int> = withContext(Dispatchers.IO) {
        return@withContext amplituda.processAudio(path, Cache.withParams(Cache.REUSE))
            .get(AmplitudaErrorListener {
                it.printStackTrace()
            })
            .amplitudesAsList()
    }
}