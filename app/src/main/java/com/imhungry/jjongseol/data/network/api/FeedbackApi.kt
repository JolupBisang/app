package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.feedback.response.FeedbackListRes
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FeedbackApi {
    @GET("/api/v1/meetings/{meetingId}/feedbacks")
    suspend fun getFeedbacks(
        @Path("meetingId") meetingId: Long
    ): Response<FeedbackListRes>
}