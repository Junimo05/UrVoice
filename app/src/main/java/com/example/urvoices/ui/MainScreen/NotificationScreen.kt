package com.example.urvoices.ui.MainScreen

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.urvoices.data.db.Entity.Notification
import com.example.urvoices.ui._component.NotificationItem
import com.example.urvoices.ui._component.TopBarBackButton
import com.example.urvoices.utils.TypeNotification
import com.example.urvoices.utils.getTimeElapsed
import com.example.urvoices.viewmodel.NavigationEvent
import com.example.urvoices.viewmodel.NotificationEvent
import com.example.urvoices.viewmodel.NotificationViewModel
import com.google.android.material.snackbar.Snackbar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NotificationScreen(
    navController: NavController,
    notificationVM: NotificationViewModel
) {
    val TAG = "NotificationScreen"
    val scope = rememberCoroutineScope()
    val context = LocalContext.current


    val listState = rememberLazyListState()
    val navigationEvent = notificationVM.navigationEvents.collectAsState(initial = null)
    val isRefreshing = notificationVM.isRefreshing.collectAsStateWithLifecycle()

    val notiList = notificationVM.notifications.collectAsLazyPagingItems()

    val recentNotifications = notiList.itemSnapshotList.filter { it?.createdAt!! > System.currentTimeMillis() - 86400000 } // < 1days
    val earlierNotifications = notiList.itemSnapshotList.filter { it?.createdAt!! < System.currentTimeMillis() - 86400000 } // >1days

    LaunchedEffect(Unit) {
        notificationVM.refreshNotifications()
        notificationVM.cleanupOldNotifications()
    }

    LaunchedEffect(navigationEvent.value) {
        when(val event = navigationEvent.value){
            is NavigationEvent.NavigateToComment -> TODO()
            is NavigationEvent.NavigateToPost -> {
                navController.navigate("post/${event.userID}/${event.postID}")
            }
            is NavigationEvent.NavigateToUser -> {
                navController.navigate("profile/${event.userID}")
            }
            null -> Unit
        }

    }

    LaunchedEffect(notiList.itemCount) {
        Log.e(TAG, "LaunchedEffect: ${notiList.itemCount}")
    }

    //Fetch more data when the list is scrolled to the end


    Scaffold(
        topBar = {
            TopBarBackButton(
                title = "Notifications",
                navController = navController
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(it)
                .fillMaxSize(),
            state = listState
        ) {
            if(recentNotifications.isNotEmpty()){
                item {
                    Text(
                        "Gần Đây",
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(8.dp))
                }
                items(recentNotifications.size) { index ->
                    if(recentNotifications[index] != null){
                        if(recentNotifications[index]!!.type == TypeNotification.REQUEST_FOLLOW){
                            FollowRequestItem(
                                notification = recentNotifications[index]!!,
                                notificationVM = notificationVM
                            )
                        }else{
                            NotificationItem(
                                notification = recentNotifications[index]!!,
                                notificationVM = notificationVM
                            )
                        }
                    }
                }
            }
            if(earlierNotifications.isNotEmpty()){
                item {
                    Text(
                        "Trước Đó",
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(8.dp))
                }
                items(earlierNotifications.size) { index ->
                    if(earlierNotifications[index] != null){
                        if(earlierNotifications[index]!!.type == TypeNotification.REQUEST_FOLLOW){
                            FollowRequestItem(
                                notification = earlierNotifications[index]!!,
                                notificationVM = notificationVM
                            )
                        }else{
                            NotificationItem(
                                notification = earlierNotifications[index]!!,
                                notificationVM = notificationVM
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun NotificationItem(
    notification: Notification,
    notificationVM: NotificationViewModel,
) {
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                when (notification.type) {
                    TypeNotification.LIKE_POST -> {
                        notificationVM.onNotificationClick(
                            NotificationEvent.LikePost(
                                notiID = notification.id,
                                relaId = notification.infoID
                            )
                        )
                    }

                    TypeNotification.LIKE_COMMENT -> {
                        notificationVM.onNotificationClick(
                            NotificationEvent.LikeComment(
                                notiID = notification.id,
                                relaId = notification.infoID
                            )
                        )
                    }

                    TypeNotification.COMMENT_POST -> {
                        notificationVM.onNotificationClick(
                            NotificationEvent.CommentPost(
                                notiID = notification.id,
                                relaId = notification.infoID
                            )
                        )
                    }

                    TypeNotification.REPLY_COMMENT -> {
                        notificationVM.onNotificationClick(
                            NotificationEvent.ReplyComment(
                                notiID = notification.id,
                                relaId = notification.infoID
                            )
                        )
                    }

                    TypeNotification.FOLLOW_USER -> {
                        notificationVM.onNotificationClick(
                            NotificationEvent.Following(
                                notiID = notification.id,
                                relaId = notification.infoID
                            )
                        )
                    }
                }
            }
        ,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                notification.message,
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
            )
            Text(
                text = getTimeElapsed(notification.createdAt),
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                ),
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
fun FollowRequestItem(
    notification: Notification,
    notificationVM: NotificationViewModel
){
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                notificationVM.onNotificationClick(
                    NotificationEvent.FollowRequest(
                        notiID = notification.id,
                        relaId = notification.infoID
                    )
                )
            }
        ,
        elevation = CardDefaults.cardElevation(4.dp)
    ){
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                notification.message,
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
            )
            if(!notification.isRead){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Button(
                        onClick = {
                            val result = notificationVM.acceptFollowRequest(
                                notiID = notification.id,
                                relaId = notification.infoID
                            )
                            if(!result){ // failed
                                Toast.makeText(context, "This request is unavailable", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Accept",
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                    Button(
                        onClick = {
                            notificationVM.rejectFollowRequest(
                                notiID = notification.id,
                                relaId = notification.infoID
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = "Reject",
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                }
            }
            Text(
                text = getTimeElapsed(notification.createdAt),
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                ),
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, start = 4.dp)
            )
        }
    }
}

