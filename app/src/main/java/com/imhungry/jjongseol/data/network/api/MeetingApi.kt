package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.meeting.MeetingReq
import com.imhungry.jjongseol.data.model.meeting.response.MeetingDetailRes
import com.imhungry.jjongseol.data.model.meeting.request.MeetingStatusUpdateReq
import com.imhungry.jjongseol.data.model.response.SuccessResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MeetingApi {
    @POST("api/meetings")
    suspend fun createMeeting(@Body request: MeetingReq): Response<Unit>

    @GET("/api/meetings/{meetingId}")
    suspend fun getMeetingDetail(
        @Path("meetingId") meetingId: Long
    ): Response<SuccessResponse<MeetingDetailRes>>

    @PUT("/api/meetings/{meetingId}/status")
    suspend fun updateMeetingStatus(
        @Path("meetingId") meetingId: Long,
        @Body statusUpdateReq: MeetingStatusUpdateReq
    ): Response<SuccessResponse<Unit>>
}
