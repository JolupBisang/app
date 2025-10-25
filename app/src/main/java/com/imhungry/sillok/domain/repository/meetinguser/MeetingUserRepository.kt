package com.imhungry.sillok.domain.repository.meetinguser

import com.imhungry.sillok.data.util.ApiResult

interface MeetingUserRepository {
    suspend fun addMeetingUser(meetingId: Long, emails: List<String>): ApiResult<Unit>
    suspend fun removeMeetingUser(meetingId: Long, participantUserId: Long): ApiResult<Unit>
}