package com.imhungry.sillok.domain.usecase.meeting

import com.imhungry.sillok.domain.model.meeting.MeetingSearchSlice
import com.imhungry.sillok.domain.repository.meeting.MeetingRepository
import javax.inject.Inject

class SearchMeetingsUseCase @Inject constructor(
    private val repository: MeetingRepository
) {
    suspend operator fun invoke(
        title: String,
        page: Int = 0,
        size: Int = 20
    ) = repository.searchMeetings(title, page, size)
}

