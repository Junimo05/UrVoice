package com.example.urvoices.ui._component.PostComponent

import android.annotation.SuppressLint
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.urvoices.data.model.Post
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.ui._component.InteractionRow
import com.example.urvoices.utils.Navigator.SpecifyScreen
import com.example.urvoices.utils.Post_Interactions
import com.example.urvoices.utils.getTimeElapsed
import com.example.urvoices.utils.toPostJson
import com.example.urvoices.viewmodel.InteractionRowViewModel
import com.example.urvoices.viewmodel.MediaPlayerViewModel
import com.example.urvoices.viewmodel.UIEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@SuppressLint("UnrememberedMutableState", "StateFlowValueCalledInComposition")
@Composable
fun ProfilePostItem(
    navController: NavController,
    post: Post,
    playerViewModel: MediaPlayerViewModel,
    modifier: Modifier = Modifier
){
    val TAG = "ProfilePostItem"
    val interactionViewModel = hiltViewModel<InteractionRowViewModel>()
    val timeText = getTimeElapsed(post.createdAt)
    val scope = CoroutineScope(Dispatchers.Main)


    val isExpandedContext = mutableStateOf(false)
    Box(
        modifier = Modifier.fillMaxWidth()
    ){
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .align(Alignment.Center)
                .clickable {
                    //TODO: NAVIGATE TO POST
                    if(post.id != null) navController.navigate("post/${post.userId}/${post.id}")
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
                        //TODO: ACTION POST
                        loveCounts = post.likes,
                        commentCounts = post.comments,
                        love_act = {

                        },
                        comment_act = {
                              navController.navigate("")
                        },
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        AudioWaveformItem(
            id = post.id!!,
            audioUrl = post.url!!,
            audioAmplitudes = post.amplitudes,
            currentPlayingAudio = playerViewModel.currentPlayingAudio,
            currentPlayingPost = playerViewModel.currentPlayingPost,
            duration = playerViewModel.duration, //fix Duration each Post
            isPlaying = playerViewModel.isPlaying,
            isStop = playerViewModel.isStop.value,
            onPlayStart = {
                playerViewModel.onUIEvents(
                    UIEvents.PlayingAudio(
                    post.url
                ))
                playerViewModel.updateCurrentPlayingPost(post.id)
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

@Preview(showBackground = true)
@Composable
fun ProfilePostItemPreview() {
    MyTheme {
        ProfilePostItem(
            post = Post(
                id = "1",
                description = "This is a description",
                url = "https://www.google.com",
                amplitudes = listOf(),
                audioName = "Audio Name",
                createdAt = 0,
                deleteAt = 0,
                likes = 0,
                comments = 0,
                userId = "1",
                tag = listOf(),
                updateAt = 0
            ),
            playerViewModel = hiltViewModel(),
            navController = rememberNavController(),
        )
    }
}
