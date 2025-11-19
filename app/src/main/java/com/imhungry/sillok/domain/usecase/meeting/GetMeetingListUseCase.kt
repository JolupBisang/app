package com.imhungry.sillok.domain.usecase.meeting

import com.imhungry.sillok.domain.repository.meeting.MeetingRepository
import javax.inject.Inject

class GetMeetingListUseCase @Inject constructor(
    private val repository: MeetingRepository
) {
    suspend operator fun invoke(year: Int, month: Int) =
        repository.getMeetings2(year, month)
}