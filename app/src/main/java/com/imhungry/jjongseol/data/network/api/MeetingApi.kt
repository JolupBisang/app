package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.home.MeetingListWrapper
import com.imhungry.jjongseol.data.model.response.SuccessResponse
import com.imhungry.jjongseol.data.model.meeting.response.MeetingDetailRes
import com.imhungry.jjongseol.data.model.meeting.MeetingReq
import com.imhungry.jjongseol.data.model.meeting.request.MeetingStatusUpdateReq
import com.imhungry.jjongseol.data.model.meeting.request.MeetingUpdateReq
import com.imhungry.jjongseol.data.model.meeting.response.MeetingCreationRes
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.PUT

interface MeetingApi {
    @POST("api/meetings")
    suspend fun createMeeting(@Body request: MeetingReq): Response<SuccessResponse<MeetingCreationRes>>

    @GET("api/meetings")
    suspend fun getMeetings(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<MeetingListWrapper>

    @GET("/api/meetings/{id}")
    suspend fun getMeetingById(@Path("id") id: Long): Response<SuccessResponse<MeetingDetailRes>>

    @GET("/api/meetings/{meetingId}")
    suspend fun getMeetingDetail(
        @Path("meetingId") meetingId: Long
    ): Response<SuccessResponse<MeetingDetailRes>>

    @PUT("/api/meetings/{meetingId}/status")
    suspend fun updateMeetingStatus(
        @Path("meetingId") meetingId: Long,
        @Body statusUpdateReq: MeetingStatusUpdateReq
    ): Response<SuccessResponse<Unit>>

    @PUT("api/meetings/{meetingId}")
    suspend fun updateMeeting(
        @Path("meetingId") meetingId: Long,
        @Body meetingUpdateReq: MeetingUpdateReq
    ): Response<SuccessResponse<Unit>>

}