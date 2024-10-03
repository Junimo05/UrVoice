package com.example.urvoices.utils.Navigator

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.urvoices.ui.AuthScreen.LoginScreen
import com.example.urvoices.ui.MainScreen.HomeScreen
import com.example.urvoices.ui.MainScreen.ProfileScreen
import com.example.urvoices.ui.MainScreen.SearchScreen
import com.example.urvoices.ui.MainScreen.SettingsScreen
import com.example.urvoices.ui.MainScreen.UploadScreen
import com.example.urvoices.ui.AuthScreen.RegisterScreen
import com.example.urvoices.ui.AuthScreen.SplashScreen
import com.example.urvoices.ui.noti_msg.MessageScreen
import com.example.urvoices.ui.noti_msg.NotificationScreen
import com.example.urvoices.viewmodel.AuthViewModel
import com.example.urvoices.viewmodel.MediaPlayerViewModel
import com.google.firebase.auth.FirebaseAuth

fun NavGraphBuilder.authGraph(
    navController: NavController,
    authViewModel: AuthViewModel
){
    navigation(route = Graph.AUTHENTICATION, startDestination = AuthScreen.SplashScreen.route){
        composable(route = AuthScreen.SplashScreen.route){
            SplashScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(route = AuthScreen.LoginScreen.route){
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(route = AuthScreen.RegisterScreen.route){
            RegisterScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(route = AuthScreen.ForgetPasswordScreen.route){

        }
    }
}

fun NavGraphBuilder.mainGraph(
    navController: NavController,
    authViewModel: AuthViewModel,
    playerViewModel: MediaPlayerViewModel
){
    navigation(route = Graph.NAV_SCREEN, startDestination = MainScreen.HomeScreen.route){
        composable(route = MainScreen.HomeScreen.route){
            HomeScreen(
                navController = navController,
                authViewModel = authViewModel,
                playerViewModel = playerViewModel
            )
        }
        composable(route = MainScreen.SearchScreen.route){
            SearchScreen(
                navController = navController,
                playerViewModel = playerViewModel,
            )
        }
        composable(route = MainScreen.UploadScreen.route){
            UploadScreen(
                navController = navController,
                playerViewModel = playerViewModel
            )
        }
        composable(route = MainScreen.ProfileScreen.route){
            authViewModel.getCurrentUser()?.uid?.let { it1 ->
                ProfileScreen(
                    navController = navController,
                    playerViewModel = playerViewModel,
                    userId = it1
                )
            }
        }
        composable(route = MainScreen.SettingsScreen.route){
            SettingsScreen(
                navController = navController,
                playerViewModel = playerViewModel
            )
        }
    }
}

fun NavGraphBuilder.notiMsgGraph(
    navController: NavController
){
    navigation(route = Graph.NOTI_MSG, startDestination = NotiMsgicationScreen.NotificationScreen.route){
        composable(route = NotiMsgicationScreen.NotificationScreen.route){
            NotificationScreen(navController = navController)
        }
        composable(route = NotiMsgicationScreen.MessageScreen.route){
            MessageScreen(navController = navController)
        }
    }
}
