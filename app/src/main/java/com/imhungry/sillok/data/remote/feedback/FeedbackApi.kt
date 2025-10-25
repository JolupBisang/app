package com.imhungry.sillok.data.remote.feedback

import com.imhungry.sillok.data.model.feedback.FeedbackListResDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface FeedbackApi {
    @GET("/api/v1/meetings/{meetingId}/feedbacks")
    suspend fun getFeedbacks(
        @Path("meetingId") meetingId: Long
    ): Response<FeedbackListResDto>
}
