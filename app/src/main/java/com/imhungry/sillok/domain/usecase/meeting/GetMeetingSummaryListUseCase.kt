package com.imhungry.sillok.domain.usecase.meeting

import com.imhungry.sillok.domain.repository.meeting.MeetingRepository
import javax.inject.Inject

class GetMeetingSummaryListUseCase @Inject constructor(
    private val repository: MeetingRepository
) {
    suspend operator fun invoke(
        year: Int? = null,
        month: Int? = null,
        title: String? = null,
        page: Int = 0,
        size: Int = 20
    ) = repository.getMeetings(year, month, title, page, size)
}