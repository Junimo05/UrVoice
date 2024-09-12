package com.example.urvoices.utils.Navigator

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.urvoices.ui.Home.HomeScreen
import com.example.urvoices.ui.Login.LoginScreen
import com.example.urvoices.ui.Register.RegisterScreen
import com.example.urvoices.ui.Splash.SplashScreen

fun NavGraphBuilder.authGraph(navController: NavController){
    navigation(route = Graph.AUTHENTICATION, startDestination = AuthScreen.SplashScreen.route){
        composable(route = AuthScreen.SplashScreen.route){
            SplashScreen(navController = navController)
        }
        composable(route = AuthScreen.LoginScreen.route){
            LoginScreen(navController = navController)
        }
        composable(route = AuthScreen.RegisterScreen.route){
            RegisterScreen(navController = navController)
        }
        composable(route = AuthScreen.ForgetPasswordScreen.route){

        }
    }
}

fun NavGraphBuilder.mainGraph(navController: NavController){
    navigation(route = Graph.NAV_SCREEN, startDestination = MainScreen.HomeScreen.route){
        composable(route = MainScreen.HomeScreen.route){
            HomeScreen(navController = navController)
        }
        composable(route = MainScreen.SearchScreen.route){

        }
        composable(route = MainScreen.RecordScreen.route){
//            RecordScreen(navController = navController)
        }
        composable(route = MainScreen.ProfileScreen.route){

        }
        composable(route = MainScreen.SettingsScreen.route){

        }
        composable(route = MainScreen.NotificationScreen.route){

        }
        composable(route = MainScreen.MessageScreen.route){

        }
    }
}
