package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.audio.response.AudioListResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface AudioApi {
    @Multipart
    @POST("/api/v1/audio/embedding")
    suspend fun embeddingAudio(
        @Part audioFile: MultipartBody.Part
    ): Response<Void>

    @GET("/api/v1/meeting/{meetingId}/fullAudio")
    suspend fun getAudioList(
        @Path("meetingId") meetingId: Long
    ): Response<AudioListResponse>
}