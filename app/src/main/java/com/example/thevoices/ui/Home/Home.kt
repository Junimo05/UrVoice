package com.example.thevoices.ui.Home

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.thevoices.R
import com.example.thevoices.presentations.components.PostComponent.NewFeedPostItem
import com.example.thevoices.presentations.theme.MyTheme
import com.example.thevoices.utils.SharedPreferencesHelper

@Composable
fun HomeScreen(
    navController: NavController
) {
    Home(navController)
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Home(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val mainStateList = rememberLazyListState()
    val isScrolled = remember {
        mutableStateOf(mainStateList.firstVisibleItemIndex > 0)
    }

    //
    val isPlayingAudio by rememberSaveable {
        mutableStateOf(false)
    }

    //

    Scaffold(
        topBar = {
            AnimatedVisibility(visible = !isScrolled.value) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(90.dp)
                    ,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = "Home",
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(1f)
                        ,
                        style = TextStyle(
                            fontSize = with(LocalDensity.current) { dimensionResource(id = R.dimen.Title).toSp()},
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .weight(0.4f, fill = true),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_actions_notifications),
                            contentDescription = null,
                            modifier = Modifier
                                .size(50.dp)
                                .padding(end = 4.dp)
                                .clickable {
                                    //TODO: Implement Notification
                                }
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_contact_message),
                            contentDescription = null,
                            modifier = Modifier
                                .size(50.dp)
                                .padding(end = 4.dp)
                                .clickable {
                                    //TODO: Implement Message
                                }
                        )
                    }
                }
            }

        }
    ) {paddingValue ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValue),
            userScrollEnabled = true,
            state = mainStateList
        ) {
            items(10){
                NewFeedPostItem()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    val context = androidx.compose.ui.platform.LocalContext.current
    MyTheme {
        Home(navController = NavController(context = context))
    }
}