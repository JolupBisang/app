package com.imhungry.sillok.domain.usecase.meeting

import com.imhungry.sillok.domain.model.meeting.CreateMeetingRequest
import com.imhungry.sillok.domain.repository.meeting.MeetingRepository
import javax.inject.Inject

class CreateMeetingUseCase @Inject constructor(
    private val repository: MeetingRepository
) {
    suspend operator fun invoke(request: CreateMeetingRequest) =
        repository.createMeeting(request)
}
