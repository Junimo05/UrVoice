package com.example.urvoices.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.floor

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

fun timeStampToDuration(position: Long): String{
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

data class Time(
    val hours: Long,
    val minutes: Long,
    val seconds: Long
)

fun generateUniqueFileName(): String {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    return "IMG_$timeStamp.jpg"
}

fun saveBitmapToUri(context: Context, bitmap: Bitmap): Uri? {
    val bytes = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "CroppedImage", null)
    return if (path != null) Uri.parse(path) else null
}

fun deleteOldImageFile(context: Context, uri: Uri) {
    val file = File(uri.path!!)
    Log.e("MediaUtil", "deleteOldImageFile: ${file.path}")
    if (file.exists()) {
        file.delete()
    }
}