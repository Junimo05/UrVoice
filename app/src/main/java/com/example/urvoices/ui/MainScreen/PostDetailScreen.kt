package com.example.urvoices.ui.MainScreen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.urvoices.R
import com.example.urvoices.data.model.Comment
import com.example.urvoices.data.model.Post
import com.example.urvoices.data.model.User
import com.example.urvoices.ui._component.InteractionRow
import com.example.urvoices.ui._component.PostComponent.AudioWaveformItem
import com.example.urvoices.ui._component.PostComponent.CommentBar
import com.example.urvoices.ui._component.PostComponent.CommentItem
import com.example.urvoices.ui._component.TopBarBackButton
import com.example.urvoices.utils.Post_Interactions
import com.example.urvoices.viewmodel.AuthViewModel
import com.example.urvoices.viewmodel.InteractionRowViewModel
import com.example.urvoices.viewmodel.MediaPlayerViewModel
import com.example.urvoices.viewmodel.PostDetailState
import com.example.urvoices.viewmodel.PostDetailViewModel
import com.example.urvoices.viewmodel.UIEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PostDetail(
    navController: NavController,
    playerViewModel: MediaPlayerViewModel,
    postDetailViewModel: PostDetailViewModel,
    authViewModel: AuthViewModel,
    postID: String = "",
    userID: String = ""
){
    val TAG = "PostDetail"
    val currentUser = authViewModel.getCurrentUser()
    val interactionViewModel = hiltViewModel<InteractionRowViewModel>()
    interactionViewModel.getLoveStatus(postID = postID)
    val uiState = postDetailViewModel.uiState.collectAsState()

    //
    postDetailViewModel.loadData(postID = postID, userID = userID)
    val currentPost by lazy {
        postDetailViewModel.currentPost
    }
//    Log.e("PostDetail", "CurrentPost: $currentPost")
    val userPost = postDetailViewModel.userPost
    val commentLists = postDetailViewModel.commentLists.collectAsLazyPagingItems()

    val listState = rememberLazyListState()
    val scrollThroughContentDetail = remember { mutableStateOf(false) }

    //Reply State
    val focusRequester = remember { FocusRequester() }
    val replyTo = remember {mutableStateOf<Comment?>(null)}
    val parentUserName = remember {
        mutableStateOf("")
    }
    val commentText = remember { mutableStateOf("") }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect { index ->
            when {
                index >= 2 -> {
                    // scroll to content detail
                    scrollThroughContentDetail.value = true
                }
                index < 2 -> {
                    // scroll to profile detail
                    scrollThroughContentDetail.value = false
                }
                else -> {

                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopBarBackButton(
                navController = navController,
                title = userPost.username,
                endIcon = R.drawable.ic_actions_more_1,
                endIconAction = {
                    /*TODO*/
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            )
        },
        bottomBar = {
            CommentBar(
                uiState = uiState.value,
                currentUserName = currentUser?.displayName,
                onSendMessage = { message, replyID ->
                    CoroutineScope(Dispatchers.Main).launch {
                        postDetailViewModel.sendComment(message, replyID)
                        commentText.value = ""
                        replyTo.value = null
                    }
                },
                replyTo = replyTo,
                parentUsername = parentUserName,
                commentText = commentText,
                onAttachFile = { /*TODO*/ },
                focusRequester = focusRequester,

            )
        },
        modifier = Modifier.fillMaxSize()
    ) {it ->
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            state = listState,
            modifier = Modifier.padding(it),
        ) {
            item {
                ProfileDetail(
                    navController = navController,
                    user = userPost,
                    isFollowed = postDetailViewModel.isFollowed
                )
            }
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
            stickyHeader {
                if (uiState.value == PostDetailState.Success || currentPost.value.id != "") {
                    ContentDetail(
                        interactionRowViewModel = interactionViewModel,
                        scrollThroughContentDetail = scrollThroughContentDetail,
                        playerViewModel = playerViewModel,
                        post = currentPost.value
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
            items(commentLists.itemCount, key = { index -> index }) { index ->
                CommentItem(
                    navController = navController,
                    uiState = uiState,
                    comment = commentLists[index]!!,
                    index = index,
                    lastParentCommentID = postDetailViewModel.lastParentCommentID,
                    postDetailViewModel = postDetailViewModel,
                    loadReply = { commentID ->
                        postDetailViewModel.loadMoreReplyComments(commentID)
                    },
                    replyAct = { comment, parentCmtUsername ->
                        replyTo.value = comment
                        parentUserName.value = parentCmtUsername
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileDetail(
    user: User,
    isFollowed: Boolean,
    navController: NavController
){
    Column(
        modifier = Modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Avatar",
            placeholder = painterResource(id = R.drawable.person),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Black, CircleShape)
                .clickable {
                    //TODO: navigate to user profile
                }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = user.username,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "@${user.username}",
            color = Color.Gray,
            style = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier.size(120.dp, 40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowed) Color(0xFF000000) else MaterialTheme.colorScheme.secondaryContainer,
                ),
                border = BorderStroke(
                    1.dp,
                    Color(0xFF000000)
                )
            ) {
                Text(
                    text = if(isFollowed) "Following" else "Follow",
                    color = if(isFollowed) Color(0xFFFFFFFF) else  MaterialTheme.colorScheme.onSecondaryContainer,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { /*TODO Message*/ },
                modifier = Modifier.size(120.dp, 40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                border = BorderStroke(
                    1.dp,
                    Color(0xFF000000)
                )
            ) {
                Text(
                    text = "Message",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun ContentDetail(
    interactionRowViewModel: InteractionRowViewModel,
    scrollThroughContentDetail: MutableState<Boolean>,
    playerViewModel: MediaPlayerViewModel,
    post: Post,
){
    val transitionVisible = remember {
        mutableStateOf(true)
    }

    val contentExpanded = remember {
        mutableStateOf(false)
    }

    LaunchedEffect(scrollThroughContentDetail.value) {
        transitionVisible.value = !scrollThroughContentDetail.value
//        Log.e("scrollChange", "Scroll" + scrollThroughContentDetail.value)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(4.dp),
    ) {
        AnimatedVisibility(
            visible = transitionVisible.value,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Card(
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .heightIn(min = 100.dp)
                    .clickable {
                        contentExpanded.value = !contentExpanded.value
                    }
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = post.description,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 16.dp),
                    style = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    maxLines = if (contentExpanded.value) Int.MAX_VALUE else 4,
                    overflow = if (contentExpanded.value) TextOverflow.Visible else TextOverflow.Ellipsis
                )
            }
        }

        AudioWaveformItem(
            id = post.id!!,
            audioUrl = post.url!!,
            audioAmplitudes = post.amplitudes,
            currentPlayingAudio = playerViewModel.currentPlayingAudio,
            currentPlayingPost = playerViewModel.currentPlayingPost,
            duration = playerViewModel.duration,
            isPlaying = playerViewModel.isPlaying,
            isStop = playerViewModel.isStop.value,
            onPlayStart = {
                playerViewModel.onUIEvents(
                    UIEvents.PlayingAudio(
                    post.url
                ))
                playerViewModel.updateCurrentPlayingPost(post.id)
            },
            onPlayPause = {
                playerViewModel.onUIEvents(UIEvents.PlayPause)
            },
            percentPlayed = playerViewModel.progress,
            onPercentChange = {
                playerViewModel.onUIEvents(UIEvents.SeekTo(it))
            },
        )

        InteractionRow(
            interactions = Post_Interactions(
                isLove = interactionRowViewModel.isLove,
                loveCounts = post.likes,
                commentCounts = post.comments,
                love_act = {
                    interactionRowViewModel.loveAction(
                        isLove = it,
                        postID = post.id,
                        targetUserID = post.userId
                    )
                },
                comment_act = {
                    //TODO: Open keyboard
                })
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun PostDetailPreview() {
//    val navController = rememberNavController()
//    MyTheme {
//        PostDetail(
//            navController,
//            "1",
//
//        )
//    }
//}