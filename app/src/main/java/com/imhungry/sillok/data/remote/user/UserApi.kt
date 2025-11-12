package com.imhungry.sillok.data.remote.user

import com.imhungry.sillok.data.model.user.UserInfoResDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApi {
    @GET("/api/v1/users/{email}")
    suspend fun getUserInfo(@Path("email") email: String): Response<UserInfoResDto>

    @GET("/api/v1/users/my-profile")
    suspend fun getMyProfile(): Response<UserInfoResDto>
}
