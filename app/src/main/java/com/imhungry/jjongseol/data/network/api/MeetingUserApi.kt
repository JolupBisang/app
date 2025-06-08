package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.meeting.request.ParticipantAddReq
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface MeetingUserApi {
    @POST("/api/meeting-users/{meetingId}")
    suspend fun addParticipants(
        @Path("meetingId") meetingId: Long,
        @Body request: ParticipantAddReq
    ): Response<Unit>

    @DELETE("/api/meeting-users/{meetingId}/{participantUserId}")
    suspend fun removeParticipant(
        @Path("meetingId") meetingId: Long,
        @Path("participantUserId") participantUserId: Long
    ): Response<Unit>
}