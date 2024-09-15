package com.example.urvoices.ui.noti_msg

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.urvoices.R
import com.example.urvoices.presentations.theme.MyTheme


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ChatScreen(
    navController: NavController
) {

    var textMsg by remember {
        mutableStateOf("")
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            Row(
               modifier = Modifier
                   .fillMaxWidth()
                   .background(MaterialTheme.colorScheme.surfaceVariant)
                   .height(80.dp)
                   .drawBehind {
                       val strokeWidth = 2.dp.toPx()
                       val y = size.height - strokeWidth / 2
                       drawLine(
                           color = Color.Black,
                           start = Offset(0f, y),
                           end = Offset(size.width, y),
                           strokeWidth = strokeWidth
                       )
                   },
                verticalAlignment = Alignment.CenterVertically,
            ){
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.width(3.dp))
                Image(
                    painter = painterResource(id = R.drawable.person),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, Color.Black, CircleShape)
                        .padding(6.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Lucas Morrison",
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_actions_more_1),
                        contentDescription = "More"
                    )
                }
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                ,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_actions_add),
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.width(2.dp))
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_actions_add_file),
                        contentDescription = "Camera",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Card(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    TextField(
                        value = textMsg,
                        onValueChange = {
                            textMsg = it
                        },
                        maxLines = 1,
                        placeholder = {
                            Text(
                                text = "Type a message...",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        },
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface),
                        enabled = true,

                    )
                }

                IconButton(onClick = {
                    /*TODO*/
                    textMsg = "" // Clear the text field

                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_send),
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        },
    ) {

    }
}

@Composable
fun MessageCard(
    message: String,
    time: String,
    isCurrentUser: Boolean
) {
    val backgroundColor = if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ) ,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .widthIn(min = 100.dp)
        ) {
            Text(
                text = message,
                color = textColor,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 8.dp),
            )
            Text(
                text = time,
                color = textColor,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessageCardPreview() {
    MyTheme {
        MessageCard(
            message = "Hello",
            time = "00.00",
            isCurrentUser = true
        )
    }
}



@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    MyTheme {
        ChatScreen(navController = rememberNavController())
    }
}