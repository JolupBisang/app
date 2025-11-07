package com.imhungry.sillok.presentation.viewmodel.meetingdetail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.presentation.state.meetingdetail.MeetingDetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.imhungry.sillok.data.local.DismissedMeetingStore
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.usecase.meeting.GetMeetingDetailUseCase
import com.imhungry.sillok.domain.usecase.agenda.GetAgendasUseCase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@HiltViewModel
class MeetingDetailViewModel @Inject constructor(
    private val getMeetingDetailUseCase: GetMeetingDetailUseCase,
    private val getAgendasUseCase: GetAgendasUseCase,
    private val dismissedMeetingStore: DismissedMeetingStore
) : ViewModel() {
    
    private val _state = MutableStateFlow(MeetingDetailState())
    val state: StateFlow<MeetingDetailState> = _state.asStateFlow()
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadMeetingDetail(meetingId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val res = getMeetingDetailUseCase(meetingId)) {
                is ApiResult.Success -> {
                    val meeting = res.data
                    try {
                        val ldt = LocalDateTime.parse(meeting.scheduledStartTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        val dateDigits = ldt.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                        val startDigits = ldt.format(DateTimeFormatter.ofPattern("HHmm"))
                        val endDigits = ldt.plusMinutes(meeting.targetTime.toLong()).format(DateTimeFormatter.ofPattern("HHmm"))

                        // agendas
                        val agendas = when (val ag = getAgendasUseCase(meetingId)) {
                            is ApiResult.Success -> ag.data.map { it.content }
                            is ApiResult.Failure -> emptyList()
                        }

                        // hostEmail from Firebase
                        var hostEmail = ""
                        try {
                            val doc = FirebaseFirestore.getInstance()
                                .collection("meetings")
                                .document(meetingId.toString())
                                .get()
                                .await()
                            hostEmail = doc.getString("hostEmail") ?: ""
                        } catch (_: Exception) { }

                        // participant emails
                        val participants = meeting.participants.map { it.email }.toMutableList()
                        if (hostEmail.isNotBlank() && participants.none { it.equals(hostEmail, ignoreCase = true) }) {
                            participants.add(0, hostEmail)
                        }

                        _state.value = _state.value.copy(
                            meetingId = meetingId,
                            title = meeting.title,
                            hostEmail = hostEmail,
                            participantEmails = participants,
                            date = dateDigits,
                            startTime = startDigits,
                            endTime = endDigits,
                            targetTime = meeting.targetTime.toString(),
                            location = meeting.location,
                            agendas = agendas,
                            breakInterval = meeting.restInterval.toString(),
                            breakDuration = meeting.restDuration.toString(),
                            isHost = meeting.isHost,
                            isLoading = false,
                            error = null
                        )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(isLoading = false, error = e.message)
                    }
                }
                is ApiResult.Failure -> {
                    _state.value = _state.value.copy(isLoading = false, error = res.message)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadDummyMeetingDetail() {
        viewModelScope.launch {
            val dummyMeetingId = 1234L
            val scheduledStart = "2025-10-28T09:30:00"
            val targetTimeMinutes = 60

            val ldt = LocalDateTime.parse(scheduledStart, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val dateDigits = ldt.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            val startDigits = ldt.format(DateTimeFormatter.ofPattern("HHmm"))
            val endDigits = ldt.plusMinutes(targetTimeMinutes.toLong()).format(DateTimeFormatter.ofPattern("HHmm"))

            val agendas = listOf(
                "소개 및 아젠다 확인",
                "기능 리뷰",
                "다음 액션 정리"
            )
            val hostEmail = "host@example.com"
            val participants = listOf(
                hostEmail,
                "alice@example.com",
                "bob@example.com"
            )

            _state.value = _state.value.copy(
                meetingId = dummyMeetingId,
                title = "더미 회의 제목",
                hostEmail = hostEmail,
                participantEmails = participants,
                date = dateDigits,
                startTime = startDigits,
                endTime = endDigits,
                targetTime = targetTimeMinutes.toString(),
                location = "서울시 강남구 위워크",
                agendas = agendas,
                breakInterval = "50",
                breakDuration = "10",
                isHost = true,
                isLoading = false,
                error = null
            )
        }
    }

    fun showDismissDialog() {
        _state.update { it.copy(showDismissDialog = true) }
    }

    fun dismissDismissDialog() {
        _state.update { it.copy(showDismissDialog = false) }
    }

    fun dismissMeeting() {
        val meetingId = state.value.meetingId
        viewModelScope.launch {
            dismissedMeetingStore.addDismissedMeeting(meetingId)
            _state.update { it.copy(showDismissDialog = false) }
        }
    }
}