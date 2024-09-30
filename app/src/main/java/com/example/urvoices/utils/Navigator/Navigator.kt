package com.example.urvoices.utils.Navigator

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.urvoices.ui._component.BottomBar
import com.example.urvoices.ui._component.MediaPlayer
import com.example.urvoices.viewmodel.AuthViewModel
import com.example.urvoices.viewmodel.MediaPlayerViewModel
import com.example.urvoices.viewmodel.UIEvents

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Navigator(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val selectedPage = rememberSaveable { mutableIntStateOf(0) }
    val isVisibleBottomBar = rememberSaveable { mutableStateOf(false) }

    val playerViewModel: MediaPlayerViewModel = hiltViewModel()

    val uiState = playerViewModel.uiState.collectAsState()

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.parent?.route) {
                Graph.AUTHENTICATION -> isVisibleBottomBar.value = false
                Graph.NAV_SCREEN -> isVisibleBottomBar.value = true
                Graph.POST -> isVisibleBottomBar.value = false
                Graph.NOTI_MSG -> isVisibleBottomBar.value = false
            }
        }
    }

    LaunchedEffect(authViewModel.authState.value) {

    }

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
            modifier = Modifier.padding(paddingValues),
            bottomBar = {
                if(playerViewModel.isPlaying){
                    MediaPlayer(
                        progress = playerViewModel.progress,
                        isAudioPlaying = playerViewModel.isPlaying,
                        currentPlayingAudio = playerViewModel.currentPlayingAudio.toString(),
                        duration = playerViewModel.duration,
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
                        isStop = playerViewModel.isStop,
                        onForward = {
                            playerViewModel.onUIEvents(UIEvents.Forward)
                        },
                        onBackward = {
                            playerViewModel.onUIEvents(UIEvents.Backward)
                        },
                        onSeekToNext = {
                            playerViewModel.onUIEvents(UIEvents.SeekToNext)
                        },
                        onLoopModeChange = {
                            playerViewModel.onUIEvents(UIEvents.LoopModeChange)
                        }
                    )
                }else {
                    Text(
                        text = "Not Playing",
                        color = Color.Black,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            },
        ) {
            NavHost(
                navController = navController,
                startDestination = Graph.AUTHENTICATION,
                route = Graph.ROOT,
                modifier = Modifier.padding(it)
            ){
                authGraph(navController, authViewModel) //authentication nav
                mainGraph(navController, authViewModel, playerViewModel) //home nav
                notiMsgGraph(navController) //notification nav
            }
        }
    }
}

object Graph {
    const val ROOT = "root_graph"
    const val AUTHENTICATION = "auth_graph"
    const val NAV_SCREEN = "nav_graph"
    const val POST = "post_graph"
    const val NOTI_MSG = "noti_msg_graph"
}