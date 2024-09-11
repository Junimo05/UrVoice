package com.example.thevoices.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.thevoices.R
import com.example.thevoices.presentations.components.CommentItem
import com.example.thevoices.presentations.components.InteractionRow
import com.example.thevoices.presentations.components.PostComponent.AudioWaveform
import com.example.thevoices.presentations.components.TopBarBackButton
import com.example.thevoices.presentations.theme.MyTheme
import com.example.thevoices.utils.Post_Interactions

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
                title = "Luca Morrison"
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
                .size(64.dp)
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
                    containerColor = Color(0xFFFFFFFF),
                ),
                border = BorderStroke(
                    1.dp,
                    Color(0xFF000000)
                )
            ) {
                Text(
                    text = "Follow",
                    color = Color(0xFF000000),
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
                    containerColor = Color(0xFFFFFFFF),
                ),
                border = BorderStroke(
                    1.dp,
                    Color(0xFF000000)
                )
            ) {
                Text(
                    text = "Message",
                    color = Color(0xFF000000),
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

    LaunchedEffect(scrollThroughContentDetail.value) {
        transitionVisible.value = !scrollThroughContentDetail.value
        Log.e("scrollChange", "Scroll" + scrollThroughContentDetail.value)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFFFF))
            .padding(4.dp),
    ) {
        AnimatedVisibility(
            visible = transitionVisible.value,
        ) {
            Text(
                text = "Content Info",
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 16.dp),
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp
                )
            )
        }
        AudioWaveform(isPlaying = false, duration = "4:12", percentPlayed = 0.5f)
        InteractionRow(Post_Interactions(/*Todo interaction data*/))
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