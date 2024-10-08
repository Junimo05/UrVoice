package com.example.urvoices.utils.Navigator

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
    }
    object SettingsScreen: MainScreen("SETTINGS_SCREEN")
}



sealed class NotiMsgScreen(
    val route: String
) {
    object NotificationScreen: NotiMsgScreen("NOTIFICATION_SCREEN")
    object MessageScreen: NotiMsgScreen("MESSAGE_SCREEN")
}

sealed class SpecifyScreen(

) {
    @Serializable data class ProfileScreen(val userId: String): SpecifyScreen() {
        companion object {
            fun getRoute(postId: String) = "PROFILE_SCREEN/$postId"
        }
    }

}