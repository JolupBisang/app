package com.imhungry.jjongseol.data.network

import com.imhungry.jjongseol.data.repository.LoginRepository
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val loginRepository: LoginRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = loginRepository.getToken()

        val newRequest = chain.request().newBuilder()
        if (!token.isNullOrEmpty()) {
            newRequest.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(newRequest.build())
    }
}
