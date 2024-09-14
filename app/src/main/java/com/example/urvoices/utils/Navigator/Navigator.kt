package com.example.urvoices.utils.Navigator

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.urvoices.ui._component.BottomBar
import com.example.urvoices.utils.SharedPreferencesHelper

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Navigator() {
    val navController = rememberNavController()
    val selectedPage = rememberSaveable { mutableIntStateOf(0) }
    val isVisibleBottomBar = rememberSaveable { mutableStateOf(false) }
    val sharedPreferences = SharedPreferencesHelper(context = LocalContext.current)

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.parent?.route) {
                Graph.AUTHENTICATION -> isVisibleBottomBar.value = false
                Graph.NAV_SCREEN -> isVisibleBottomBar.value = true
                Graph.POST -> isVisibleBottomBar.value = false
                Graph.NOTI_MSG -> isVisibleBottomBar.value = false
            }
        }
    }

    //
    sharedPreferences.setLoggedIn(false)

    Scaffold(
        bottomBar = {
            if (isVisibleBottomBar.value) {
                BottomBar(
                    selectedPage = selectedPage,
                    navController = navController
                )
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = if (sharedPreferences.isLoggedIn()) Graph.NAV_SCREEN else Graph.AUTHENTICATION,
            route = Graph.ROOT
        ){
            authGraph(navController) //authentication nav
            mainGraph(navController) //home nav
        }
    }
}

object Graph {
    const val ROOT = "root_graph"
    const val AUTHENTICATION = "auth_graph"
    const val NAV_SCREEN = "nav_graph"
    const val POST = "post_graph"
    const val NOTI_MSG = "noti_msg_graph"
}