package com.example.urvoices.ui._component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.urvoices.R
import com.example.urvoices.data.model.Audio
import com.example.urvoices.utils.formatToMinSecFromMillisec
import com.example.urvoices.viewmodel.State.AppGlobalState.isStop
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPlayer(
    navController: NavController,
    playlist: MutableList<Audio>,
    modifier: Modifier = Modifier,
    progress: Float,
    isAudioPlaying: Boolean,
    currentPlayingIndex: Int, //index of current playing audio in playlist
    currentPlayingAudio: String, //url audio
    duration: Long,
    isStop: Boolean,
    onProgress: (Float) -> Unit,
    onStartPlayer:(Audio) -> Unit,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onForward: () -> Unit,
    onBackward: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onRemoveFromPlaylist: (Audio) -> Unit, //remove audio from playlist
    onPlayFromList: (Int) -> Unit, //play audio from playlist
    onLoopModeChange: () -> Unit, //change loop mode
    expandOptionBar: Boolean,
    setExpandOptionBar: (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val optionItemList = listOf(
        OptionItem(
            icon = R.drawable.ic_actions_menu,
            des = "Playlist",
            onClick = {scope.launch {
                bottomSheetState.expand()
            }}
        ),
        OptionItem(
            icon = R.drawable.step_backward_svgrepo_com,
            des = "Previous",
            onClick = {}
        ),
        OptionItem(
            icon = R.drawable.backward_svgrepo_com,
            des = "Backward",
            onClick = onBackward
        ),
        OptionItem(
            icon = R.drawable.forward_svgrepo_com,
            des = "Forward",
            onClick = onForward
        ),
        OptionItem(
            icon = R.drawable.step_forward_svgrepo_com,
            des = "Next",
            onClick = {}
        ),
        OptionItem(
            icon = R.drawable.loop_svgrepo_com,
            des = "Loop",
            onClick = onLoopModeChange
        ),

    )

    val expandAnimation by animateDpAsState(targetValue = if(expandOptionBar) 150.dp else 100.dp)

    Row (
        modifier = Modifier
            .fillMaxWidth()
            .height(expandAnimation)
            .border(
                BorderStroke(
                    width = 1.dp,
                    brush = SolidColor(MaterialTheme.colorScheme.onSurface),
                ),
                shape = MaterialTheme.shapes.medium.copy(CornerSize(20.dp))
            )
            .background(MaterialTheme.colorScheme.surface)
            .then(modifier)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ){
            MainBar(
                isAudioPlaying = isAudioPlaying,
                progress = progress,
                duration = duration,
                onProgress = onProgress,
                onPlayPause = onPlayPause,
                onStop = onStop
            )
            IconButton(
                onClick = {
                    setExpandOptionBar(!expandOptionBar)
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = if (expandOptionBar) R.drawable.ic_chevron_top else R.drawable.ic_chevron_down),
                    contentDescription = "More"
                )
            }
            if(expandOptionBar){
                OptionBar(
                    itemList = optionItemList,
                )
            }
        }

        //BottomSheet
        if(bottomSheetState.isVisible){
            ModalBottomSheet(
                onDismissRequest = { /*TODO*/ },
                sheetState = bottomSheetState
            ) {
                LazyColumn(

                    modifier = Modifier
                        .fillMaxHeight(0.8f)
                        .fillMaxWidth()
                ){
                    items(playlist.size){
                        val audio = playlist[it]
                        PlaylistItem(
                            index = it,
                            playlistItemData = PlaylistItemData(audio = audio),
                            isAudioPlaying = isAudioPlaying,
                            onPlayPause = onPlayPause,
                            onPlayFromList = onPlayFromList,
                            isCurrentPlaying = it == currentPlayingIndex,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainBar(
    isAudioPlaying: Boolean,
    progress: Float,
    duration: Long,
    onProgress: (Float) -> Unit,
    onPlayPause: () -> Unit,
    onStop: () -> Unit
){
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        IconButton(onClick = {
            onPlayPause()
        }) {
            Icon(
                painter = painterResource(id = if(isAudioPlaying) R.drawable.pause_svgrepo_com else R.drawable.play_svgrepo_com),
                modifier = Modifier.size(24.dp),
                contentDescription = "PlayPause"
            )
        }
        Slider(
            value = progress,
            onValueChange = {
                onProgress(it)
            },
            valueRange = 0f..1f,
            modifier = Modifier
                .weight(1f)
                .height(24.dp),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.inversePrimary
            )
        )
        TimeStampMedia(
            progress = progress,
            duration = duration
        )
        IconButton(onClick = {
            onStop()
        }) {
            Icon(
                painter = painterResource(id = R.drawable.close_round_svgrepo_com),
                modifier = Modifier.size(24.dp),
                contentDescription = "Close"
            )
        }
    }
}

@Composable
fun PlaylistItem(
    index: Int,
    playlistItemData: PlaylistItemData,
    isAudioPlaying: Boolean,
    isCurrentPlaying: Boolean = false,
    onPlayPause: () -> Unit,
    onPlayFromList: (Int) -> Unit,
    onRemove: () -> Unit = {},
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                if(!isCurrentPlaying){
                    onPlayPause()
                } else {
                    onPlayFromList(index)
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ){
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(12.dp).weight(1f)
        ){
            Text(
                text = playlistItemData.audio.title,
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = if(isCurrentPlaying) Modifier.basicMarquee(
                    animationMode = MarqueeAnimationMode.Immediately,
                    initialDelayMillis = 5000,
                ) else Modifier,
                color = if(isCurrentPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = playlistItemData.audio.author,
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                )
            )
        }
        Text(
            text = formatToMinSecFromMillisec(playlistItemData.audio.duration),
            style = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
            )
        )
    }
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth()
    )
}

data class PlaylistItemData(
    val audio: Audio
)

@Composable
fun OptionBar(
    itemList : List<OptionItem>,
    modifier: Modifier = Modifier
){
    Row(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ){
        itemList.forEach {
            IconButton(
                onClick = it.onClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = it.icon),
                    contentDescription = it.des,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

data class OptionItem(
    val icon: Int,
    val des: String,
    val onClick: () -> Unit
)

@Composable
fun TimeStampMedia(
    progress: Float,
    duration: Long
){
    val progressMillis = (progress * duration).toLong()
    val progressMinutes = TimeUnit.MILLISECONDS.toMinutes(progressMillis)
    val progressSeconds = TimeUnit.MILLISECONDS.toSeconds(progressMillis) - TimeUnit.MINUTES.toSeconds(progressMinutes)

    val stringMinute = progressMinutes.toString()
    val stringSec = if(progressSeconds < 10) "0$progressSeconds" else progressSeconds.toString()
    Surface {
        Text(
            text = "$stringMinute:$stringSec/${formatToMinSecFromMillisec(duration)}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(4.dp)
        )
    }
}