package com.imhungry.jjongseol.data.network.api

import retrofit2.http.POST
import retrofit2.http.Path

interface FeedbackApi {
    @POST("api/feedback/send/{meetingId}")
    suspend fun sendTestFeedback(@Path("meetingId") meetingId: Long)
}