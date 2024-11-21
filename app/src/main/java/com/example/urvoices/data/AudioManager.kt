package com.example.urvoices.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.urvoices.utils.getRealPathFromUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import linc.com.amplituda.Amplituda
import linc.com.amplituda.Cache
import linc.com.amplituda.Compress
import linc.com.amplituda.callback.AmplitudaErrorListener
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.inject.Inject


class AudioManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val amplituda: Amplituda
){
    val TAG = "AudioManager"

    data class ConfigProcessing(
        val compressType: Int = Compress.AVERAGE,
        val cache: Int = Cache.REUSE
    )

    suspend fun getAmplitudes(url: String, id: String): List<Int> = withContext(Dispatchers.IO) {
        if(!isValidUrl(url) || url.isEmpty()){
            return@withContext emptyList<Int>()
        }
        val cacheFile = downloadFileToCache(context, url, id)
        try {
            // Process audio file
            return@withContext amplituda.processAudio(
                cacheFile.absolutePath,
                Cache.withParams(Cache.REUSE, id)
            )
                .get(
                    AmplitudaErrorListener {
                        it.printStackTrace()
                        Log.e("AudioManager", "AudioID: $id Error: ${it.message}")
                    }
                )
                .amplitudesAsList()
        } finally {
            // Delete file from cache
            cacheFile.delete()
        }
    }

    suspend fun getAmplitudes(uri: Uri): List<Int> = withContext(Dispatchers.IO) {
        //get File from Local Storage in app
        try {
            // Process audio file
            return@withContext amplituda.processAudio(
                getRealPathFromUri(context, uri),
                Compress.withParams(Compress.AVERAGE, 2)
            )
                .get(
                    AmplitudaErrorListener {
                        it.printStackTrace()
                        Log.e("AudioManager", "Error: ${it.message}")
                    }
                )
                .amplitudesAsList()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList<Int>()
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    private fun downloadFileToCache(context: Context, url: String, id: String): File {
        val cacheDir = context.cacheDir
        val cacheFile = File(cacheDir, "$id.mp3")
        URL(url).openStream().use { input ->
            FileOutputStream(cacheFile).use { output ->
                input.copyTo(output)
            }
        }
        return cacheFile
    }
    private fun generateAudioIdFromUrl(url: String): String {
        //audios%2FgqI0GasZJscy9Oj3ZfYY8J5Am1s1%2FTest?alt=media&token=fa90c757-42df-47b4-9468-92b3c1761377
        //Generate ID from URL
        return url.substringAfter("token=").substringBefore("&")
    }
}