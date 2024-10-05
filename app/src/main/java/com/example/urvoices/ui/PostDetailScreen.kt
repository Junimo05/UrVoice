package com.example.urvoices.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.urvoices.R
import com.example.urvoices.ui._component.PostComponent.CommentItem
import com.example.urvoices.ui._component.InteractionRow
import com.example.urvoices.ui._component.PostComponent.AudioWaveformItem
import com.example.urvoices.ui._component.TopBarBackButton
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.utils.Post_Interactions

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PostDetail(
    navController: NavController,
) {
    var scrollThroughContentDetail = remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect { index ->
            when {
                index >= 2 -> {
                    // scroll to content detail
                    scrollThroughContentDetail.value = true
                }
                index < 2 -> {
                    // scroll to profile detail
                    scrollThroughContentDetail.value = false
                }
                else -> {

                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopBarBackButton(
                navController = navController,
                title = "Luca Morrison",
                endIcon = R.drawable.ic_actions_more_1,
                endIconAction = {
                    /*TODO*/
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {it ->
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            state = listState,
            modifier = Modifier.padding(it),
        ){
            item {
                ProfileDetail()
            }
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
            stickyHeader {
                ContentDetail(
                    scrollThroughContentDetail = scrollThroughContentDetail
                )
            }
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
            items(10) {
                CommentItem()
            }
        }
    }
}

@Composable
fun ProfileDetail(

){
    Column(
        modifier = Modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.person),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Black, CircleShape)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Luca Morrison",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "@lucamorrison",
            color = Color.Gray,
            style = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier.size(120.dp, 40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                border = BorderStroke(
                    1.dp,
                    Color(0xFF000000)
                )
            ) {
                Text(
                    text = "Follow",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier.size(120.dp, 40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                border = BorderStroke(
                    1.dp,
                    Color(0xFF000000)
                )
            ) {
                Text(
                    text = "Message",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun ContentDetail(
    scrollThroughContentDetail: MutableState<Boolean>
){
    val transitionVisible = remember {
        mutableStateOf(true)
    }

    val contentExpanded = remember {
        mutableStateOf(false)
    }

    LaunchedEffect(scrollThroughContentDetail.value) {
        transitionVisible.value = !scrollThroughContentDetail.value
        Log.e("scrollChange", "Scroll" + scrollThroughContentDetail.value)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(4.dp),
    ) {
        AnimatedVisibility(
            visible = transitionVisible.value,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Card(
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .heightIn(min = 100.dp)
                    .clickable {
                        contentExpanded.value = !contentExpanded.value
                    }
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = "Content Info doiawjdoawidjwaoidajwdoiwajdaoiwjdaoiwjdaoiwjcvndjaiowdjwadjaowidjaowidojiawdjawoidjaoiwdjaoiwdjwaodiojawoidjawoidjawoidjawoidjawdoiajw",
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 16.dp),
                    style = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    maxLines = if (contentExpanded.value) Int.MAX_VALUE else 4,
                    overflow = if (contentExpanded.value) TextOverflow.Visible else TextOverflow.Ellipsis
                )
            }
        }

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

        AudioWaveformItem(id = "", isPlaying = false, duration = 100, onPlayStart = {} ,percentPlayed = 0.5f, onPercentChange = {}, onPlayPause = {}, isStop = false, currentPlayingAudio = 0, currentPlayingPost = "", audioUrl = "", audioAmplitudes = amplitudesTest.value)
        InteractionRow(Post_Interactions(
            loveCounts = 0,
            commentCounts = 0,
            love_act = {},
            comment_act = {},
        ))
    }
}

//@Preview(showBackground = true)
//@Composable
//fun ProfileDetailPreview() {
//    ProfileDetail()
//}
//
//@Preview(showBackground = true)
//@Composable
//fun ContentDetailPreview() {
//    ContentDetail()
//}

@Preview(showBackground = true)
@Composable
fun PostDetailPreview() {
    val navController = rememberNavController()
    MyTheme {
        PostDetail(navController)
    }
}