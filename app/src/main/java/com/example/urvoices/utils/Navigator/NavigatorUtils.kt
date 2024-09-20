package com.example.urvoices.utils.Navigator

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
    object ProfileScreen: MainScreen("PROFILE_SCREEN")
    object SettingsScreen: MainScreen("SETTINGS_SCREEN")
}

sealed class NotiMsgicationScreen(
    val route: String
) {
    object NotificationScreen: NotiMsgicationScreen("NOTIFICATION_SCREEN")
    object MessageScreen: NotiMsgicationScreen("MESSAGE_SCREEN")
}

sealed class PostDetailScreen(
    val route: String
) {
    object PostDetail: PostDetailScreen("POST_DETAIL")
}