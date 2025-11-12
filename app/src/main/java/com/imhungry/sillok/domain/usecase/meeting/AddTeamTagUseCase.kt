package com.imhungry.sillok.domain.usecase.meeting

import com.imhungry.sillok.domain.model.meeting.AddTeamTagRequest
import com.imhungry.sillok.domain.repository.meeting.MeetingRepository
import javax.inject.Inject

class AddTeamTagUseCase @Inject constructor(
    private val repository: MeetingRepository
) {
    suspend operator fun invoke(meetingId: Long, request: AddTeamTagRequest) =
        repository.addTeamTag(meetingId, request)
}

