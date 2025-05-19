package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.home.MeetingListWrapper
import com.imhungry.jjongseol.data.model.meeting.MeetingReq
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MeetingApi {
    @POST("api/meetings")
    suspend fun createMeeting(@Body request: MeetingReq): Response<Unit>

    @GET("api/meetings")
    suspend fun getMeetings(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<MeetingListWrapper>
}