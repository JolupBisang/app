package com.imhungry.jjongseol.data.repository

import android.content.Context
import android.util.Base64
import com.google.gson.JsonParser
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.Charset
import javax.inject.Inject

class LoginRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPrefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        sharedPrefs.edit().putString("jwt_token", token).apply()
    }

    fun getToken(): String? = sharedPrefs.getString("jwt_token", null)

    fun isLoggedIn(): Boolean {
        val token = getToken() ?: return false
        return !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return true

            val payloadJson = String(Base64.decode(parts[1], Base64.URL_SAFE), Charset.defaultCharset())
            val jsonObj = JsonParser.parseString(payloadJson).asJsonObject

            val exp = jsonObj["exp"]?.asLong ?: return true
            val now = System.currentTimeMillis() / 1000
            now >= exp
        } catch (e: Exception) {
            true
        }
    }
}
