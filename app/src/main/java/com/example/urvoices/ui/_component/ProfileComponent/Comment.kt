package com.example.urvoices.ui._component.ProfileComponent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.urvoices.R

data class Comment(
    val id: Int,
    val author: String,
    val content: String,
    val replies: List<Comment> = emptyList()
)

@Composable
fun CommentSection(comments: List<Comment>) {
    LazyColumn {
        items(comments) { comment ->
            CommentItem(comment = comment)
        }
    }
}

@Composable
fun CommentItem(comment: Comment, depth: Int = 0) {
    var isExpanded by remember { mutableStateOf(false) }
    var showReplyField by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = comment.author,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = comment.content,
                    fontSize = 14.sp
                )
            }
            if (comment.replies.isNotEmpty()) {
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        painter = painterResource(id = R.drawable.home),
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }
        }

        if (isExpanded && comment.replies.isNotEmpty()) {
            comment.replies.forEach { reply ->
                CommentItem(comment = reply, depth = depth + 1)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { showReplyField = !showReplyField }) {
                Text("Reply", color = Color.Blue)
            }
        }

        if (showReplyField) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                TextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Write a reply...") }
                )
                IconButton(
                    onClick = {
                        // Here you would typically add the reply to the comment
                        // For this example, we'll just clear the field and hide it
                        replyText = ""
                        showReplyField = false
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Post Reply")
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun InfiniteNestedCommentsScreen() {
    val sampleComments = listOf(
        Comment(
            id = 1,
            author = "User1",
            content = "This is a top-level comment.",
            replies = listOf(
                Comment(
                    id = 2,
                    author = "User2",
                    content = "This is a reply to the top-level comment.",
                    replies = listOf(
                        Comment(
                            id = 3,
                            author = "User3",
                            content = "This is a nested reply.",
                            replies = listOf(
                                Comment(
                                    id = 4,
                                    author = "User4",
                                    content = "This is a deeply nested reply."
                                )
                            )
                        )
                    )
                )
            )
        ),
        Comment(
            id = 5,
            author = "User5",
            content = "This is another top-level comment."
        )
    )

    Surface(color = MaterialTheme.colorScheme.background) {
        CommentSection(comments = sampleComments)
    }
}

@Preview(showBackground = true)
@Composable
fun InfiniteNestedCommentsScreenPreview() {
    InfiniteNestedCommentsScreen()
}