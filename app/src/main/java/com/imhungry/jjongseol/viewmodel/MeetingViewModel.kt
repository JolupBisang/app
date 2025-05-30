package com.imhungry.jjongseol.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.data.model.meeting.MeetingReq
import com.imhungry.jjongseol.data.model.meeting.MeetingStatus
import com.imhungry.jjongseol.data.model.meeting.response.MeetingDetailRes
import com.imhungry.jjongseol.data.network.api.MeetingApi
import com.imhungry.jjongseol.data.repository.LoginRepository
import com.imhungry.jjongseol.data.repository.MeetingRepository
import com.imhungry.jjongseol.data.repository.MeetingResult
import com.imhungry.jjongseol.feature.audio.AudioStreamingService
import com.imhungry.jjongseol.feature.meeting.MeetingSseSubscriber
import com.imhungry.jjongseol.feature.meeting.MeetingStreamController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeetingViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val meetingRepository: MeetingRepository,
    private val meetingApi: MeetingApi,
    val streamController: MeetingStreamController,
    val sseSubscriber: MeetingSseSubscriber
) : ViewModel() {

    init {
        streamController.onWebSocketErrorMessage = { msg ->
            Log.e("MeetingViewModel", "WebSocket 에러 수신됨: $msg")  // 이 로그가 찍히는지 확인
            //setError(ApiError(msg, null))
        }
    }

    private val _meetingDetail = MutableStateFlow<MeetingDetailRes?>(null)
    val meetingDetail: StateFlow<MeetingDetailRes?> = _meetingDetail

    private val _meetingStatus = MutableStateFlow<MeetingStatus?>(null)
    val meetingStatus: StateFlow<MeetingStatus?> = _meetingStatus

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun createMeeting(
        meetingReq: MeetingReq,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = meetingApi.createMeeting(meetingReq)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("에러 발생: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                onError("예외 발생: ${e.message}")
            }
        }
    }

    fun loadMeetingDetail(meetingId: Long) {
        viewModelScope.launch {
            when (val result = meetingRepository.getMeetingDetail(meetingId)) {
                is MeetingResult.Success -> {
                    _meetingDetail.value = result.data
                    _meetingStatus.value = MeetingStatus.from(result.data.meetingStatus)
                }
                is MeetingResult.Error -> {
                    _errorMessage.value = result.errorResponse?.message ?: result.message
                }
                is MeetingResult.Exception -> {
                    _errorMessage.value = result.throwable.message ?: "네트워크 오류"
                }
            }
        }
    }

    fun updateMeetingStatus(meetingId: Long, targetStatus: MeetingStatus) {
        viewModelScope.launch {
            when (val result = meetingRepository.updateMeetingStatus(meetingId, targetStatus)) {
                is MeetingResult.Success -> {
                    loadMeetingDetail(meetingId)
                }
                is MeetingResult.Error -> {
                    _errorMessage.value = result.errorResponse?.message ?: result.message
                }
                is MeetingResult.Exception -> {
                    _errorMessage.value = result.throwable.message ?: "네트워크 오류"
                }
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun initializeSession(
        meetingId: Long,
        timeProvider: () -> String,
        scope: CoroutineScope
    ) {
        val token = loginRepository.getToken() ?: ""

        AudioStreamingService.onWebSocketErrorMessage = { msg ->
            streamController.onWebSocketErrorMessage?.invoke(msg)
        }

        if (streamController.startStreamingSafely(meetingId, token)) {
            Log.d("MeetingScreen", "initializeSession 호출됨")
            streamController.resumeEncoding()
        }

        sseSubscriber.apply {
            subscribeToSummary(meetingId)
            subscribeToParticipationRate(meetingId)
            subscribeToFeedback(meetingId)
        }
    }

    fun cleanupSession() {
        streamController.stopStreaming()
        streamController.resetMicState()
        sseSubscriber.stopSse()
    }
}
