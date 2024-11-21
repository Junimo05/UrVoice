package com.example.urvoices.utils.Auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.actionCodeSettings
import java.util.regex.Pattern
val BASE_URL = "https://urvoices.page.link"
fun isValidUsername(username: String): Boolean {
    val usernamePattern = "^[a-zA-Z0-9]*$"
    return username.matches(usernamePattern.toRegex())
}

fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun isPasswordStrong(password: String): Boolean {
    // A strong password has at least 8 characters, contains at least one digit,
    // at least one lower case letter, at least one upper case letter and at least one special character
    val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%&*])(?=\\S+$).{8,}$"
    val pattern = Pattern.compile(passwordPattern)
    val matcher = pattern.matcher(password)

    return matcher.matches()
}

val actionCodeSettings = actionCodeSettings {
    url = "https://www.urvoices.com/emailSignInLink"
    handleCodeInApp = true
    setIOSBundleId("com.example.ios")
    setAndroidPackageName("com.example.android", true, "12")
    dynamicLinkDomain = "example.page.link"
}