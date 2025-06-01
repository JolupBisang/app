package com.imhungry.jjongseol.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.data.model.meeting.MeetingReq
import com.imhungry.jjongseol.data.model.meeting.MeetingStatus
import com.imhungry.jjongseol.data.model.meeting.dto.FeedbackDto
import com.imhungry.jjongseol.data.model.meeting.dto.SummaryDto
import com.imhungry.jjongseol.data.model.meeting.response.MeetingDetailRes
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
import javax.inject.Inject

@HiltViewModel
class MeetingViewModel @Inject constructor(
    private val meetingRepository: MeetingRepository,
    private val meetingApi: MeetingApi,
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
                    loadMeetingDetail(meetingId)
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
}
