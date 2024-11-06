package com.example.urvoices.utils.audio_record

import android.content.ContentValues
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import com.example.urvoices.data.db.Entity.AudioDes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class AndroidAudioRecorder @Inject constructor(
    private val context: Context,
): AudioRecorder {
    private var recorder: MediaRecorder? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    var tempFile = File(context.cacheDir, "temp_audio.mp3")

    var isPaused = mutableStateOf(false)
    private var isRecording = mutableStateOf(false)

    private fun createRecorder(): MediaRecorder {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else MediaRecorder()
    }

    override fun startRecording() {
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(tempFile.absolutePath)

            try {
                prepare()
                start()
                recorder = this
                isRecording.value = true
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun stopRecording() {
        try {
            isRecording.value = false
            recorder?.stop()
            recorder?.reset()
            recorder?.release()
            recorder = null
        } finally {
            isPaused.value = false
        }
    }

    override fun cancelRecording() {
        isRecording.value = false
        isPaused.value = false

        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        tempFile.delete()
    }

    override fun pauseRecording() {
        recorder?.pause()
        isPaused.value = true
    }

    override fun resumeRecording() {
        recorder?.resume()
        isPaused.value = false
    }

    // Clean up resources
    override fun releaseRecording() {
        scope.cancel()
        recorder?.release()
        recorder = null
    }

    fun getMaxAmplitude(): Int {
        return recorder?.maxAmplitude ?: 0
    }

    //File
    fun getAudioFilePath(): String {
        return tempFile.absolutePath
    }

    fun getAudioFile(): File {
        return tempFile
    }

    fun getAudioFileUri(): Uri {
        return Uri.fromFile(tempFile)
    }

    override fun toItem(): AudioDes {
        TODO("Not yet implemented")
    }

}



