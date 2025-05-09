package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.user.UserDto
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApi {
    @GET("api/users/{email}")
    suspend fun getUserByEmail(@Path("email") email: String): UserDto
}