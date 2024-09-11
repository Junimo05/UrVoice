package com.example.thevoices.utils.Navigator

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
    object RecordScreen: MainScreen("RECORD_SCREEN")
    object ProfileScreen: MainScreen("PROFILE_SCREEN")
    object SettingsScreen: MainScreen("SETTINGS_SCREEN")
    object NotificationScreen: MainScreen("NOTIFICATION_SCREEN")
    object MessageScreen: MainScreen("MESSAGE_SCREEN")
}

sealed class PostDetailScreen(
    val route: String
) {
    object PostDetail: PostDetailScreen("POST_DETAIL")
}