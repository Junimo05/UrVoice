package com.example.urvoices.ui._component.PostComponent

import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.urvoices.R
import com.example.urvoices.ui._component.waveform.AudioWaveform
import com.linc.audiowaveform.infiniteLinearGradient
import com.linc.audiowaveform.model.AmplitudeType
import com.linc.audiowaveform.model.WaveformAlignment

@SuppressLint("UnrememberedMutableState")
@Composable
fun AudioItem(
    id: String,
    audioUrl: String,
    audioAmplitudes: List<Int>?,
    currentPlayingAudio: String,
    currentPlayingPost: String,
    onPlayStart: () -> Unit,
    onPlayPause: () -> Unit,
    percentPlayed: Float,
    onPercentChange: (Float) -> Unit,
    isPlaying: Boolean,
    isStop: Boolean,
    duration: Long,
    modifier: Modifier = Modifier
) {
    val TAG = "AudioItem"
    val colorBrush = SolidColor(MaterialTheme.colorScheme.onPrimary)
    val colorDone = SolidColor(MaterialTheme.colorScheme.surfaceVariant)
    val animatedGradientBrush = Brush.infiniteLinearGradient(
        colors = listOf(Color(0x500c8bde), Color(0xa10a90b8)),
        animation = tween(durationMillis = 6000, easing = LinearEasing),
        width = 128F
    )

    val loadedWaveForm = remember {
        mutableStateOf(false)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(62.dp)
            .width(240.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.inversePrimary)
            .border(1.dp, MaterialTheme.colorScheme.onBackground, MaterialTheme.shapes.small)
            .padding(8.dp)
            .alpha(
                if(loadedWaveForm.value) 1F
                else 0F
            )
            .then(modifier)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                if(currentPlayingPost != id) {
                    // Play new audio
                    onPlayStart()
                } else {
                    // Toggle play/pauseRecording current audio
                    onPlayPause()
                }
            },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                painter = if(currentPlayingPost == id && isPlaying) painterResource(id = R.drawable.ic_media_stop) else painterResource(id = R.drawable.ic_media_play),
                contentDescription = "PlayOrPause"
            )
        }
        Box(modifier = Modifier.weight(1f)) {
//            Log.e(TAG, "AudioItem of PostID : $id and AudioUrl: $audioUrl")
            AudioWaveform(
                // Spike DrawStyle: Fill or Stroke
                style = Fill,
                waveformAlignment = WaveformAlignment.Center,
                amplitudeType = AmplitudeType.Avg,
                // Colors could be updated with Brush API
                progressBrush = animatedGradientBrush,
                waveformBrush = colorBrush,
                spikeWidth = 4.dp,
                spikePadding = 2.dp,
                spikeRadius = 4.dp,
                progress = if(!isStop && currentPlayingPost == id) percentPlayed else 0F,
                amplitudes = audioAmplitudes!!,
                onProgressChange = {
                    if(isPlaying && currentPlayingPost == id) {
                        onPercentChange(it)
                    }
                },
                onProgressChangeFinished = {

                },
                loading = loadedWaveForm,
                onLoadingComplete = {
                    loadedWaveForm.value = true
                }
            )
        }
    }
}