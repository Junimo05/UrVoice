package com.example.urvoices.ui._component.PostComponent

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.ui._component.InteractionRow
import com.example.urvoices.ui._component.ProfileComponent.Comment
import com.example.urvoices.utils.Comment_Interactions

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedMutableState")
@Composable
fun CommentItem(
    navController: NavController,
    loadReply: (String) -> Unit,
    //comment data

    //interactions data
){
    val isExpandedComment = mutableStateOf(false)
    var isExpandedReply by remember { mutableStateOf(false) }
    var showReplyField by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    var replies by remember { mutableStateOf(emptyList<Comment>()) }

    Card(
        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //Avatar
                Image(
                    painter = painterResource(id = R.drawable.person),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Black, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "User Name",
                    maxLines = 1,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_actions_more_2),
                    contentDescription = "Menu",
                    modifier = Modifier.size(24.dp)
                )
            }

            // Comment content
            Text(
                text = "This is a comment dawioudjawdoiawjdoiwajdoiawdjaowidjawoidjawoiddjaoiwjdoiawdjawoidjdjioawjdoaiwjdwaoidawoidjwaoidjwaioddadwwdwajwaodijwaoidjwaoidjwaoidjawdoiwajdoaiwdjwaoidjawoidaj",
                modifier = Modifier
                    .padding(16.dp)
                    .clickable { isExpandedComment.value = !isExpandedComment.value }, // Toggle expanded status on click
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp
                ),
                overflow = if (isExpandedComment.value) TextOverflow.Visible else TextOverflow.Ellipsis, // Control overflow based on expanded status
                maxLines = if (isExpandedComment.value) Int.MAX_VALUE else 3 // Control max lines based on expanded status
            )
            // Comment interaction
            InteractionRow(interactions = Comment_Interactions(
                loveCounts = 0,
                love_act = {

                },
                commentCounts = 0,
                comment_act = {
                    isExpandedReply = !isExpandedReply
                    loadReply("comment_id")
                },
                reply_act = {

                }
            ))
            // Replies
            if (isExpandedComment.value && replies.isNotEmpty()) {
                replies.forEach { reply ->
                    CommentItem(
                        navController = navController,
                        loadReply = { loadReply(it) }
                        //reply data
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommentItemPreview() {
    MyTheme {
        CommentItem(
            navController = rememberNavController(),
            loadReply = {}
        )
    }
}