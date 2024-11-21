package com.example.urvoices.ui.noti_msg

import android.annotation.SuppressLint
import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.urvoices.R
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.ui._component.MessageItem
import com.example.urvoices.ui._component.SearchBar
import com.example.urvoices.ui._component.TopBarBackButton

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MessageScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopBarBackButton(
                navController = navController,
                title = "Messages",
                endIcon = R.drawable.ic_pentool,
                endIconAction = {
                    //TODO
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,

        ) {
//            SearchBar()
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                userScrollEnabled = true
            ) {
                items(10){
                    MessageItem(
                        username = "Lucas Morrison",
                        message = "Lmao lmao lmao lmao ladaodwdwadaw",
                        time = "00.00 AM"
                    )
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyTheme {
        MessageScreen(navController = rememberNavController())
    }
}