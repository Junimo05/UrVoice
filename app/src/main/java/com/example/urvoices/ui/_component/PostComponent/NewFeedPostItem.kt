package com.example.urvoices.ui._component.PostComponent

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.urvoices.R
import com.example.urvoices.data.model.Post
import com.example.urvoices.ui._component.InteractionRow
import com.example.urvoices.utils.Post_Interactions
import com.example.urvoices.utils.getTimeElapsed
import com.example.urvoices.viewmodel.HomeViewModel
import com.example.urvoices.viewmodel.MediaPlayerViewModel
import com.example.urvoices.viewmodel.UIEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun NewFeedPostItem(
    post: Post,
    homeViewModel: HomeViewModel,
    playerViewModel: MediaPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val TAG = "NewFeedPostItem"
    val timeText = getTimeElapsed(post.createdAt)
    val scope = CoroutineScope(Dispatchers.Main)
    val amplitudesTest = rememberSaveable {
        mutableStateOf(
            listOf(
                45, 23, 67, 89, 12, 34, 56, 78, 90, 11, 22, 33, 44, 55, 66, 77, 88,
                99, 10, 20, 30, 40, 50, 60, 70, 80, 91, 92, 93, 94, 95, 96, 97, 98,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 13, 14, 15, 16, 17, 18, 19, 21, 24, 25,
                26, 27, 28, 29, 31, 32, 35, 36, 37, 38, 39, 41, 42, 43, 46, 47, 48,
                49, 51, 52, 53, 54, 57, 58, 59, 61, 62, 63, 64, 65, 68, 69, 71, 72,
                73, 74, 75, 76, 79, 81, 82, 83, 84, 85, 86, 87, 100
            )
        )
    }


    Card(
        shape = RoundedCornerShape(40.dp),
    ) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(20.dp)
        )
        {
            Row{
                ProfileInfo(
                    userId = post.userId,
                    postDes = post.description,
                    homeViewModel = homeViewModel,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    onClick = {
                        /*TODO*/
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_actions_more_2),
                        contentDescription = "ActionMore",
                        modifier = Modifier
                            .weight(0.1f)
                    )
                }
            }
            Text(
                text = timeText,
                style = TextStyle(
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.padding(top = 4.dp, bottom = 0.dp, start = 14.dp, end = 0.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            AudioWaveformItem(
                id = post.id!!,
                currentPlayingAudio = playerViewModel.currentPlayingAudio,
                currentPlayingPost = playerViewModel.currentPlayingPost,
                duration = playerViewModel.duration, //fix Duration each Post
                isPlaying = playerViewModel.isPlaying,
                isStop = playerViewModel.isStop.value,
                onPlayStart = {
                    playerViewModel.onUIEvents(UIEvents.PlayingAudio(
                        post.url!!
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
                initAmplitudes = amplitudesTest.value,
            )
            Spacer(modifier = Modifier.height(8.dp))
            InteractionRow(interactions = Post_Interactions(
                love_act = {},
                comment_act = {},
                //TODO: do act
            ))
        }
    }
}


@Composable
fun ProfileInfo(
    userId: String,
    postDes: String,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier
){
    val (isLoaded, setIsLoaded) = rememberSaveable { mutableStateOf(false) }
    val userInfo by produceState(initialValue = mapOf<String, String>(), producer = {
        value = homeViewModel.getUserInfo(userId).let {
            setIsLoaded(true)
            it
        }
    })

    if(!isLoaded){
        CircularProgressIndicator()
    }else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .then(modifier)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(userInfo["avatarUrl"])
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                placeholder = painterResource(id = R.drawable.person),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Black, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = userInfo["username"] ?: "Unknown",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = postDes,
                    style = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun ProfileNewFeedPreview() {
//    val redrawTrigger = remember {
//        mutableStateOf(0)
//    }
//    MyTheme {
//        NewFeedPostItem(redrawTrigger.value)
//    }
//}