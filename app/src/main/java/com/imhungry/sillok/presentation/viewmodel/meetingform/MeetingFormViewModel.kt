package com.imhungry.sillok.presentation.viewmodel.meetingform

import android.os.Build
import android.util.Patterns
import androidx.annotation.RequiresApi
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.data.local.UserStore
import com.imhungry.sillok.data.model.meeting.MeetingUpdateReqDto
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.agenda.Agenda
import com.imhungry.sillok.domain.model.meeting.AddTeamTagRequest
import com.imhungry.sillok.domain.model.meeting.CreateMeetingRequest
import com.imhungry.sillok.domain.model.meeting.Meeting
import com.imhungry.sillok.domain.model.meeting.RemoveTeamTagRequest
import com.imhungry.sillok.domain.usecase.agenda.AddAgendaUseCase
import com.imhungry.sillok.domain.usecase.agenda.DeleteAgendaUseCase
import com.imhungry.sillok.domain.usecase.agenda.UpdateAgendaUseCase
import com.imhungry.sillok.domain.usecase.meeting.CheckDuplicatedTimeUseCase
import com.imhungry.sillok.domain.usecase.meeting.CreateMeetingUseCase
import com.imhungry.sillok.domain.usecase.meeting.GetMeetingDetailUseCase
import com.imhungry.sillok.domain.usecase.meeting.UpdateMeetingUseCase
import com.imhungry.sillok.domain.usecase.meetinguser.AddMeetingUserUseCase
import com.imhungry.sillok.domain.usecase.meetinguser.RemoveMeetingUserUseCase
import com.imhungry.sillok.domain.usecase.places.SearchPlacesUseCase
import com.imhungry.sillok.domain.usecase.team.GetMyTeamsUseCase
import com.imhungry.sillok.domain.usecase.team.GetTeamMembersUseCase
import com.imhungry.sillok.domain.usecase.meeting.AddTeamTagUseCase
import com.imhungry.sillok.domain.usecase.meeting.RemoveTeamTagUseCase
import com.imhungry.sillok.presentation.state.meetingform.MemberInfo
import com.imhungry.sillok.presentation.state.meetingform.SelectedTeamInfo
import com.imhungry.sillok.presentation.state.meetingform.TeamInfo
import com.imhungry.sillok.presentation.state.meetingform.MeetingData
import com.imhungry.sillok.presentation.state.meetingform.MeetingFormEvent
import com.imhungry.sillok.presentation.state.meetingform.MeetingFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
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
    private val addAgendaUseCase: AddAgendaUseCase,
    private val deleteAgendaUseCase: DeleteAgendaUseCase,
    private val updateAgendaUseCase: UpdateAgendaUseCase,
    private val checkDuplicatedTimeUseCase: CheckDuplicatedTimeUseCase,
    private val getMyTeamsUseCase: GetMyTeamsUseCase,
    private val getTeamMembersUseCase: GetTeamMembersUseCase,
    private val addTeamTagUseCase: AddTeamTagUseCase,
    private val removeTeamTagUseCase: RemoveTeamTagUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MeetingFormState())
    val state = _state.asStateFlow()
    private var currentUserEmail: String? = null

    private val _events = MutableSharedFlow<MeetingFormEvent>()
    val events = _events.asSharedFlow()

    private var loadedParticipants: List<Meeting.Participant> = emptyList()
    private var loadedAgendas: List<Agenda> = emptyList()
    private var loadedTeamIds: List<Long> = emptyList()
    private var initialSnapshot: FormSnapshot? = null

    init {
        loadTeamsAndMembers()
    }

    private fun loadTeamsAndMembers() {
        viewModelScope.launch {
            try {
                // 현재 사용자 정보 가져오기
                val currentUser = userStore.user.first()
                val currentUserId = currentUser?.id

                // 팀 리스트 가져오기
                when (val teamsResult = getMyTeamsUseCase()) {
                    is ApiResult.Success -> {
                        val teams = teamsResult.data.map { teamListItem ->
                            TeamInfo(id = teamListItem.teamId, name = teamListItem.teamName)
                        }
                        android.util.Log.d("MeetingFormViewModel", "팀 리스트 로드 성공: ${teams.size}개")

                        // 각 팀의 멤버 가져오기 (id 기준 중복 제거)
                        val memberMap = ConcurrentHashMap<Long, MemberInfo>()
                        
                        coroutineScope {
                            // 각 팀의 멤버를 병렬로 가져오기
                            teams.map { team ->
                                async {
                                    when (val membersResult = getTeamMembersUseCase(team.id)) {
                                        is ApiResult.Success -> {
                                            // 각 멤버에 대해 MemberInfo 생성
                                            membersResult.data
                                                .filter { it.id != currentUserId } // 본인 제외
                                                .forEach { teamMember ->
                                                    val memberInfo = MemberInfo(
                                                        id = teamMember.id,
                                                        name = teamMember.name,
                                                        email = teamMember.email,
                                                        profileImage = teamMember.pictureURL.takeIf { it.isNotBlank() }
                                                    )
                                                    // 중복 체크 및 추가 (id 기준)
                                                    memberMap.putIfAbsent(memberInfo.id, memberInfo)
                                                }
                                        }
                                        is ApiResult.Failure -> {
                                            android.util.Log.e("MeetingFormViewModel", "팀 멤버 로드 실패: teamId=${team.id}, ${membersResult.message}")
                                        }
                                    }
                                }
                            }.forEach { it.await() }
                        }

                        android.util.Log.d("MeetingFormViewModel", "멤버 리스트 로드 성공: ${memberMap.size}명 (본인 제외, 중복 제외)")
                        _state.update {
                            it.copy(
                                teams = teams,
                                members = memberMap.values.toList()
                            )
                        }
                    }
                    is ApiResult.Failure -> {
                        android.util.Log.e("MeetingFormViewModel", "팀 리스트 로드 실패: ${teamsResult.message}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MeetingFormViewModel", "팀 및 멤버 로드 예외 발생: ${e.message}", e)
            }
        }
    }

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

                        // 아젠다는 회의 상세 응답에서 가져오기
                        loadedAgendas = meeting.agendas
                        val agendas = meeting.agendas.map { it.content }.ifEmpty { listOf("") }

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

                        // 팀 정보 저장: teamNames를 기반으로 state.teams에서 팀 ID 찾기
                        val currentTeams = _state.value.teams
                        loadedTeamIds = meeting.teamNames.mapNotNull { teamName ->
                            currentTeams.find { it.name == teamName }?.id
                        }

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
                                participantEmails = meetingData.participants.toSet(),
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
                            participantEmails = meetingData.participants.toSet()
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
                        if (event.email.isNotEmpty() && isValidEmail(event.email)) {
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
                                participantEmails = currentEmails - event.email
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
                        checkDuplicationAndProceed()
                    }

                    is MeetingFormEvent.DuplicationDialogConfirmed -> {
                        _state.update { it.copy(showDuplicationDialog = false) }
                        if (_state.value.isEditMode) {
                            updateMeeting()
                        } else {
                            createMeeting()
                        }
                    }

                    is MeetingFormEvent.DuplicationDialogDismissed -> {
                        _state.update { it.copy(showDuplicationDialog = false) }
                    }

                    is MeetingFormEvent.ShowTeamSearchDialog -> {
                        _state.update { it.copy(showTeamSearchDialog = true) }
                    }

                    is MeetingFormEvent.DismissTeamSearchDialog -> {
                        _state.update { 
                            it.copy(
                                showTeamSearchDialog = false,
                                teamSearchText = ""
                            )
                        }
                    }

                    is MeetingFormEvent.TeamSearchTextChanged -> {
                        _state.update { it.copy(teamSearchText = event.text) }
                    }

                    is MeetingFormEvent.TeamSelected -> {
                        _state.update { 
                            it.copy(
                                selectedTeam = event.team,
                                teamSearchText = ""
                            )
                        }
                    }

                    is MeetingFormEvent.InviteTeamMembers -> {
                        val selectedTeam = _state.value.selectedTeam
                        if (selectedTeam != null) {
                            viewModelScope.launch {
                                // 선택된 팀의 멤버를 다시 로드
                                when (val membersResult = getTeamMembersUseCase(selectedTeam.id)) {
                                    is ApiResult.Success -> {
                                        val currentUser = userStore.user.first()
                                        val currentUserId = currentUser?.id
                                        val teamMembers = membersResult.data
                                            .filter { it.id != currentUserId } // 본인 제외
                                            .map { teamMember ->
                                                MemberInfo(
                                                    id = teamMember.id,
                                                    name = teamMember.name,
                                                    email = teamMember.email,
                                                    profileImage = teamMember.pictureURL.takeIf { it.isNotBlank() }
                                                )
                                            }
                                        _state.update { 
                                            it.copy(
                                                showTeamSearchDialog = false,
                                                showTeamMemberSelectionDialog = true,
                                                teamMembersForSelection = teamMembers,
                                                selectedTeamMembers = teamMembers.map { it.id }.toSet()
                                            )
                                        }
                                    }
                                    is ApiResult.Failure -> {
                                        android.util.Log.e("MeetingFormViewModel", "팀 멤버 로드 실패: ${membersResult.message}")
                                        // 실패해도 다이얼로그는 열기
                                        _state.update { 
                                            it.copy(
                                                showTeamSearchDialog = false,
                                                showTeamMemberSelectionDialog = true,
                                                teamMembersForSelection = emptyList()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is MeetingFormEvent.ShowTeamMemberSelectionDialog -> {
                        _state.update { it.copy(showTeamMemberSelectionDialog = true) }
                    }

                    is MeetingFormEvent.DismissTeamMemberSelectionDialog -> {
                        _state.update { 
                            it.copy(
                                showTeamMemberSelectionDialog = false,
                                selectedTeamMembers = emptySet(),
                                selectedTeam = null,
                                teamMembersForSelection = emptyList()
                            )
                        }
                    }

                    is MeetingFormEvent.TeamMemberToggled -> {
                        val currentSelected = _state.value.selectedTeamMembers.toMutableSet()
                        if (currentSelected.contains(event.memberId)) {
                            currentSelected.remove(event.memberId)
                        } else {
                            currentSelected.add(event.memberId)
                        }
                        _state.update { it.copy(selectedTeamMembers = currentSelected) }
                    }

                    is MeetingFormEvent.SelectAllTeamMembers -> {
                        val teamMembers = _state.value.teamMembersForSelection
                        val allMemberIds = teamMembers.map { it.id }.toSet()
                        val currentSelected = _state.value.selectedTeamMembers
                        
                        // 모든 멤버가 선택되어 있으면 모두 해제, 그렇지 않으면 모두 선택
                        val newSelected = if (currentSelected == allMemberIds) {
                            emptySet<Long>()
                        } else {
                            allMemberIds
                        }
                        
                        _state.update { 
                            it.copy(
                                selectedTeamMembers = newSelected
                            )
                        }
                    }

                    is MeetingFormEvent.ConfirmTeamMemberSelection -> {
                        val selectedMembers = _state.value.selectedTeamMembers
                        val teamMembers = _state.value.teamMembersForSelection
                        val selectedTeam = _state.value.selectedTeam
                        
                        if (selectedTeam != null) {
                            // 선택된 멤버 정보
                            val selectedMemberInfos = teamMembers
                                .filter { selectedMembers.contains(it.id) }
                            
                            // 선택된 멤버의 이메일을 Set에 추가
                            val emailsToAdd = selectedMemberInfos.map { it.email }.toSet()
                            val updatedParticipantEmails = _state.value.participantEmails + emailsToAdd
                            
                            // 선택된 팀 정보 생성
                            val newSelectedTeam = SelectedTeamInfo(
                                teamId = selectedTeam.id,
                                teamName = selectedTeam.name,
                                selectedMembers = selectedMemberInfos
                            )
                            
                            // 선택된 팀 목록 업데이트
                            val existingTeam = _state.value.selectedTeams.find { it.teamId == selectedTeam.id }
                            val updatedSelectedTeams = if (existingTeam != null) {
                                // 기존 팀 정보 업데이트
                                _state.value.selectedTeams.map { 
                                    if (it.teamId == selectedTeam.id) newSelectedTeam else it
                                }
                            } else {
                                // 새 팀 추가
                                _state.value.selectedTeams + newSelectedTeam
                            }
                            
                            _state.update {
                                it.copy(
                                    participantEmails = updatedParticipantEmails,
                                    selectedTeams = updatedSelectedTeams,
                                    showTeamMemberSelectionDialog = false,
                                    selectedTeamMembers = emptySet(),
                                    selectedTeam = null,
                                    teamMembersForSelection = emptyList()
                                )
                            }
                        }
                    }

                    is MeetingFormEvent.RemoveSelectedTeam -> {
                        val teamToRemove = _state.value.selectedTeams.find { it.teamId == event.teamId }
                        if (teamToRemove != null) {
                            // 제거할 팀의 멤버 이메일을 Set에서 제거
                            val teamEmailsToRemove = teamToRemove.selectedMembers.map { it.email }.toSet()
                            val updatedParticipantEmails = _state.value.participantEmails - teamEmailsToRemove
                            
                            // 선택된 팀 목록에서 제거
                            val updatedSelectedTeams = _state.value.selectedTeams.filter { 
                                it.teamId != event.teamId 
                            }
                            
                            _state.update {
                                it.copy(
                                    participantEmails = updatedParticipantEmails,
                                    selectedTeams = updatedSelectedTeams
                                )
                            }
                        }
                    }

                    is MeetingFormEvent.ShowTeamMemberSelectionResultDialog -> {
                        val team = _state.value.selectedTeams.find { it.teamId == event.teamId }
                        _state.update {
                            it.copy(
                                showTeamMemberSelectionResultDialog = true,
                                selectedTeamForResult = team
                            )
                        }
                    }

                    is MeetingFormEvent.DismissTeamMemberSelectionResultDialog -> {
                        _state.update {
                            it.copy(
                                showTeamMemberSelectionResultDialog = false,
                                selectedTeamForResult = null
                            )
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private fun searchEmails(query: String) {
        viewModelScope.launch {
            val trimmedQuery = query.trim().lowercase()
            val currentState = _state.value
            
            if (trimmedQuery.length >= 2) {
                _state.update { it.copy(isSearching = true) }
                
                // 멤버 리스트에서 이메일 필터링
                val suggestions = currentState.members
                    .map { it.email }
                    .filter { email ->
                        // 이미 추가된 이메일은 제외
                        !currentState.participantEmails.contains(email)
                    }
                    .filter { email ->
                        // 쿼리로 시작하거나 포함하는 이메일
                        val lowerEmail = email.lowercase()
                        lowerEmail.startsWith(trimmedQuery) || lowerEmail.contains(trimmedQuery)
                    }
                    .sortedWith(compareBy<String> { !it.lowercase().startsWith(trimmedQuery) }
                        .thenBy { it.lowercase() }) // 쿼리로 시작하는 것 우선, 그 다음 알파벳 순
                    .distinct()
                    .take(10) // 최대 10개까지만 표시
                
                _state.update {
                    it.copy(
                        emailSuggestions = suggestions,
                        showEmailSuggestions = suggestions.isNotEmpty(),
                        isSearching = false
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        emailSuggestions = emptyList(),
                        showEmailSuggestions = false,
                        isSearching = false
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
                // 선택된 팀 ID 리스트 (팀이 없으면 빈 리스트)
                val teamIds = s.selectedTeams.map { it.teamId }

                // 제목이 비어있으면 placeholder 텍스트 사용
                val meetingTitle = if (s.title.trim().isEmpty()) {
                    "${s.userName}님의 회의"
                } else {
                    s.title
                }

                val request = CreateMeetingRequest(
                    title = meetingTitle,
                    location = s.location,
                    scheduledStartTime = scheduledStartTime,
                    targetTime = targetTime,
                    restInterval = restInterval,
                    restDuration = restDuration,
                    participants = emptyList(), // 생성 시 호스트만 등록, 참석자는 별도로 추가
                    agendas = s.agendas.filter { it.isNotBlank() },
                    teams = teamIds
                )

                when (val result = createMeetingUseCase(request)) {
                    is ApiResult.Success -> {
                        val meetingId = result.data
                        // 참석자 추가 (Set을 List로 변환)
                        val emails = s.participantEmails
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .toList()
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

                // 제목이 비어있으면 placeholder 텍스트 사용
                val meetingTitle = if (s.title.trim().isEmpty()) {
                    "${s.userName}님의 회의"
                } else {
                    s.title
                }

                val updateReq = MeetingUpdateReqDto(
                    title = meetingTitle,
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
                    // API 요청 시 Set을 List로 변환
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

                // 4) 팀 태그 동기화
                val originalTeamIds = loadedTeamIds.toSet()
                val desiredTeamIds = s.selectedTeams.map { it.teamId }.toSet()

                // 추가할 팀 태그
                val teamIdsToAdd = desiredTeamIds.subtract(originalTeamIds)
                for (teamId in teamIdsToAdd) {
                    when (val addRes = addTeamTagUseCase(meetingId, AddTeamTagRequest(teamId))) {
                        is ApiResult.Failure -> {
                            _state.update { it.copy(isLoading = false, error = addRes.message) }
                            return@launch
                        }
                        is ApiResult.Success -> {}
                    }
                }

                // 제거할 팀 태그
                val teamIdsToRemove = originalTeamIds.subtract(desiredTeamIds)
                for (teamId in teamIdsToRemove) {
                    when (val removeRes = removeTeamTagUseCase(meetingId, RemoveTeamTagRequest(teamId))) {
                        is ApiResult.Failure -> {
                            _state.update { it.copy(isLoading = false, error = removeRes.message) }
                            return@launch
                        }
                        is ApiResult.Success -> {}
                    }
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
                snap.participantEmails != s.participantEmails
    }

    private data class FormSnapshot(
        val title: String,
        val date: String,
        val startTime: String,
        val endTime: String,
        val duration: String,
        val location: String,
        val agendas: List<String>,
        val participantEmails: Set<String>
    )

    // 이메일 형식 유효성 검사 함수
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkDuplicationAndProceed() {
        viewModelScope.launch {
            val s = _state.value
            try {
                // 시작 시간과 목표 시간(분) 계산
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
                val targetMinutes = s.duration.toIntOrNull()?.toLong() ?: 0L

                // 중복 체크
                when (val result = checkDuplicatedTimeUseCase(scheduledStartTime, targetMinutes)) {
                    is ApiResult.Success -> {
                        if (result.data.isNotEmpty()) {
                            // 중복된 회의가 있으면 다이얼로그 표시
                            _state.update { it.copy(showDuplicationDialog = true) }
                        } else {
                            // 중복이 없으면 바로 진행
                            if (s.isEditMode) {
                                updateMeeting()
                            } else {
                                createMeeting()
                            }
                        }
                    }
                    is ApiResult.Failure -> {
                        // 중복 체크 실패 시에도 진행 (에러는 무시하고 계속)
                        if (s.isEditMode) {
                            updateMeeting()
                        } else {
                            createMeeting()
                        }
                    }
                }
            } catch (e: Exception) {
                // 예외 발생 시에도 진행
                if (s.isEditMode) {
                    updateMeeting()
                } else {
                    createMeeting()
                }
            }
        }
    }

    // 폼 검증 메서드
    private fun validateForm() {
        val errors = mutableMapOf<String, String>()

//        if (_state.value.title.trim().isEmpty()) {
//            errors["title"] = "제목을 입력해주세요"
//        }

//        if (_state.value.participantEmails.isEmpty()) {
//            errors["participants"] = "참석자를 한 명 이상 추가해주세요"
//        }

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