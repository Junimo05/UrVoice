package com.example.urvoices.ui._component.PostComponent

import android.util.Log
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.urvoices.R
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.utils.waveform.AudioWaveform
import com.linc.audiowaveform.infiniteLinearGradient
import com.linc.audiowaveform.model.AmplitudeType
import com.linc.audiowaveform.model.WaveformAlignment

@Composable
fun AudioWaveformItem(
    id: String,
    audioUrl: String,
    audioAmplitudes: List<Int>?,
    currentPlayingAudio: Int,
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
    val TAG = "AudioWaveformItem"

    val colorBrush = SolidColor(MaterialTheme.colorScheme.onPrimary)
    val colorDone = SolidColor(MaterialTheme.colorScheme.surfaceVariant)
    val staticGradientBrush = Brush.linearGradient(colors = listOf(Color(0x500c8bde), Color(0xa10a90b8)))
    val animatedGradientBrush = Brush.infiniteLinearGradient(
        colors = listOf(Color(0x500c8bde), Color(0xa10a90b8)),
        animation = tween(durationMillis = 6000, easing = LinearEasing),
        width = 128F
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(62.dp)
            .width(240.dp)
            .background(MaterialTheme.colorScheme.inversePrimary)
            .border(1.dp, MaterialTheme.colorScheme.onBackground, MaterialTheme.shapes.small)
            .padding(8.dp)
            .then(modifier)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Todo: Button to play/pause audio

        IconButton(
            onClick = {
                  if(!isPlaying && isStop) {
                      onPlayStart()
                  } else {
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
                amplitudes = audioAmplitudes ?: listOf(),
                onProgressChange = {
                   if(isPlaying && currentPlayingPost == id) {
                       onPercentChange(it)
                   }
                },
                onProgressChangeFinished = {

                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AudioWaveformPreview() {
    MyTheme {
        AudioWaveformItem(
            id = "Post",
            isPlaying = true,
            duration = 100,
            percentPlayed = 0.5f,
            onPercentChange = {

            },
            onPlayStart = {

            },
            onPlayPause = {

            },
            isStop = false,
            currentPlayingAudio = 0,
            currentPlayingPost = "Post",
            audioUrl = "",
            audioAmplitudes = listOf(),
        )
    }
}