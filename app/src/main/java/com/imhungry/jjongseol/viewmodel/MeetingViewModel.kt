package com.imhungry.jjongseol.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.data.model.agenda.AgendaDto
import com.imhungry.jjongseol.data.model.feedback.FeedbackItem
import com.imhungry.jjongseol.data.model.meeting.SummaryItem
import com.imhungry.jjongseol.data.network.FeedbackApi
import com.imhungry.jjongseol.data.network.ParticipationRateApi
import com.imhungry.jjongseol.data.network.SseClient
import com.imhungry.jjongseol.data.network.SummaryApi
import com.imhungry.jjongseol.data.repository.AgendaRepository
import com.imhungry.jjongseol.service.AudioStreamingService
import com.imhungry.jjongseol.util.handleHttpException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class MeetingViewModel @Inject constructor(
    application: Application,
    private val agendaRepository: AgendaRepository,
    private val sseClient: SseClient,
    private val summaryApi: SummaryApi,
    private val participationRateApi: ParticipationRateApi,
    private val feedbackApi: FeedbackApi
) : AndroidViewModel(application) {

    private val context by lazy { application.applicationContext }

    // 음성 Streaming control
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

    // 아젠다 state
    private val _agendaItems = MutableStateFlow<List<AgendaDto>>(emptyList())
    val agendaItems: StateFlow<List<AgendaDto>> = _agendaItems.asStateFlow()

    private var loadedMeetingId: Long? = null

    private val _checkedStates = MutableStateFlow<List<Boolean>>(emptyList())
    val checkedStates: StateFlow<List<Boolean>> = _checkedStates.asStateFlow()

    val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadAgendas(meetingId: Long) {
        if (loadedMeetingId == meetingId && _agendaItems.value.isNotEmpty()) return

        viewModelScope.launch {
            try {
                val agendas = agendaRepository.getAgendas(meetingId)
                _agendaItems.value = agendas
                val savedStates = loadCheckedStatesFromPrefs(meetingId)
                _checkedStates.value = if (savedStates.size == agendas.size) savedStates else agendas.map { it.isCompleted }
                loadedMeetingId = meetingId
            } catch (e: HttpException) {
                val error = handleHttpException(e)
                _errorMessage.value = if (e.code() == 401) "TOKEN_EXPIRED" else error.message
            } catch (e: Exception) {
                _errorMessage.value = "알 수 없는 오류가 발생했습니다."
            }
        }
    }

    private fun loadCheckedStatesFromPrefs(meetingId: Long): List<Boolean> {
        val saved = context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
            .getString("checked_states_$meetingId", null)
        return saved?.split(",")?.map { it.toBooleanStrictOrNull() ?: false } ?: emptyList()
    }

    fun toggleAgendaChecked(index: Int) {
        val updated = _checkedStates.value.toMutableList().apply {
            this[index] = !this[index]
        }
        _checkedStates.value = updated
        saveCheckedStatesToPrefs()
    }

    private fun saveCheckedStatesToPrefs() {
        val states = _checkedStates.value.joinToString(",") { it.toString() }
        context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE).edit()
            .putString("checked_states_${loadedMeetingId ?: -1}", states)
            .apply()
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // Summary SSE
    private val _summaryList = MutableStateFlow<List<SummaryItem>>(emptyList())
    val summaryList: StateFlow<List<SummaryItem>> = _summaryList.asStateFlow()

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

    // Participation SSE
    private val _participationRate = MutableStateFlow<String?>(null)
    val participationRate: StateFlow<String?> = _participationRate.asStateFlow()

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

    // Feedback SSE
    private val _feedbackList = MutableStateFlow<List<FeedbackItem>>(emptyList())
    val feedbackList: StateFlow<List<FeedbackItem>> = _feedbackList.asStateFlow()

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

    // Test send jobs
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

    fun stopSse() {
        sseClient.disconnect()
    }
}
