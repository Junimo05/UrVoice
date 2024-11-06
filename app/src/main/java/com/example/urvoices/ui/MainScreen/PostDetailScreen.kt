package com.example.urvoices.ui.MainScreen

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.paging.LoadState
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
import com.example.urvoices.viewmodel.CommentViewModel
import com.example.urvoices.viewmodel.InteractionRowViewModel
import com.example.urvoices.viewmodel.MediaPlayerVM
import com.example.urvoices.viewmodel.PostDetailState
import com.example.urvoices.viewmodel.PostDetailViewModel
import com.example.urvoices.viewmodel.UIEvents
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PostDetail(
    navController: NavController,
    playerViewModel: MediaPlayerVM,
    postDetailViewModel: PostDetailViewModel,
    authViewModel: AuthViewModel,
    postID: String = "",
    userID: String = ""
) {
    val currentUser = authViewModel.getCurrentUser()

    val uiState = postDetailViewModel.uiState.collectAsState()

    val commentViewModel = hiltViewModel<CommentViewModel>()
    val interactionViewModel = hiltViewModel<InteractionRowViewModel>()

    //Post Interaction Status
    val isLove = remember {
        mutableStateOf(false)
    }

    LaunchedEffect(postID, userID) {
//        Log.e("PostDetail", "PostID: $postID, UserID: $userID")
        postDetailViewModel.loadData(postID = postID, userID = userID)
        interactionViewModel.getLoveStatus(postID = postID){ result ->
            isLove.value = result
        }
    }

    val currentPost by postDetailViewModel.currentPost
    val userPost = postDetailViewModel.userPost
    val commentLists = postDetailViewModel.commentFlow.collectAsLazyPagingItems()

    // UI States
    val listState = rememberLazyListState()
    val scrollThroughContentDetail = remember { mutableStateOf(false) }

    //Reply State
    val focusRequester = remember { FocusRequester() }
    val replyTo = remember { mutableStateOf<Comment?>(null) }
    val parentUserName = remember { mutableStateOf("") }
    val commentText = remember { mutableStateOf("") }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                scrollThroughContentDetail.value = index >= 2
            }
    }

    Scaffold(
        topBar = {
            TopBarBackButton(
                navController = navController,
                title = userPost.username,
                endIcon = R.drawable.ic_actions_more_1,
                endIconAction = { /*TODO*/ },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            )
        },
        bottomBar = {
            CommentBar(
                uiState = uiState.value,
                currentUserName = currentUser?.displayName,
                onSendMessage = { message, replyID ->
                    postDetailViewModel.viewModelScope.launch {
//                        Log.e("CommentBar", "ReplyID: $replyID")
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
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Profile section
            item {
                ProfileDetail(
                    navController = navController,
                    user = userPost,
                    isFollowed = postDetailViewModel.isFollowed
                )
                Spacer(modifier = Modifier.height(30.dp))
            }

            // Content section
            item {
                if (uiState.value == PostDetailState.Success || currentPost.ID?.isNotEmpty() == true) {
                    ContentDetail(
                        navController = navController,
                        isLove = isLove,
                        interactionRowViewModel = interactionViewModel,
                        scrollThroughContentDetail = scrollThroughContentDetail,
                        playerViewModel = playerViewModel,
                        post = currentPost
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
            }

            // Comments section
            items(
                count = commentLists.itemCount,
                key = { index -> commentLists[index]?.id ?: index }
            ) { index ->
                commentLists[index]?.let { comment ->
                    CommentItem(
                        navController = navController,
                        uiState = uiState,
                        comment = comment,
                        index = index,
                        lastParentCommentID = postDetailViewModel.lastParentCommentID,
                        postDetailViewModel = postDetailViewModel,
                        interactionViewModel = interactionViewModel,
                        commentViewModel = commentViewModel,
                        loadReply = { commentID ->
                            postDetailViewModel.loadMoreReplyComments(commentID)
                        },
                        replyAct = { commentItem, parentCmtUsername ->
                            replyTo.value = commentItem
                            parentUserName.value = parentCmtUsername
                        }
                    )
                }
            }

            // Loading and error states for comments
            when {
                commentLists.loadState.append is LoadState.Loading -> {
                    item { LoadingItem() }
                }
                commentLists.loadState.append is LoadState.Error -> {
                    item { ErrorItem { commentLists.retry() } }
                }
            }
        }
    }
}

@Composable
private fun LoadingItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorItem(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Error loading comments")
        Button(onClick = onRetry) {
            Text(text = "Retry")
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
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
                    navController.navigate("profile/${user.id}")
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
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun ContentDetail(
    navController: NavController,
    isLove: MutableState<Boolean>,
    interactionRowViewModel: InteractionRowViewModel,
    scrollThroughContentDetail: MutableState<Boolean>,
    playerViewModel: MediaPlayerVM,
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
            id = post.ID!!,
            audioUrl = post.url!!,
            audioAmplitudes = post.amplitudes,
            currentPlayingAudio = playerViewModel.currentPlayingAudio,
            currentPlayingPost = playerViewModel.currentPlayingPost,
            duration = playerViewModel.duration,
            isPlaying = playerViewModel.isPlaying,
            isStop = playerViewModel.isStop.value,
            onPlayStart = {
                if(post.url.isNotEmpty()){
                    playerViewModel.onUIEvents(
                        UIEvents.PlayingAudio(
                            post.url
                        ))
                    playerViewModel.updateCurrentPlayingPost(post.ID)
                }
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
                isLove = isLove.value,
                loveCounts = post.likes!!,
                commentCounts = post.comments!!,
                love_act = {
                    isLove.value = it
                    interactionRowViewModel.loveAction(
                        isLove = it,
                        postID = post.ID,
                        targetUserID = post.userId
                    ) {result ->
                        if(result) post.likes = post.likes!! + 1
                    }
                },
                comment_act = {
                    //Do nothing
                }
            )
        )
    }
}
