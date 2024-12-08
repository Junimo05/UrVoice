package com.example.urvoices.ui._component

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.urvoices.R
import com.example.urvoices.data.model.Audio
import com.example.urvoices.utils.audio_player.services.PlayMode
import com.example.urvoices.utils.formatToMinSecFromMillisec
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPlayer(
    navController: NavController,
    playlist: List<Audio>,
    modifier: Modifier = Modifier,
    //MediaPlayerMinimize State
    isMinimize: MutableState<Boolean>,
    lastInteractionTime: Long,
    //
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
    onAddToPlaylist: (Audio, Int) -> Unit, //add audio to playlist
    onRemoveFromPlaylist: (Int) -> Unit, //remove audio from playlist
    onPlayFromList: (Int) -> Unit, //play audio from playlist
    //
    playMode : String,
    onPlayModeChange: () -> Unit, //change play mode
    //
    onPlaylistReorder: (Int, Int) -> Unit, //reorder playlist
    expandOptionBar: Boolean,
    setExpandOptionBar: (Boolean) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    //Reorder Playlist
    val snackbarHostState = remember{ SnackbarHostState() }
    val scrollChannel = Channel<Float>()
    val lazyListState = rememberLazyListState()
    val isDragging = remember { mutableStateOf(false) }
    var draggingItemIndex: Int? by remember {
        mutableStateOf(null)
    }
    var draggingItem: LazyListItemInfo? by remember {
        mutableStateOf(null)
    }
    var delta: Float by remember { //dragging item offset
        mutableFloatStateOf(0f)
    }
    val animatedDelta by animateFloatAsState(targetValue = delta, label = "")
    LaunchedEffect(lazyListState) {
        while (true) {
            val diff = scrollChannel.receive()
            lazyListState.scrollBy(diff)
        }
    }

    //Minimize MediaBar
    LaunchedEffect(lastInteractionTime) {
        while(true){
            delay(10000) // Check every 5 seconds
            if (System.currentTimeMillis() - lastInteractionTime > 15000 && !bottomSheetState.isVisible) { // 10 seconds of inactivity
                isMinimize.value = true
            }
        }
    }

    val optionItemList = listOf(
        OptionItem(
            icon = R.drawable.ic_actions_menu,
            des = "Playlist",
            onClick = {scope.launch {
//                Log.e("MediaPlayer Show", "${playlist}")
                bottomSheetState.expand()
            }}
        ),
        OptionItem(
            icon = R.drawable.step_backward_svgrepo_com,
            des = "Previous",
            onClick = onPrevious
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
            onClick = onNext
        ),
        OptionItem(
            icon = when(playMode){
                PlayMode.OFF_MODE -> R.drawable.media_playlist_no_repeat_symbolic_svgrepo_com
                PlayMode.REPEAT_ONE -> R.drawable.media_playlist_repeat_song_symbolic_svgrepo_com
                PlayMode.REPEAT_ALL -> R.drawable.media_playlist_repeat_symbolic_svgrepo_com
                PlayMode.SHUFFLE -> R.drawable.media_playlist_shuffle_symbolic_svgrepo_com
                else -> {
                    R.drawable.media_playlist_no_repeat_symbolic_svgrepo_com
                }
            }
            ,
            des = "Loop",
            onClick = onPlayModeChange
        ),

    )

    val expandAnimation by animateDpAsState(targetValue = if(expandOptionBar) 170.dp else 125.dp,
        label = "expand_animation"
    )

    if(isMinimize.value){
        val getCurrentNavDes = navController.currentDestination?.route
        if(getCurrentNavDes!!.startsWith("post/")){
            Box(
                modifier = Modifier.padding(bottom = 40.dp)
                    .shadow(8.dp, shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ){
                IconButton(
                    onClick = { isMinimize.value = false },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.cassette_svgrepo_com),
                        contentDescription = "Expand Media Player",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.shadow(8.dp, shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ){
                IconButton(
                    onClick = { isMinimize.value = false },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.cassette_svgrepo_com),
                        contentDescription = "Expand Media Player",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    } else {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .height(expandAnimation)
                .padding(2.dp)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.primary)
                .then(modifier)
            ,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ){
                IconButton(
                    onClick = {
                        isMinimize.value = true
                    },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.minimize_svgrepo_com),
                        contentDescription = "More"
                    )
                }
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
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(

                        painter = painterResource(id = if (expandOptionBar) R.drawable.ic_chevron_top  else R.drawable.ic_chevron_down),
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
                    onDismissRequest = {  },
                    sheetState = bottomSheetState,
                ) {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxHeight(0.8f)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { offset ->
                                        lazyListState.layoutInfo.visibleItemsInfo
                                            //find the item that is being dragged
                                            .firstOrNull { item -> offset.y.toInt() in item.offset..(item.offset + item.size) }
                                            ?.also {
                                                //if Found then get the index of the item
                                                (it.contentType as? DraggableItem)?.let { draggableItem ->
                                                    draggingItem = it
                                                    draggingItemIndex = draggableItem.index
                                                }
                                            }
                                    },
                                    onDragEnd = {
                                        isDragging.value = false
                                        draggingItem = null
                                        draggingItemIndex = null
                                        delta = 0f
                                    },
                                    onDragCancel = {
                                        isDragging.value = false
                                        draggingItem = null
                                        delta = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        delta += dragAmount.y

                                        val currentDraggingItemIndex =
                                            draggingItemIndex
                                                ?: return@detectDragGesturesAfterLongPress
                                        val currentDraggingItem =
                                            draggingItem ?: return@detectDragGesturesAfterLongPress

                                        val startOffset = currentDraggingItem.offset + delta
                                        val endOffset =
                                            currentDraggingItem.offset + currentDraggingItem.size + delta
                                        val middleOffset =
                                            startOffset + (endOffset - startOffset) / 2

                                        val targetItem =
                                            lazyListState.layoutInfo.visibleItemsInfo.find { item ->
                                                middleOffset.toInt() in item.offset..item.offset + item.size &&
                                                        currentDraggingItem.index != item.index &&
                                                        item.contentType is DraggableItem
                                            }

                                        if (targetItem != null) {
                                            val targetIndex =
                                                (targetItem.contentType as DraggableItem).index
                                            //reorder the playlist
                                            onPlaylistReorder(currentDraggingItemIndex, targetIndex)
                                            //update the dragging item index and offset
                                            draggingItemIndex = targetIndex
                                            draggingItem = targetItem
                                            delta += currentDraggingItem.offset - targetItem.offset
                                        } else {
                                            val startOffsetToTop =
                                                startOffset - lazyListState.layoutInfo.viewportStartOffset
                                            val endOffsetToBottom =
                                                endOffset - lazyListState.layoutInfo.viewportEndOffset
                                            val scroll =
                                                when {
                                                    startOffsetToTop < 0 -> startOffsetToTop.coerceAtMost(
                                                        0f
                                                    )

                                                    endOffsetToBottom > 0 -> endOffsetToBottom.coerceAtLeast(
                                                        0f
                                                    )

                                                    else -> 0f
                                                }
                                            val canScrollDown =
                                                currentDraggingItemIndex != playlist.size - 1 && endOffsetToBottom > 0
                                            val canScrollUp =
                                                currentDraggingItemIndex != 0 && startOffsetToTop < 0
                                            if (scroll != 0f && (canScrollUp || canScrollDown)) {
                                                scrollChannel.trySend(scroll)
                                            }
                                        }
                                    }
                                )
                            },
                    ){
                        itemsIndexed(
                            items = playlist,
                            contentType = { index, _ -> DraggableItem(index = index)}
                        ){
                                index, audio ->
                            val modifierTransition = if (draggingItemIndex == index) { //if the item is being dragged
                                Modifier
                                    .zIndex(1f)
                                    .graphicsLayer {
                                        translationY = animatedDelta
                                    }
                            } else {
                                Modifier
                            }
                            PlaylistItem(
                                index = index,
                                playlistItemData = PlaylistItemData(audio = audio),
                                isAudioPlaying = isAudioPlaying,
                                onPlayPause = onPlayPause,
                                onPlayFromList = onPlayFromList,
                                onRemove = {
                                    scope.launch {
                                        onRemoveFromPlaylist(index)
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Audio removed from playlist",
                                            actionLabel = "Undo"
                                        )
                                        if(result == SnackbarResult.ActionPerformed){
                                            onAddToPlaylist(audio, index)
                                        }
                                    }
                                },
                                screenWidth = screenWidth,
                                currentPlayingIndex = currentPlayingIndex,
                                modifier = modifierTransition
                            )
                        }
                    }
                    //Snackbar
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                painter = painterResource(id = if(isAudioPlaying) R.drawable.media_pause_circle_svgrepo_com else R.drawable.media_play_circle_svgrepo_com),
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

data class DraggableItem(
    val index: Int
)

@Composable
fun PlaylistItem(
    index: Int,
    playlistItemData: PlaylistItemData,
    isAudioPlaying: Boolean,
    currentPlayingIndex: Int,
    onPlayPause: () -> Unit,
    onPlayFromList: (Int) -> Unit,
    onRemove: () -> Unit,
    screenWidth: Dp,
    modifier: Modifier
){
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX, label = "offset")
    val revealWidth by animateDpAsState(targetValue = (offsetX / screenWidth.value * screenWidth.value).dp.coerceAtMost(screenWidth),
        label = "remove_width"
    )
    val audioWidth by animateDpAsState(targetValue = screenWidth - revealWidth, label = "audio_width")
    Row(
        modifier = Modifier.fillMaxWidth()
    ){
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(revealWidth)
                .background(Color.Red)
                .padding(end = 16.dp)
                .zIndex(0f)
            ,
            contentAlignment = Alignment.CenterStart
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_actions_trash),
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier
                    .size(64.dp)
                    .padding(16.dp)
            )
        }
        Column(
            modifier = Modifier.width(audioWidth)
        ){
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        if (currentPlayingIndex == index) {
                            onPlayPause()
                        } else {
                            onPlayFromList(index)
                        }
                    }
                    .background(MaterialTheme.colorScheme.surface)
                    .pointerInput(Unit) {
                        if (currentPlayingIndex != index) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (offsetX > screenWidth.value) {
                                        onRemove()
                                    }
                                    offsetX = 0f
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    offsetX += dragAmount
                                }
                            )
                        }
                    }
                    .graphicsLayer { translationX = animatedOffsetX }
                    .then(modifier),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .padding(12.dp)
                        .weight(0.9f)
                    ,
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
                        modifier = if(currentPlayingIndex == index) Modifier.basicMarquee(
                            animationMode = MarqueeAnimationMode.Immediately,
                            initialDelayMillis = 5000,
                        ) else Modifier,
                        color = if(currentPlayingIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
                    ),
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
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
                modifier = Modifier.size(30.dp)
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
    Surface(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = "$stringMinute:$stringSec/${formatToMinSecFromMillisec(duration)}",
            style = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
            ),
            modifier = Modifier.padding(4.dp)
        )
    }
}