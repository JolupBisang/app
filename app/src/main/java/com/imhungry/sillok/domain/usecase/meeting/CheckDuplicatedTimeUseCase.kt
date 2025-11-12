package com.imhungry.sillok.domain.usecase.meeting

import com.imhungry.sillok.domain.repository.meeting.MeetingRepository
import javax.inject.Inject

class CheckDuplicatedTimeUseCase @Inject constructor(
    private val repository: MeetingRepository
) {
    suspend operator fun invoke(startTime: String, targetMinutes: Long) =
        repository.checkDuplicatedTime(startTime, targetMinutes)
}

