package com.example.urvoices.utils

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

