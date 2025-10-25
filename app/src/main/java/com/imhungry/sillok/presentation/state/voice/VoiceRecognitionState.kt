package com.imhungry.sillok.presentation.state.voice

import androidx.compose.runtime.Stable

@Stable
data class VoiceRecognitionState(
    // 현재 단계(1..TOTAL_STEPS)
    val currentStep: Int = 1,
    // 녹음 상태(대기/녹음 중/녹음 완료/너무 짧음)
    val recordState: RecordState = RecordState.Idle,
    // 누적 녹음 시간(초)
    val recordedDuration: Float = 0f,
    // 서버 업로드/처리 등 비동기 작업 중 여부
    val isProcessing: Boolean = false,
    // 사용자에게 노출할 에러 메시지
    val error: String? = null,
    // 업로드 진행 중 여부
    val isUploading: Boolean = false,
    // 업로드 성공 여부
    val uploadSuccess: Boolean = false
)

sealed class RecordState {
    object Idle : RecordState()
    object Recording : RecordState()
    object Recorded : RecordState()
    object TooShort : RecordState()
}

object VoiceRecognitionConstants {
    const val TOTAL_STEPS = 3
    const val MIN_RECORDING_DURATION = 0.5f
}