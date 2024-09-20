package com.example.urvoices.utils.Navigator

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.urvoices.ui.Login.LoginScreen
import com.example.urvoices.ui.MainScreen.HomeScreen
import com.example.urvoices.ui.MainScreen.ProfileScreen
import com.example.urvoices.ui.MainScreen.SearchScreen
import com.example.urvoices.ui.MainScreen.SettingsScreen
import com.example.urvoices.ui.MainScreen.UploadScreen
import com.example.urvoices.ui.Register.RegisterScreen
import com.example.urvoices.ui.Splash.SplashScreen
import com.example.urvoices.ui.noti_msg.MessageScreen
import com.example.urvoices.ui.noti_msg.NotificationScreen
import com.example.urvoices.viewmodel.AuthViewModel

fun NavGraphBuilder.authGraph(navController: NavController, authViewModel: AuthViewModel){
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

fun NavGraphBuilder.mainGraph(navController: NavController, authViewModel: AuthViewModel){
    navigation(route = Graph.NAV_SCREEN, startDestination = MainScreen.HomeScreen.route){
        composable(route = MainScreen.HomeScreen.route){
            HomeScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(route = MainScreen.SearchScreen.route){
            SearchScreen(navController = navController)
        }
        composable(route = MainScreen.UploadScreen.route){
             UploadScreen(navController = navController)
        }
        composable(route = MainScreen.ProfileScreen.route){
            ProfileScreen(navController = navController)
        }
        composable(route = MainScreen.SettingsScreen.route){
            SettingsScreen()
        }
    }
}

fun NavGraphBuilder.notiMsgGraph(navController: NavController){
    navigation(route = Graph.NOTI_MSG, startDestination = NotiMsgicationScreen.NotificationScreen.route){
        composable(route = NotiMsgicationScreen.NotificationScreen.route){
            NotificationScreen(navController = navController)
        }
        composable(route = NotiMsgicationScreen.MessageScreen.route){
            MessageScreen(navController = navController)
        }
    }
}
