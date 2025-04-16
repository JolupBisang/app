package com.imhungry.jjongseol.data.network

import retrofit2.http.POST
import retrofit2.http.Path

interface ParticipationRateApi {
    @POST("api/participation_rate/send/{meetingId}")
    suspend fun sendParticipationRate(@Path("meetingId") meetingId: Long)
}
