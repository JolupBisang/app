package com.imhungry.sillok.data.remote.places

import com.imhungry.sillok.data.model.places.NaverPlacesResponseDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface PlacesApi {
    @GET("v1/search/local.json")
    suspend fun searchPlaces(
        @Header("X-Naver-Client-Id") clientId: String,
        @Header("X-Naver-Client-Secret") clientSecret: String,
        @Query("query") query: String,
        @Query("display") display: Int = 10,
        @Query("start") start: Int = 1,
        @Query("sort") sort: String = "random"
    ): NaverPlacesResponseDto
}