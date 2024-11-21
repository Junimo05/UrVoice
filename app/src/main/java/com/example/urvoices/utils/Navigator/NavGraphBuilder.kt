package com.example.urvoices.utils.Navigator

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.algolia.search.model.rule.Edit
import com.example.urvoices.data.model.Post
import com.example.urvoices.ui.AuthScreen.ForgotPasswordScreen
import com.example.urvoices.ui.AuthScreen.LoginScreen
import com.example.urvoices.ui.AuthScreen.RegisterScreen
import com.example.urvoices.ui.AuthScreen.SplashScreen
import com.example.urvoices.ui.MainScreen.HomeScreen
import com.example.urvoices.ui.MainScreen.ProfileScreen
import com.example.urvoices.ui.MainScreen.SearchScreen
import com.example.urvoices.ui.MainScreen.SettingsScreen
import com.example.urvoices.ui.MainScreen.UploadScreen
import com.example.urvoices.ui.MainScreen.PostDetail
import com.example.urvoices.ui._component.PostComponent.EditPostScreen
import com.example.urvoices.ui._component.ProfileComponent.ProfileEditScreen
import com.example.urvoices.ui._component.SettingComponents.BlockScreen
import com.example.urvoices.ui._component.SettingComponents.SavedPostScreen
import com.example.urvoices.ui.noti_msg.MessageScreen
import com.example.urvoices.ui.noti_msg.NotificationScreen
import com.example.urvoices.utils.Auth.BASE_URL
import com.example.urvoices.viewmodel.AuthViewModel
import com.example.urvoices.viewmodel.EditPostVM
import com.example.urvoices.viewmodel.HomeViewModel
import com.example.urvoices.viewmodel.MediaPlayerVM
import com.example.urvoices.viewmodel.MediaRecorderVM
import com.example.urvoices.viewmodel.PostDetailViewModel
import com.example.urvoices.viewmodel.ProfileViewModel
import com.example.urvoices.viewmodel.SearchViewModel
import com.example.urvoices.viewmodel.SettingViewModel
import com.example.urvoices.viewmodel.UploadViewModel
import kotlin.reflect.typeOf


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
            ForgotPasswordScreen(navController = navController, authViewModel = authViewModel)
        }
    }
}

fun NavGraphBuilder.mainGraph(
    navController: NavController,
    authViewModel: AuthViewModel,
    searchViewModel: SearchViewModel,
    playerViewModel: MediaPlayerVM,
    homeViewModel: HomeViewModel,
    uploadViewModel: UploadViewModel,
    profileViewModel: ProfileViewModel,
    mediaRecorderVM: MediaRecorderVM,
    settingVM: SettingViewModel
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
                multiSearcher = searchViewModel.multiSearcher,
                searchBoxState = searchViewModel.searchBoxState,
                userState = searchViewModel.userState,
                postState = searchViewModel.postState,
                filterState = searchViewModel.filterState,
                filterListState = searchViewModel.filterListState,
                onClearFilter = { searchViewModel.onClearFilter() },
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
                    userId = it1,
                )
            }
        }
        composable(route = MainScreen.ProfileScreen.EditProfileScreen.route){
            ProfileEditScreen(
                navController = navController,
                profileViewModel = profileViewModel
            )
        }
        composable(route = MainScreen.SettingsScreen.MainSettingsScreen.route){
            SettingsScreen(
                navController = navController,
                playerViewModel = playerViewModel,
                authViewModel = authViewModel,
                settingVM = settingVM
            )
        }

        composable(route = MainScreen.SettingsScreen.BlockedUsersScreen.route){
            BlockScreen(
                navController = navController,
                settingVM = settingVM
            )
        }

        composable(route = MainScreen.SettingsScreen.SavedPostsScreen.route){
            SavedPostScreen(
                navController = navController,
                settingVM = settingVM,
                mediaPlayerVM = playerViewModel,
                profileVM = profileViewModel
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
            val postUpdated = navBackStackEntry.savedStateHandle.get<Post>("post")
            if(postUpdated != null){
                postDetailViewModel.postFlow.tryEmit(postUpdated)
            }
            val postId = navBackStackEntry.arguments?.getString("postId")
            val userId = navBackStackEntry.arguments?.getString("userId")
            if (postId != null && userId != null) {
                PostDetail(
                    navController = navController,
                    playerViewModel = playerViewModel,
                    postDetailViewModel = postDetailViewModel,
                    authViewModel = authViewModel,
                    postID = postId,
                    userID = userId,
                    onEditPost = {post ->
                        navController.navigate(
                            EditPostScreen(
                                post = post
                            )
                        )
                    }
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

        composable<EditPostScreen>(
            typeMap = mapOf(
                typeOf<Post>() to CustomNavType.PostType,
            )
        ) {navBackStackEntry ->
            val arguments = navBackStackEntry.toRoute<EditPostScreen>()
            EditPostScreen(
                navController = navController,
                editPostVM = hiltViewModel<EditPostVM>(),
                mediaPlayerVM = playerViewModel,
                post = arguments.post,
                onUpdate = { post ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("post", post)
                    navController.popBackStack()
                }
            )
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