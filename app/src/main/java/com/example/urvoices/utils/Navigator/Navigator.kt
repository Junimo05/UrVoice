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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.urvoices.ui._component.BottomBar
import com.example.urvoices.ui._component.MediaPlayer
import com.example.urvoices.viewmodel.AuthViewModel
import com.example.urvoices.viewmodel.HomeViewModel
import com.example.urvoices.viewmodel.InteractionRowViewModel
import com.example.urvoices.viewmodel.MediaPlayerViewModel
import com.example.urvoices.viewmodel.PostDetailViewModel
import com.example.urvoices.viewmodel.ProfileViewModel
import com.example.urvoices.viewmodel.UIEvents
import com.example.urvoices.viewmodel.UploadViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
fun Navigator(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val selectedPage = rememberSaveable { mutableIntStateOf(0) }
    val isVisibleBottomBar = rememberSaveable { mutableStateOf(false) }
    val isVisibleMediaBar = rememberSaveable { mutableStateOf(false) }
    //ViewModel instance

    val playerViewModel: MediaPlayerViewModel = hiltViewModel()
    val postDetailViewModel: PostDetailViewModel = hiltViewModel()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val uploadViewModel: UploadViewModel = hiltViewModel()
    val profileViewModel = hiltViewModel<ProfileViewModel>()
    val interactionRowViewModel = hiltViewModel<InteractionRowViewModel>()


    val playerState = playerViewModel.uiState.collectAsState()

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.parent?.route) {
                Graph.AUTHENTICATION -> isVisibleBottomBar.value = false
                Graph.NAV_SCREEN -> isVisibleBottomBar.value = true
                Graph.SPECIFY -> isVisibleBottomBar.value = false
                Graph.NOTI_MSG -> isVisibleBottomBar.value = false
            }
            when (destination.route) {
                Graph.AUTHENTICATION -> isVisibleMediaBar.value = false
                Graph.NAV_SCREEN -> isVisibleMediaBar.value = true
                Graph.SPECIFY -> {
                    if(destination.route!!.startsWith("post/") == true){
                        isVisibleMediaBar.value = false
                    }else {
                        isVisibleMediaBar.value = true
                    }
                }
                Graph.NOTI_MSG -> isVisibleMediaBar.value = true

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
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()),
            bottomBar = {
                if(!playerViewModel.isStop.collectAsState().value && isVisibleMediaBar.value){
                    MediaPlayer(
                        progress = playerViewModel.progress,
                        isAudioPlaying = playerViewModel.isPlaying,
                        currentPlayingAudio = playerViewModel.currentPlayingAudio,
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
                        isStop = playerViewModel.isStop.value,
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
                        color = Color.Black
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
                mainGraph(
                    navController,
                    authViewModel,
                    playerViewModel = playerViewModel,
                    homeViewModel = homeViewModel,
                    uploadViewModel = uploadViewModel,
                    profileViewModel = profileViewModel
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

object Graph {
    const val ROOT = "root_graph"
    const val AUTHENTICATION = "auth_graph"
    const val NAV_SCREEN = "nav_graph"
    const val SPECIFY = "specify_graph"
    const val NOTI_MSG = "noti_msg_graph"
}