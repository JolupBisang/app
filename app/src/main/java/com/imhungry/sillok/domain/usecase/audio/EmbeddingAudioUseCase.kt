package com.imhungry.sillok.domain.usecase.audio

import com.imhungry.sillok.domain.repository.audio.AudioRepository
import java.io.File
import javax.inject.Inject

class EmbeddingAudioUseCase @Inject constructor(
    private val repository: AudioRepository
) {
    suspend operator fun invoke(audioFile: File) =
        repository.embeddingAudio(audioFile)
}