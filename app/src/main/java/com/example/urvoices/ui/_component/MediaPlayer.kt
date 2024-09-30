package com.example.urvoices.ui._component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.urvoices.R
import com.example.urvoices.utils.timeStampToDuration
import java.util.concurrent.TimeUnit

@Composable
fun MediaPlayer(
    modifier: Modifier = Modifier,
    progress: Float,
    isAudioPlaying: Boolean,
    currentPlayingAudio: String,
    duration: Long,
    onProgress: (Float) -> Unit,
    onStartPlayer:(String) -> Unit,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    isStop: Boolean,
    onForward: () -> Unit,
    onBackward: () -> Unit,
    onSeekToNext: () -> Unit,
    onLoopModeChange: () -> Unit
) {
    BottomAppBar (
        content = {
            Column{
                Row (
                    modifier = Modifier
                        .fillMaxSize()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    PlayerButton(
                        isStop = isAudioPlaying,
                        onStart = onPlayPause,
                        onNext = onSeekToNext,
                    )
                    Slider(
                        value = progress,
                        onValueChange = {onProgress(it)},
                        valueRange = 0f..100f,
                        modifier = Modifier.weight(1f)
                    )
                    TimeStampMedia(
                        progress = progress,
                        duration = duration
                    )
                }
            }
        }
    )
}

@Composable
fun TimeStampMedia(
    progress: Float,
    duration: Long
){
    val progressMillis = (progress / 100f * duration).toLong()
    val progressMinutes = TimeUnit.MILLISECONDS.toMinutes(progressMillis)
    val progressSeconds = TimeUnit.MILLISECONDS.toSeconds(progressMillis) - TimeUnit.MINUTES.toSeconds(progressMinutes)

    val stringMinute = progressMinutes.toString()
    val stringSec = if(progressSeconds < 10) "0$progressSeconds" else progressSeconds.toString()
    Surface {
        Text(
            text = "$stringMinute:$stringSec/${timeStampToDuration(duration)}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Composable
fun PlayerButton(
    isStop: Boolean,
    onStart: () -> Unit,
    onNext: () -> Unit
){
    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(56.dp)
            .padding(4.dp)
    ){
        IconButton(onClick = {
            onStart()
        }) {
            Icon(
                painter = painterResource(id = if(isStop) R.drawable.ic_media_pause else R.drawable.ic_media_play),
                contentDescription = "PlayPause"
            )
        }
        Spacer(modifier = Modifier.size(4.dp))
        IconButton(onClick = {
            onNext()
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_media_next),
                contentDescription = "PlayPause"
            )
        }
    }
}