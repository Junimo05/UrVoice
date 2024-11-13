package com.example.urvoices.utils.Navigator

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.urvoices.ui._component.BottomBar
import com.example.urvoices.ui._component.MediaPlayer
import com.example.urvoices.viewmodel.AuthViewModel
import com.example.urvoices.viewmodel.HomeViewModel
import com.example.urvoices.viewmodel.InteractionViewModel
import com.example.urvoices.viewmodel.MediaPlayerVM
import com.example.urvoices.viewmodel.MediaRecorderVM
import com.example.urvoices.viewmodel.PostDetailViewModel
import com.example.urvoices.viewmodel.ProfileViewModel
import com.example.urvoices.viewmodel.SearchViewModel
import com.example.urvoices.viewmodel.UIEvents
import com.example.urvoices.viewmodel.UploadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
fun Navigator(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val selectedPage = rememberSaveable { mutableIntStateOf(0) }
    val isVisibleBottomBar = rememberSaveable { mutableStateOf(false) }
    val isVisibleMediaBar = rememberSaveable { mutableStateOf(false) }
    //ViewModel instance

    val searchVM: SearchViewModel = hiltViewModel()
    val postDetailViewModel: PostDetailViewModel = hiltViewModel()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val uploadViewModel: UploadViewModel = hiltViewModel()
    val profileViewModel = hiltViewModel<ProfileViewModel>()


    val playerViewModel: MediaPlayerVM = hiltViewModel()
    val mediaRecorderVM = hiltViewModel<MediaRecorderVM>()

    val interactionViewModel = hiltViewModel<InteractionViewModel>()

    //Bar for Media
    var expandOptionBar = remember { mutableStateOf(false) }

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.parent?.route) {
                Graph.AUTHENTICATION -> {
                    isVisibleBottomBar.value = false
                    isVisibleMediaBar.value = false
                }
                Graph.NAV_SCREEN -> {
                    isVisibleBottomBar.value = true
                    isVisibleMediaBar.value = true
                }
                Graph.SPECIFY -> {
                    isVisibleBottomBar.value = true
                    if(destination.route?.startsWith("post/") == true){
                        isVisibleMediaBar.value = true
                        isVisibleBottomBar.value = false
                    }
                }
                Graph.NOTI_MSG -> {
                    isVisibleBottomBar.value = false
                    isVisibleMediaBar.value = false
                }
            }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    if (expandOptionBar.value) {
                        expandOptionBar.value = false
                    }
                }
            }
    ){
        Scaffold(
            bottomBar = {
                if (isVisibleBottomBar.value) {
                    BottomBar(
                        selectedPage = selectedPage,
                        navController = navController
                    )
                }
            },
        ) {paddingValues ->
            Scaffold(
                modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()),
                floatingActionButtonPosition = FabPosition.Center,
                floatingActionButton = {
                    if(isVisibleMediaBar.value){
                        AnimatedVisibility(
                            visible = !playerViewModel.isStop.collectAsState().value,
                            enter = slideInVertically(
                                initialOffsetY = { it }, // Slide in from the bottom
                                animationSpec = tween(durationMillis = 300) // Animation duration
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .background(
                                        color = Color.Transparent
                                    )
                                    .shadow(8.dp, shape = MaterialTheme.shapes.medium)
                                    .align(Alignment.BottomCenter)
                            ) {
                                MediaPlayer(
                                    navController = navController,
                                    playlist = playerViewModel.playlist,
                                    progress = playerViewModel.progress,
                                    isAudioPlaying = playerViewModel.isPlaying,
                                    currentPlayingIndex = playerViewModel.currentPlayingIndex,
                                    currentPlayingAudio = playerViewModel.currentAudio.value.url,
                                    duration = playerViewModel.duration,
                                    isStop = playerViewModel.isStop.value,
                                    onProgress = {
                                        playerViewModel.onUIEvents(UIEvents.SeekTo(it))
                                    },
                                    onStartPlayer = {
                                        playerViewModel.onUIEvents(UIEvents.PlayingAudio(it))
                                    },
                                    onPlayPause = {
                                        playerViewModel.onUIEvents(UIEvents.PlayPause)
                                    },
                                    onStop = {
                                        playerViewModel.onUIEvents(UIEvents.Stop)
                                    },
                                    onForward = {
                                        playerViewModel.onUIEvents(UIEvents.Forward)
                                    },
                                    onBackward = {
                                        playerViewModel.onUIEvents(UIEvents.Backward)
                                    },
                                    onNext = {
                                        playerViewModel.onUIEvents(UIEvents.NextAudio)
                                    },
                                    onPrevious = {
                                        playerViewModel.onUIEvents(UIEvents.PreviousAudio)
                                    },
                                    onRemoveFromPlaylist = {
                                        playerViewModel.onUIEvents(UIEvents.RemoveFromPlaylist(it))
                                    },
                                    onPlayFromList = {
                                        playerViewModel.onUIEvents(UIEvents.PlaySelectedFromList(it))
                                    },
                                    onLoopModeChange = {
                                        playerViewModel.onUIEvents(UIEvents.LoopModeChange)
                                    },
                                    expandOptionBar = expandOptionBar.value,
                                    setExpandOptionBar = {
                                        expandOptionBar.value = it
                                    },
                                    modifier = Modifier.clickable {
                                        expandOptionBar.value = !expandOptionBar.value
                                    }
                                )
                            }
                        }
                    }
                },
                bottomBar = {
                    if(isVisibleMediaBar.value){
                        if(playerViewModel.isStop.collectAsState().value){
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                val shimmerColors = listOf(
                                    Color(0xFFFFDBDB),
                                    Color(0xFFFFDCDC),
                                    Color(0xFFFFF0F0),
                                    Color(0xFFFFEDED),
                                    Color(0xFFFFF5F5)
                                )
                                val transition = rememberInfiniteTransition(label = "")
                                val translateAnim by transition.animateFloat(
                                    initialValue = 0f,
                                    targetValue = 1500f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ), label = ""
                                )
                                Row(
                                    modifier = Modifier
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = shimmerColors,
                                                start = Offset(translateAnim, 0f),
                                                end = Offset(translateAnim + 500f, 500f)
                                            )
                                        )
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        "No audio playing",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Graph.AUTHENTICATION,
                    route = Graph.ROOT,
                    modifier = Modifier.padding(it)
                ){
                    authGraph(navController, authViewModel) //authentication nav
                    mainGraph(
                        navController,
                        authViewModel,
                        playerViewModel = playerViewModel,
                        homeViewModel = homeViewModel,
                        uploadViewModel = uploadViewModel,
                        profileViewModel = profileViewModel,
                        mediaRecorderVM = mediaRecorderVM,
                        searchViewModel = searchVM
                    ) //home nav
                    specifyGraph(
                        navController,
                        authViewModel,
                        playerViewModel = playerViewModel,
                        postDetailViewModel = postDetailViewModel,
                        profileViewModel = profileViewModel
                    ) //specify nav
                    notiMsgGraph(navController) //notification nav
                }
            }
        }
    }

}

object Graph {
    const val ROOT = "root_graph"
    const val AUTHENTICATION = "auth_graph"
    const val NAV_SCREEN = "nav_graph"
    const val SPECIFY = "specify_graph"
    const val NOTI_MSG = "noti_msg_graph"
}