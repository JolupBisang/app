package com.imhungry.jjongseol.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.controller.StreamingController
import com.imhungry.jjongseol.controller.TestDataSender
import com.imhungry.jjongseol.data.model.MeetingReq
import com.imhungry.jjongseol.data.model.error.ApiError
import com.imhungry.jjongseol.data.model.feedback.FeedbackItem
import com.imhungry.jjongseol.data.model.meeting.SummaryItem
import com.imhungry.jjongseol.data.network.MeetingApi
import com.imhungry.jjongseol.data.network.SseClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeetingViewModel @Inject constructor(
    application: Application,
    private val sseClient: SseClient,
    private val streamingController: StreamingController,
    private val testDataSender: TestDataSender,
    private val meetingApi: MeetingApi
) : AndroidViewModel(application) {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun setError(apiError: ApiError) {
        _errorMessage.value = when (apiError.message) {
            "만료된 토큰입니다." -> "TOKEN_EXPIRED"
            else -> apiError.message ?: "알 수 없는 오류 발생"
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    //새 회의 생성
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

    fun startStreamingService() = streamingController.startStreamingService()
    fun stopStreamingService() = streamingController.stopStreamingService()
    fun pauseEncoding() = streamingController.pauseEncoding()
    fun resumeEncoding() = streamingController.resumeEncoding()

    private val _summaryList = MutableStateFlow<List<SummaryItem>>(emptyList())
    val summaryList: StateFlow<List<SummaryItem>> = _summaryList.asStateFlow()

    private val _participationRate = MutableStateFlow<String?>(null)
    val participationRate: StateFlow<String?> = _participationRate.asStateFlow()

    private val _feedbackList = MutableStateFlow<List<FeedbackItem>>(emptyList())
    val feedbackList: StateFlow<List<FeedbackItem>> = _feedbackList.asStateFlow()

    fun subscribeToSummary(meetingId: Long, timeProvider: () -> String) {
        sseClient.subscribeToEvent(
            endpoint = "summary",
            meetingId = meetingId,
            eventType = "SUMMARY",
            onEventReceived = {
                _summaryList.value += SummaryItem(it.trim('"'), timeProvider())
            },
            onError = {
                _errorMessage.value = "요약 수신 실패: $it"
            }
        )
    }

    fun subscribeToParticipationRate(meetingId: Long) {
        sseClient.subscribeToEvent(
            endpoint = "participation_rate",
            meetingId = meetingId,
            eventType = "PARTICIPATION_RATE",
            onEventReceived = {
                _participationRate.value = it
            },
            onError = {
                _errorMessage.value = "점유율 수신 실패: $it"
            }
        )
    }

    fun subscribeToFeedback(meetingId: Long, timeProvider: () -> String) {
        sseClient.subscribeToEvent(
            endpoint = "feedback",
            meetingId = meetingId,
            eventType = "FEEDBACK",
            onEventReceived = {
                _feedbackList.value += FeedbackItem(it.trim('"'), timeProvider())
            },
            onError = {
                _errorMessage.value = "피드백 수신 실패: $it"
            }
        )
    }

    fun stopSse() {
        sseClient.disconnect()
    }

    fun startSendingTestData(meetingId: Long) {
        testDataSender.startSummary(meetingId, viewModelScope)
        testDataSender.startParticipation(meetingId, viewModelScope)
        testDataSender.startFeedback(meetingId, viewModelScope)
    }

    fun stopSendingTestData() {
        testDataSender.stopSummary()
        testDataSender.stopParticipation()
        testDataSender.stopFeedback()
    }
}