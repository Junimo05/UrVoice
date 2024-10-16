import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

data class Comment(
    val id: String,
    val author: String,
    val content: String,
    val avatar: String,
    val replies: List<Comment> = emptyList()
)

@Composable
fun CommentThread(comments: List<Comment>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        comments.forEachIndexed { index, comment ->
            CommentItem(comment, isLast = index == comments.lastIndex, depth = 0)
        }
    }
}

@Composable
fun CommentItem(comment: Comment, isLast: Boolean, depth: Int) {
    Box(modifier = Modifier.fillMaxWidth()) {
        if (depth > 0) {
            ConnectionLine(isLast)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (depth * 16).dp, top = 8.dp, bottom = 8.dp, end = 16.dp)
        ) {
            AsyncImage(
                model = comment.avatar,
                contentDescription = "Avatar of ${comment.author}",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = comment.author, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = comment.content, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
    if (comment.replies.isNotEmpty()) {
        Column(modifier = Modifier.padding(start = ((depth + 1) * 16).dp)) {
            comment.replies.forEachIndexed { index, reply ->
                CommentItem(reply, isLast = index == comment.replies.lastIndex, depth = depth + 1)
            }
        }
    }
}

@Composable
fun ConnectionLine(isLast: Boolean) {
    Canvas(
        modifier = Modifier
            .fillMaxHeight()
            .width(40.dp)
            .padding(start = 20.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Draw vertical line
        drawLine(
            color = Color.Gray,
            start = Offset(0f, 0f),
            end = Offset(0f, canvasHeight),
            strokeWidth = 2f
        )

        // Draw horizontal line for non-last items
        if (!isLast) {
            drawLine(
                color = Color.Gray,
                start = Offset(0f, 20.dp.toPx()),
                end = Offset(canvasWidth, 20.dp.toPx()),
                strokeWidth = 2f
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommentThreadPreview() {
    val sampleComments = listOf(
        Comment(
            id = "1",
            author = "User 1",
            content = "This is the main comment",
            avatar = "https://picsum.photos/200",
            replies = listOf(
                Comment(
                    id = "2",
                    author = "User 2",
                    content = "This is a reply",
                    avatar = "https://picsum.photos/201",
                    replies = listOf(
                        Comment(
                            id = "3",
                            author = "User 3",
                            content = "This is a nested reply",
                            avatar = "https://picsum.photos/202"
                        )
                    )
                ),
                Comment(
                    id = "4",
                    author = "User 4",
                    content = "Another reply",
                    avatar = "https://picsum.photos/203"
                )
            )
        )
    )

    CommentThread(comments = sampleComments)
}