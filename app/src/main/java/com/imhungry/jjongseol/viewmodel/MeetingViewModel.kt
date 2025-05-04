package com.imhungry.jjongseol.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.data.model.feedback.FeedbackItem
import com.imhungry.jjongseol.data.model.meeting.SummaryItem
import com.imhungry.jjongseol.data.network.FeedbackApi
import com.imhungry.jjongseol.data.network.ParticipationRateApi
import com.imhungry.jjongseol.data.network.SseClient
import com.imhungry.jjongseol.data.network.SummaryApi
import com.imhungry.jjongseol.service.AudioStreamingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeetingViewModel @Inject constructor(
    application: Application,
    private val sseClient: SseClient,
    private val summaryApi: SummaryApi,
    private val participationRateApi: ParticipationRateApi,
    private val feedbackApi: FeedbackApi
) : AndroidViewModel(application) {

    private val context by lazy { application.applicationContext }

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // 음성 스트리밍
    fun pauseEncoding() = AudioStreamingService.pauseEncoding()
    fun resumeEncoding() = AudioStreamingService.resumeEncoding()

    fun startStreamingService() {
        val prefs = context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
        if (!prefs.contains("meetingStartedAt")) {
            prefs.edit()
                .putBoolean("isMeetingOngoing", true)
                .putLong("meetingStartedAt", System.currentTimeMillis())
                .apply()
        }

        val intent = Intent(context, AudioStreamingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(intent)
        else
            context.startService(intent)
    }

    fun stopStreamingService() {
        context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE).edit()
            .putBoolean("isMeetingOngoing", false)
            .remove("meetingStartedAt")
            .apply()
        context.stopService(Intent(context, AudioStreamingService::class.java))
    }

    // SSE 수신
    private val _summaryList = MutableStateFlow<List<SummaryItem>>(emptyList())
    val summaryList: StateFlow<List<SummaryItem>> = _summaryList.asStateFlow()

    private val _participationRate = MutableStateFlow<String?>(null)
    val participationRate: StateFlow<String?> = _participationRate.asStateFlow()

    private val _feedbackList = MutableStateFlow<List<FeedbackItem>>(emptyList())
    val feedbackList: StateFlow<List<FeedbackItem>> = _feedbackList.asStateFlow()

    fun subscribeToSummary(meetingId: Long, timeProvider: () -> String) {
        sseClient.subscribeToSummary(
            meetingId,
            onEventReceived = {
                _summaryList.value += SummaryItem(it.trim('"'), timeProvider())
            },
            onError = {
                _errorMessage.value = "요약 수신 실패: $it"
            }
        )
    }

    fun subscribeToParticipationRate(meetingId: Long) {
        sseClient.subscribeToParticipationRate(
            meetingId,
            onEventReceived = {
                _participationRate.value = it
            },
            onError = {
                _errorMessage.value = "점유율 수신 실패: $it"
            }
        )
    }

    fun subscribeToFeedback(meetingId: Long, timeProvider: () -> String) {
        sseClient.subscribeToFeedback(
            meetingId,
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

    // 테스트 데이터 전송
    private var summaryTestJob: Job? = null
    private var participationTestJob: Job? = null
    private var feedbackTestJob: Job? = null

    fun startSendingTestSummary(meetingId: Long) {
        if (summaryTestJob?.isActive == true) return
        summaryTestJob = viewModelScope.launch {
            while (isActive) {
                try {
                    summaryApi.sendTestSummary(meetingId)
                } catch (e: Exception) {
                    println("요약 전송 실패: ${e.message}")
                }
                delay(5000)
            }
        }
    }

    fun stopSendingTestSummary() {
        summaryTestJob?.cancel()
        summaryTestJob = null
    }

    fun startSendingTestParticipationRate(meetingId: Long) {
        if (participationTestJob?.isActive == true) return
        participationTestJob = viewModelScope.launch {
            while (isActive) {
                try {
                    participationRateApi.sendTestParticipationRate(meetingId)
                } catch (e: Exception) {
                    println("점유율 전송 실패: ${e.message}")
                }
                delay(5000)
            }
        }
    }

    fun stopSendingTestParticipationRate() {
        participationTestJob?.cancel()
        participationTestJob = null
    }

    fun startSendingTestFeedback(meetingId: Long) {
        if (feedbackTestJob?.isActive == true) return
        feedbackTestJob = viewModelScope.launch {
            while (isActive) {
                try {
                    feedbackApi.sendTestFeedback(meetingId)
                } catch (e: Exception) {
                    println("피드백 전송 실패: ${e.message}")
                }
                delay(5000)
            }
        }
    }

    fun stopSendingTestFeedback() {
        feedbackTestJob?.cancel()
        feedbackTestJob = null
    }
}
