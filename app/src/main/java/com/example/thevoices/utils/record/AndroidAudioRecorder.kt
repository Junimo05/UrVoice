package com.example.thevoices.utils.record

import android.content.ContentValues
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.mutableLongStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class AndroidAudioRecorder @Inject constructor(
    private val context: Context,
    private val reloadData: () -> Unit
): AudioRecorder {

    private var recorder: MediaRecorder? = null
    private var tempFile = File(context.cacheDir, "temp_audio.mp3")
    private var targetUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private val scope = CoroutineScope(Dispatchers.Main)
    var recordingTime = mutableLongStateOf(0L)

    var isPaused: Boolean = false
    private var isStop: Boolean = false

    private fun createRecorder(): MediaRecorder {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else MediaRecorder()
    }

    override fun start() {
        isStop = false
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(tempFile.absolutePath)

            prepare()
            start()

            recorder = this
        }

        scope.launch {
            while (recorder != null) {
                delay(1000L)
                if(!isPaused) {
                    recordingTime.longValue += 1000
                }
            }
        }

    }

    override fun stop(filename: String) {
        isStop = true
        recorder?.stop()
        recorder?.reset()
        recorder = null
        scope.launch {
            saveAudioFile(filename)
            delay(1000L)
            reloadData()
        }
        recordingTime.longValue = 0
    }

    fun cancel() {
        isStop = true
        recordingTime.longValue = 0
        recorder?.stop()
        recorder?.reset()
        recorder = null
        tempFile.delete()
    }

    fun pause() {
        recorder?.pause()
        isPaused = true
    }

    fun resume() {
        isPaused = false
        recorder?.resume()

    }

    private fun saveAudioFile(filename: String) { //Todo: Change save audio file
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3")
        }

        val uri = resolver.insert(targetUri, contentValues)

        uri?.let {
            val outputStream = resolver.openOutputStream(it)
            val inputStream = tempFile.inputStream()

            copyStream(inputStream, outputStream)

            inputStream.close()
            outputStream?.close()
        }
        tempFile.delete()
    }

    private fun copyStream(input: InputStream, output: OutputStream?) {
        val buffer = ByteArray(1024)
        var read: Int
        while (input.read(buffer).also { read = it } != -1) {
            output?.write(buffer, 0, read)
        }
    }
}

