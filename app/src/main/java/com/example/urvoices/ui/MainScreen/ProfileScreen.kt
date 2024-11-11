package com.example.urvoices.ui.MainScreen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.urvoices.R
import com.example.urvoices.data.model.User
import com.example.urvoices.ui._component.SavedItems
import com.example.urvoices.ui._component.PostComponent.ProfilePostItem
import com.example.urvoices.utils.Navigator.MainScreen
import com.example.urvoices.utils.processUsername
import com.example.urvoices.viewmodel.InteractionViewModel
import com.example.urvoices.viewmodel.MediaPlayerVM
import com.example.urvoices.viewmodel.ProfileState
import com.example.urvoices.viewmodel.ProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileScreen(
    userId: String = "",
    navController: NavController,
    playerViewModel: MediaPlayerVM,
    profileViewModel: ProfileViewModel,
) {
    val TAG = "ProfileScreen"
    val context = LocalContext.current
    val scope = CoroutineScope(Dispatchers.Main)

    //interactionVM init
    val interactionViewModel = hiltViewModel<InteractionViewModel>()

    //State & Data
    val uiState = profileViewModel.uiState.collectAsState()
    val postList = profileViewModel.posts.collectAsLazyPagingItems()
    val savedPostsList = profileViewModel.savedPosts.collectAsLazyPagingItems()

    val isUser by lazy {
        mutableStateOf(
            profileViewModel.isCurrentUser
        )
    }
    val user by lazy { mutableStateOf(
        profileViewModel.displayuser
    ) }


    var tab by rememberSaveable {
        mutableIntStateOf(0)
    }

    LaunchedEffect(userId) {
        profileViewModel.loadData(userId)
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
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
                horizontalArrangement = Arrangement.Start
            ){
                Row(
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Text(
                        text = user.value.username,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                IconButton(onClick = {
                     if(isUser.value){
                         navController.navigate(MainScreen.UploadScreen.route)
                     }else {
                         //TODO:
                     }
                },
                    modifier = Modifier.size(24.dp)

                ){
                    Icon(
                        painter = painterResource(id = if(isUser.value) R.drawable.ic_actions_add else R.drawable.ic_actions_more_1),
                        contentDescription = "ActionMore",
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.CenterVertically),
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(top = it.calculateTopPadding())
                .fillMaxSize()
                .padding(top = 8.dp)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User handle and name
            UserInfo(
                navController = navController,
                uiStates = uiState.value,
                isUser = isUser,
                user = user,
                followStatus = profileViewModel.isFollowed,
                postsCount = profileViewModel.postCounts,
                followingCount = profileViewModel.followings,
                followersCount = profileViewModel.followers,
                followAction = {
                    profileViewModel.followUser()
                },
            )
            Spacer(modifier = Modifier.height(4.dp))

            //Posts/Playlist
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .clickable {
                             tab = 0
                        }
                ) {
                    Text(
                        text = "Posts",
                        style = TextStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier
                        .height(4.dp)
                        .fillMaxWidth(fraction = 0.5f)
                        .background(if (tab == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                    )
                }
                Column(
                    modifier = Modifier
                        .clickable {
                            tab = 1
                        }
                ) {
                    Text(
                        text = "Urvoice Loving",
                        style = TextStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier
                        .height(4.dp)
                        .fillMaxWidth()
                        .background(if (tab == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
                    )
                } 
            }
            Spacer(modifier = Modifier.height(8.dp))



            // Grid of thumbnails
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ){
                if(tab == 0) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = true,
                        verticalArrangement = Arrangement.spacedBy(8.dp),

                    ) {
                        items(postList.itemCount) { index ->
                            ProfilePostItem(
                                navController = navController,
                                post = postList[index]!!,
                                playerViewModel = playerViewModel,
                                interactionViewModel = interactionViewModel
                            )
//                            Log.e(TAG, "PostItem: ${postList[index]!!.ID} && ${postList[index]!!.amplitudes}")
                        }

                        postList.apply {
                            when {
                                loadState.refresh is LoadState.Loading -> {
                                    item {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }
                                loadState.append is LoadState.Loading -> {
                                    item {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }
                                loadState.refresh is LoadState.Error -> {
                                    val e = postList.loadState.refresh as LoadState.Error
                                    item { Text(text = e.error.localizedMessage ?: "Unknown Error") }
                                }
                                loadState.append is LoadState.Error -> {
                                    val e = postList.loadState.append as LoadState.Error
                                    item { Text(text = e.error.localizedMessage ?: "Unknown Error") }
                                }
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        userScrollEnabled = true,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(savedPostsList.itemCount) { index ->
                            SavedItems(
                                navController = navController,
                                post = savedPostsList[index]!!,
                                playerVM = playerViewModel,
                                profileVM = profileViewModel
                            )
                        }

                        savedPostsList.apply {
                            when {
                                loadState.refresh is LoadState.Loading -> {
                                    item {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }
                                loadState.append is LoadState.Loading -> {
                                    item {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }
                                loadState.refresh is LoadState.Error -> {
                                    val e = savedPostsList.loadState.refresh as LoadState.Error
                                    item { Text(text = e.error.localizedMessage ?: "Unknown Error") }
                                }
                                loadState.append is LoadState.Error -> {
                                    val e = savedPostsList.loadState.append as LoadState.Error
                                    item { Text(text = e.error.localizedMessage ?: "Unknown Error") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun UserInfo(
    navController: NavController,
    uiStates: ProfileState,
    isUser: MutableState<Boolean>,
    user: MutableState<User>,
    postsCount: Int,
    followStatus: Boolean = false,
    followingCount: Int,
    followersCount: Int,
    followAction: () -> Unit,
    //messageAction: () -> Unit
){
    val infoList = listOf(
        "Posts" to postsCount,
        "Following" to followingCount,
        "Followers" to followersCount
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            ,
        verticalAlignment = Alignment.CenterVertically,
    ){
        // User Avatar/Name/Link
        Column(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .fillMaxHeight()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.value.avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                placeholder = painterResource(id = R.drawable.person),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color.Black, CircleShape)
                    .padding(6.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = user.value.username,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "@${processUsername(user.value.username)}",
                style = TextStyle(
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.inverseSurface)
                    .size(128.dp, 32.dp)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    //Link tag
                    text = user.value.country,
                    style = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        // Account Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .padding(top = 10.dp, end = 10.dp, bottom = 10.dp, start = 0.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                infoList.forEach { (title, count) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = count.toString(),
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                        )
                        Text(
                            text = title,
                            style = TextStyle(
                                fontWeight = FontWeight.Light,
                                fontSize = 12.sp
                            ),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f)
                    ,
            ){
                Text(
                    text = user.value.bio,
                    style = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.padding(15.dp),
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                if(isUser.value) {
                    Card(
                        modifier = Modifier
                            .padding(4.dp)
                            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable {
                                navController.navigate(MainScreen.ProfileScreen.EditProfileScreen.route)
                            }
                    ) {
                        Text(
                            text = "Edit Profile",
                            style = TextStyle(
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .width(100.dp)
                            .padding(4.dp)
                            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable {
                                followAction()
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (followStatus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        )

                    ) {
                        Text(
                            text = if (followStatus) "Following" else "Follow",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (followStatus) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                    Card(
                        modifier = Modifier
                            .width(100.dp)
                            .padding(4.dp)
                            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable {
                                //TODO: Message
                            }
                    ) {
                        Text(
                            text = "Message",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}
