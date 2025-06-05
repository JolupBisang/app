package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.response.SuccessResponse
import com.imhungry.jjongseol.data.model.user.UserDto
import com.imhungry.jjongseol.data.model.user.response.UserInfoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApi {
    @GET("api/users/{email}")
    suspend fun getUserByEmail(@Path("email") email: String): UserDto

    @GET("/api/users/{email}")
    suspend fun getUserInfo(
        @Path("email") email: String
    ): Response<SuccessResponse<UserInfoResponse>>

    @GET("/api/users/my_nickname")
    suspend fun getMyNickname2(): Response<SuccessResponse<String>>
}