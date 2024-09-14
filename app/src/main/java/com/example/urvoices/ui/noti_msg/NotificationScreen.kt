package com.example.urvoices.ui.noti_msg

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.ui._component.NotificationItem
import com.example.urvoices.ui._component.TopBarBackButton

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NotificationScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
             TopBarBackButton(
                 navController = navController,
                 title = "Notifications",
                 modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
             )
        }
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(it)
                .fillMaxSize()
        ) {
            NotiPart(titlePart = "New")
        }
    }
}

@Composable
fun NotiPart(
    titlePart: String,
//    notiList: List<String>
){
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = titlePart,
        style = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        ),
        modifier = Modifier.padding(start = 40.dp)
    )
    Spacer(modifier = Modifier.height(4.dp))
    LazyColumn {
        //        items(notiList.size) { index ->
        items(5) { index ->
            NotificationItem(
                username = "username",
                message = "message",
                time =  "00.00 AM"
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewNotificationScreen() {
    var navController = rememberNavController()
    MyTheme {
        NotificationScreen(navController = navController)
    }
}