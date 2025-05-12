package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.common.ApiResponse
import com.imhungry.jjongseol.data.model.meeting.MeetingDetailRes
import com.imhungry.jjongseol.data.model.meeting.MeetingReq
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MeetingApi {
    @POST("api/meetings")
    suspend fun createMeeting(@Body request: MeetingReq): Response<Unit>

    @GET("api/meetings/{meetingId}")
    suspend fun getMeetingDetail(@Path("meetingId") meetingId: Long): ApiResponse<MeetingDetailRes>
}