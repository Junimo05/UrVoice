package com.example.urvoices.ui._component.PostComponent

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.urvoices.R
import com.example.urvoices.data.model.Comment
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.ui._component.InteractionColumn
import com.example.urvoices.utils.Comment_Interactions
import com.example.urvoices.utils.getTimeElapsed
import com.example.urvoices.viewmodel.CommentViewModel
import com.example.urvoices.viewmodel.InteractionRowViewModel
import com.example.urvoices.viewmodel.PostDetailState
import com.example.urvoices.viewmodel.PostDetailViewModel
import kotlinx.coroutines.flow.StateFlow

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedMutableState",
    "StateFlowValueCalledInComposition"
)
@Composable
fun CommentItem(
    navController: NavController,
    uiState: State<PostDetailState>,
    comment: Comment,
    index: Int,
    lastParentCommentID: MutableState<String>,
    postDetailViewModel: PostDetailViewModel,
    loadReply: (String) -> Unit,
    depth: Int = 0
    //comment data

    //interactions data
){
    val TAG = "CommentItem"
    val interactionViewModel = hiltViewModel<InteractionRowViewModel>(
        key = comment.id
    )
    val commentViewModel = hiltViewModel<CommentViewModel>(
        key = comment.id
    )
    val (isLoadedUser, setIsLoaded) = rememberSaveable { mutableStateOf(false) }
    val userInfo by produceState(initialValue = mapOf(), producer = {
        value = commentViewModel.getUserInfo(comment.userId).let {
            setIsLoaded(true)
            it
        }
    })
    interactionViewModel.getLoveStatus(comment.id!!)

    var totalReplyByTime = 0
    val isExpandedComment = mutableStateOf(false)
    val isExpandedReply = rememberSaveable { mutableStateOf(false) }
    var showReplyField by remember { mutableStateOf(false) }

    val replyListFromViewModel by postDetailViewModel.replyLists.collectAsState(initial = emptyList())

    var replies by remember { mutableStateOf(emptyList<Comment>()) }

    LaunchedEffect(replyListFromViewModel) {
        replies = replyListFromViewModel
        totalReplyByTime += replies.size
    }

    Card(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp)
            .fillMaxWidth()
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(start = 25.dp * depth)

        ) {
            Box (
                modifier = Modifier.fillMaxWidth()
            ){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterEnd),
                ) {
                    if(isLoadedUser){
                        Column(
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(userInfo["avatarUrl"])
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Avatar",
                                placeholder = painterResource(id = R.drawable.person),
                                contentScale = ContentScale.Crop,
                                error = painterResource(id = R.drawable.person),
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.Black, CircleShape)
                                    .clickable {
                                        //To Profile
                                        navController.navigate("profile/${comment.userId}")
                                    }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = getTimeElapsed(comment.createdAt),
                                style = TextStyle(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .fillMaxWidth(0.9f)
                        ){
                            Text(
                                text = userInfo["username"]!!, // Username
                                maxLines = 1,
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                            )
                            Text(
                                text = "This is a comment dawioudjawdoidaojiwdjawoidjawoidajwdoiawdjawoidjawodiawjdoiawdjawoidjoiawjdaowidjawoidjwaoidjawoidawjdoiwajdoiawdjaowidjawoidjawoiddjaoiwjdoiawdjawoidjdjioawjdoaiwjdwaoidawoidjwaoidjwaioddadwwdwajwaodijwaoidjwaoidjwaoidjawdoiwajdoaiwdjwaoidjawoidaj",
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .clickable {
                                        isExpandedComment.value = !isExpandedComment.value
                                    }, // Toggle expanded status on click
                                style = TextStyle(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp
                                ),
                                overflow = if (isExpandedComment.value) TextOverflow.Visible else TextOverflow.Ellipsis, // Control overflow based on expanded status
                                maxLines = if (isExpandedComment.value) Int.MAX_VALUE else 5 // Control max lines based on expanded status
                            )
                        }
                        InteractionColumn(interactions = Comment_Interactions(
                            isLove = interactionViewModel.isLove,
                            loveCounts = comment.likes,
                            love_act = {
                                interactionViewModel.loveAction(
                                    isLove = it,
                                    targetUserID = comment.userId,
                                    commentID = comment.id,
                                    postID = comment.postId
                                )
                            },
                            commentCounts = comment.replyComments,
                            comment_act = {
                                isExpandedReply.value = !isExpandedReply.value
                                loadReply(comment.id)
                            },
                            reply_act = {
                                //TODO: Comment
                            }
                        ))
                    } else {
                        CircularProgressIndicator()
                    }
                }
            }
            // Replies
            if (isExpandedReply.value && replies.isNotEmpty()) {
                replies.forEachIndexed { index, reply ->
                    CommentItem(
                        navController = navController,
                        uiState = uiState,
                        comment = reply,
                        index = index,
                        lastParentCommentID = lastParentCommentID,
                        postDetailViewModel = postDetailViewModel,
                        depth = depth + 1,
                        loadReply = {
                            loadReply(it)
                        },
                    )
                }
                if(totalReplyByTime < comment.replyComments){
                    Text(
                        text = "Load more",
                        modifier = Modifier.clickable {
                            loadReply(comment.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectionLine(
    isLast: Boolean,
    lineColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
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
            color = lineColor,
            start = Offset(0f, 0f),
            end = Offset(0f, canvasHeight),
            strokeWidth = 2f
        )

        // Draw horizontal line for non-last items
//        if (!isLast) {
//            drawLine(
//                color = lineColor,
//                start = Offset(0f, 20.dp.toPx()),
//                end = Offset(canvasWidth, 20.dp.toPx()),
//                strokeWidth = 2f
//            )
//        }
        drawLine(
            color = lineColor,
            start = Offset(0f, 20.dp.toPx()),
            end = Offset(canvasWidth, 20.dp.toPx()),
            strokeWidth = 2f
        )
    }
}
