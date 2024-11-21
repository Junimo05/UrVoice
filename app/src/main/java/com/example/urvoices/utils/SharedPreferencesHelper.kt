package com.example.urvoices.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SharedPreferencesHelper(context: Context) {
    private val PREFS_NAME = "com.example.urvoices"
    private val LOGGED_IN = "logged_in"
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val appContext = context.applicationContext

    fun getUserSharedPreferences(userId: String): SharedPreferences {
        return appContext.getSharedPreferences("user_prefs_$userId", Context.MODE_PRIVATE)
    }

    fun getAllSettings(): Map<String, *> {
        return prefs.all
    }

    fun getUserSettings(userId: String): Map<String, *> {
        return getUserSharedPreferences(userId).all
    }

    fun save(key: String, value: Any, userId: String? = null) {
        val editor = if (userId != null) {
            getUserSharedPreferences(userId).edit()
        } else {
            prefs.edit()
        }

        when (value) {
            is String -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is Float -> editor.putFloat(key, value)
            is Long -> editor.putLong(key, value)
            else -> throw IllegalArgumentException("Unsupported type")
        }

        editor.apply()
    }

    fun getString(key: String, default: String = "", userId: String? = null): String {
        return if (userId != null) {
            getUserSharedPreferences(userId).getString(key, default) ?: default
        } else {
            prefs.getString(key, default) ?: default
        }
    }

    fun getInt(key: String, default: Int = 0, userId: String? = null): Int {
        return if (userId != null) {
            getUserSharedPreferences(userId).getInt(key, default)
        } else {
            prefs.getInt(key, default)
        }
    }

    fun getBoolean(key: String, default: Boolean = false, userId: String? = null): Boolean {
        return if (userId != null) {
            getUserSharedPreferences(userId).getBoolean(key, default)
        } else {
            prefs.getBoolean(key, default)
        }
    }

    fun getFloat(key: String, default: Float = 0f, userId: String? = null): Float {
        return if (userId != null) {
            getUserSharedPreferences(userId).getFloat(key, default)
        } else {
            prefs.getFloat(key, default)
        }
    }

    fun getLong(key: String, default: Long = 0L, userId: String? = null): Long {
        return if (userId != null) {
            getUserSharedPreferences(userId).getLong(key, default)
        } else {
            prefs.getLong(key, default)
        }
    }

    fun remove(key: String, userId: String? = null) {
        val editor = if (userId != null) {
            getUserSharedPreferences(userId).edit()
        } else {
            prefs.edit()
        }
        editor.remove(key)
        editor.apply()
    }

    fun clear(userId: String? = null) {
        val editor = if (userId != null) {
            getUserSharedPreferences(userId).edit()
        } else {
            prefs.edit()
        }
        editor.clear()
        editor.apply()
    }
}
object SharedPreferencesKeys {
    const val isFirstTime = "isFirstTime"
    const val shareLoving = "shareLoving"
    const val privateAccount = "privateAccount"
}


class UserPreferences(private val context: Context){
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }

    // Lưu thông tin người dùng
    suspend fun saveUserInfo(userId: String, userName: String, userEmail: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USER_NAME_KEY] = userName
            preferences[USER_EMAIL_KEY] = userEmail
        }
    }

    // Lấy thông tin người dùng
    val userIdFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ID_KEY]
        }

    val userNameFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_NAME_KEY]
        }

    val userEmailFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_EMAIL_KEY]
        }

    // Xóa thông tin người dùng (đăng xuất)
    suspend fun clearUserInfo() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}