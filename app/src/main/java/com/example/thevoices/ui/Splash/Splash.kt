package com.example.thevoices.ui.Splash

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thevoices.R
import com.example.thevoices.utils.Navigator.AuthScreen
import com.example.thevoices.utils.Navigator.MainScreen
import com.example.thevoices.utils.SharedPreferencesHelper

@Composable
fun SplashScreen(
    navController: NavController,
) {
    var isVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    LaunchedEffect(key1 = true) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(), // Add fade in animation when the content appears
        exit = fadeOut(), // Add fade out animation when the content disappears
    ) {
        if(SharedPreferencesHelper(context = context).isLoggedIn()){
            navController.navigate(MainScreen.HomeScreen.route)
        }else {
            Splash(navController = navController)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Splash(
    navController: NavController,
    modifier: Modifier = Modifier
){


    val textColor = remember { mutableStateOf(Color.Black) }
    val buttonColor = remember {
        mutableStateOf(Color.White)
    }

    val TAG = "Splash_Screen"


    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ){
                Text(
                    text = "@2024_NAT",
                    color = textColor.value,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 200.dp)
                        .background(Color.White)
                        .clip(CircleShape)
                    ,
                    contentAlignment = Alignment.Center,
                ) {
                    var imageSize by remember { mutableStateOf(IntSize.Zero) }
                    Image(
                        painter = painterResource(id = R.drawable.preview_backgroundremoved),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(200.dp)
                            .onGloballyPositioned { coordinates ->
                                imageSize = coordinates.size
                            }
                    )
                }
                Card(
                    modifier = Modifier
                        .width(350.dp)
                        .height(50.dp)
                        .border(2.dp, textColor.value, CircleShape),
                    onClick = {
                        navController.navigate(AuthScreen.LoginScreen.route)
                    },
                ) {
                    Row (
                        modifier = Modifier
                            .fillMaxSize()
                            .background(buttonColor.value),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        AnimatedVisibility(visible = true) {
                            Text(
                                text = "Get Started",
                                color = textColor.value,
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSplashScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    Splash(navController = NavController(context))
}
