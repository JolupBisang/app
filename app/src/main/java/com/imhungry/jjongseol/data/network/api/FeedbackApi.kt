package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.feedback.response.FeedbackListRes
import com.imhungry.jjongseol.data.model.response.SliceResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FeedbackApi {
    @GET("/api/feedback/{meetingId}")
    suspend fun getFeedbacks(
        @Path("meetingId") meetingId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 30,
        @Query("sort") sort: String = "timestamp,DESC"
    ): Response<SliceResponse<FeedbackListRes>>
}