package com.example.urvoices.ui._component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.urvoices.R
import com.example.urvoices.data.model.Post
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.viewmodel.MediaPlayerVM
import com.example.urvoices.viewmodel.UIEvents

@Composable
fun SavedItems(
    navController: NavController,
    post: Post,
    duration: String,
    playerVM: MediaPlayerVM
) {
    val TAG = "SavedItems"

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .height(200.dp)
            .width(200.dp)
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
            .clickable {
                navController.navigate("post/${post.userId}/${post.ID}")
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("")
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                placeholder = painterResource(id = R.drawable.person),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(8.dp) //outsidePadding
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color.Black, CircleShape)
                    .padding(6.dp) //insidePadding
            )
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
                    Text(
                        text = post.audioName!!,
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = duration,
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
            }

            IconButton(onClick = {
                if(playerVM.currentPlayingPost != post.ID) {
                    if(post.url!!.isNotEmpty()){
                        playerVM.onUIEvents(UIEvents.PlayingAudio(
                            post.url
                        ))
                        playerVM.updateCurrentPlayingPost(post.ID!!)
                    }
                } else {
                    playerVM.onUIEvents(UIEvents.PlayPause)
                }
            }) {
                Icon(
                    painter = if(playerVM.isPlaying) painterResource(id = R.drawable.ic_media_pause) else painterResource(id = R.drawable.ic_media_play),
                    contentDescription = "Play/Pause",
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
