package com.imhungry.sillok.domain.usecase.meeting

import com.imhungry.sillok.domain.repository.meeting.MeetingRepository
import javax.inject.Inject

class GetMeetingDetailUseCase @Inject constructor(
    private val repository: MeetingRepository
) {
    suspend operator fun invoke(meetingId: Long) =
        repository.getMeetingDetail(meetingId)
}
