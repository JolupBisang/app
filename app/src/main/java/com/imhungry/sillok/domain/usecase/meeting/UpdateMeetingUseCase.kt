package com.imhungry.sillok.domain.usecase.meeting

import com.imhungry.sillok.data.model.meeting.MeetingUpdateReqDto
import com.imhungry.sillok.domain.repository.meeting.MeetingRepository
import javax.inject.Inject

class UpdateMeetingUseCase @Inject constructor(
    private val repository: MeetingRepository
) {
    suspend operator fun invoke(meetingId: Long, request: MeetingUpdateReqDto) =
        repository.updateMeeting(meetingId, request)
}
