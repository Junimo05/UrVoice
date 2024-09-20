package com.example.urvoices.ui._component.PostComponent

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.example.urvoices.R
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.utils.waveform.AudioWaveform
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.linc.audiowaveform.infiniteLinearGradient
import com.linc.audiowaveform.model.AmplitudeType
import com.linc.audiowaveform.model.WaveformAlignment

@Composable
fun AudioWaveformItem(
    //exoPlayer: ExoPlayer,
    percentPlayed: Float,
    initAmplitudes: List<Int> = listOf(
        45, 23, 67, 89, 12, 34, 56, 78, 90, 11, 22, 33, 44, 55, 66, 77, 88,
        99, 10, 20, 30, 40, 50, 60, 70, 80, 91, 92, 93, 94, 95, 96, 97, 98,
        1, 2, 3, 4, 5, 6, 7, 8, 9, 13, 14, 15, 16, 17, 18, 19, 21, 24, 25,
        26, 27, 28, 29, 31, 32, 35, 36, 37, 38, 39, 41, 42, 43, 46, 47, 48,
        49, 51, 52, 53, 54, 57, 58, 59, 61, 62, 63, 64, 65, 68, 69, 71, 72,
        73, 74, 75, 76, 79, 81, 82, 83, 84, 85, 86, 87, 100
    ),
    redrawTrigger: Int = 0,
    waveformProgress: MutableState<Float> = remember {
        mutableStateOf(0F)
    },
    isPlaying: Boolean,
    duration: String,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    var amplitudes by remember { mutableStateOf(initAmplitudes) }
    val colorBrush = SolidColor(MaterialTheme.colorScheme.onPrimary)
    val colorDone = SolidColor(MaterialTheme.colorScheme.surfaceVariant)
    val staticGradientBrush = Brush.linearGradient(colors = listOf(Color(0xff22c1c3), Color(0xfffdbb2d)))
    val animatedGradientBrush = Brush.infiniteLinearGradient(
        colors = listOf(Color(0xff22c1c3), Color(0xfffdbb2d)),
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
                      /*TODO*/
            },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                painter = if(!isPlaying) painterResource(id = R.drawable.ic_media_stop) else painterResource(id = R.drawable.ic_media_play),
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
                progressBrush = colorDone,
                waveformBrush = colorBrush,
                spikeWidth = 4.dp,
                spikePadding = 2.dp,
                spikeRadius = 4.dp,
                progress = waveformProgress.value,
                amplitudes = amplitudes,
                onProgressChange = {
                    waveformProgress.value = it
                },
                onProgressChangeFinished = {

                },
                redrawTrigger = redrawTrigger
            )
        }
        Text(text = duration, modifier = Modifier.align(Alignment.CenterVertically))
    }
}

fun saveWaveform(context: Context, audioId: String, barHeights: List<Float>) {
    val sharedPreferences = context.getSharedPreferences("waveforms", Context.MODE_PRIVATE)
    val gson = Gson()
    val json = gson.toJson(barHeights)
    sharedPreferences.edit {
        putString(audioId, json)
    }
}

fun loadWaveform(context: Context, audioId: String): List<Float>? {
    val sharedPreferences = context.getSharedPreferences("waveforms", Context.MODE_PRIVATE)
    val gson = Gson()
    val json = sharedPreferences.getString(audioId, null) ?: return null
    val type = object : TypeToken<List<Float>>() {}.type
    return gson.fromJson(json, type)
}

@Preview(showBackground = true)
@Composable
fun AudioWaveformPreview() {
    val redrawTrigger = remember {
        mutableStateOf(0)
    }
    val waveformProgress = remember {
        mutableStateOf(0F)
    }
    MyTheme {
        AudioWaveformItem(isPlaying = true, duration = "4:12", percentPlayed = 0.5f,
            redrawTrigger = redrawTrigger.value,
            waveformProgress =waveformProgress
        )
    }
}