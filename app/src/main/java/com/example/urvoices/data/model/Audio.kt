package com.example.urvoices.data.model

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import kotlinx.parcelize.Parcelize

@Parcelize
data class Audio(
    val url: String,
    val displayName: String,
    val id: String,
    val amplitudes: List<Int>,
    val type: String,
    val duration: Long,
    val audioCreated: Long,
    val audioSize: Long,
): Parcelable {
    companion object {
        val DiffCallback = object: DiffUtil.ItemCallback<Audio>() {
            override fun areItemsTheSame(oldItem: Audio, newItem: Audio): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Audio, newItem: Audio): Boolean {
                return oldItem == newItem
            }
        }
    }
}