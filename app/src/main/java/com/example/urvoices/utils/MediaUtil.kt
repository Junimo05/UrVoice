package com.example.urvoices.utils

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import wseemann.media.FFmpegMediaMetadataRetriever
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.floor
import android.database.Cursor
import android.provider.MediaStore
import android.provider.DocumentsContract


fun getRealPathFromUri(context: Context, contentUri: Uri): String? {
    var filePath: String? = null
    try {
        context.contentResolver.openInputStream(contentUri)?.use { inputStream ->
            val fileName = getFileName(context, contentUri)
            val file = File(context.cacheDir, fileName)

            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }

            filePath = file.absolutePath
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return filePath
}

fun checkHttps(url: String): Boolean {
    return if (url.startsWith("https://") || url.startsWith("http://")) {
        true
    } else {
       false
    }
}

private fun getFileName(context: Context, uri: Uri): String {
    var fileName = "file"

    // Try to get the display name from the document
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (displayNameIndex != -1) {
                fileName = cursor.getString(displayNameIndex)
            }
        }
    }

    // If we couldn't get a name, generate one with timestamp
    if (fileName == "file") {
        fileName = "file_${System.currentTimeMillis()}"
    }

    return fileName
}

fun formatFileSize(size: Long): String {
    val kilobyte: Long = 1024
    val megabyte = kilobyte * 1024
    val gigabyte = megabyte * 1024
    val terabyte = gigabyte * 1024

    return when {
        size < kilobyte -> "$size B"
        size < megabyte -> "${size / kilobyte} KB"
        size < gigabyte -> "${size / megabyte} MB"
        size < terabyte -> "${size / gigabyte} GB"
        else -> "${size / terabyte} TB"
    }
}

fun getDurationFromUrl(url: String): Long{
    val mediaMetadataRetriever = FFmpegMediaMetadataRetriever()
    try {
        mediaMetadataRetriever.setDataSource(url)
        if (mediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION) == null) return 0L
        return mediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        mediaMetadataRetriever.release()
    }
    return 0L
}

fun getTimeElapsed(createdAt: Long): String {
    val currentTime = System.currentTimeMillis()
    val timeDifference = currentTime - createdAt

    // Convert the time difference to seconds, minutes, hours, or days as needed
    val seconds = timeDifference / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7
    val months = days / 30
    val years = days / 365
    // Return a string that represents the time elapsed since the post or notification was created
    return when {
        years > 0 -> "$years năm trước"
        months > 0 -> "$months tháng trước"
        weeks > 0 -> "$weeks tuần trước"
        days > 0 -> "$days ngày trước"
        hours > 0 -> "$hours giờ trước"
        minutes > 0 -> "$minutes phút trước"
        else -> "$seconds giây trước"
    }
}

fun getDayTime(time: Long): String {
    val date = Date(time)
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(date)
}


fun formatToMinSecFromMillisec(position: Long): String{
    val totalSec = floor(position / 1E3).toInt()
    val minutes = totalSec / 60
    val remainingSec = totalSec - minutes * 60
    return if(position < 0) "--.--"
    else if (remainingSec < 10) "$minutes:0$remainingSec"
    else "$minutes:$remainingSec"
}

fun processUsername(username: String): String {
    return username.replace(" ", "").lowercase()
}

fun generateUniqueFileName(): String {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    return "IMG_$timeStamp.jpg"
}

fun saveBitmapToUri(context: Context, bitmap: Bitmap, fileName: String): Uri? {
    val file = File(context.cacheDir, fileName)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
    }
    return Uri.fromFile(file)
}

fun deleteOldImageFile(uri: Uri) {
    val file = File(uri.path!!)
//    Log.e("MediaUtil", "deleteOldImageFile: ${file.path}")
    if (file.exists()) {
        file.delete()
    }
}

