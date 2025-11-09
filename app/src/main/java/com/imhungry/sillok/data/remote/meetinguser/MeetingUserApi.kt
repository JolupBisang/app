package com.imhungry.sillok.data.remote.meetinguser

import com.imhungry.sillok.data.model.meetinguser.ParticipantAddReqDto
import com.imhungry.sillok.data.model.meetinguser.ParticipantAdditionResDto
import com.imhungry.sillok.data.model.meetinguser.ParticipantRemovalResDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface MeetingUserApi {
    @POST("/api/v1/meetings/{meetingId}/participants")
    suspend fun addMeetingUser(
        @Path("meetingId") meetingId: Long,
        @Body request: ParticipantAddReqDto
    ): Response<ParticipantAdditionResDto>

    @DELETE("/api/v1/meetings/{meetingId}/participant/{participantId}")
    suspend fun removeMeetingUser(
        @Path("meetingId") meetingId: Long,
        @Path("participantId") participantUserId: Long
    ): Response<ParticipantRemovalResDto>
}
