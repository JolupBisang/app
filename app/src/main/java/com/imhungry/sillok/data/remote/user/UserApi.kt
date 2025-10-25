package com.imhungry.sillok.data.remote.user

import com.imhungry.sillok.data.model.user.UserInfoResDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Response

interface UserApi {
    @GET("/api/users/{email}")
    suspend fun getUserInfo(@Path("email") email: String): Response<UserInfoResDto>

    @GET("/api/users/my-profile")
    suspend fun getMyProfile(): Response<UserInfoResDto>
}
