package com.imhungry.sillok.data.repository.meetinguser

import com.imhungry.sillok.data.model.meetinguser.ParticipantAddReqDto
import com.imhungry.sillok.data.remote.meetinguser.MeetingUserApi
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.repository.meetinguser.MeetingUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MeetingUserRepositoryImpl @Inject constructor(
    private val api: MeetingUserApi
) : MeetingUserRepository {
    override suspend fun addMeetingUser(meetingId: Long, emails: List<String>): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = api.addMeetingUser(meetingId, ParticipantAddReqDto(emails))
            if (res.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Failure(res.message())
            }
        } catch (e: Exception) {
            ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
        }
    }

    override suspend fun removeMeetingUser(meetingId: Long, participantUserId: Long): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = api.removeMeetingUser(meetingId, participantUserId)
            if (res.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Failure(res.message())
            }
        } catch (e: Exception) {
            ApiResult.Failure(e.localizedMessage ?: "알 수 없는 오류")
        }
    }
}
