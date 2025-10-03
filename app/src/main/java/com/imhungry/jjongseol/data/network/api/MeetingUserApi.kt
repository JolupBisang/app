package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.meeting.request.ParticipantAddReq
import com.imhungry.jjongseol.data.model.meeting.response.ParticipantAdditionRes
import com.imhungry.jjongseol.data.model.meeting.response.ParticipantRemovalRes
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface MeetingUserApi {
    @POST("/api/v1/meeting/{meetingId}/participants")
    suspend fun addParticipants(
        @Path("meetingId") meetingId: Long,
        @Body request: ParticipantAddReq
    ): Response<ParticipantAdditionRes>

    @DELETE("/api/v1/meetings/{meetingId}/participant/{participantId}")
    suspend fun removeParticipant(
        @Path("meetingId") meetingId: Long,
        @Path("participantId") participantId: Long
    ): Response<ParticipantRemovalRes>
}