package com.imhungry.sillok.domain.usecase.voice

import com.imhungry.sillok.data.local.VoiceRecognitionStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CheckVoiceRecognitionCompletionUseCase @Inject constructor(
    private val voiceRecognitionStore: VoiceRecognitionStore
) {
    suspend operator fun invoke(): Boolean {
        return voiceRecognitionStore.isCompleted.first()
    }
}