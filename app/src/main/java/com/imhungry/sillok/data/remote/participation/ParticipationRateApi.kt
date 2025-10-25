package com.imhungry.sillok.data.remote.participation

import com.imhungry.sillok.data.model.participation.ParticipationRateHistoryResDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ParticipationRateApi {
    @GET("/api/v1/meeting/{meetingId}/participation-rate")
    suspend fun getParticipationRateHistory(
        @Path("meetingId") meetingId: Long
    ): Response<ParticipationRateHistoryResDto>
}