package com.example.urvoices.ui._component.PostComponent

import android.annotation.SuppressLint
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.urvoices.R
import com.example.urvoices.data.model.Post
import com.example.urvoices.ui._component.InteractionRow
import com.example.urvoices.utils.Navigator.SpecifyScreen
import com.example.urvoices.utils.Post_Interactions
import com.example.urvoices.utils.getTimeElapsed
import com.example.urvoices.viewmodel.HomeViewModel
import com.example.urvoices.viewmodel.InteractionRowViewModel
import com.example.urvoices.viewmodel.MediaPlayerViewModel
import com.example.urvoices.viewmodel.UIEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun NewFeedPostItem(
    navController: NavController,
    post: Post,
    homeViewModel: HomeViewModel,
    playerViewModel: MediaPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val TAG = "NewFeedPostItem"
    val timeText = getTimeElapsed(post.createdAt)
    val scope = CoroutineScope(Dispatchers.Main)
    val interactionViewModel = hiltViewModel<InteractionRowViewModel>()
    interactionViewModel.getLoveStatus(post.id!!)
    Card(
        shape = RoundedCornerShape(40.dp),
        modifier = modifier.clickable {
            //TODO: Navigate to Post Detail
            navController.navigate("post/${post.userId}/${post.id}")
        }
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
                    navController = navController,
                    userId = post.userId,
                    postId = post.id,
                    postDes = post.description,
                    homeViewModel = homeViewModel,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    onClick = {
                        /*TODO ACTION MORE
                        TODO Access Profile
                        TODO Access Post
                        *  */
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = post.audioName!!,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                overflow = TextOverflow.Clip,
                maxLines = 1,
                modifier = if(playerViewModel.currentPlayingPost == post.id) Modifier.basicMarquee(
                    animationMode = MarqueeAnimationMode.Immediately,
                    initialDelayMillis = 10000,
                ) else Modifier,
                color = if(playerViewModel.currentPlayingPost == post.id) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))
            AudioWaveformItem(
                id = post.id,
                audioUrl = post.url!!,
                audioAmplitudes = post.amplitudes,
                currentPlayingAudio = playerViewModel.currentPlayingAudio,
                currentPlayingPost = playerViewModel.currentPlayingPost,
                duration = playerViewModel.duration, //fix Duration each Post
                isPlaying = playerViewModel.isPlaying,
                isStop = playerViewModel.isStop.value,
                onPlayStart = {
                    playerViewModel.onUIEvents(UIEvents.PlayingAudio(
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
            )
            Spacer(modifier = Modifier.height(8.dp))
            InteractionRow(
                interactions = Post_Interactions(
                    loveCounts = post.likes,
                    commentCounts = post.comments,
                    isLove = interactionViewModel.isLove,
                    love_act = {
                        interactionViewModel.loveAction(
                            isLove = it,
                            targetUserID = post.userId,
                            postID = post.id
                        )
                    },
                    comment_act = {
                       //TODO: do act
                    },
                )
            )
        }
    }
}


@Composable
fun ProfileInfo(
    navController: NavController,
    userId: String,
    postId: String,
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
                    .clickable {
                        //To Profile
                        navController.navigate(SpecifyScreen.ProfileScreen(userId))

                    }
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
                    modifier = Modifier.clickable {
                        //To Profile
                        navController.navigate(SpecifyScreen.ProfileScreen(userId))
                    }
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