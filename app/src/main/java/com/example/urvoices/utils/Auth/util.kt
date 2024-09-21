package com.example.urvoices.utils.Auth

import java.util.regex.Pattern

fun isPasswordStrong(password: String): Boolean {
    // A strong password has at least 8 characters, contains at least one digit,
    // at least one lower case letter, at least one upper case letter and at least one special character
    val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    val pattern = Pattern.compile(passwordPattern)
    val matcher = pattern.matcher(password)

    return matcher.matches()
}