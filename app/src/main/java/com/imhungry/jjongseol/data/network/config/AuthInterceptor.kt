package com.imhungry.jjongseol.data.network.config

import com.imhungry.jjongseol.data.repository.LoginRepository
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val loginRepository: LoginRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder().apply {
            loginRepository.getToken()?.let {
                addHeader("Authorization", "Bearer $it")
            }
        }.build()

        val response = chain.proceed(request)

        if (!response.isSuccessful && response.code == 401) {
            loginRepository.clearToken()
        }

        return response
    }
}


