package com.example.thevoices.presentations.components.PostComponent

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thevoices.R
import com.example.thevoices.presentations.components.InteractionRow
import com.example.thevoices.presentations.theme.MyTheme
import com.example.thevoices.utils.Post_Interactions
@Composable
fun NewFeedPostItem(
    //Todo: Add post data list
    //Todo: NavController
) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp)
    )
    {
        Row{
            ProfileInfo(modifier = Modifier.weight(1f))
            Icon(
                painter = painterResource(id = R.drawable.ic_actions_more_2),
                contentDescription = "ActionMore",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(0.1f)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        AudioWaveform(duration = "4:12", isPlaying = true, percentPlayed = 0.5f)
        Spacer(modifier = Modifier.height(8.dp))
        InteractionRow(interactions = Post_Interactions(/*Todo interaction data*/))
    }
}


@Composable
fun ProfileInfo(
    modifier: Modifier = Modifier
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .then(modifier)
    ) {
        Image(
            painter = painterResource(id = R.drawable.person),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Black, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = "Luca Morrison",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                ),
            )
            Text(
                text = "My story of moving to Japan",
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileNewFeedPreview() {
    MyTheme {
        NewFeedPostItem()
    }
}