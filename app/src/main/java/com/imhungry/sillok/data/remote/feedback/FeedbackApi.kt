package com.imhungry.sillok.data.remote.feedback

import com.imhungry.sillok.data.model.feedback.FeedbackListResDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FeedbackApi {
    @GET("/api/v1/meetings/{meetingId}/feedbacks")
    suspend fun getFeedbacks(
        @Path("meetingId") meetingId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 30
    ): Response<FeedbackListResDto>
}
