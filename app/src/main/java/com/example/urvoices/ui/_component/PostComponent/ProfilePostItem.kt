package com.example.urvoices.ui._component.PostComponent

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.urvoices.data.model.Post
import com.example.urvoices.ui._component.InteractionRow
import com.example.urvoices.utils.Post_Interactions
import com.example.urvoices.utils.getTimeElapsed
import com.example.urvoices.viewmodel.InteractionRowViewModel
import com.example.urvoices.viewmodel.MediaPlayerVM
import com.example.urvoices.viewmodel.UIEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@SuppressLint("UnrememberedMutableState", "StateFlowValueCalledInComposition")
@Composable
fun ProfilePostItem(
    navController: NavController,
    post: Post,
    playerViewModel: MediaPlayerVM,
    interactionViewModel: InteractionRowViewModel,
    modifier: Modifier = Modifier
){
    val TAG = "ProfilePostItem"

    val timeText = getTimeElapsed(post.createdAt)
    val scope = CoroutineScope(Dispatchers.Main)

    //State
    val isLove = rememberSaveable { mutableStateOf(false) }
    val loadedWaveForm = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        interactionViewModel.getLoveStatus(postID = post.ID!!) {result ->
            isLove.value = result
        }
    }

    Log.e(TAG, "Post in ${TAG}: ${post.ID} - ${post.amplitudes}")

    val isExpandedContext = mutableStateOf(false)
    Box(
        modifier = Modifier.fillMaxWidth()
    ){
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .align(Alignment.Center)
                .clickable {
                    navController.navigate("post/${post.userId}/${post.ID}")
                }
                ,
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ){
                Text(
                    text = post.description,
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    overflow = if(isExpandedContext.value) TextOverflow.Visible else TextOverflow.Ellipsis,
                    maxLines = if(isExpandedContext.value) Int.MAX_VALUE else 2,
                    modifier = Modifier
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 40.dp)
                        .clickable(onClick = { isExpandedContext.value = !isExpandedContext.value })
                )
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = timeText,
                    style = TextStyle(
                        fontWeight = FontWeight.Light,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )
                InteractionRow(
                    interactions = Post_Interactions(
                        isLove = isLove.value,
                        loveCounts = post.likes!!,
                        commentCounts = post.comments!!,
                        love_act = {
                            isLove.value = it
                            interactionViewModel.loveAction(
                                isLove = it,
                                postID = post.ID!!,
                                targetUserID = post.userId
                            ){ result ->
                                if(result) post.likes = post.likes!! + 1
                            }
                        },
                        comment_act = {
                            navController.navigate("post/${post.userId}/${post.ID}")
                        },
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        AudioWaveformItem(
            id = post.ID!!,
            audioUrl = post.url!!,
            audioAmplitudes = post.amplitudes,
            currentPlayingAudio = playerViewModel.currentPlayingAudio,
            currentPlayingPost = playerViewModel.currentPlayingPost,
            duration = playerViewModel.duration, //fix Duration each Post
            isPlaying = playerViewModel.isPlaying,
            isStop = playerViewModel.isStop.value,
            onPlayStart = {
                if(post.url.isNotEmpty()){
                    playerViewModel.onUIEvents(
                        UIEvents.PlayingAudio(
                            post.url
                        ))
                    playerViewModel.updateCurrentPlayingPost(post.ID)
                }
            },
            onPlayPause = {
                playerViewModel.onUIEvents(UIEvents.PlayPause)
            },
            percentPlayed = playerViewModel.progress,
            onPercentChange = {
                playerViewModel.onUIEvents(UIEvents.SeekTo(it))
            },
            modifier = Modifier
                .align(Alignment.Center)
        )
    }
}
