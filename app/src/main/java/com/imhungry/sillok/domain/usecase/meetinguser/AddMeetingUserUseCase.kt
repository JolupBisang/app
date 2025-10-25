package com.imhungry.sillok.domain.usecase.meetinguser

import com.imhungry.sillok.domain.repository.meetinguser.MeetingUserRepository
import javax.inject.Inject

class AddMeetingUserUseCase @Inject constructor(
    private val repository: MeetingUserRepository
) {
    suspend operator fun invoke(meetingId: Long, emails: List<String>) =
        repository.addMeetingUser(meetingId, emails)
}
