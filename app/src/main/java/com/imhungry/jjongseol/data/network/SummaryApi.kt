package com.imhungry.jjongseol.data.network

import retrofit2.http.POST
import retrofit2.http.Path

interface SummaryApi {
    @POST("/api/summary/send/{meetingId}")
    suspend fun sendTestSummary(@Path("meetingId") meetingId: Long)
}
