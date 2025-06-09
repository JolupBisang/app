package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.participationrate.response.ParticipationRateHistoryRes
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ParticipationRateApi {
    @GET("/api/participation-rate/{meetingId}")
    suspend fun getParticipationRateHistory(
        @Path("meetingId") meetingId: Long
    ): Response<ParticipationRateHistoryRes>
}