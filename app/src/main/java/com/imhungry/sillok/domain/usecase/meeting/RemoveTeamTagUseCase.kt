package com.imhungry.sillok.domain.usecase.meeting

import com.imhungry.sillok.domain.model.meeting.RemoveTeamTagRequest
import com.imhungry.sillok.domain.repository.meeting.MeetingRepository
import javax.inject.Inject

class RemoveTeamTagUseCase @Inject constructor(
    private val repository: MeetingRepository
) {
    suspend operator fun invoke(meetingId: Long, request: RemoveTeamTagRequest) =
        repository.removeTeamTag(meetingId, request)
}

