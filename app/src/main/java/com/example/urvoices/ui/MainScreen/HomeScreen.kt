package com.example.urvoices.ui.MainScreen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.work.impl.utils.ForceStopRunnable.BroadcastReceiver
import com.example.urvoices.R
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.ui._component.PostComponent.NewFeedPostItem
import com.example.urvoices.utils.Navigator.AuthScreen
import com.example.urvoices.utils.Navigator.MainScreen
import com.example.urvoices.utils.UserPreferences
import com.example.urvoices.viewmodel.AuthState
import com.example.urvoices.viewmodel.AuthViewModel
import com.example.urvoices.viewmodel.HomeState
import com.example.urvoices.viewmodel.HomeViewModel
import com.example.urvoices.viewmodel.MediaPlayerVM
import com.example.urvoices.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    playerViewModel: MediaPlayerVM,
    notificationVM: NotificationViewModel
) {
    Home(navController, authViewModel, homeViewModel,notificationVM ,playerViewModel)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
@Composable
fun Home(
    navController: NavController,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    notificationVM: NotificationViewModel,
    playerViewModel: MediaPlayerVM,
    modifier: Modifier = Modifier
){
    val authState = authViewModel.authState.observeAsState()
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    val mainStateList = rememberLazyListState()
    val homeState = homeViewModel.homeState.collectAsState()
    val isRefreshing by homeViewModel.isRefreshing.collectAsStateWithLifecycle()
    val scrollToTopEvent by homeViewModel.scrollToTopEvent.collectAsState()



    val isScrolled = remember {
        mutableStateOf(mainStateList.firstVisibleItemIndex > 0)
    }

    val newNoti = remember{ mutableStateOf(false)}

    DisposableEffect(Unit) {
        val receiver =
        @SuppressLint("RestrictedApi")
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                // Update state
                Log.e("HomeScreen", "New Notification Received")
                newNoti.value = true
                Log.e("HomeScreen", "New Notification Received ${newNoti.value}")
                // Gọi ViewModel để xử lý thêm nếu cần
                scope.launch {
                    notificationVM.refreshNotifications()
                }
            }
        }

        val intentFilter = IntentFilter("NEW_NOTIFICATION_RECEIVED")
        context.registerReceiver(receiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)

        // When the effect leaves the Composition, remove the callback
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    val postList = homeViewModel.postList.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        scope.launch {
            notificationVM.refreshNotifications()
        }
        homeViewModel.checkFirstLogin()
    }

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> {
                navController.navigate(AuthScreen.LoginScreen.route)
            }
            else -> Unit
        }
     }

    LaunchedEffect(scrollToTopEvent){
        if (scrollToTopEvent) {
            mainStateList.animateScrollToItem(0)
            homeViewModel.resetScrollToTopEvent()
            //
            homeViewModel.refreshHomeScreen()
        }
    }

    LaunchedEffect(postList.loadState) {
        when (postList.loadState.refresh) {
            is LoadState.Loading -> {
                homeViewModel.setIsRefreshing(true)
            }
            is LoadState.NotLoading -> {
                homeViewModel.setIsRefreshing(false)
            }
            is LoadState.Error -> {
                homeViewModel.setIsRefreshing(false)
            }
        }
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(visible = !isScrolled.value) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.background)
                        .drawBehind {
                            val strokeWidth = 2.dp.toPx()
                            val y = size.height - strokeWidth / 2
                            drawLine(
                                color = colorScheme.onSurfaceVariant,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        }
                    ,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = "Home",
                        modifier = Modifier
                            .padding(16.dp)
                        ,
                        style = TextStyle(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        //Notification
                        Icon(
                            painter = painterResource(id = if(newNoti.value) R.drawable.notification_on_svgrepo_com else R.drawable.notification_svgrepo_com),
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .padding(end = 4.dp)
                                .clickable {
                                    navController.navigate(MainScreen.NotificationScreen.route)
                                }
                        )
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_contact_message),
//                            contentDescription = null,
//                            modifier = Modifier
//                                .size(36.dp)
//                                .padding(end = 4.dp)
//                                .clickable {
//                                    //TODO: Implement Message
//                                }
//                        )
                    }
                }
            }

        }
    ) {paddingValue ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                homeViewModel.refreshHomeScreen()
            },
            modifier = Modifier.padding(top = 2.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValue.calculateTopPadding())
                    .background(MaterialTheme.colorScheme.background),
                userScrollEnabled = true,
                state = mainStateList,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if(isRefreshing){
                    //refreshing time
                } else {
                    items(
                        count = postList.itemCount,
                        key = { postList[it]?.ID ?: it}
                    ){
                        index ->
                        NewFeedPostItem(
                            navController = navController,
                            authVM = authViewModel,
                            post = postList[index]!!,
                            homeViewModel = homeViewModel,
                            playerViewModel = playerViewModel,
                        )
                    }
                }

                postList.apply {
                    when {
                        loadState.refresh is LoadState.Loading -> {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        loadState.append is LoadState.Loading -> {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
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
        }
    }
}