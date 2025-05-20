package com.imhungry.jjongseol.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPrefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _isLoggedInFlow = MutableStateFlow(isLoggedIn())
    val isLoggedInFlow: StateFlow<Boolean> = _isLoggedInFlow

    fun saveToken(token: String) {
        sharedPrefs.edit().putString("jwt_token", token).apply()
        _isLoggedInFlow.value = true
    }

    fun clearToken() {
        sharedPrefs.edit().remove("jwt_token").apply()
        _isLoggedInFlow.value = false
    }

    fun getToken(): String? = sharedPrefs.getString("jwt_token", null)

    fun isLoggedIn(): Boolean = !getToken().isNullOrEmpty()
}

