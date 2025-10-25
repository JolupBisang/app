package com.imhungry.sillok.presentation.viewmodel.voice

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.data.local.VoiceRecognitionStore
import com.imhungry.sillok.data.local.VoiceRecognitionStateData
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.usecase.audio.EmbeddingAudioUseCase
import com.imhungry.sillok.presentation.state.voice.RecordState
import com.imhungry.sillok.presentation.state.voice.VoiceRecognitionConstants
import com.imhungry.sillok.presentation.state.voice.VoiceRecognitionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class VoiceRecognitionViewModel @Inject constructor(
    private val embeddingAudioUseCase: EmbeddingAudioUseCase,
    private val voiceRecognitionStore: VoiceRecognitionStore
) : ViewModel() {
    
    private val _state = MutableStateFlow(VoiceRecognitionState())
    val state: StateFlow<VoiceRecognitionState> = _state.asStateFlow()
    
    private var recorder: MediaRecorder? = null
    private var startTime: Long = 0L
    private var audioFile: File? = null
    
    init {
        restoreState()
    }
    
    private fun restoreState() {
        viewModelScope.launch {
            voiceRecognitionStore.voiceRecognitionState.collect { savedState ->
                if (savedState.isVoiceRecognitionActive) {
                    _state.update { currentState ->
                        currentState.copy(
                            currentStep = savedState.currentStep,
                            recordState = stringToRecordState(savedState.recordState),
                            recordedDuration = savedState.recordedDuration,
                            isProcessing = savedState.isProcessing,
                            error = savedState.error,
                            isUploading = savedState.isUploading,
                            uploadSuccess = savedState.uploadSuccess
                        )
                    }
                }
            }
        }
    }
    
    private fun stringToRecordState(stateString: String): RecordState {
        return when (stateString) {
            "Recording" -> RecordState.Recording
            "Recorded" -> RecordState.Recorded
            "TooShort" -> RecordState.TooShort
            else -> RecordState.Idle
        }
    }
    
    private fun recordStateToString(recordState: RecordState): String {
        return when (recordState) {
            is RecordState.Recording -> "Recording"
            is RecordState.Recorded -> "Recorded"
            is RecordState.TooShort -> "TooShort"
            else -> "Idle"
        }
    }
    
    private suspend fun saveCurrentState() {
        val currentState = _state.value
        val existing = voiceRecognitionStore.voiceRecognitionState.first()
        val stateData = VoiceRecognitionStateData(
            currentStep = currentState.currentStep,
            recordState = recordStateToString(currentState.recordState),
            recordedDuration = currentState.recordedDuration,
            isProcessing = currentState.isProcessing,
            error = currentState.error,
            isUploading = currentState.isUploading,
            uploadSuccess = currentState.uploadSuccess,
            isVoiceRecognitionActive = existing.isVoiceRecognitionActive,
            isCompleted = existing.isCompleted
        )
        voiceRecognitionStore.saveVoiceRecognitionState(stateData)
    }
    
    fun startRecording(context: Context) {

        viewModelScope.launch {
            try {
                _state.update { it.copy(isProcessing = true) }
                
                val file = createAudioFile(context, state.value.currentStep)
                audioFile = file
                
                recorder = createMediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioSamplingRate(16000)
                    setAudioChannels(1)
                    setOutputFile(file.absolutePath)
                    prepare()
                    start()
                }
                
                startTime = System.currentTimeMillis()
                _state.update { 
                    it.copy(
                        recordState = RecordState.Recording,
                        isProcessing = false
                    ) 
                }
                
                saveCurrentState()
                
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        recordState = RecordState.Idle,
                        isProcessing = false
                    ) 
                }
                cleanupRecorder()
            }
        }
    }
    
    fun stopRecording() {
        viewModelScope.launch {
            try {
                val endTime = System.currentTimeMillis()
                val duration = (endTime - startTime) / 1000f
                
                recorder?.apply {
                    stop()
                    release()
                }
                recorder = null
                
                if (duration < VoiceRecognitionConstants.MIN_RECORDING_DURATION) {
                    _state.update { 
                        it.copy(
                            recordState = RecordState.TooShort,
                            recordedDuration = duration
                        ) 
                    }
                } else {
                    _state.update { 
                        it.copy(
                            recordState = RecordState.Recorded,
                            recordedDuration = duration
                        ) 
                    }
                }
                
                saveCurrentState()
                
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        recordState = RecordState.TooShort
                    ) 
                }
                cleanupRecorder()
            }
        }
    }
    
    fun retryRecording() {
        audioFile?.delete()
        audioFile = null
        _state.update { 
            it.copy(
                recordState = RecordState.Idle,
                recordedDuration = 0f,
                error = null,
                uploadSuccess = false
            ) 
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    fun nextStep() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isUploading = false,
                    uploadSuccess = true
                )
            }
            if (state.value.currentStep < VoiceRecognitionConstants.TOTAL_STEPS) {
                _state.update {
                    it.copy(
                        currentStep = it.currentStep + 1,
                        recordState = RecordState.Idle,
                        recordedDuration = 0f,
                        isProcessing = false,
                        uploadSuccess = false
                    )
                }
                audioFile = null
            } else {
                // 모든 단계가 완료되면 완료 플래그를 저장
                _state.update {
                    it.copy(
                        currentStep = it.currentStep + 1,
                        isProcessing = false,
                        uploadSuccess = false
                    )
                }
                // 총 3단계 완료 시에만 완료 플래그 저장
                voiceRecognitionStore.setCompleted(true)
            }

            saveCurrentState()
//
//            try {
//                _state.update { it.copy(isProcessing = true, error = null) }
//
//                audioFile?.let { file ->
//                    _state.update { it.copy(isUploading = true) }
//
//                    val result = embeddingAudioUseCase(file)
//
//                    when (result) {
//                        is ApiResult.Success -> {
//                            _state.update {
//                                it.copy(
//                                    isUploading = false,
//                                    uploadSuccess = true
//                                )
//                            }
//                        }
//                        is ApiResult.Failure -> {
//                            _state.update {
//                                it.copy(
//                                    isUploading = false,
//                                    error = result.message ?: "오디오 업로드에 실패했습니다."
//                                )
//                            }
//                            return@launch
//                        }
//                    }
//                }
//
//                if (state.value.currentStep < VoiceRecognitionConstants.TOTAL_STEPS) {
//                    _state.update {
//                        it.copy(
//                            currentStep = it.currentStep + 1,
//                            recordState = RecordState.Idle,
//                            recordedDuration = 0f,
//                            isProcessing = false,
//                            uploadSuccess = false
//                        )
//                    }
//                    audioFile = null
//                } else {
//                    // 모든 단계가 완료되면 완료 플래그를 저장
//                    _state.update {
//                        it.copy(
//                            currentStep = it.currentStep + 1,
//                            isProcessing = false,
//                            uploadSuccess = false
//                        )
//                    }
//                    // 총 3단계 완료 시에만 완료 플래그 저장
//                    voiceRecognitionStore.setCompleted(true)
//                }
//
//                saveCurrentState()
//
//            } catch (e: Exception) {
//                _state.update {
//                    it.copy(
//                        isProcessing = false,
//                        isUploading = false,
//                        error = e.localizedMessage ?: "오류가 발생했습니다."
//                    )
//                }
//            }
        }
    }
    
    private fun createAudioFile(context: Context, step: Int): File {
        val fileName = "voice_record_step${step}_${System.currentTimeMillis()}.m4a"
        return File(context.cacheDir, fileName)
    }
    
    private fun createMediaRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder()
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }
    
    private fun cleanupRecorder() {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            // 무시
        } finally {
            recorder = null
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        cleanupRecorder()
        audioFile?.delete()
    }
} 