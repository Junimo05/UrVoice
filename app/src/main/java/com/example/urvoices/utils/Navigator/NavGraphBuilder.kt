package com.example.urvoices.utils.Navigator

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.urvoices.ui.AuthScreen.LoginScreen
import com.example.urvoices.ui.AuthScreen.RegisterScreen
import com.example.urvoices.ui.AuthScreen.SplashScreen
import com.example.urvoices.ui.MainScreen.HomeScreen
import com.example.urvoices.ui.MainScreen.ProfileScreen
import com.example.urvoices.ui.MainScreen.SearchScreen
import com.example.urvoices.ui.MainScreen.SettingsScreen
import com.example.urvoices.ui.MainScreen.UploadScreen
import com.example.urvoices.ui.MainScreen.PostDetail
import com.example.urvoices.ui._component.ProfileComponent.ProfileEditScreen
import com.example.urvoices.ui.noti_msg.MessageScreen
import com.example.urvoices.ui.noti_msg.NotificationScreen
import com.example.urvoices.viewmodel.AuthViewModel
import com.example.urvoices.viewmodel.HomeViewModel
import com.example.urvoices.viewmodel.MediaPlayerVM
import com.example.urvoices.viewmodel.MediaRecorderVM
import com.example.urvoices.viewmodel.PostDetailViewModel
import com.example.urvoices.viewmodel.ProfileViewModel
import com.example.urvoices.viewmodel.UploadViewModel

val BASE_URL = "https://urvoices.com"

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
    playerViewModel: MediaPlayerVM,
    homeViewModel: HomeViewModel,
    uploadViewModel: UploadViewModel,
    profileViewModel: ProfileViewModel,
    mediaRecorderVM: MediaRecorderVM
){
    navigation(route = Graph.NAV_SCREEN, startDestination = MainScreen.HomeScreen.route){
        composable(route = MainScreen.HomeScreen.route){
            HomeScreen(
                navController = navController,
                authViewModel = authViewModel,
                playerViewModel = playerViewModel,
                homeViewModel = homeViewModel
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
                playerViewModel = playerViewModel,
                uploadViewModel = uploadViewModel,
                mediaRecorderVM = mediaRecorderVM
            )
        }
        composable(route = MainScreen.ProfileScreen.MainProfileScreen.route){
            authViewModel.getCurrentUser()?.uid?.let { it1 ->
                ProfileScreen(
                    navController = navController,
                    playerViewModel = playerViewModel,
                    profileViewModel = profileViewModel,
                    userId = it1
                )
            }
        }
        composable(route = MainScreen.ProfileScreen.EditProfileScreen.route){
            ProfileEditScreen(
                navController = navController,
                profileViewModel = profileViewModel
            )
        }
        composable(route = MainScreen.SettingsScreen.route){
            SettingsScreen(
                navController = navController,
                playerViewModel = playerViewModel
            )
        }
    }
}
fun NavGraphBuilder.specifyGraph(
    navController: NavController,
    authViewModel: AuthViewModel,
    playerViewModel: MediaPlayerVM,
    postDetailViewModel: PostDetailViewModel,
    profileViewModel: ProfileViewModel
){
    navigation(route = Graph.SPECIFY, startDestination = "post"){
        composable(
            route = "post/{userId}/{postId}",
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "$BASE_URL/post/{userId}/{postId}"
            })
        ) { navBackStackEntry ->
            val postId = navBackStackEntry.arguments?.getString("postId")
            val userId = navBackStackEntry.arguments?.getString("userId")
            if (postId != null && userId != null) {
                PostDetail(
                    navController = navController,
                    playerViewModel = playerViewModel,
                    postDetailViewModel = postDetailViewModel,
                    authViewModel = authViewModel,
                    postID = postId,
                    userID = userId
                )
            } else {
                // Handle the case where postId is null
            }
        }

        composable(
            route = "profile/{userId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "$BASE_URL/profile/{userId}"
            })
        ) { navBackStackEntry ->
            val userId = navBackStackEntry.arguments?.getString("userId")
            if (userId != null) {
                ProfileScreen(
                    navController = navController,
                    playerViewModel = playerViewModel,
                    profileViewModel = profileViewModel,
                    userId = userId
                )
            } else {
                // Handle the case where userId is null
            }
        }
    }
}

fun NavGraphBuilder.notiMsgGraph(
    navController: NavController
){
    navigation(route = Graph.NOTI_MSG, startDestination = NotiMsgScreen.NotificationScreen.route){
        composable(route = NotiMsgScreen.NotificationScreen.route){
            NotificationScreen(navController = navController)
        }
        composable(route = NotiMsgScreen.MessageScreen.route){
            MessageScreen(navController = navController)
        }
    }
}


//        composable<SpecifyScreen.PostDetailScreen>(
//            typeMap = mapOf(
//                typeOf<Post>() to CustomNavType.PostType
//            )
//        ){
//            val arguments = it.toRoute<SpecifyScreen.PostDetailScreen>()
//            PostDetail(
//                navController = navController,
//                playerViewModel = playerViewModel,
//                authViewModel = authViewModel,
//                post = arguments.post
//            )
//        }