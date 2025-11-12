package com.imhungry.sillok.presentation.viewmodel.waitingroom

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.imhungry.sillok.data.local.UserStore
import com.imhungry.sillok.data.model.meeting.TargetMeetingStatus
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.usecase.agenda.ChangeAgendaStatusUseCase
import com.imhungry.sillok.domain.usecase.agenda.GetAgendasUseCase
import com.imhungry.sillok.domain.usecase.meeting.GetMeetingDetailUseCase
import com.imhungry.sillok.domain.usecase.meeting.UpdateMeetingStatusUseCase
import com.imhungry.sillok.presentation.state.waitingroom.WaitingRoomEvent
import com.imhungry.sillok.presentation.state.waitingroom.WaitingRoomState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class WaitingRoomViewModel @Inject constructor(
    private val userStore: UserStore,
    private val changeAgendaStatusUseCase: ChangeAgendaStatusUseCase,
    private val updateMeetingStatusUseCase: UpdateMeetingStatusUseCase,
    private val getMeetingDetailUseCase: GetMeetingDetailUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(WaitingRoomState())
    val state: StateFlow<WaitingRoomState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<WaitingRoomEvent>()
    val events = _events.asSharedFlow()

    private var currentUserEmail: String? = null
    private var currentUserId: String? = null
    private var meetingStartedListener: ListenerRegistration? = null

    init {
        viewModelScope.launch {
            userStore.user.collect { user ->
                currentUserEmail = user?.email
                currentUserId = user?.id?.toString()
                // 사용자 정보가 있으면 리스너 등록
                if (!currentUserId.isNullOrBlank()) {
                    attachMeetingStartedListener(currentUserId!!)
                } else {
                    detachMeetingStartedListener()
                }
            }
        }
    }

    fun loadAgendasAndMeetingDetail(meetingId: Long) {
        _state.update { it.copy(isLoading = true, error = null, meetingId = meetingId) }
        viewModelScope.launch {
            when (val detailResult = getMeetingDetailUseCase(meetingId)) {
                is ApiResult.Success -> {
                    val minutes = detailResult.data.targetTime
                    val display = formatDurationForDisplay(minutes)
                    val isHost = detailResult.data.isHost
                    val agendas = detailResult.data.agendas
                    _state.update {
                        it.copy(
                            isLoading = false,
                            agendas = agendas,
                            targetTimeDisplay = display,
                            isHost = isHost,
                            error = null
                        )
                    }
                }

                is ApiResult.Failure -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isHost = false, // 실패 시 기본값으로 설정
                            error = detailResult.message
                        )
                    }
                }
            }
        }
    }

    // 분 단위 시간을 "HH:MM:SS" 형식으로 변환
    private fun formatDurationForDisplay(totalMinutes: Int): String {
        if (totalMinutes <= 0) {
            return "00:00:00"
        }
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val seconds = 0
        fun two(n: Int) = n.toString().padStart(2, '0')
        val result = "${two(hours)}:${two(minutes)}:${two(seconds)}"
        return result
    }

    // 아젠다 체크 상태를 낙관적 업데이트하고 서버 요청 실패 시 롤백.
    fun changeAgendaStatus(meetingId: Long, agendaId: Long, isCompleted: Boolean) {
        val previous = state.value.agendas
        val updated = previous.map { agenda ->
            if (agenda.agendaId == agendaId) agenda.copy(isCompleted = isCompleted) else agenda
        }

        viewModelScope.launch {
            when (val result = changeAgendaStatusUseCase(meetingId, agendaId, isCompleted)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(agendas = updated) }
                }

                is ApiResult.Failure -> {
                    _state.update { it.copy(agendas = previous, error = result.message) }
                }
            }
        }
    }

    // 회의를 시작: 상태 변경 → 상세 조회 → Firestore 사용자들에게 시작 알림 플래그 업데이트 → 이벤트 발행
    fun startMeeting() {
        val meetingId = state.value.meetingId
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val statusRes =
                updateMeetingStatusUseCase(meetingId, TargetMeetingStatus.IN_PROGRESSING)) {
                is ApiResult.Failure -> {
                    _state.update { it.copy(isLoading = false, error = statusRes.message) }
                    _events.emit(WaitingRoomEvent.StartFailed(statusRes.message))
                    return@launch
                }

                is ApiResult.Success -> {}
            }

            when (val detailRes = getMeetingDetailUseCase(meetingId)) {
                is ApiResult.Failure -> {
                    _state.update { it.copy(isLoading = false, error = detailRes.message) }
                    _events.emit(WaitingRoomEvent.StartFailed(detailRes.message))
                    return@launch
                }

                is ApiResult.Success -> {
                    try {
                        val db = FirebaseFirestore.getInstance()
                        val me = currentUserEmail
                        val emails = detailRes.data.participants.map { it.email }
                            .filter { email ->
                                email.isNotBlank() && (me.isNullOrBlank() || !email.equals(
                                    me,
                                    ignoreCase = true
                                ))
                            }
                            .toSet()
                        for (email in emails) {
                            val snapshot = db.collection("users")
                                .whereEqualTo("email", email)
                                .limit(1)
                                .get()
                                .await()
                            if (!snapshot.isEmpty) {
                                val docRef = snapshot.documents.first().reference
                                docRef.update(
                                    mapOf(
                                        "meetingStarted" to true,
                                        "startedMeetingId" to meetingId
                                    )
                                ).await()
                            }
                        }
                        _state.update { it.copy(isLoading = false, error = null) }
                        _events.emit(WaitingRoomEvent.MeetingStarted)
                    } catch (e: Exception) {
                        _state.update { it.copy(isLoading = false, error = e.message) }
                        _events.emit(WaitingRoomEvent.StartFailed(e.message))
                    }
                }
            }
        }
    }

    // ========================================
    // Firestore 리스너 관리
    // ========================================

    private fun attachMeetingStartedListener(uid: String) {
        meetingStartedListener?.remove()
        val db = FirebaseFirestore.getInstance()
        meetingStartedListener = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, _ ->
                val meetingStarted = snapshot?.getBoolean("meetingStarted") ?: false
                if (meetingStarted) {
                    handleMeetingStartedDetected(db, uid, snapshot)
                }
            }
    }

    private fun handleMeetingStartedDetected(
        db: FirebaseFirestore,
        uid: String,
        snapshot: DocumentSnapshot
    ) {
        val startedMeetingId = snapshot.getLong("startedMeetingId") ?: 0L
        val currentMeetingId = state.value.meetingId

        // 현재 대기실의 meetingId와 시작된 meetingId가 같으면 회의 중 화면으로 이동
        if (startedMeetingId > 0L && currentMeetingId > 0L && startedMeetingId == currentMeetingId) {
            viewModelScope.launch {
                // meetingStarted 플래그 리셋
                resetMeetingStartedFlag(db, uid)
                // 회의 중 화면으로 이동 이벤트 발행
                _events.emit(WaitingRoomEvent.MeetingStarted)
            }
        }
    }

    private fun resetMeetingStartedFlag(db: FirebaseFirestore, uid: String) {
        db.collection("users").document(uid)
            .update(mapOf("meetingStarted" to false, "updatedAt" to System.currentTimeMillis()))
    }

    private fun detachMeetingStartedListener() {
        meetingStartedListener?.remove()
        meetingStartedListener = null
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun showNotHostDialog() {
        _state.update { it.copy(showNotHostDialog = true) }
    }

    fun dismissNotHostDialog() {
        _state.update { it.copy(showNotHostDialog = false) }
    }

    public override fun onCleared() {
        super.onCleared()
        detachMeetingStartedListener()
    }

    // 디버깅/시연을 위한 더미 데이터 주입
    fun loadDummyWaitingRoomState(meetingId: Long = 9999L) {
        val dummyAgendas = listOf(
            com.imhungry.sillok.domain.model.agenda.Agenda(
                agendaId = 1L,
                content = "킥오프 및 일정 확인",
                isCompleted = false
            ),
            com.imhungry.sillok.domain.model.agenda.Agenda(
                agendaId = 2L,
                content = "요구사항 정리",
                isCompleted = false
            ),
            com.imhungry.sillok.domain.model.agenda.Agenda(
                agendaId = 3L,
                content = "다음 액션 아이템 도출",
                isCompleted = false
            )
        )

        _state.update {
            it.copy(
                meetingId = meetingId,
                agendas = dummyAgendas,
                targetTimeDisplay = "- 01:00:00",
                isLoading = false,
                error = null
            )
        }
    }
}