package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.audio.response.AudioListResponse
import com.imhungry.jjongseol.data.model.response.SuccessResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface AudioApi {
    @Multipart
    @POST("/api/audio/embedding")
    suspend fun embeddingAudio(
        @Part audioFile: MultipartBody.Part
    ): Response<SuccessResponse<Unit>>

    @GET("/api/audio/meeting/{meetingId}")
    suspend fun getAudioList(
        @Path("meetingId") meetingId: Long
    ): Response<SuccessResponse<AudioListResponse>>
}