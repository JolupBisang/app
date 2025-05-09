package com.imhungry.jjongseol.data.network.api

import retrofit2.http.POST
import retrofit2.http.Path

interface SummaryApi {
    @POST("/api/summary/send/{meetingId}")
    suspend fun sendTestSummary(@Path("meetingId") meetingId: Long)
}
