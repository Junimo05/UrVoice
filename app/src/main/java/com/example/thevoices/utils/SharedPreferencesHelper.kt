package com.example.thevoices.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper(context: Context) {
    private val PREFS_NAME = "com.example.thevoices"
    private val LOGGED_IN = "logged_in"
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setLoggedIn(isLoggedIn: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean(LOGGED_IN, isLoggedIn)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(LOGGED_IN, false)
    }

    fun clear() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}