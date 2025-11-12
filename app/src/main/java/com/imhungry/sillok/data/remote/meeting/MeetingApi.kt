package com.imhungry.sillok.data.remote.meeting

import com.imhungry.sillok.data.model.meeting.DuplicationCheckRes
import com.imhungry.sillok.data.model.meeting.MeetingCreationResDto
import com.imhungry.sillok.data.model.meeting.MeetingDetailResDto
import com.imhungry.sillok.data.model.meeting.MeetingDetailSummaryResDto
import com.imhungry.sillok.data.model.meeting.MeetingDetailUpdateResDto
import com.imhungry.sillok.data.model.meeting.MeetingReqDto
import com.imhungry.sillok.data.model.meeting.MeetingStatusChangeResDto
import com.imhungry.sillok.data.model.meeting.MeetingStatusUpdateReqDto
import com.imhungry.sillok.data.model.meeting.MeetingUpdateReqDto
import com.imhungry.sillok.data.model.meeting.TeamTagAdditionReqDto
import com.imhungry.sillok.data.model.meeting.TeamTagAdditionResDto
import com.imhungry.sillok.data.model.meeting.TeamTagRemovalReqDto
import com.imhungry.sillok.data.model.meeting.TeamTagRemovalResDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface MeetingApi {
    @POST("/api/v1/meetings")
    suspend fun createMeeting(
        @Body request: MeetingReqDto
    ): Response<MeetingCreationResDto>

    @GET("/api/v1/meetings/{meetingId}")
    suspend fun getMeetingDetail(
        @Path("meetingId") meetingId: Long
    ): Response<MeetingDetailResDto>

    @GET("/api/v1/meetings")
    suspend fun getMeetings(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<List<MeetingDetailSummaryResDto>>

    @PUT("/api/v1/meetings/{meetingId}/status")
    suspend fun updateMeetingStatus(
        @Path("meetingId") meetingId: Long,
        @Body request: MeetingStatusUpdateReqDto
    ): Response<MeetingStatusChangeResDto>

    @PUT("/api/v1/meetings/{meetingId}")
    suspend fun updateMeeting(
        @Path("meetingId") meetingId: Long,
        @Body request: MeetingUpdateReqDto
    ): Response<MeetingDetailUpdateResDto>

    @GET("/api/v1/meetings/duplicated")
    suspend fun checkDuplicatedTime(
        @Query("startTime") startTime: String,
        @Query("targetMinutes") targetMinutes: Long
    ): Response<DuplicationCheckRes>

    @POST("/api/v1/meetings/{meetingId}/team-tags")
    suspend fun addTeamTag(
        @Path("meetingId") meetingId: Long,
        @Body request: TeamTagAdditionReqDto
    ): Response<TeamTagAdditionResDto>

    @DELETE("/api/v1/meetings/{meetingId}/team-tags")
    suspend fun removeTeamTag(
        @Path("meetingId") meetingId: Long,
        @Body request: TeamTagRemovalReqDto
    ): Response<TeamTagRemovalResDto>
}
