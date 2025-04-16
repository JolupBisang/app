package com.imhungry.jjongseol.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.data.model.agenda.AgendaDto
import com.imhungry.jjongseol.data.model.meeting.SummaryItem
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
    private val participationRateApi: ParticipationRateApi
) : AndroidViewModel(application) {
    private val context by lazy { application.applicationContext }

    fun pauseEncoding() = AudioStreamingService.pauseEncoding()
    fun resumeEncoding() = AudioStreamingService.resumeEncoding()

    fun startStreamingService() {
        val prefs = context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
        val alreadyStarted = prefs.contains("meetingStartedAt")
        if (!alreadyStarted) {
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
        val prefs = context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("isMeetingOngoing", false)
            .remove("meetingStartedAt")
            .apply()

        context.stopService(Intent(context, AudioStreamingService::class.java))
    }

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
                _checkedStates.value = if (savedStates.size == agendas.size) {
                    savedStates
                } else {
                    agendas.map { it.isCompleted }
                }

                loadedMeetingId = meetingId
            } catch (e: HttpException) {
                val error = handleHttpException(e)
                if (e.code() == 401) {
                    _errorMessage.value = "TOKEN_EXPIRED"
                } else {
                    _errorMessage.value = error.message
                }
            } catch (e: Exception) {
                _errorMessage.value = "알 수 없는 오류가 발생했습니다."
            }
        }
    }

    private fun loadCheckedStatesFromPrefs(meetingId: Long): List<Boolean> {
        val prefs = context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
        val saved = prefs.getString("checked_states_$meetingId", null)
        return saved?.split(",")?.map { it.toBooleanStrictOrNull() ?: false } ?: emptyList()
    }

    fun toggleAgendaChecked(index: Int) {
        val updated = _checkedStates.value.toMutableList()
        updated[index] = !updated[index]
        _checkedStates.value = updated

        saveCheckedStatesToPrefs()
    }

    private fun saveCheckedStatesToPrefs() {
        val prefs = context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val states = _checkedStates.value.joinToString(",") { it.toString() }
        editor.putString("checked_states_${loadedMeetingId ?: -1}", states)
        editor.apply()
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    private val _summaryList = MutableStateFlow<List<SummaryItem>>(emptyList())
    val summaryList: StateFlow<List<SummaryItem>> = _summaryList.asStateFlow()

    fun subscribeToSummary(meetingId: Long, timeProvider: () -> String) {
        sseClient.subscribeToSummary(
            meetingId = meetingId,
            onEventReceived = { rawSummary ->
                val cleaned = rawSummary.trim('"')
                val currentTime = timeProvider()
                val newItem = SummaryItem(cleaned, currentTime)
                _summaryList.value = _summaryList.value + newItem
            },
            onError = { error ->
                _errorMessage.value = "요약 수신 실패: $error"
            }
        )
    }

    private val _participationRate = MutableStateFlow<String?>(null)
    val participationRate: StateFlow<String?> = _participationRate.asStateFlow()

    fun subscribeToParticipationRate(meetingId: Long) {
        sseClient.subscribeToParticipationRate(
            meetingId = meetingId,
            onEventReceived = { rate ->
                _participationRate.value = rate
            },
            onError = { error ->
                _errorMessage.value = "점유율 수신 실패: $error"
            }
        )
    }

    private var summaryTestJob: Job? = null

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

    private var participationTestJob: Job? = null

    fun startSendingTestParticipationRate(meetingId: Long) {
        if (participationTestJob?.isActive == true) return

        participationTestJob = viewModelScope.launch {
            while (isActive) {
                try {
                    participationRateApi.sendParticipationRate(meetingId)
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

    fun stopSse() {
        sseClient.disconnect()
    }
}
