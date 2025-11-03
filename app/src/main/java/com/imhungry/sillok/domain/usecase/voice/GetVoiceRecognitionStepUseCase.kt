package com.imhungry.sillok.domain.usecase.voice

import com.imhungry.sillok.data.local.VoiceRecognitionStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 현재 음성 학습 단계를 가져오는 UseCase
 */
class GetVoiceRecognitionStepUseCase @Inject constructor(
    private val voiceRecognitionStore: VoiceRecognitionStore
) {
    suspend operator fun invoke(): Int {
        return voiceRecognitionStore.currentStep.first()
    }
}

