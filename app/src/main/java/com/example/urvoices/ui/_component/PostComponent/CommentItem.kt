package com.example.urvoices.ui._component.PostComponent

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.urvoices.R
import com.example.urvoices.data.model.Comment
import com.example.urvoices.ui._component.DeleteConfirmationDialog
import com.example.urvoices.ui._component.InteractionColumn
import com.example.urvoices.utils.Comment_Interactions
import com.example.urvoices.utils.getTimeElapsed
import com.example.urvoices.viewmodel.CommentViewModel
import com.example.urvoices.viewmodel.InteractionViewModel
import com.example.urvoices.viewmodel.PostDetailState
import com.example.urvoices.viewmodel.PostDetailViewModel
import kotlinx.coroutines.runBlocking

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedMutableState",
    "StateFlowValueCalledInComposition", "RememberReturnType"
)
@Composable
fun CommentItem(
    navController: NavController,
    uiState: State<PostDetailState>,
    comment: Comment,
    postDetailViewModel: PostDetailViewModel,
    interactionViewModel: InteractionViewModel,
    commentViewModel: CommentViewModel,
    replyAct: (Comment, String) -> Unit,
    depth: Int = 0
){
    val TAG = "CommentItem"
    val currentUser = postDetailViewModel.currentUser
    val isLove = rememberSaveable { mutableStateOf(false) }

    var repliesLoading by remember(comment.id) {
        mutableStateOf(false)
    }
    var replies by remember(comment.id) {
        mutableStateOf(emptyList<Comment>())
    }


    val userInfo by remember(comment.userId) {
        mutableStateOf(runBlocking { commentViewModel.getUserInfo(comment.userId) })
    }

    var totalReplyByTime by remember(comment.userId) {
        mutableIntStateOf(0)
    }
    val isExpandedComment = remember { mutableStateOf(false) }
    val showDeleteAlert = rememberSaveable { mutableStateOf(false) }
    val isExpandedReply = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(replies) {
        totalReplyByTime += replies.size
    }

    LaunchedEffect(Unit) {
        interactionViewModel.getLoveStatus(commentID = comment.id!!) {
            isLove.value = it
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

    if(userInfo.isNotEmpty()){
        Card(
            modifier = Modifier
                .background(Color.Transparent)
                .padding(
                    start = if (depth > 0) 32.dp else 0.dp,  // Add indent for replies
                    end = if (depth > 0) 4.dp else 0.dp, // Add indent for replies
                    top = 4.dp,
                    bottom = 2.dp
                )
                .padding(5.dp)
                .clip(RoundedCornerShape(26.dp))
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            if (comment.userId == currentUser?.uid) {
                                showDeleteAlert.value = true
                            }
                        }
                    )
                }
        ){
            Column(
                modifier = Modifier

                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                    ,
                ) {
                    if (userInfo.isNotEmpty()){
                        Column(
                            modifier = Modifier.padding(top = 12.dp)
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
                                .padding(start = 16.dp, top = 12.dp)
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
                                    //TODO: Add click listener to tag -> redirect to profile
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
                            isLove = isLove.value,
                            loveCounts = comment.likes,
                            love_act = {
                                interactionViewModel.loveAction(
                                    isLove = it,
                                    targetUserID = comment.userId,
                                    commentID = comment.id!!,
                                    postID = comment.postId,
                                    callback = {result ->
                                        //get isLove input then update UI if result = true
//                                        Log.e(TAG, "love_act: $result")
                                        if(result){
                                            isLove.value = it
                                            if(isLove.value){
                                                comment.likes += 1
                                            } else {
                                                comment.likes -= 1
                                            }
                                        }
                                    }
                                )
                            },
                            commentCounts = comment.replyComments,
                            comment_act = {
                                if(comment.replyComments > 0){
                                    isExpandedReply.value = true
                                    repliesLoading = true
                                    loadMoreReply(
                                        commentID = comment.id!!,
                                        commentViewModel = commentViewModel,
                                        callback = { result ->
                                            replies = result
                                            repliesLoading = false
                                        }
                                    )
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
                if (isExpandedReply.value) {
                    if(!repliesLoading){
                        replies.forEachIndexed { _, reply ->
                            CommentItem(
                                navController = navController,
                                uiState = uiState,
                                comment = reply,
                                postDetailViewModel = postDetailViewModel,
                                interactionViewModel = interactionViewModel,
                                commentViewModel = commentViewModel,
                                depth = depth + 1,
                                replyAct = {comment, parentCmtUsername ->
                                    replyAct(comment, parentCmtUsername)
                                }
                            )
                        }
                        if(totalReplyByTime < comment.replyComments){
                            Text(
                                text = "Load more",
                                modifier = Modifier.clickable {
                                    loadMoreReply(
                                        commentID = comment.id!!,
                                        commentViewModel = commentViewModel,
                                        callback = { result ->
                                            replies = result
                                        }
                                    )
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

    if(showDeleteAlert.value) {
        DeleteConfirmationDialog(
            showDialog = showDeleteAlert,
            onConfirm = {
                commentViewModel.softDeleteComment(comment) { result ->
                    if (result) {
                        postDetailViewModel.triggerRefresh()
                    }
                }
            },
            onCancel = {
                showDeleteAlert.value = false
            }
        )
    }
}

fun loadMoreReply(commentID: String, commentViewModel: CommentViewModel, callback: (List<Comment>) -> Unit){
    commentViewModel.loadMoreReplyComments(commentID) { result ->
        callback(result)
    }
}

