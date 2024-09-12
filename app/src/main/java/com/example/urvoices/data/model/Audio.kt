package com.example.urvoices.data.model

import android.net.Uri
import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import kotlinx.parcelize.Parcelize

@Parcelize
data class Audio(
    val uri: Uri,
    val displayName: String,
    val id: Long,
    val artist: String,
    val data: String,
    val duration: Int,
    val title: String,
    val audioCreated: Long,
    val audioSize: Long,
    val audioFavorite: Boolean,
    var audioAmplitudes: List<Float> = emptyList()
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