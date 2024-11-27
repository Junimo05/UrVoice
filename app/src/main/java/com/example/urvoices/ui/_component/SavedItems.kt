package com.example.urvoices.ui._component

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.urvoices.R
import com.example.urvoices.data.db.Entity.SavedPost
import com.example.urvoices.data.model.Audio
import com.example.urvoices.data.model.Post
import com.example.urvoices.utils.formatToMinSecFromMillisec
import com.example.urvoices.viewmodel.MediaPlayerVM
import com.example.urvoices.viewmodel.ProfileViewModel
import com.example.urvoices.viewmodel.UIEvents
import kotlinx.coroutines.runBlocking

@Composable
fun SavedItems(
    navController: NavController,
    post: Post = Post(),
    savedPost: SavedPost = SavedPost(),
    playerVM: MediaPlayerVM,
    profileVM: ProfileViewModel,
    unSave: (String) -> Unit = {}
) {
    val TAG = "SavedItems"

    //User Basic Info
    val userInfo by remember(post.userId) {
        mutableStateOf(runBlocking {
            profileVM.loadUserBaseInfo(post.userId)
        })
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .height(200.dp)
            .width(200.dp)
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
            .clickable {
                if (post.ID != "") {
                    navController.navigate("post/${post.userId}/${post.ID}")
                } else {
                    navController.navigate("post/${savedPost.userID}/${savedPost.id}")
                }
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            //USER INFO
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if(userInfo.isNotEmpty() || savedPost.id.isNotEmpty()){
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(
                                if(!post.ID.isNullOrEmpty() || savedPost.id.isNotEmpty()) {
                                    if(savedPost.avatarUrl.isNotEmpty()) {
                                        savedPost.avatarUrl
                                    } else if(userInfo["avatarUrl"] != null && userInfo["avatarUrl"] != "") {
                                        userInfo["avatarUrl"]
                                    } else {
                                        R.drawable.person
                                    }
                                } else {
                                    R.drawable.person
                                }
                            )
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
                                if (!post.ID.isNullOrEmpty()) {
                                    navController.navigate("profile/${post.userId}")
                                } else {
                                    navController.navigate("profile/${savedPost.userID}")
                                }
                            }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if(!post.ID.isNullOrEmpty()){
                            userInfo["username"]!!.ifEmpty { "Unknown" }
                        } else {
                            savedPost.username.ifEmpty { "Unknown" }
                        },
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(32.dp)
                            .padding(8.dp)
                    )
                }
            }

            //POST INFO
            if(post.deletedAt == null){
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        (if(post.ID!!.isNotEmpty() || savedPost.id.isNotEmpty()) {
                            if(savedPost.audioName.isNotEmpty()) {
                                savedPost.audioName
                            } else if(post.audioName!!.isNotEmpty()) {
                                post.audioName
                            } else {
                                "No Name"
                            }
                        } else {
                            "No Name"
                        }).let {
                            Text(
                                text = it,
                                style = TextStyle(
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        AnimatedVisibility(
                            visible = playerVM.isPlaying && playerVM.currentAudio.value.id == post.ID,
                            enter = slideInVertically(initialOffsetY = { -it }),
                            exit = slideOutVertically(targetOffsetY = { -it })
                        ){
                            Text(
                                text = "${playerVM.progressString}/${formatToMinSecFromMillisec(playerVM.durationPlayer)}",
                                style = TextStyle(
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            )
                        }
                    }
                }
                if(!post.ID.isNullOrEmpty()){
                    IconButton(onClick = {
                        if(playerVM.currentAudio.value.id != post.ID) {
                            if(post.url!!.isNotEmpty()){
                                playerVM.onUIEvents(UIEvents.PlayingAudio(
                                    Audio(
                                        id = post.ID,
                                        url = post.url,
                                        title = post.audioName!!,
                                        duration = post.duration?:0,
                                        author = userInfo["username"]!!,
                                    )
                                ))
                            }
                        } else if(!playerVM.isEnd) {
                            playerVM.onUIEvents(UIEvents.PlayPause)
                        } else {
                            playerVM.onUIEvents(UIEvents.SeekTo(0F))
                        }
                    }) {
                        Icon(
                            painter = if(playerVM.isPlaying && playerVM.currentAudio.value.id == post.ID) painterResource(id = R.drawable.ic_media_pause) else painterResource(id = R.drawable.ic_media_play),
                            contentDescription = "Play/Pause",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                if(savedPost.id.isNotEmpty()){
                    IconButton(onClick = {
                        unSave(savedPost.id)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_action_remove_ribbon),
                            contentDescription = "Delete",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ){
                    //Deleted Post
                    Text(
                        text = "Post isn't available",
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
