package com.example.urvoices.utils.Navigator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.urvoices.data.model.Post
import kotlinx.serialization.Serializable

sealed class AuthScreen(
    val route: String
) {
    object SplashScreen: AuthScreen("SPLASH_SCREEN")
    object LoginScreen: AuthScreen("LOGIN_SCREEN")
    object RegisterScreen: AuthScreen("REGISTER_SCREEN")
    object ForgetPasswordScreen: AuthScreen("FORGET_PASSWORD_SCREEN")
}

sealed class MainScreen(
    val route: String,
) {
    object HomeScreen: MainScreen("HOME_SCREEN")
    object SearchScreen: MainScreen("SEARCH_SCREEN")
    object UploadScreen: MainScreen("UPLOAD_SCREEN")
    sealed class ProfileScreen(
        val route: String
    ) {
        object MainProfileScreen: ProfileScreen("PROFILE_SCREEN")
        object EditProfileScreen: ProfileScreen("PROFILE_SCREEN/EditProfileScreen")
        companion object {
            val route: String  = "PROFILE_SCREEN"
        }
    }
    sealed class SettingsScreen(
        val route: String
    ){
        companion object {
            val route: String = "SETTINGS_SCREEN"
        }
        object MainSettingsScreen: SettingsScreen("SETTINGS_SCREEN")
        object BlockedUsersScreen: SettingsScreen("SETTINGS_SCREEN/BlockedUsersScreen")
        object SavedPostsScreen: SettingsScreen("SETTINGS_SCREEN/SavedPostsScreen")
        object DeleteAccount: SettingsScreen("SETTINGS_SCREEN/DeleteAccount")
    }
}



sealed class NotiMsgScreen(
    val route: String
) {
    object NotificationScreen: NotiMsgScreen("NOTIFICATION_SCREEN")
    object MessageScreen: NotiMsgScreen("MESSAGE_SCREEN")
}

@Serializable
data class EditPostScreen(
    val post: Post
)

@Composable
inline fun <reified T: ViewModel> NavBackStackEntry.scopedViewModel(
    navController: NavHostController
): T {
    // if the destination route doesn't have a parent create a brand
    // new view model instance
    val navGraphRoute = destination.parent?.route ?: return hiltViewModel()

    // the destination does have a parent screen
    val parentEntry: NavBackStackEntry = remember(key1 = this) {
        navController.getBackStackEntry(navGraphRoute)
    }

    // return the view model associated with the parent destination
    return hiltViewModel(parentEntry)
}