package com.example.chamasegura.utils

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_TOKEN = "user_token"
    }

    fun saveAuthData(name: String, token: String) {
        prefs.edit().putString(KEY_USER_NAME, name).putString(KEY_USER_TOKEN, token).apply()
    }

    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    fun getToken(): String? {
        return prefs.getString(KEY_USER_TOKEN, null)
    }

    fun clearAuthData() {
        prefs.edit().clear().apply()
    }
}