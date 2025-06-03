package com.imhungry.jjongseol.viewmodel

import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.data.model.meeting.MeetingReq
import com.imhungry.jjongseol.data.model.meeting.MeetingStatus
import com.imhungry.jjongseol.data.model.meeting.dto.FeedbackDto
import com.imhungry.jjongseol.data.model.meeting.dto.SummaryDto
import com.imhungry.jjongseol.data.model.error.ApiError
import com.imhungry.jjongseol.data.model.home.MeetingResponse
import com.imhungry.jjongseol.data.model.home.toMeetingInfo
import com.imhungry.jjongseol.data.model.meeting.request.MeetingUpdateReq
import com.imhungry.jjongseol.data.model.meeting.response.MeetingDetailRes
import com.imhungry.jjongseol.data.network.api.AgendaApi
import com.imhungry.jjongseol.ui.home.meetingdata.MeetingInfo
import com.imhungry.jjongseol.data.network.api.MeetingApi
import com.imhungry.jjongseol.data.repository.FeedbackRepository
import com.imhungry.jjongseol.data.repository.MeetingRepository
import com.imhungry.jjongseol.data.repository.MeetingResult
import com.imhungry.jjongseol.data.repository.SummaryRepository
import com.imhungry.jjongseol.feature.meeting.MeetingStreamController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MeetingViewModel @Inject constructor(
    private val meetingRepository: MeetingRepository,
    private val meetingApi: MeetingApi,
    private val agendaApi: AgendaApi,
    val streamController: MeetingStreamController,
    private val feedbackRepository: FeedbackRepository,
    private val summaryRepository: SummaryRepository
) : ViewModel() {

    private val _meetingDetail = MutableStateFlow<MeetingDetailRes?>(null)
    val meetingDetail: StateFlow<MeetingDetailRes?> = _meetingDetail

    private val _meetingStatus = MutableStateFlow<MeetingStatus?>(null)
    val meetingStatus: StateFlow<MeetingStatus?> = _meetingStatus

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isStatusUpdating = MutableStateFlow(false)
    val isStatusUpdating: StateFlow<Boolean> = _isStatusUpdating

    private val _feedbackList = MutableStateFlow<List<FeedbackDto>>(emptyList())
    val feedbackList: StateFlow<List<FeedbackDto>> = _feedbackList.asStateFlow()

    private val _summaryList = MutableStateFlow<List<SummaryDto>>(emptyList())
    val summaryList: StateFlow<List<SummaryDto>> = _summaryList.asStateFlow()

    private val _scheduledMeetings = MutableStateFlow<List<MeetingInfo>>(emptyList())
    val scheduledMeetings: StateFlow<List<MeetingInfo>> = _scheduledMeetings.asStateFlow()

    private val _pastMeetings = MutableStateFlow<List<MeetingInfo>>(emptyList())
    val pastMeetings: StateFlow<List<MeetingInfo>> = _pastMeetings.asStateFlow()

    private val _meetings = MutableStateFlow<List<MeetingResponse>>(emptyList())
    val meetings: StateFlow<List<MeetingResponse>> = _meetings.asStateFlow()

    private val _selectedMeeting = MutableStateFlow<MeetingDetailRes?>(null)
    val selectedMeeting: StateFlow<MeetingDetailRes?> = _selectedMeeting.asStateFlow()

    private val _agendas = MutableStateFlow<List<String>>(emptyList())
    val agendas: StateFlow<List<String>> = _agendas.asStateFlow()

    private val _scheduledMonthOffset = MutableStateFlow(0)
    private val _pastMonthOffset = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            feedbackRepository.feedbackFlow.collect { feedback ->
                _feedbackList.value = _feedbackList.value + feedback
            }
        }
        viewModelScope.launch {
            summaryRepository.summaryFlow.collect { summary ->
                _summaryList.value = _summaryList.value + summary
            }
        }
    }

    fun resetMonthOffsets() {
        _scheduledMonthOffset.value = 0
        _pastMonthOffset.value = 0
    }

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

    fun loadMeetingDetail2(meetingId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
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
            _isLoading.value = false
        }
    }

    fun updateMeetingStatus(meetingId: Long, targetStatus: MeetingStatus) {
        viewModelScope.launch {
            _isStatusUpdating.value = true
            when (val result = meetingRepository.updateMeetingStatus(meetingId, targetStatus)) {
                is MeetingResult.Success -> {
                    loadMeetingDetail2(meetingId)
                }
                is MeetingResult.Error -> {
                    _errorMessage.value = result.errorResponse?.message ?: result.message
                }
                is MeetingResult.Exception -> {
                    _errorMessage.value = result.throwable.message ?: "네트워크 오류"
                }
            }
            _isStatusUpdating.value = false
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
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
                        _meetingStatus.value = MeetingStatus.from(meeting.meetingStatus)
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

    fun confirmEdit(
        id: Long,
        title: String,
        location: String,
        date: String,
        startTime: String,
        targetTime: Int,
        restInterval: Int,
        restDuration: Int,
        agendas: List<String>,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val fullStartTime = try {
            LocalDateTime.parse("${date}T${startTime}", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
                .format(formatter)
        } catch (e: Exception) {
            onError(e)
            return
        }

        val req = MeetingUpdateReq(
            title = title,
            location = location,
            scheduledStartTime = fullStartTime,
            targetTime = targetTime,
            restInterval = restInterval,
            restDuration = restDuration,
            agendas = agendas
        )

        Log.d("MeetingUpdateReq", "보내는 데이터: $req")

        updateMeetingInfo(id, req, onSuccess, onError)
    }

    fun updateMeetingInfo(
        id: Long,
        req: MeetingUpdateReq,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val res = meetingApi.updateMeeting(id, req)
                if (res.isSuccessful) {
                    onSuccess()
                } else {
                    onError(Exception("수정 실패: ${res.code()}"))
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}