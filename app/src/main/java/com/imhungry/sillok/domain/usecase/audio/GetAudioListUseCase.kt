package com.imhungry.sillok.domain.usecase.audio

import com.imhungry.sillok.domain.repository.audio.AudioRepository
import javax.inject.Inject

class GetAudioListUseCase @Inject constructor(
    private val repository: AudioRepository
) {
    suspend operator fun invoke(meetingId: Long) =
        repository.getAudioList(meetingId)
}