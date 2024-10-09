package com.example.urvoices.utils.Auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.regex.Pattern

fun isPasswordStrong(password: String): Boolean {
    // A strong password has at least 8 characters, contains at least one digit,
    // at least one lower case letter, at least one upper case letter and at least one special character
    val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    val pattern = Pattern.compile(passwordPattern)
    val matcher = pattern.matcher(password)

    return matcher.matches()
}

fun checkProvider(authUser: FirebaseUser?): String {
    if (authUser != null) {
        for (userInfo in authUser.providerData) {
            when (userInfo.providerId) {
                "facebook.com" -> {
                    // User is signed in using Facebook
                    return "Facebook"
                }
                "google.com" -> {
                    // User is signed in using Google
                    return "Google"
                }
                "password" -> {
                    // User is signed in using email and password
                    return "Email"
                }
            }
        }
    }
    // If the provider is not recognized, return "Unknown"
    return "Unknown"
}