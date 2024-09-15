package com.example.urvoices.ui._component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import com.example.urvoices.R
import com.example.urvoices.presentations.theme.MyTheme

@Composable
fun NotificationItem (
    username: String,
    message: String,
    time: String
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable {
                //TODO
            }
            .background(MaterialTheme.colorScheme.background)
    ){
        Image(
            painter = painterResource(id = R.drawable.person),
            contentDescription = "Avatar",
            modifier = Modifier
                .padding(start = 10.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                .size(64.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Black, CircleShape)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center
        ){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                ,
            ){
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        ) {
                            append(username)
                        }
                        append(" ")
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp
                            )
                        ) {
                            append(message)
                        }
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = time,
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationItemPreview(){
    MyTheme {
        NotificationItem(
            username = "Luca Morrison",
            message = "liked your post kdapodadkapoddấdasdadweqwvfqwdfwqoa",
            time = "2 hours ago"
        )
    }
}