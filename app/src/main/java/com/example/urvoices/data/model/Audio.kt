package com.example.urvoices.data.model

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import kotlinx.parcelize.Parcelize

@Parcelize
data class Audio(
    val id: String,
    val url: String,
    val title: String,
    val author: String,
    val duration: Long
): Parcelable