package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.response.SuccessResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AudioApi {
    @Multipart
    @POST("/api/audio/embedding")
    suspend fun embeddingAudio(
        @Part audioFile: MultipartBody.Part
    ): Response<SuccessResponse<Unit>>
}