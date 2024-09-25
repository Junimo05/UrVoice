package com.example.urvoices.ui.MainScreen

import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.navigation.NavController
import com.example.urvoices.R
import com.example.urvoices.data.model.Post
import com.example.urvoices.data.service.FirebasePostService
import com.example.urvoices.presentations.theme.MyTheme
import com.example.urvoices.ui._component.PostComponent.NewFeedPostItem
import com.example.urvoices.utils.Navigator.AuthScreen
import com.example.urvoices.utils.UserPreferences
import com.example.urvoices.viewmodel.AuthState
import com.example.urvoices.viewmodel.AuthViewModel
import com.example.urvoices.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val viewModel: HomeViewModel = hiltViewModel()
    Home(navController, authViewModel, viewModel)
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
@Composable
fun Home(
    navController: NavController,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {


    val authState = authViewModel.authState.observeAsState()
    val scope = rememberCoroutineScope()
    val mainStateList = rememberLazyListState()
    val userPreferences = UserPreferences(LocalContext.current)
    val redrawTrigger = remember {
        mutableStateOf(0)
    }
    val isScrolled = remember {
        mutableStateOf(mainStateList.firstVisibleItemIndex > 0)
    }

    //
    val isPlayingAudio by rememberSaveable {
        mutableStateOf(false)
    }

    //
    scope.launch {

    }

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> {
                navController.navigate(AuthScreen.LoginScreen.route)
            }
            else -> Unit
        }
     }

    LaunchedEffect(remember { derivedStateOf { mainStateList.firstVisibleItemIndex } }) {
        redrawTrigger.value++
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
                                color = Color.Black,
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
                    val userName by userPreferences.userNameFlow.collectAsState(initial = "")
                    val userEmail by userPreferences.userEmailFlow.collectAsState(initial = "")
                    userName?.let {
                        Text(
                            text = it,
                            modifier = Modifier
                                .padding(16.dp),
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            ,
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_actions_notifications),
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .padding(end = 4.dp)
                                .clickable {
                                    //TODO: Implement Notification
                                }
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_contact_message),
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .padding(end = 4.dp)
                                .clickable {
                                    //TODO: Implement Message
                                }
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_actions_log_out),
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .padding(end = 4.dp)
                                .clickable {
                                    scope.launch {
                                        authViewModel.signOut()
                                    }
                                }
                        )
                    }
                }
            }

        }
    ) {paddingValue ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue)
                .background(MaterialTheme.colorScheme.background),
            userScrollEnabled = true,
            state = mainStateList,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(10){
                NewFeedPostItem(redrawTrigger = redrawTrigger.value)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val authViewModel: AuthViewModel = hiltViewModel()
    val homeViewModel: HomeViewModel = hiltViewModel()
    MyTheme {
        Home(navController = NavController(context = context), authViewModel = authViewModel, homeViewModel = homeViewModel)
    }
}