package com.imhungry.sillok.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.voiceRecognitionDataStore: DataStore<Preferences> by preferencesDataStore(name = "voice_recognition_store")

@Singleton
class VoiceRecognitionStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionStore: PermissionStore
) {
    private val dataStore = context.voiceRecognitionDataStore

    companion object {
        // 현재 단계(1..TOTAL_STEPS)
        private val CURRENT_STEP_KEY = intPreferencesKey("current_step")
        // 녹음 상태(문자열로 저장)
        private val RECORD_STATE_KEY = stringPreferencesKey("record_state")
        // 누적 녹음 시간(초)
        private val RECORDED_DURATION_KEY = stringPreferencesKey("recorded_duration")
        // 처리 중 여부(로딩)
        private val IS_PROCESSING_KEY = stringPreferencesKey("is_processing")
        // 에러 메시지
        private val ERROR_KEY = stringPreferencesKey("error")
        // 업로드 중 여부
        private val IS_UPLOADING_KEY = stringPreferencesKey("is_uploading")
        // 업로드 성공 여부
        private val UPLOAD_SUCCESS_KEY = stringPreferencesKey("upload_success")
        // 음성 인식 세션 활성 여부(복원 판단)
        private val IS_VOICE_RECOGNITION_ACTIVE_KEY = stringPreferencesKey("is_voice_recognition_active")
        // 음성 인식 온보딩/학습 완료 여부
        private val IS_VOICE_RECOGNITION_COMPLETED_KEY = stringPreferencesKey("is_voice_recognition_completed")
    }

    val voiceRecognitionState: Flow<VoiceRecognitionStateData> =
        combine(dataStore.data.map { preferences ->
            VoiceRecognitionInternalState(
                currentStep = preferences[CURRENT_STEP_KEY] ?: 1,
                recordState = preferences[RECORD_STATE_KEY] ?: "Idle",
                recordedDuration = preferences[RECORDED_DURATION_KEY]?.toFloatOrNull() ?: 0f,
                isProcessing = preferences[IS_PROCESSING_KEY]?.toBooleanStrictOrNull() ?: false,
                error = preferences[ERROR_KEY],
                isUploading = preferences[IS_UPLOADING_KEY]?.toBooleanStrictOrNull() ?: false,
                uploadSuccess = preferences[UPLOAD_SUCCESS_KEY]?.toBooleanStrictOrNull() ?: false,
                isVoiceRecognitionActive = preferences[IS_VOICE_RECOGNITION_ACTIVE_KEY]?.toBooleanStrictOrNull() ?: false,
                isCompleted = preferences[IS_VOICE_RECOGNITION_COMPLETED_KEY]?.toBooleanStrictOrNull() ?: false
            )
        }, permissionStore.permissionState) { internal, perm ->
            VoiceRecognitionStateData(
                currentStep = internal.currentStep,
                recordState = internal.recordState,
                recordedDuration = internal.recordedDuration,
                isProcessing = internal.isProcessing,
                hasPermission = perm.hasPermission,
                hasNotificationPermission = perm.hasNotificationPermission,
                error = internal.error,
                isUploading = internal.isUploading,
                uploadSuccess = internal.uploadSuccess,
                isVoiceRecognitionActive = internal.isVoiceRecognitionActive,
                isCompleted = internal.isCompleted
            )
        }

    // 전체 음성 인식 상태를 저장합니다.
    suspend fun saveVoiceRecognitionState(state: VoiceRecognitionStateData) {
        dataStore.edit { preferences ->
            preferences[CURRENT_STEP_KEY] = state.currentStep
            preferences[RECORD_STATE_KEY] = state.recordState
            preferences[RECORDED_DURATION_KEY] = state.recordedDuration.toString()
            preferences[IS_PROCESSING_KEY] = state.isProcessing.toString()
            state.error?.let { preferences[ERROR_KEY] = it }
            preferences[IS_UPLOADING_KEY] = state.isUploading.toString()
            preferences[UPLOAD_SUCCESS_KEY] = state.uploadSuccess.toString()
            preferences[IS_VOICE_RECOGNITION_ACTIVE_KEY] = state.isVoiceRecognitionActive.toString()
            preferences[IS_VOICE_RECOGNITION_COMPLETED_KEY] = state.isCompleted.toString()
        }
    }

    // 저장값 전체를 초기화합니다
    suspend fun clearVoiceRecognitionState() {
        dataStore.edit { preferences ->
            preferences.remove(CURRENT_STEP_KEY)
            preferences.remove(RECORD_STATE_KEY)
            preferences.remove(RECORDED_DURATION_KEY)
            preferences.remove(IS_PROCESSING_KEY)
            preferences.remove(ERROR_KEY)
            preferences.remove(IS_UPLOADING_KEY)
            preferences.remove(UPLOAD_SUCCESS_KEY)
            preferences.remove(IS_VOICE_RECOGNITION_ACTIVE_KEY)
        }
    }

    // 전체 프로세스 완료 여부를 설정합니다.
    suspend fun setCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_VOICE_RECOGNITION_COMPLETED_KEY] = completed.toString()
        }
    }
}

data class VoiceRecognitionStateData(
    val currentStep: Int = 1,
    val recordState: String = "Idle",
    val recordedDuration: Float = 0f,
    val isProcessing: Boolean = false,
    val hasPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val error: String? = null,
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val isVoiceRecognitionActive: Boolean = false,
    val isCompleted: Boolean = false
)

private data class VoiceRecognitionInternalState(
    val currentStep: Int,
    val recordState: String,
    val recordedDuration: Float,
    val isProcessing: Boolean,
    val error: String?,
    val isUploading: Boolean,
    val uploadSuccess: Boolean,
    val isVoiceRecognitionActive: Boolean,
    val isCompleted: Boolean
)
