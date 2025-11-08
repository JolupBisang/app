package com.imhungry.sillok.data.remote.interceptor

import com.imhungry.sillok.data.local.TokenExpirationManager
import com.imhungry.sillok.data.local.TokenStore
import com.imhungry.sillok.data.local.UserStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
    private val userStore: UserStore,
    private val tokenExpirationManager: TokenExpirationManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = runBlocking { tokenStore.accessToken.first() }

        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(newRequest)

        // 토큰 만료 처리 (401 Unauthorized 응답)
        if (response.code == 401) {
            android.util.Log.w("AuthInterceptor", "토큰 만료")
            runBlocking {
                // 토큰과 사용자 정보 삭제
                tokenStore.clearTokens()
                userStore.clearUser()
                tokenExpirationManager.notifyTokenExpired()
            }
        }

        return response
    }
}


