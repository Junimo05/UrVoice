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
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.ui._component.InteractionRow
import com.example.urvoices.utils.Post_Interactions

@SuppressLint("UnrememberedMutableState")
@Composable
fun ProfilePostItem(
    title: String,
    starsCount: Int,
    commentsCount: Int,
    onPlayClick: () -> Unit
){
    val isExpandedContext = mutableStateOf(false)
    Box(
        modifier = Modifier.fillMaxWidth()
    ){
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .align(Alignment.Center)
                ,
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) // Light pink color
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ){
                Text(
                    text = title,
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    overflow = if(isExpandedContext.value) TextOverflow.Visible else TextOverflow.Ellipsis,
                    maxLines = if(isExpandedContext.value) Int.MAX_VALUE else 2,
                    modifier = Modifier.
                            padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 40.dp)
                        .clickable(onClick = { isExpandedContext.value = !isExpandedContext.value })
                )
                Spacer(modifier = Modifier.height(40.dp))
                InteractionRow(
                    interactions = Post_Interactions(),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        AudioWaveformItem(
            id = "Post",
            duration = 1000,
            isPlaying = true,
            percentPlayed = 0.5f,
            onPercentChange = {},
            onPlayStart = {},
            onPlayPause = {},
            isStop = false,
            currentPlayingAudio = 0,
            currentPlayingPost = "Post",
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
            title = "Title",
            starsCount = 10,
            commentsCount = 5,
            onPlayClick = {}
        )
    }
}
