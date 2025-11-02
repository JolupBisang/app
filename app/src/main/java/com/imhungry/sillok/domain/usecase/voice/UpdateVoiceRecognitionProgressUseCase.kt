package com.imhungry.sillok.domain.usecase.voice

import com.imhungry.sillok.data.local.VoiceRecognitionStore
import com.imhungry.sillok.presentation.state.voice.VoiceRecognitionConstants
import javax.inject.Inject

class UpdateVoiceRecognitionProgressUseCase @Inject constructor(
    private val voiceRecognitionStore: VoiceRecognitionStore
) {
    suspend operator fun invoke(currentStep: Int): ProgressUpdateResult {
        val newStep = currentStep + 1
        
        return if (currentStep < VoiceRecognitionConstants.TOTAL_STEPS) {
            // 다음 단계로 이동
            voiceRecognitionStore.setCurrentStep(newStep)
            ProgressUpdateResult.NextStep(newStep)
        } else {
            // 모든 단계 완료
            voiceRecognitionStore.setCurrentStep(newStep)
            voiceRecognitionStore.setCompleted(true)
            ProgressUpdateResult.Completed
        }
    }
}

sealed class ProgressUpdateResult {
    data class NextStep(val step: Int) : ProgressUpdateResult()
    object Completed : ProgressUpdateResult()
}

