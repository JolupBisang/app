package com.imhungry.jjongseol.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.feature.audio.StreamingController
import com.imhungry.jjongseol.feature.audio.devtool.TestDataSender
import com.imhungry.jjongseol.data.model.meeting.MeetingReq
import com.imhungry.jjongseol.data.model.error.ApiError
import com.imhungry.jjongseol.data.model.feedback.FeedbackItem
import com.imhungry.jjongseol.data.model.home.MeetingResponse
import com.imhungry.jjongseol.data.model.home.toMeetingInfo
import com.imhungry.jjongseol.data.model.meeting.MeetingDetailRes
import com.imhungry.jjongseol.ui.home.meetingdata.MeetingInfo
import com.imhungry.jjongseol.data.model.meeting.SummaryItem
import com.imhungry.jjongseol.data.network.api.AgendaApi
import com.imhungry.jjongseol.data.network.api.MeetingApi
import com.imhungry.jjongseol.data.network.client.SseClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MeetingViewModel @Inject constructor(
    application: Application,
    private val sseClient: SseClient,
    private val streamingController: StreamingController,
    private val testDataSender: TestDataSender,
    private val meetingApi: MeetingApi,
    private val agendaApi: AgendaApi,
) : AndroidViewModel(application) {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _scheduledMeetings = MutableStateFlow<List<MeetingInfo>>(emptyList())
    val scheduledMeetings: StateFlow<List<MeetingInfo>> = _scheduledMeetings.asStateFlow()

    private val _pastMeetings = MutableStateFlow<List<MeetingInfo>>(emptyList())
    val pastMeetings: StateFlow<List<MeetingInfo>> = _pastMeetings.asStateFlow()

    private val _summaryList = MutableStateFlow<List<SummaryItem>>(emptyList())
    val summaryList: StateFlow<List<SummaryItem>> = _summaryList.asStateFlow()

    private val _participationRate = MutableStateFlow<String?>(null)
    val participationRate: StateFlow<String?> = _participationRate.asStateFlow()

    private val _feedbackList = MutableStateFlow<List<FeedbackItem>>(emptyList())
    val feedbackList: StateFlow<List<FeedbackItem>> = _feedbackList.asStateFlow()

    private val _meetings = MutableStateFlow<List<MeetingResponse>>(emptyList())
    val meetings: StateFlow<List<MeetingResponse>> = _meetings.asStateFlow()

    private val _selectedMeeting = MutableStateFlow<MeetingDetailRes?>(null)
    val selectedMeeting: StateFlow<MeetingDetailRes?> = _selectedMeeting.asStateFlow()

    private val _agendas = MutableStateFlow<List<String>>(emptyList())
    val agendas: StateFlow<List<String>> = _agendas.asStateFlow()

    private val _scheduledMonthOffset = MutableStateFlow(0)
    private val _pastMonthOffset = MutableStateFlow(0)

    fun resetMonthOffsets() {
        _scheduledMonthOffset.value = 0
        _pastMonthOffset.value = 0
    }

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

    fun subscribeToSummary(meetingId: Long, timeProvider: () -> String) {
        sseClient.subscribeToEvent(
            endpoint = "summary",
            meetingId = meetingId,
            eventType = "SUMMARY",
            onEventReceived = {
                _summaryList.value += SummaryItem(it.trim('"'), timeProvider())
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

    fun markAllFeedbackAsRead() {
        _feedbackList.value = _feedbackList.value.map {
            if (!it.isRead) it.copy(isRead = true) else it
        }
    }

    fun loadMeetings() {
        viewModelScope.launch {
            try {
                val now = LocalDate.now()
                val response = meetingApi.getMeetings(now.year, now.monthValue)

                if (response.isSuccessful) {
                    val meetings = response.body()?.data?.meetings ?: emptyList<MeetingResponse>()
                    _meetings.value = meetings

                    Log.d("loadMeetings", "응답 회의 수: ${meetings.size}")
                    val meetingInfos = meetings.map { it.toMeetingInfo() }
                    val (upcoming, past) = splitAndSortMeetings(meetingInfos)

                    Log.d("loadMeetings", "예정: ${upcoming.size}, 지난: ${past.size}")
                    _scheduledMeetings.value = upcoming
                    _pastMeetings.value = past

                } else {
                    _errorMessage.value = "불러오기 실패: ${response.code()}"
                    Log.e("loadMeetings", "API 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "예외 발생: ${e.message}"
                Log.e("loadMeetings", "예외 발생: ${e.message}")
            }
        }
    }

    fun loadMorePastMeetings() {
        viewModelScope.launch {
            try {
                val now = LocalDate.now()
                val targetDate = now.minusMonths((_pastMonthOffset.value + 1).toLong())
                val response = meetingApi.getMeetings(targetDate.year, targetDate.monthValue)

                if (response.isSuccessful) {
                    val meetings = response.body()?.data?.meetings ?: emptyList()
                    val meetingInfos = meetings.map { it.toMeetingInfo() }

                    val (_, past) = splitAndSortMeetings(meetingInfos)
                    _pastMeetings.value = _pastMeetings.value + past
                    _pastMonthOffset.value += 1

                    Log.d("loadMorePastMeetings", "${targetDate.month}월 데이터 ${past.size}개 추가됨")
                } else {
                    _errorMessage.value = "불러오기 실패: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "예외 발생: ${e.message}"
            }
        }
    }


    fun loadMoreScheduledMeetings() {
        viewModelScope.launch {
            try {
                val now = LocalDate.now()
                val targetDate = now.plusMonths((_scheduledMonthOffset.value + 1).toLong())
                val response = meetingApi.getMeetings(targetDate.year, targetDate.monthValue)

                if (response.isSuccessful) {
                    val meetings = response.body()?.data?.meetings ?: emptyList()
                    val meetingInfos = meetings.map { it.toMeetingInfo() }
                    val (upcoming, _) = splitAndSortMeetings(meetingInfos)

                    _scheduledMeetings.value = _scheduledMeetings.value + upcoming
                    _scheduledMonthOffset.value += 1

                    Log.d("loadMoreScheduled", "${targetDate.month}월 예정 회의 ${upcoming.size}개 추가")
                } else {
                    _errorMessage.value = "불러오기 실패: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "예외 발생: ${e.message}"
            }
        }
    }


    private fun splitAndSortMeetings(meetings: List<MeetingInfo>): Pair<List<MeetingInfo>, List<MeetingInfo>> {
        val now = LocalDateTime.now()

        val (upcoming, past) = meetings.partition {
            it.startDateTime.toLocalDate() >= now.toLocalDate()
        }

        val sortedUpcoming = upcoming.sortedWith(
            compareBy<MeetingInfo> { it.startDateTime.toLocalDate() }
                .thenBy { it.startDateTime.toLocalTime() }
                .thenBy { it.endDateTime.toLocalTime() }
        )

        val sortedPast = past.sortedWith(
            compareByDescending<MeetingInfo> { it.startDateTime.toLocalDate() }
                .thenByDescending { it.endDateTime.toLocalTime() }
        )

        return sortedUpcoming to sortedPast
    }

    fun loadMeetingDetail(meetingId: Long) {
        viewModelScope.launch {
            try {
                val response = meetingApi.getMeetingById(meetingId)
                if (response.isSuccessful) {
                    val meeting = response.body()?.data
                    if (meeting != null) {
                        _selectedMeeting.value = meeting
                        Log.d("MEETING_DETAIL", "Loaded: $meeting")
                    } else {
                        Log.e("MEETING_DETAIL", "No meeting data in response")
                    }
                } else {
                    Log.e("MEETING_DETAIL", "API error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("MEETING_DETAIL", "Exception: ${e.message}", e)
            }
        }
    }

    fun loadAgendas(meetingId: Long) {
        viewModelScope.launch {
            try {
                val response = agendaApi.getAgendas(meetingId)
                if (response.isSuccessful) {
                    val agendas = response.body()?.data?.agendaDetails?.map { it.content } ?: emptyList()
                    _agendas.value = agendas
                    Log.d("AGENDA_API", "아젠다 ${agendas.size}개 로드 완료")
                } else {
                    Log.e("AGENDA_API", "아젠다 불러오기 실패 - HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("AGENDA_API", "아젠다 로드 중 예외 발생: ${e.localizedMessage}", e)
            }
        }
    }

}