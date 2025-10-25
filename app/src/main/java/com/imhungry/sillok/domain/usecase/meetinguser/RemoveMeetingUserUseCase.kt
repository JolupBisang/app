package com.imhungry.sillok.domain.usecase.meetinguser

import com.imhungry.sillok.domain.repository.meetinguser.MeetingUserRepository
import javax.inject.Inject

class RemoveMeetingUserUseCase @Inject constructor(
    private val repository: MeetingUserRepository
) {
    suspend operator fun invoke(meetingId: Long, participantUserId: Long) =
        repository.removeMeetingUser(meetingId, participantUserId)
}
