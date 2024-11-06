package com.example.urvoices.ui._component.PostComponent

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.urvoices.viewmodel.CommentState
import com.example.urvoices.viewmodel.CommentViewModel
import com.example.urvoices.viewmodel.InteractionRowViewModel
import com.example.urvoices.viewmodel.PostDetailState
import com.example.urvoices.viewmodel.PostDetailViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedMutableState",
    "StateFlowValueCalledInComposition", "RememberReturnType"
)
@Composable
fun CommentItem(
    navController: NavController,
    uiState: State<PostDetailState>,
    comment: Comment,
    parentCmtUsername: String = "",
    index: Int,
    lastParentCommentID: MutableState<String>,
    postDetailViewModel: PostDetailViewModel,
    interactionViewModel: InteractionRowViewModel,
    commentViewModel: CommentViewModel,
    loadReply: (String) -> Unit,
    replyAct: (Comment, String) -> Unit,
    depth: Int = 0
    //comment data

    //interactions data
){
    val TAG = "CommentItem"

    var isLove by remember {
        mutableStateOf(false)
    }

    var replies by remember(comment.id) {
        mutableStateOf(emptyList<Comment>())
    }

//    val interactionViewModel = hiltViewModel<InteractionRowViewModel>(
//        key = comment.id
//    )

    val userInfo by remember(comment.userId) {
        mutableStateOf(runBlocking { commentViewModel.getUserInfo(comment.userId) })
    }

    var totalReplyByTime = 0
    val isExpandedComment = remember { mutableStateOf(false) }
    val isExpandedReply = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(replies) {
        totalReplyByTime += replies.size
    }

    LaunchedEffect(Unit) {
        interactionViewModel.getLoveStatus(commentID = comment.id!!) {
            isLove = it
        }
    }

    val contentString = buildAnnotatedString {
        val words = comment.content.split(" ")
        var tagFound = false
        for (word in words) {
            if (word.startsWith("@") && !tagFound) {
                tagFound = true
                pushStringAnnotation(tag = "profile_tag", annotation = word)
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Blue, textDecoration = TextDecoration.Underline)) {
                    append("$word ")
                }
                pop()
            } else {
                append("$word ")
            }
        }
    }

    Card(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(
                start = if (depth > 0) 32.dp else 0.dp,  // Add indent for replies
                end = if (depth > 0) 8.dp else 0.dp, // Add indent for replies
                top = 8.dp,
                bottom = 8.dp
            )
            .fillMaxWidth()
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box (
                modifier = Modifier.fillMaxWidth()
            ){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterEnd),
                ) {
                    if (userInfo.isNotEmpty()){
                        Column(
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(userInfo["avatar"])
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
                                text = userInfo["username"] ?: "Unknown",
                                maxLines = 1,
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .clickable {
                                        navController.navigate("profile/${comment.userId}")
                                    }
                            )

                            Text(
                                text = contentString,
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    //TODO: Add click listener to tag
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
                            isLove = isLove,
                            loveCounts = comment.likes,
                            love_act = {
                                   //TODO: "Implement love action"
//                                interactionViewModel.loveAction(
//                                    isLove = it,
//                                    targetUserID = comment.userId,
//                                    commentID = comment.id!!,
//                                    postID = comment.postId
//                                )
                            },
                            commentCounts = comment.replyComments,
                            comment_act = {
                                  if(comment.replyComments > 0){
                                      isExpandedReply.value = !isExpandedReply.value
                                      commentViewModel.loadMoreReplyComments(comment.id!!) { result ->
                                          Log.e(TAG, "loadMoreReplyComments: ${result.size}")
                                          replies = result
                                      }
//                                      loadReply(comment.id!!)
                                  }
                            },
                            reply_act = {
                                replyAct(comment, userInfo["username"]!!)
                            }
                        ))
                    } else {
                        CircularProgressIndicator()
                    }
                }
            }
            if (isExpandedReply.value) {
                if(replies.isNotEmpty()){
                    replies.forEachIndexed { index, reply ->
                        CommentItem(
                            navController = navController,
                            uiState = uiState,
                            comment = reply,
                            parentCmtUsername = userInfo["username"]!!, //ten user cua comment cha
                            index = index,
                            lastParentCommentID = lastParentCommentID,
                            postDetailViewModel = postDetailViewModel,
                            interactionViewModel = interactionViewModel,
                            commentViewModel = commentViewModel,
                            depth = depth + 1,
                            loadReply = {
                                loadReply(it)
                            },
                            replyAct = {comment, parentCmtUsername ->
                                replyAct(comment, parentCmtUsername)
                            }
                        )
                    }
                    if(totalReplyByTime < comment.replyComments){
                        Text(
                            text = "Load more",
                            modifier = Modifier.clickable {
                                loadReply(comment.id!!)
                            }
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator()
                    }
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
//                startRecording = Offset(0f, 20.dp.toPx()),
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
