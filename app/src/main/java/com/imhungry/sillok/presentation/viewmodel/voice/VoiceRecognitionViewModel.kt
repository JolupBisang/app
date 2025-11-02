package com.imhungry.sillok.presentation.viewmodel.voice

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.usecase.audio.EmbeddingAudioUseCase
import com.imhungry.sillok.domain.usecase.voice.GetVoiceRecognitionStepUseCase
import com.imhungry.sillok.domain.usecase.voice.ProgressUpdateResult
import com.imhungry.sillok.domain.usecase.voice.UpdateVoiceRecognitionProgressUseCase
import com.imhungry.sillok.presentation.state.voice.RecordState
import com.imhungry.sillok.presentation.state.voice.VoiceRecognitionConstants
import com.imhungry.sillok.presentation.state.voice.VoiceRecognitionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class VoiceRecognitionViewModel @Inject constructor(
    private val embeddingAudioUseCase: EmbeddingAudioUseCase,
    private val getVoiceRecognitionStepUseCase: GetVoiceRecognitionStepUseCase,
    private val updateVoiceRecognitionProgressUseCase: UpdateVoiceRecognitionProgressUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(VoiceRecognitionState())
    val state: StateFlow<VoiceRecognitionState> = _state.asStateFlow()
    
    private var recorder: MediaRecorder? = null
    private var startTime: Long = 0L
    private var audioFile: File? = null
    
    init {
        loadCurrentStep()
    }
    
    private fun loadCurrentStep() {
        viewModelScope.launch {
            val currentStep = getVoiceRecognitionStepUseCase()
            _state.update { it.copy(currentStep = currentStep) }
        }
    }
    
    fun startRecording(context: Context) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isProcessing = true) }
                
                val file = createAudioFile(context, state.value.currentStep)
                audioFile = file
                
                recorder = setupMediaRecorder(file)
                
                startTime = System.currentTimeMillis()
                _state.update { 
                    it.copy(
                        recordState = RecordState.Recording,
                        isProcessing = false
                    ) 
                }
                
            } catch (e: Exception) {
                handleRecordingError()
            }
        }
    }
    
    fun stopRecording() {
        viewModelScope.launch {
            try {
                val duration = calculateRecordingDuration()
                stopAndReleaseRecorder()
                updateStateAfterRecording(duration)
            } catch (e: Exception) {
                handleStopRecordingError()
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
            try {
                _state.update { it.copy(isProcessing = true, error = null) }

                // 오디오 파일 업로드
                val uploadSuccess = uploadAudioFile()
                if (!uploadSuccess) return@launch

                // 진행 상태 업데이트
                handleProgressUpdate()

            } catch (e: Exception) {
                handleNextStepError(e)
            }
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
    
    private fun setupMediaRecorder(file: File): MediaRecorder {
        return createMediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(16000)
            setAudioChannels(1)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
    }
    
    private fun calculateRecordingDuration(): Float {
        val endTime = System.currentTimeMillis()
        return (endTime - startTime) / 1000f
    }
    
    private fun stopAndReleaseRecorder() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }
    
    private fun updateStateAfterRecording(duration: Float) {
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
    }
    
    private suspend fun uploadAudioFile(): Boolean {
        audioFile?.let { file ->
            _state.update { it.copy(isUploading = true) }
            val result = embeddingAudioUseCase(file)

            return when (result) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            isUploading = false,
                            uploadSuccess = true
                        )
                    }
                    true
                }
                is ApiResult.Failure -> {
                    _state.update {
                        it.copy(
                            isUploading = false,
                            error = result.message ?: "오디오 업로드에 실패했습니다."
                        )
                    }
                    false
                }
            }
        }
        return true
    }
    
    private suspend fun handleProgressUpdate() {
        val progressResult = updateVoiceRecognitionProgressUseCase(state.value.currentStep)
        
        when (progressResult) {
            is ProgressUpdateResult.NextStep -> {
                updateStateForNextStep(progressResult.step)
            }
            is ProgressUpdateResult.Completed -> {
                updateStateForCompletion()
            }
        }
    }
    
    private fun updateStateForNextStep(step: Int) {
        _state.update {
            it.copy(
                currentStep = step,
                recordState = RecordState.Idle,
                recordedDuration = 0f,
                isProcessing = false,
                uploadSuccess = false
            )
        }
        audioFile = null
    }
    
    private fun updateStateForCompletion() {
        _state.update {
            it.copy(
                currentStep = VoiceRecognitionConstants.TOTAL_STEPS + 1,
                isProcessing = false,
                uploadSuccess = false
            )
        }
    }
    
    private fun handleRecordingError() {
        _state.update { 
            it.copy(
                recordState = RecordState.Idle,
                isProcessing = false
            ) 
        }
        cleanupRecorder()
    }
    
    private fun handleStopRecordingError() {
        _state.update { 
            it.copy(
                recordState = RecordState.TooShort
            ) 
        }
        cleanupRecorder()
    }
    
    private fun handleNextStepError(e: Exception) {
        _state.update {
            it.copy(
                isProcessing = false,
                isUploading = false,
                error = e.localizedMessage ?: "오류가 발생했습니다."
            )
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