package com.example.urvoices.ui._component.PostComponent

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.urvoices.R
import com.example.urvoices.ui._component.InteractionRow
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.utils.Post_Interactions
@Composable
fun NewFeedPostItem(
    redrawTrigger: Int
) {
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
    var waveformProgress = rememberSaveable { mutableStateOf(0F)}

    DisposableEffect(key1 = redrawTrigger){
        onDispose {
            //Do nothing
        }
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
                ProfileInfo(modifier = Modifier.weight(1f))
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
                text = "2 hours ago",
                style = TextStyle(
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.padding(top = 4.dp, bottom = 0.dp, start = 14.dp, end = 0.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            AudioWaveformItem(
                duration = "4:12",
                isPlaying = true,
                percentPlayed = 0.5f,
                initAmplitudes = amplitudesTest.value,
                redrawTrigger = redrawTrigger,
                waveformProgress = waveformProgress
            )
            Spacer(modifier = Modifier.height(8.dp))
            InteractionRow(interactions = Post_Interactions(/*Todo interaction data*/))
        }
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
            Spacer(modifier = Modifier.height(10.dp))
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
    val redrawTrigger = remember {
        mutableStateOf(0)
    }
    MyTheme {
        NewFeedPostItem(redrawTrigger.value)
    }
}