package com.example.urvoices.ui._component.PostComponent

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.urvoices.R
import com.example.urvoices.data.model.Audio
import com.example.urvoices.data.model.Post
import com.example.urvoices.data.model.User
import com.example.urvoices.ui._component.InteractionRow
import com.example.urvoices.ui._component.MoreAction.DropDownMenu
import com.example.urvoices.ui._component.MoreAction.PostAction
import com.example.urvoices.utils.Auth.BASE_URL
import com.example.urvoices.utils.Navigator.MainScreen
import com.example.urvoices.utils.Post_Interactions
import com.example.urvoices.utils.getTimeElapsed
import com.example.urvoices.viewmodel.InteractionRowState
import com.example.urvoices.viewmodel.InteractionViewModel
import com.example.urvoices.viewmodel.MediaPlayerVM
import com.example.urvoices.viewmodel.UIEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@SuppressLint("UnrememberedMutableState", "StateFlowValueCalledInComposition")
@Composable
fun ProfilePostItem(
    navController: NavController,
    post: Post,
    user: User,
    playerViewModel: MediaPlayerVM,
    interactionViewModel: InteractionViewModel,
    isBlock : MutableState<Boolean>,
    blockInfo: MutableState<String>,
    modifier: Modifier = Modifier
){
    val TAG = "ProfilePostItem"
    val context = LocalContext.current
    val timeText = getTimeElapsed(post.createdAt)
    val scope = CoroutineScope(Dispatchers.Main)

    //State
    val interactionState by interactionViewModel.uiState.collectAsState()
    val isLove = rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        interactionViewModel.getLoveStatus(postID = post.ID!!) {result ->
            isLove.value = result
        }
    }


    val isExpandedContext = mutableStateOf(false)
    val expandMenu = rememberSaveable { mutableStateOf(false) }
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

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

                Row(
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 40.dp)
                ){
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = post.audioName!!,
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 18.sp
                            ),
                            overflow = TextOverflow.Clip,
                            maxLines = 1,
                            modifier = if(playerViewModel.currentAudio.value.id == post.ID) Modifier.basicMarquee(
                                animationMode = MarqueeAnimationMode.Immediately,
                                initialDelayMillis = 10000,
                            ) else Modifier,
                            color = if(playerViewModel.currentAudio.value.id == post.ID) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = post.description,
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            overflow = if(isExpandedContext.value) TextOverflow.Visible else TextOverflow.Ellipsis,
                            maxLines = if(isExpandedContext.value) Int.MAX_VALUE else 2,
                            modifier = Modifier
                                .clickable(onClick = { isExpandedContext.value = !isExpandedContext.value })
                        )
                    }
                    Row{
                        IconButton(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            onClick = {
                                expandMenu.value = !expandMenu.value
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_actions_more_2),
                                contentDescription = "ActionMore",
                                modifier = Modifier
                                    .weight(0.1f)
                            )
                        }
                        DropDownMenu(
                            expand = expandMenu,
                            actions = PostAction(
                                isCurrentUserPost = user.ID == post.userId,
                                addToPlaylist = {
                                    playerViewModel.onUIEvents(
                                        UIEvents.AddToPlaylist(
                                            Audio(
                                                id = post.ID!!,
                                                title = post.audioName!!,
                                                url = post.url!!,
                                                author = user.username,
                                                duration = post.duration?:0
                                            )
                                        )
                                    )
                                    expandMenu.value = false
                                },
                                goToPost = {
                                    navController.navigate("post/${post.userId}/${post.ID}")
                                    expandMenu.value = false
                                },
                                goToUser = {
                                    navController.navigate("profile/${post.userId}")
                                    expandMenu.value = false
                                },
                                copyLink = {
                                    val clip = ClipData.newPlainText("PostLink", "$BASE_URL/post/${post.userId}/${post.ID}")
                                    clipboardManager.setPrimaryClip(clip)
                                    Toast.makeText(context, "Link Copied", Toast.LENGTH_SHORT).show()
                                    expandMenu.value = false
                                },
                                deletePost = {
                                    interactionViewModel.deletePost(post.ID!!){result ->
                                        if(result){
                                            navController.navigate(MainScreen.HomeScreen.route)
                                        } else {
                                            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                                        }
                                        expandMenu.value = false
                                    }
                                },
                                blockInfo = blockInfo,
                                blockUser = {
                                    val result = interactionViewModel.blockUser(post.userId)
                                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
                                    if(interactionState == InteractionRowState.Success){
                                        isBlock.value = true
                                    }
                                    expandMenu.value = false
                                },
                                unblockUser = {
                                    val result = interactionViewModel.unblockUser(post.userId)
                                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
                                    if(interactionState == InteractionRowState.Success){
                                        isBlock.value = false
                                    }
                                    expandMenu.value = false
                                },
                                editPost = {
                                    navController.navigate(
                                        com.example.urvoices.utils.Navigator.EditPostScreen(
                                            post = post
                                        )
                                    )
                                    expandMenu.value = false
                                }
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(50.dp))
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
                            interactionViewModel.loveAction(
                                isLove = it,
                                postID = post.ID!!,
                                targetUserID = post.userId
                            ){ result ->
                                //get isLove input then update UI if result = true
                                if(result){
                                    isLove.value = it
                                    if(isLove.value){
                                        post.likes = post.likes!! + 1
                                    } else {
                                        post.likes = post.likes!! - 1
                                    }
                                }
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
        AudioItem(
            id = post.ID!!,
            audioUrl = post.url!!,
            audioAmplitudes = post.amplitudes,
            currentPlayingAudio = playerViewModel.currentAudio.value.url,
            currentPlayingPost = playerViewModel.currentAudio.value.id,
            duration = playerViewModel.durationPlayer,
            isPlaying = playerViewModel.isPlaying,
            isStop = playerViewModel.isStop.value,
            onPlayStart = {
                if(post.url.isNotEmpty()){
                    playerViewModel.onUIEvents(
                        UIEvents.PlayingAudio(
                            Audio(
                                id = post.ID,
                                url = post.url,
                                title = post.audioName!!,
                                duration = post.duration?:0,
                                author = user.username
                            )
                        ))
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
