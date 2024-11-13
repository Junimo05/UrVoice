package com.example.urvoices.data.db.Entity

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.urvoices.data.model.Audio

@Entity(tableName = "audio")
data class AudioDes(
    @PrimaryKey(autoGenerate = true) var id: String,
    @ColumnInfo(name = "audio_name") var audioName: String,
    @ColumnInfo(name = "audio_duration") var audioDuration: Long,
    @ColumnInfo(name = "audio_path") var audioPath: String,
    @ColumnInfo(name = "audio_created") var audioCreated: Long,
    @ColumnInfo(name = "audio_removed") var audioRemoved: Long,
    @ColumnInfo(name = "audio_size") var audioSize: Long,
    @ColumnInfo(name = "audio_type") var audioType: String,
    @ColumnInfo(name = "audio_waveform_processed") var audioWaveformProcessed: Boolean,
    @ColumnInfo(name = "audio_bookmarked") var audioFavorite: Boolean,
)
