package com.imhungry.sillok.presentation.viewmodel.meetingform

import android.os.Build
import android.util.Patterns
import androidx.annotation.RequiresApi
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.imhungry.sillok.data.local.UserStore
import com.imhungry.sillok.data.model.meeting.MeetingUpdateReqDto
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.agenda.Agenda
import com.imhungry.sillok.domain.model.meeting.CreateMeetingRequest
import com.imhungry.sillok.domain.model.meeting.Meeting
import com.imhungry.sillok.domain.usecase.agenda.AddAgendaUseCase
import com.imhungry.sillok.domain.usecase.agenda.DeleteAgendaUseCase
import com.imhungry.sillok.domain.usecase.agenda.GetAgendasUseCase
import com.imhungry.sillok.domain.usecase.agenda.UpdateAgendaUseCase
import com.imhungry.sillok.domain.usecase.meeting.CreateMeetingUseCase
import com.imhungry.sillok.domain.usecase.meeting.GetMeetingDetailUseCase
import com.imhungry.sillok.domain.usecase.meeting.UpdateMeetingUseCase
import com.imhungry.sillok.domain.usecase.meetinguser.AddMeetingUserUseCase
import com.imhungry.sillok.domain.usecase.meetinguser.RemoveMeetingUserUseCase
import com.imhungry.sillok.domain.usecase.places.SearchPlacesUseCase
import com.imhungry.sillok.presentation.state.meetingform.MeetingData
import com.imhungry.sillok.presentation.state.meetingform.MeetingFormEvent
import com.imhungry.sillok.presentation.state.meetingform.MeetingFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class MeetingFormViewModel @Inject constructor(
    private val searchPlacesUseCase: SearchPlacesUseCase,
    private val userStore: UserStore,
    private val createMeetingUseCase: CreateMeetingUseCase,
    private val addMeetingUserUseCase: AddMeetingUserUseCase,
    private val removeMeetingUserUseCase: RemoveMeetingUserUseCase,
    private val getMeetingDetailUseCase: GetMeetingDetailUseCase,
    private val updateMeetingUseCase: UpdateMeetingUseCase,
    private val getAgendasUseCase: GetAgendasUseCase,
    private val addAgendaUseCase: AddAgendaUseCase,
    private val deleteAgendaUseCase: DeleteAgendaUseCase,
    private val updateAgendaUseCase: UpdateAgendaUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MeetingFormState())
    val state = _state.asStateFlow()
    private var currentUserEmail: String? = null

    private val _events = MutableSharedFlow<MeetingFormEvent>()
    val events = _events.asSharedFlow()

    private var loadedParticipants: List<Meeting.Participant> = emptyList()
    private var loadedAgendas: List<Agenda> = emptyList()
    private var initialSnapshot: FormSnapshot? = null

    fun setEditMode(meetingId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isEditMode = true, meetingId = meetingId) }
            when (val detailResult = getMeetingDetailUseCase(meetingId)) {
                is ApiResult.Success -> {
                    val meeting = detailResult.data
                    try {
                        val iso = meeting.scheduledStartTime
                        val ldt = LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        val dateDigits = ldt.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                        val startDigits = ldt.format(DateTimeFormatter.ofPattern("HHmm"))
                        val endDigits = ldt.plusMinutes(meeting.targetTime.toLong())
                            .format(DateTimeFormatter.ofPattern("HHmm"))

                        // 아젠다 불러오기
                        val agendas =
                            when (val agendasResult = getAgendasUseCase(meeting.meetingId)) {
                                is ApiResult.Success -> {
                                    loadedAgendas = agendasResult.data
                                    agendasResult.data.map { it.content }.ifEmpty { listOf("") }
                                }

                                is ApiResult.Failure -> listOf("")
                            }

                        val filteredParticipants = meeting.participants
                            .map { it.email }
                            .filter { email ->
                                val me = currentUserEmail
                                if (me.isNullOrBlank()) true else !email.equals(
                                    me,
                                    ignoreCase = true
                                )
                            }

                        // 참석자 저장 (호스트 포함)
                        loadedParticipants = meeting.participants

                        val meetingData = MeetingData(
                            title = meeting.title,
                            date = dateDigits,
                            startTime = startDigits,
                            endTime = endDigits,
                            location = meeting.location,
                            agendas = agendas,
                            breakInterval = meeting.restInterval.toString(),
                            breakDuration = meeting.restDuration.toString(),
                            participants = filteredParticipants
                        )

                        _state.update {
                            it.copy(
                                isEditMode = true,
                                meetingId = meetingId,
                                title = meetingData.title,
                                titleTextFieldValue = TextFieldValue(
                                    meetingData.title,
                                    TextRange(meetingData.title.length)
                                ),
                                date = meetingData.date,
                                startTime = meetingData.startTime,
                                endTime = meetingData.endTime,
                                duration = meeting.targetTime.toString(),
                                location = meetingData.location,
                                locationTextFieldValue = TextFieldValue(meetingData.location),
                                agendas = meetingData.agendas.ifEmpty {
                                    listOf(
                                        ""
                                    )
                                },
                                breakInterval = meetingData.breakInterval,
                                breakDuration = meetingData.breakDuration,
                                participantEmails = meetingData.participants,
                                isLoading = false,
                                error = null
                            )
                        }

                        initialSnapshot = FormSnapshot(
                            title = meetingData.title,
                            date = meetingData.date,
                            startTime = meetingData.startTime,
                            endTime = endDigits,
                            duration = meeting.targetTime.toString(),
                            location = meetingData.location,
                            agendas = (meetingData.agendas.ifEmpty {
                                listOf(
                                    ""
                                )
                            }),
                            participantEmails = meetingData.participants
                        )
                    } catch (e: Exception) {
                        _state.update { it.copy(isLoading = false, error = e.message) }
                    }
                }

                is ApiResult.Failure -> {
                    _state.update { it.copy(isLoading = false, error = detailResult.message) }
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            userStore.user.collect { user ->
                currentUserEmail = user?.email
                _state.update { current ->
                    current.copy(userName = user?.nickname ?: current.userName)
                }
            }
        }

        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is MeetingFormEvent.TitleChanged -> {
                        _state.update { it.copy(title = event.title) }
                    }

                    is MeetingFormEvent.TitleTextFieldValueChanged -> {
                        _state.update {
                            it.copy(
                                title = event.textFieldValue.text,
                                titleTextFieldValue = event.textFieldValue
                            )
                        }
                    }

                    is MeetingFormEvent.ParticipantsChanged -> {
                        _state.update { it.copy(participants = event.participants) }
                        searchEmails(event.participants)
                    }

                    is MeetingFormEvent.ParticipantEmailSelected -> {
                        val currentEmails = _state.value.participantEmails
                        if (event.email.isNotEmpty() && !currentEmails.contains(event.email) && isValidEmail(
                                event.email
                            )
                        ) {
                            _state.update {
                                it.copy(
                                    participantEmails = currentEmails + event.email,
                                    participants = "",
                                    showEmailSuggestions = false
                                )
                            }
                        }
                    }

                    is MeetingFormEvent.ParticipantEmailRemoved -> {
                        val currentEmails = _state.value.participantEmails
                        _state.update {
                            it.copy(
                                participantEmails = currentEmails.filterIndexed { i, _ -> i != event.index }
                            )
                        }
                    }

                    is MeetingFormEvent.DateChanged -> {
                        _state.update { it.copy(date = event.date) }
                    }

                    is MeetingFormEvent.StartTimeChanged -> {
                        _state.update { it.copy(startTime = event.startTime) }
                    }

                    is MeetingFormEvent.EndTimeChanged -> {
                        _state.update { it.copy(endTime = event.endTime) }
                    }

                    is MeetingFormEvent.DurationChanged -> {
                        _state.update { it.copy(duration = event.duration) }
                    }

                    is MeetingFormEvent.LocationChanged -> {
                        _state.update { it.copy(location = event.location) }
                        searchPlaces(event.location)
                    }

                    is MeetingFormEvent.LocationTextFieldValueChanged -> {
                        _state.update {
                            it.copy(
                                location = event.textFieldValue.text,
                                locationTextFieldValue = event.textFieldValue
                            )
                        }
                        searchPlaces(event.textFieldValue.text)
                    }

                    is MeetingFormEvent.LocationSelected -> {
                        val textFieldValue = TextFieldValue(
                            text = event.location,
                            selection = TextRange(event.location.length)
                        )
                        _state.update {
                            it.copy(
                                location = event.location,
                                locationTextFieldValue = textFieldValue,
                                showLocationSuggestions = false
                            )
                        }
                    }

                    is MeetingFormEvent.AgendaChanged -> {
                        val currentAgendas = _state.value.agendas.toMutableList()
                        if (event.index < currentAgendas.size) {
                            currentAgendas[event.index] = event.agenda
                            _state.update { it.copy(agendas = currentAgendas) }
                        }
                    }

                    is MeetingFormEvent.AgendaAdded -> {
                        val currentAgendas = _state.value.agendas.toMutableList()
                        currentAgendas.add("")
                        _state.update { it.copy(agendas = currentAgendas) }
                    }

                    is MeetingFormEvent.AgendaRemoved -> {
                        val currentAgendas = _state.value.agendas.toMutableList()
                        if (event.index < currentAgendas.size) {
                            currentAgendas.removeAt(event.index)
                            _state.update { it.copy(agendas = currentAgendas) }
                        }
                    }

                    is MeetingFormEvent.BreakIntervalChanged -> {
                        _state.update { it.copy(breakInterval = event.breakInterval) }
                    }

                    is MeetingFormEvent.BreakDurationChanged -> {
                        _state.update { it.copy(breakDuration = event.breakDuration) }
                    }

                    is MeetingFormEvent.CreateMeeting -> {
                        createMeeting()
                    }

                    is MeetingFormEvent.CancelClicked -> {
                        // 변경 사항이 없으면 바로 뒤로가기, 있으면 확인 다이얼로그 노출
                        if (!isDirty()) {
                            viewModelScope.launch { _events.emit(MeetingFormEvent.BackClicked) }
                        } else {
                            _state.update { it.copy(showCancelDialog = true) }
                        }
                    }

                    is MeetingFormEvent.CancelConfirmed -> {
                        _state.update { it.copy(showCancelDialog = false) }
                        viewModelScope.launch {
                            _events.emit(MeetingFormEvent.BackClicked)
                        }
                    }

                    is MeetingFormEvent.CancelDismissed -> {
                        _state.update { it.copy(showCancelDialog = false) }
                    }

                    is MeetingFormEvent.ClearFocus -> {
                        _state.update {
                            it.copy(
                                showEmailSuggestions = false,
                                showLocationSuggestions = false,
                                showTimePicker = false
                            )
                        }
                    }

                    is MeetingFormEvent.TimePickerShown -> {
                        _state.update { it.copy(showTimePicker = true) }
                    }

                    // 검증 관련 이벤트 처리
                    is MeetingFormEvent.ValidateForm -> {
                        validateForm()
                    }

                    is MeetingFormEvent.ClearValidationErrors -> {
                        _state.update {
                            it.copy(
                                validationErrors = emptyMap(),
                                showValidationErrors = false
                            )
                        }
                    }

                    is MeetingFormEvent.ShowValidationError -> {
                        val currentErrors = _state.value.validationErrors.toMutableMap()
                        currentErrors[event.field] = event.message
                        _state.update {
                            it.copy(
                                validationErrors = currentErrors,
                                showValidationErrors = true
                            )
                        }
                    }

                    is MeetingFormEvent.FormValidationSuccess -> {
                        if (_state.value.isEditMode) {
                            updateMeeting()
                        } else {
                            createMeeting()
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private fun searchEmails(query: String) {
        viewModelScope.launch {
            if (query.length >= 2) {
                _state.update { it.copy(isSearching = true) }
                try {
                    // Firestore에서 이메일 검색 로직
                    val db = FirebaseFirestore.getInstance()
                    val firestoreQuery = db.collection("users")
                        .whereGreaterThanOrEqualTo("email", query)
                        .whereLessThanOrEqualTo("email", query + '\uf8ff')
                        .limit(10)
                        .get()
                        .await()

                    val suggestions = firestoreQuery.documents
                        .mapNotNull { doc -> doc.getString("email") }
                        .filter {
                            it.isNotBlank() && !it.equals(
                                currentUserEmail,
                                ignoreCase = true
                            )
                        }

                    _state.update {
                        it.copy(
                            emailSuggestions = suggestions,
                            showEmailSuggestions = suggestions.isNotEmpty(),
                            isSearching = false
                        )
                    }
                } catch (e: Exception) {
                    _state.update {
                        it.copy(
                            emailSuggestions = emptyList(),
                            showEmailSuggestions = false,
                            isSearching = false
                        )
                    }
                }
            } else {
                _state.update {
                    it.copy(
                        emailSuggestions = emptyList(),
                        showEmailSuggestions = false
                    )
                }
            }
        }
    }

    private fun searchPlaces(query: String) {
        viewModelScope.launch {
            if (query.length >= 2) {
                _state.update { it.copy(isLocationSearching = true) }
                try {
                    val suggestions = searchPlacesUseCase(query)
                    _state.update {
                        it.copy(
                            locationSuggestions = suggestions,
                            showLocationSuggestions = suggestions.isNotEmpty(),
                            isLocationSearching = false
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _state.update {
                        it.copy(
                            locationSuggestions = emptyList(),
                            showLocationSuggestions = false,
                            isLocationSearching = false
                        )
                    }
                }
            } else {
                _state.update {
                    it.copy(
                        locationSuggestions = emptyList(),
                        showLocationSuggestions = false
                    )
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createMeeting() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val s = _state.value
                val dateDigits = s.date.filter { it.isDigit() }.padStart(8, '0')
                val timeDigits = s.startTime.filter { it.isDigit() }.padStart(4, '0')
                val datePart = "${dateDigits.substring(0, 4)}-${
                    dateDigits.substring(
                        4,
                        6
                    )
                }-${dateDigits.substring(6, 8)}"
                val timePart = "${timeDigits.substring(0, 2)}:${timeDigits.substring(2, 4)}:00"
                val scheduledStartTime = "${datePart}T${timePart}"
                val targetTime = s.duration.toIntOrNull() ?: 0
                val restInterval = s.breakInterval.toIntOrNull() ?: 0
                val restDuration = s.breakDuration.toIntOrNull() ?: 0
                val request = CreateMeetingRequest(
                    title = s.title,
                    location = s.location,
                    scheduledStartTime = scheduledStartTime,
                    targetTime = targetTime,
                    restInterval = restInterval,
                    restDuration = restDuration,
                    participants = emptyList(), // 생성 시 호스트만 등록, 참석자는 별도로 추가
                    agendas = s.agendas.filter { it.isNotBlank() }
                )

                when (val result = createMeetingUseCase(request)) {
                    is ApiResult.Success -> {
                        val meetingId = result.data
                        // 참석자 추가
                        val emails =
                            s.participantEmails.map { it.trim() }.filter { it.isNotEmpty() }
                        var addOk = true
                        if (emails.isNotEmpty()) {
                            when (val addRes = addMeetingUserUseCase(meetingId, emails)) {
                                is ApiResult.Success -> addOk = true
                                is ApiResult.Failure -> {
                                    addOk = false
                                    _state.update {
                                        it.copy(
                                            isLoading = false,
                                            error = addRes.message
                                        )
                                    }
                                }
                            }
                        }

                        if (addOk) {
                            // 파이어베이스에 회의 정보 저장 및 사용자 hasNewMeeting 플래그 업데이트
                            try {
                                val db = FirebaseFirestore.getInstance()
                                val hostEmail = currentUserEmail
                                val data = hashMapOf(
                                    "meetingId" to meetingId,
                                    "title" to s.title,
                                    "participants" to emails
                                )
                                db.collection("meetings").document(meetingId.toString())
                                    .set(data)
                                    .await()

                                val emailsToUpdate = mutableSetOf<String>()
                                hostEmail?.let { emailsToUpdate.add(it) }
                                emailsToUpdate.addAll(emails)
                                for (email in emailsToUpdate) {
                                    val snapshot = db.collection("users")
                                        .whereEqualTo("email", email)
                                        .limit(1)
                                        .get()
                                        .await()
                                    if (!snapshot.isEmpty) {
                                        val docRef = snapshot.documents.first().reference
                                        docRef.update(
                                            mapOf("hasNewMeeting" to true)
                                        ).await()
                                    }
                                }
                            } catch (e: Exception) {
                                _state.update { it.copy(error = e.message) }
                            }

                            _state.update { it.copy(isLoading = false) }
                            // 생성 성공 이벤트 전파
                            _events.emit(MeetingFormEvent.MeetingCreated(meetingId))
                        }
                    }

                    is ApiResult.Failure -> {
                        _state.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    // 회의 수정 메서드 추가
    private fun updateMeeting() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val s = _state.value
                val meetingId =
                    s.meetingId ?: throw IllegalStateException("meetingId is null in edit mode")

                // 1) 회의 본문 정보 업데이트
                val dateDigits = s.date.filter { it.isDigit() }.padStart(8, '0')
                val timeDigits = s.startTime.filter { it.isDigit() }.padStart(4, '0')
                val datePart = "${dateDigits.substring(0, 4)}-${
                    dateDigits.substring(
                        4,
                        6
                    )
                }-${dateDigits.substring(6, 8)}"
                val timePart = "${timeDigits.substring(0, 2)}:${timeDigits.substring(2, 4)}:00"
                val scheduledStartTime = "${datePart}T${timePart}"
                val targetTime = s.duration.toIntOrNull() ?: 0
                val restInterval = s.breakInterval.toIntOrNull() ?: 0
                val restDuration = s.breakDuration.toIntOrNull() ?: 0
                val agendas = s.agendas

                val updateReq = MeetingUpdateReqDto(
                    title = s.title,
                    location = s.location,
                    scheduledStartTime = scheduledStartTime,
                    targetTime = targetTime,
                    restInterval = restInterval,
                    restDuration = restDuration,
                    //agendas = agendas
                )

                when (val updateRes = updateMeetingUseCase(meetingId, updateReq)) {
                    is ApiResult.Failure -> {
                        _state.update { it.copy(isLoading = false, error = updateRes.message) }
                        return@launch
                    }

                    is ApiResult.Success -> { /* continue */
                    }
                }

                // 2) 참석자 동기화
                // loadedParticipants: setEditMode에서 로드된 원본(호스트 포함)
                val originalEmails = loadedParticipants.map { it.email }.toSet()
                val desiredEmails =
                    s.participantEmails.map { it.trim() }.filter { it.isNotEmpty() }.toSet()

                val emailsToAdd = desiredEmails.subtract(originalEmails)
                if (emailsToAdd.isNotEmpty()) {
                    when (val addRes = addMeetingUserUseCase(meetingId, emailsToAdd.toList())) {
                        is ApiResult.Failure -> {
                            _state.update { it.copy(isLoading = false, error = addRes.message) }
                            return@launch
                        }

                        is ApiResult.Success -> {}
                    }
                }

                // 제거는 userId 필요. 원본에서 제거 대상 추출 (호스트는 제거 대상 아님)
                val me = currentUserEmail
                val emailsToRemove = originalEmails.subtract(desiredEmails).filter { email ->
                    if (me.isNullOrBlank()) true else !email.equals(me, ignoreCase = true)
                }
                if (emailsToRemove.isNotEmpty()) {
                    loadedParticipants
                        .filter { p -> emailsToRemove.contains(p.email) }
                        .forEach { p ->
                            when (val rmRes = removeMeetingUserUseCase(meetingId, p.userId)) {
                                is ApiResult.Failure -> {
                                    _state.update {
                                        it.copy(
                                            isLoading = false,
                                            error = rmRes.message
                                        )
                                    }
                                    return@launch
                                }

                                is ApiResult.Success -> {}
                            }
                        }
                }

                // 3) 아젠다 동기화
                // 기존: loadedAgendas (id, content)
                val originalById = loadedAgendas.associateBy { it.agendaId }
                val originalContents = loadedAgendas.map { it.content }
                val desiredContents = s.agendas.map { it.trim() }.filter { it.isNotEmpty() }

                // 원본과 동일 인덱스 범위 내에서 내용 다른 경우 업데이트, 그 외는 추가/삭제 처리
                val minSize = minOf(originalContents.size, desiredContents.size)
                for (i in 0 until minSize) {
                    val oldContent = originalContents[i]
                    val newContent = desiredContents[i]
                    if (oldContent != newContent) {
                        val agendaId = loadedAgendas[i].agendaId
                        when (val upRes = updateAgendaUseCase(meetingId, agendaId, newContent)) {
                            is ApiResult.Failure -> {
                                _state.update { it.copy(isLoading = false, error = upRes.message) }
                                return@launch
                            }

                            is ApiResult.Success -> {}
                        }
                    }
                }

                // desired가 더 길면 나머지 추가
                if (desiredContents.size > originalContents.size) {
                    val contentsToAdd =
                        desiredContents.subList(originalContents.size, desiredContents.size)
                    when (val addAgRes = addAgendaUseCase(meetingId, contentsToAdd)) {
                        is ApiResult.Failure -> {
                            _state.update { it.copy(isLoading = false, error = addAgRes.message) }
                            return@launch
                        }

                        is ApiResult.Success -> {}
                    }
                }

                // original이 더 길면 초과분 삭제
                if (originalContents.size > desiredContents.size) {
                    for (i in desiredContents.size until originalContents.size) {
                        val agendaId = loadedAgendas[i].agendaId
                        when (val delRes = deleteAgendaUseCase(meetingId, agendaId)) {
                            is ApiResult.Failure -> {
                                _state.update { it.copy(isLoading = false, error = delRes.message) }
                                return@launch
                            }

                            is ApiResult.Success -> {}
                        }
                    }
                }

                // 4) 파이어베이스 회의 문서 업데이트
                try {
                    val db = FirebaseFirestore.getInstance()
                    val hostEmail = currentUserEmail
                    // 서버에서 최신 참석자 목록 조회 (삭제 후 반영된 상태)
                    val latestParticipantEmails =
                        when (val detailResult = getMeetingDetailUseCase(meetingId)) {
                            is ApiResult.Success -> {
                                detailResult.data.participants.map { it.email }
                            }

                            is ApiResult.Failure -> {
                                // 조회 실패 시 폼 상태 사용
                                s.participantEmails.map { it.trim() }.filter { it.isNotEmpty() }
                            }
                        }
                    db.collection("meetings").document(meetingId.toString())
                        .update(
                            mapOf(
                                "title" to s.title,
                                "participants" to latestParticipantEmails
                            )
                        )
                        .await()

                    val emailsToUpdate = mutableSetOf<String>()
                    hostEmail?.let { emailsToUpdate.add(it) }
                    emailsToUpdate.addAll(latestParticipantEmails)
                    // 제거된 참가자들도 hasNewMeeting = true로 유지
                    emailsToUpdate.addAll(emailsToRemove)
                    for (email in emailsToUpdate) {
                        val snapshot = db.collection("users")
                            .whereEqualTo("email", email)
                            .limit(1)
                            .get()
                            .await()
                        if (!snapshot.isEmpty) {
                            val docRef = snapshot.documents.first().reference
                            docRef.update(
                                mapOf("hasNewMeeting" to true)
                            ).await()
                        }
                    }
                } catch (e: Exception) {
                    _state.update { it.copy(error = e.message) }
                }

                _state.update { it.copy(isLoading = false) }
                // 수정 성공 이벤트 전파
                _events.emit(MeetingFormEvent.MeetingUpdated)
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun onEvent(event: MeetingFormEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    private fun isDirty(): Boolean {
        val s = _state.value
        val snap = initialSnapshot ?: return s.title.isNotBlank() ||
                s.participantEmails.isNotEmpty() ||
                s.date.isNotBlank() || s.startTime.isNotBlank() || s.endTime.isNotBlank() ||
                s.location.isNotBlank() || s.agendas.any { it.isNotBlank() } ||
                s.breakInterval.isNotBlank() || s.breakDuration.isNotBlank()

        return snap.title != s.title ||
                snap.date != s.date ||
                snap.startTime != s.startTime ||
                snap.endTime != s.endTime ||
                snap.duration != s.duration ||
                snap.location != s.location ||
                snap.agendas != s.agendas ||
                snap.participantEmails.toSet() != s.participantEmails.toSet()
    }

    private data class FormSnapshot(
        val title: String,
        val date: String,
        val startTime: String,
        val endTime: String,
        val duration: String,
        val location: String,
        val agendas: List<String>,
        val participantEmails: List<String>
    )

    // 이메일 형식 유효성 검사 함수
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // 폼 검증 메서드
    private fun validateForm() {
        val errors = mutableMapOf<String, String>()

        if (_state.value.title.trim().isEmpty()) {
            errors["title"] = "제목을 입력해주세요"
        }

        if (_state.value.participantEmails.isEmpty()) {
            errors["participants"] = "참석자를 한 명 이상 추가해주세요"
        }

        if (_state.value.date.trim().isEmpty()) {
            errors["date"] = "날짜를 선택해주세요"
        }

        if (_state.value.startTime.trim().isEmpty()) {
            errors["startTime"] = "시작 시간을 선택해주세요"
        }
        if (_state.value.endTime.trim().isEmpty()) {
            errors["endTime"] = "종료 시간을 선택해주세요"
        }

        if (_state.value.duration.trim().isEmpty()) {
            errors["duration"] = "시작 시간은 종료시간보다 빨라야 합니다."
        }

        if (_state.value.location.trim().isEmpty()) {
            errors["location"] = "회의 장소를 입력해주세요"
        }

        if (_state.value.agendas.isEmpty() || _state.value.agendas.all { it.trim().isEmpty() }) {
            errors["agendas"] = "회의 안건을 한 개 이상 입력해주세요"
        }

        if (_state.value.breakInterval.trim().isEmpty()) {
            errors["breakInterval"] = "쉬는 시간 간격을 입력해주세요"
        }
        if (_state.value.breakDuration.trim().isEmpty()) {
            errors["breakDuration"] = "쉬는 시간 지속 시간을 입력해주세요"
        }

        val interval = _state.value.breakInterval.trim().toIntOrNull()

        if (interval != null && interval <= 0) {
            errors["breakInterval"] = "쉬는 시간 간격은 0보다 커야 합니다"
        }

        val duration = _state.value.breakDuration.trim().toIntOrNull()

        if (duration != null && duration <= 0) {
            errors["breakDuration"] = "쉬는 시간 지속 시간은 0보다 커야 합니다"
        }

        if (errors.isEmpty()) {
            _state.update {
                it.copy(
                    validationErrors = emptyMap(),
                    showValidationErrors = false,
                    isFormValid = true
                )
            }
            viewModelScope.launch {
                _events.emit(MeetingFormEvent.FormValidationSuccess)
            }
        } else {
            _state.update {
                it.copy(
                    validationErrors = errors,
                    showValidationErrors = true,
                    isFormValid = false
                )
            }
        }
    }
}