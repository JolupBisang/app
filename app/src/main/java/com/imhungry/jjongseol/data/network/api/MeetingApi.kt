package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.home.MeetingListWrapper
import com.imhungry.jjongseol.data.model.meeting.response.MeetingDetailRes
import com.imhungry.jjongseol.data.model.meeting.MeetingReq
import com.imhungry.jjongseol.data.model.meeting.request.MeetingStatusUpdateReq
import com.imhungry.jjongseol.data.model.meeting.request.MeetingUpdateReq
import com.imhungry.jjongseol.data.model.meeting.response.MeetingCreationRes
import com.imhungry.jjongseol.data.model.meeting.response.MeetingStatusChangeRes
import com.imhungry.jjongseol.data.model.meeting.response.MeetingDetailUpdateRes
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.PUT

interface MeetingApi {
    @POST("/api/v1/meetings")
    suspend fun createMeeting(@Body request: MeetingReq): Response<MeetingCreationRes>

    @GET("api/meetings")
    suspend fun getMeetings(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<MeetingListWrapper>

    @GET("/api/v1/meetings/{meetingId}")
    suspend fun getMeetingDetail(
        @Path("meetingId") meetingId: Long
    ): Response<MeetingDetailRes>

    @PUT("/api/v1/meetings/{meetingId}/status")
    suspend fun updateMeetingStatus(
        @Path("meetingId") meetingId: Long,
        @Body statusUpdateReq: MeetingStatusUpdateReq
    ): Response<MeetingStatusChangeRes>

    @PUT("/api/v1/meetings/{meetingId}")
    suspend fun updateMeeting(
        @Path("meetingId") meetingId: Long,
        @Body meetingUpdateReq: MeetingUpdateReq
    ): Response<MeetingDetailUpdateRes>

}