package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.meeting.MeetingReq
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MeetingApi {
    @POST("api/meetings")
    suspend fun createMeeting(@Body request: MeetingReq): Response<Unit>
}