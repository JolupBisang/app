package com.imhungry.jjongseol.data.repository

import com.imhungry.jjongseol.data.model.meeting.request.ParticipantAddReq
import com.imhungry.jjongseol.data.network.api.MeetingUserApi
import javax.inject.Inject

class MeetingUserRepository @Inject constructor(
    private val api: MeetingUserApi
) {
    suspend fun addParticipants(meetingId: Long, emails: List<String>): Boolean {
        val response = api.addParticipants(meetingId, ParticipantAddReq(emails))
        return response.isSuccessful
    }

    suspend fun removeParticipant(meetingId: Long, participantUserId: Long): Boolean {
        val response = api.removeParticipant(meetingId, participantUserId)
        return response.isSuccessful
    }
}
