package com.example.urvoices.ui._component.PostComponent

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.urvoices.data.model.Audio
import com.example.urvoices.data.model.Post
import com.example.urvoices.ui._component.InteractionRow
import com.example.urvoices.ui._component.MoreAction.DropDownMenu
import com.example.urvoices.ui._component.MoreAction.PostAction
import com.example.urvoices.ui._component.OptionBar
import com.example.urvoices.ui._component.OptionItem
import com.example.urvoices.utils.Navigator.BASE_URL
import com.example.urvoices.utils.Post_Interactions
import com.example.urvoices.utils.getTimeElapsed
import com.example.urvoices.viewmodel.AuthViewModel
import com.example.urvoices.viewmodel.HomeViewModel
import com.example.urvoices.viewmodel.InteractionViewModel
import com.example.urvoices.viewmodel.MediaPlayerVM
import com.example.urvoices.viewmodel.UIEvents
import kotlinx.coroutines.runBlocking

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun NewFeedPostItem(
    navController: NavController,
    authVM: AuthViewModel,
    post: Post,
    homeViewModel: HomeViewModel,
    playerViewModel: MediaPlayerVM,
    modifier: Modifier = Modifier
) {
    val TAG = "NewFeedPostItem"
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val timeText = getTimeElapsed(post.createdAt)

    val interactionViewModel = hiltViewModel<InteractionViewModel>(
        key = post.ID
    )

    val currentUser = authVM.getCurrentUser()
    val userBaseInfo by remember(post.userId) {
        mutableStateOf(runBlocking {
            homeViewModel.getUserInfo(post.userId)
        })
    }

    //State
    val expandMenu = rememberSaveable { mutableStateOf(false) }
    val isBlock = rememberSaveable { mutableStateOf(false) }
    val isSaved = rememberSaveable { mutableStateOf(false) }
    val isLove = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(post.ID) {
        interactionViewModel.getLoveStatus(post.ID!!) {
            isLove.value = it
        }
        interactionViewModel.getSaveStatus(post.ID) {
//            Log.e(TAG, "Save Status: $it")
            isSaved.value = it
        }
        //No need to Block Status Getter
    }

    Card(
        shape = RoundedCornerShape(40.dp),
        modifier = modifier
            .padding(top = 4.dp)
            .clickable {
                navController.navigate("post/${post.userId}/${post.ID}")
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
                    isBlock = isBlock,
                    navController = navController,
                    userId = post.userId,
                    postDes = post.description,
                    userInfo = userBaseInfo,
                    modifier = Modifier.weight(1f)
                )
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
                            isCurrentUserPost = currentUser?.uid == post.userId,
                            addToPlaylist = {
                                playerViewModel.onUIEvents(
                                    UIEvents.AddToPlaylist(
                                        Audio(
                                            id = post.ID!!,
                                            title = post.audioName!!,
                                            url = post.url!!,
                                            author = userBaseInfo["username"]!!,
                                            duration = post.duration
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
                            savePost = {
                                interactionViewModel.savePost(post.ID!!){result ->
                                    if(result != null){
                                        if(result){
                                            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Unsaved", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    expandMenu.value = false
                                }
                            },
                            isBlock = isBlock,
                            blockUser = {
                                if(isBlock.value){
                                    val result = interactionViewModel.unblockUser(post.userId)
                                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
                                    isBlock.value = false
                                } else {
                                    val result = interactionViewModel.blockUser(post.userId)
                                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
                                    isBlock.value = true
                                }
                                expandMenu.value = false
                            }
                        )
                    )
                }
            }
            if(!isBlock.value){
                Text(
                    text = timeText,
                    style = TextStyle(
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp,
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
                    modifier = if(playerViewModel.currentAudio.value.id == post.ID) Modifier.basicMarquee(
                        animationMode = MarqueeAnimationMode.Immediately,
                        initialDelayMillis = 10000,
                    ) else Modifier,
                    color = if(playerViewModel.currentAudio.value.id == post.ID) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))
                AudioWaveformItem(
                    id = post.ID!!,
                    audioUrl = post.url!!,
                    audioAmplitudes = post.amplitudes,
                    currentPlayingAudio = playerViewModel.currentAudio.value.url,
                    currentPlayingPost = playerViewModel.currentAudio.value.id,
                    duration = playerViewModel.duration, //fix Duration each Post
                    isPlaying = playerViewModel.isPlaying,
                    isStop = playerViewModel.isStop.value,
                    onPlayStart = {
                        if(post.url.isNotEmpty()){
                            playerViewModel.onUIEvents(UIEvents.PlayingAudio(
                                Audio(
                                    id = post.ID,
                                    title = post.audioName,
                                    url = post.url,
                                    author = userBaseInfo["username"]!!,
                                    duration = post.duration
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
                )
                Spacer(modifier = Modifier.height(8.dp))

                OptionBar(
                    itemList = listOf(
                        OptionItem(
                            icon = R.drawable.playlist_add_svgrepo_com,
                            onClick = {
                                  playerViewModel.onUIEvents(
                                      UIEvents.AddToPlaylist(
                                          Audio(
                                              id = post.ID,
                                              title = post.audioName,
                                              url = post.url,
                                              author = userBaseInfo["username"]!!,
                                              duration = post.duration
                                          )
                                      )
                                  )
                            },
                            des = "Add to Playlist"
                        )
                    ),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ){
                    InteractionRow(
                        interactions = Post_Interactions(
                            loveCounts = post.likes!!,
                            commentCounts = post.comments!!,
                            isLove = isLove.value,
                            love_act = {
                                interactionViewModel.loveAction(
                                    isLove = it,
                                    targetUserID = post.userId,
                                    postID = post.ID
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
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        interactionViewModel.savePost(post.ID){result ->
                            if(result != null){
                                if(result){
                                    isSaved.value = true
                                    Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                                } else {
                                    isSaved.value = false
                                    Toast.makeText(context, "Unsaved", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Unknown Error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Icon(
                            painter = if(isSaved.value) painterResource(id = R.drawable.ic_action_remove_ribbon) else painterResource(id = R.drawable.ic_actions_add_ribbon),
                            contentDescription = "Save",
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfo(
    isBlock: MutableState<Boolean>,
    navController: NavController,
    userInfo: Map<String, String>,
    userId: String,
    postDes: String,
    modifier: Modifier = Modifier
){

    if(userInfo.isEmpty()){
        CircularProgressIndicator()
    } else if(isBlock.value){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .then(modifier)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_security_locked),
                contentDescription = "Blocked Icon",
                tint = Color(0xFFD9534F),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "This user has been blocked.",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .then(modifier)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(userInfo["avatarUrl"].takeIf { !it.isNullOrEmpty() } ?: R.drawable.person)
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
                        navController.navigate("profile/$userId")
                    }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = userInfo["username"] ?: "Unknown",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.clickable {
                        //To Profile
                        navController.navigate("profile/$userId")
                    }
                )
                Box(
                    modifier = Modifier
                        .height(70.dp)
                        .fillMaxWidth(0.8f)
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = postDes,
                        style = TextStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
