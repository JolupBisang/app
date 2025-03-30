package com.imhungry.jjongseol.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LoginRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPrefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        sharedPrefs.edit().putString("jwt_token", token).apply()
    }

    fun getToken(): String? = sharedPrefs.getString("jwt_token", null)

    fun isLoggedIn(): Boolean = !getToken().isNullOrEmpty()
}