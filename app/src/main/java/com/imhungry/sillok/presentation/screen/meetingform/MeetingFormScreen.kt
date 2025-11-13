package com.imhungry.sillok.presentation.screen.meetingform

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.screen.meetingform.components.AgendaInputField
import com.imhungry.sillok.presentation.screen.meetingform.components.BreakTimeInputField
import com.imhungry.sillok.presentation.screen.meetingform.components.DateInputField
import com.imhungry.sillok.presentation.screen.meetingform.components.EmailInputFieldWithAutocomplete
import com.imhungry.sillok.presentation.screen.meetingform.components.ErrorText
import com.imhungry.sillok.presentation.screen.meetingform.components.InputField
import com.imhungry.sillok.presentation.screen.meetingform.components.SelectedTeamsList
import com.imhungry.sillok.presentation.screen.meetingform.components.TeamMemberSelectionDialog
import com.imhungry.sillok.presentation.screen.meetingform.components.TeamMemberSelectionResultDialog
import com.imhungry.sillok.presentation.screen.meetingform.components.TeamSearchDialog
import com.imhungry.sillok.presentation.screen.meetingform.components.TimeInputField
import com.imhungry.sillok.presentation.state.meetingform.MeetingFormEvent
import com.imhungry.sillok.presentation.viewmodel.meetingform.MeetingFormViewModel
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.ui.components.MediumSillokButton
import com.imhungry.sillok.ui.components.ScreenHeader
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.components.SillokDialog
import com.imhungry.sillok.ui.theme.gray400
import com.imhungry.sillok.ui.theme.gray500
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.primaryButton
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.secondaryButton
import com.imhungry.sillok.ui.theme.secondaryTextColor
import com.imhungry.sillok.ui.theme.whiteBackground
import java.time.LocalDate
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MeetingFormScreen(
    onCreateMeeting: () -> Unit,
    onBackClick: () -> Unit,
    isEditMode: Boolean = false,
    meetingId: Long? = null,
    viewModel: MeetingFormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val participantsFocusRequester = remember { FocusRequester() }

    // 편집 모드일 때 기존 회의 데이터 로드
    LaunchedEffect(isEditMode, meetingId) {
        if (isEditMode && meetingId != null) {
            viewModel.setEditMode(meetingId)
        }
    }

    // 이벤트 처리
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MeetingFormEvent.BackClicked -> onBackClick()
                is MeetingFormEvent.MeetingCreated -> {
                    onCreateMeeting()
                }

                is MeetingFormEvent.MeetingUpdated -> {
                    onCreateMeeting()
                }

                else -> {}
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BasicBox(
            statusBarColor = primaryBackground,
            navigationBarColor = primaryBackground,
            backgroundColor = primaryBackground,
            isLoading = state.isLoading
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusManager.clearFocus()
                        viewModel.onEvent(MeetingFormEvent.ClearFocus)
                    }
            ) {
                ScreenHeader(
                    title = if (isEditMode) "회의 정보" else "회의 생성",
                    onBackClick = { viewModel.onEvent(MeetingFormEvent.CancelClicked) }
                )

                // 메인 콘텐츠 영역
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 제목
                        item {
                            Column {
                                InputField(
                                    label = "제목",
                                    value = state.title,
                                    onValueChange = {
                                        viewModel.onEvent(
                                            MeetingFormEvent.TitleChanged(
                                                it
                                            )
                                        )
                                    },
                                    placeholder = "${state.userName}님의 회의",
                                    textFieldValue = state.titleTextFieldValue,
                                    onTextFieldValueChange = {
                                        viewModel.onEvent(
                                            MeetingFormEvent.TitleTextFieldValueChanged(
                                                it
                                            )
                                        )
                                    },
                                    onImeDone = { participantsFocusRequester.requestFocus() }
                                )
                                // 제목 에러 메시지
                                if (state.showValidationErrors && state.validationErrors.containsKey(
                                        "title"
                                    )
                                ) {
                                    ErrorText(
                                        text = state.validationErrors["title"] ?: "",
                                        modifier = Modifier.padding(start = 68.dp)
                                    )
                                }
                            }
                        }

                        // 참석자
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // 이메일 자동완성 드롭다운
                                if (state.showEmailSuggestions) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 36.dp, start = 65.dp)
                                            .zIndex(999f)
                                    ) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(
                                                    1.dp,
                                                    Color(0xFFE7E7E7),
                                                    RoundedCornerShape(8.dp)
                                                ),
                                            shape = RoundedCornerShape(8.dp),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                            colors = CardDefaults.cardColors(containerColor = primaryBackground)
                                        ) {
                                            LazyColumn(
                                                modifier = Modifier.heightIn(max = 216.dp)
                                            ) {
                                                items(state.emailSuggestions.filter { email ->
                                                    !state.participantEmails.contains(email)
                                                }) { email ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(Color.White)
                                                            .clickable {
                                                                viewModel.onEvent(
                                                                    MeetingFormEvent.ParticipantEmailSelected(
                                                                        email
                                                                    )
                                                                )
                                                            }
                                                            .padding(
                                                                horizontal = 16.dp,
                                                                vertical = 12.dp
                                                            ),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .border(1.dp, whiteBackground, RoundedCornerShape(12.dp))
                                                                .background(secondaryButton)
                                                                .padding(start = 12.dp, end = 12.dp, top = 3.dp, bottom = 4.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = email,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.Normal
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Column {
                                            EmailInputFieldWithAutocomplete(
                                                label = "참석자",
                                                value = state.participants,
                                                onValueChange = {
                                                    viewModel.onEvent(
                                                        MeetingFormEvent.ParticipantsChanged(
                                                            it
                                                        )
                                                    )
                                                },
                                                placeholder = "이메일",
                                                emailSuggestions = state.emailSuggestions,
                                                showEmailSuggestions = state.showEmailSuggestions,
                                                isSearching = state.isSearching,
                                                participantEmails = state.participantEmails,
                                                onEmailSelected = { email ->
                                                    viewModel.onEvent(
                                                        MeetingFormEvent.ParticipantEmailSelected(
                                                            email
                                                        )
                                                    )
                                                },
                                                onEmailSubmitted = { email ->
                                                    viewModel.onEvent(
                                                        MeetingFormEvent.ParticipantEmailSelected(
                                                            email
                                                        )
                                                    )
                                                },
                                                onEmailRemoved = { index ->
                                                    viewModel.onEvent(
                                                        MeetingFormEvent.ParticipantEmailRemoved(
                                                            index
                                                        )
                                                    )
                                                },
                                                modifier = Modifier.focusRequester(
                                                    participantsFocusRequester
                                                )
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 65.dp, top = 8.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(36.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(primaryBackground)
                                                        .border(1.dp, primaryButton, RoundedCornerShape(4.dp))
                                                        .clickable { viewModel.onEvent(MeetingFormEvent.ShowTeamSearchDialog) },
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = "팀 추가",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Normal,
                                                        color = primaryButton
                                                    )
                                                }
                                            }

                                            // 선택된 팀 목록 표시
                                            if (state.selectedTeams.isNotEmpty()) {
                                                SelectedTeamsList(
                                                    teams = state.selectedTeams,
                                                    onTeamRemoved = { teamId ->
                                                        viewModel.onEvent(MeetingFormEvent.RemoveSelectedTeam(teamId))
                                                    },
                                                    onTeamClick = { teamId ->
                                                        viewModel.onEvent(MeetingFormEvent.ShowTeamMemberSelectionResultDialog(teamId))
                                                    },
                                                    modifier = Modifier.padding(start = 65.dp, top = 8.dp)
                                                )
                                            }

                                            // 참석자 에러 메시지
                                            if (state.showValidationErrors && state.validationErrors.containsKey(
                                                    "participants"
                                                )
                                            ) {
                                                ErrorText(
                                                    text = state.validationErrors["participants"]
                                                        ?: "",
                                                    modifier = Modifier.padding(start = 68.dp)
                                                )
                                            }
                                        }

                                        Column {
                                            DateInputField(
                                                label = "일시",
                                                value = state.date,
                                                onValueChange = {
                                                    viewModel.onEvent(
                                                        MeetingFormEvent.DateChanged(
                                                            it
                                                        )
                                                    )
                                                },
                                                placeholder = "YYYY / MM / DD"
                                            )
                                            // 날짜 에러 메시지
                                            if (state.showValidationErrors && state.validationErrors.containsKey(
                                                    "date"
                                                )
                                            ) {
                                                ErrorText(
                                                    text = state.validationErrors["date"] ?: "",
                                                    modifier = Modifier.padding(start = 68.dp)
                                                )
                                            }
                                        }

                                        Column {
                                            TimeInputField(
                                                label = "시간",
                                                startTime = state.startTime,
                                                endTime = state.endTime,
                                                duration = state.duration,
                                                onStartTimeChange = {
                                                    viewModel.onEvent(
                                                        MeetingFormEvent.StartTimeChanged(it)
                                                    )
                                                },
                                                onEndTimeChange = {
                                                    viewModel.onEvent(
                                                        MeetingFormEvent.EndTimeChanged(it)
                                                    )
                                                },
                                                onDurationChange = {
                                                    viewModel.onEvent(
                                                        MeetingFormEvent.DurationChanged(it)
                                                    )
                                                },
                                                showTimePicker = state.showTimePicker,
                                                onTimePickerDismiss = {
                                                    viewModel.onEvent(
                                                        MeetingFormEvent.ClearFocus
                                                    )
                                                },
                                                onStartTimeClick = {
                                                    viewModel.onEvent(
                                                        MeetingFormEvent.TimePickerShown
                                                    )
                                                },
                                                onEndTimeClick = {
                                                    viewModel.onEvent(
                                                        MeetingFormEvent.TimePickerShown
                                                    )
                                                }
                                            )
                                            // 시간 에러 메시지
                                            if (state.showValidationErrors &&
                                                (state.validationErrors.containsKey("startTime")
                                                        || state.validationErrors.containsKey("endTime")
                                                        || state.validationErrors.containsKey("duration"))
                                            ) {
                                                val timeError = state.validationErrors["startTime"]
                                                    ?: state.validationErrors["endTime"]
                                                    ?: state.validationErrors["duration"] ?: ""
                                                if (timeError.isNotEmpty()) {
                                                    ErrorText(
                                                        text = timeError,
                                                        modifier = Modifier.padding(start = 68.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Box(
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            // 장소 자동완성 드롭다운
                                            if (state.showLocationSuggestions) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 36.dp, start = 65.dp)
                                                        .zIndex(999f)
                                                ) {
                                                    Card(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .border(
                                                                1.dp,
                                                                Color(0xFFE7E7E7),
                                                                RoundedCornerShape(8.dp)
                                                            ),
                                                        shape = RoundedCornerShape(8.dp),
                                                        elevation = CardDefaults.cardElevation(
                                                            defaultElevation = 0.dp
                                                        ),
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = primaryBackground
                                                        )
                                                    ) {
                                                        LazyColumn(
                                                            modifier = Modifier.heightIn(max = 216.dp)
                                                        ) {
                                                            items(state.locationSuggestions) { place ->
                                                                Row(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .background(Color.White)
                                                                        .clickable {
                                                                            viewModel.onEvent(
                                                                                MeetingFormEvent.LocationSelected(
                                                                                    place.name
                                                                                )
                                                                            )
                                                                        }
                                                                        .padding(
                                                                            horizontal = 16.dp,
                                                                            vertical = 12.dp
                                                                        ),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Column {
                                                                        Text(
                                                                            text = place.name,
                                                                            style = MaterialTheme.typography.bodyMedium,
                                                                            fontWeight = FontWeight.Normal,
                                                                            color = secondaryTextColor
                                                                        )
                                                                        Text(
                                                                            text = place.address,
                                                                            style = MaterialTheme.typography.bodySmall,
                                                                            color = gray400,
                                                                            fontSize = 14.sp
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            Column(
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Column(
                                                    modifier = Modifier.fillMaxSize(),
                                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                                ) {
                                                    Column {
                                                        InputField(
                                                            label = "장소",
                                                            value = state.location,
                                                            onValueChange = {
                                                                viewModel.onEvent(
                                                                    MeetingFormEvent.LocationChanged(
                                                                        it
                                                                    )
                                                                )
                                                            },
                                                            placeholder = "장소",
                                                            textFieldValue = state.locationTextFieldValue,
                                                            onTextFieldValueChange = {
                                                                viewModel.onEvent(
                                                                    MeetingFormEvent.LocationTextFieldValueChanged(
                                                                        it
                                                                    )
                                                                )
                                                            }
                                                        )
                                                        // 장소 에러 메시지
                                                        if (state.showValidationErrors && state.validationErrors.containsKey(
                                                                "location"
                                                            )
                                                        ) {
                                                            ErrorText(
                                                                text = state.validationErrors["location"]
                                                                    ?: "",
                                                                modifier = Modifier.padding(start = 68.dp)
                                                            )
                                                        }
                                                    }

                                                    Column {
                                                        AgendaInputField(
                                                            label = "안건",
                                                            agendas = state.agendas,
                                                            onAgendaChanged = { index, agenda ->
                                                                viewModel.onEvent(
                                                                    MeetingFormEvent.AgendaChanged(
                                                                        index,
                                                                        agenda
                                                                    )
                                                                )
                                                            },
                                                            onAgendaAdded = {
                                                                viewModel.onEvent(MeetingFormEvent.AgendaAdded)
                                                            },
                                                            onAgendaRemoved = { index ->
                                                                viewModel.onEvent(
                                                                    MeetingFormEvent.AgendaRemoved(
                                                                        index
                                                                    )
                                                                )
                                                            }
                                                        )
                                                        // 아젠다 에러 메시지
                                                        if (state.showValidationErrors && state.validationErrors.containsKey(
                                                                "agendas"
                                                            )
                                                        ) {
                                                            ErrorText(
                                                                text = state.validationErrors["agendas"]
                                                                    ?: "",
                                                                modifier = Modifier.padding(start = 68.dp)
                                                            )
                                                        }
                                                    }

                                                    Column {
                                                        BreakTimeInputField(
                                                            label = "쉬는시간",
                                                            breakInterval = state.breakInterval,
                                                            breakDuration = state.breakDuration,
                                                            onBreakIntervalChanged = { interval ->
                                                                viewModel.onEvent(
                                                                    MeetingFormEvent.BreakIntervalChanged(
                                                                        interval
                                                                    )
                                                                )
                                                            },
                                                            onBreakDurationChanged = { duration ->
                                                                viewModel.onEvent(
                                                                    MeetingFormEvent.BreakDurationChanged(
                                                                        duration
                                                                    )
                                                                )
                                                            }
                                                        )

                                                        // 쉬는 시간 에러 메시지
                                                        if (state.showValidationErrors && (state.validationErrors.containsKey(
                                                                "breakInterval"
                                                            ) || state.validationErrors.containsKey(
                                                                "breakDuration"
                                                            ))
                                                        ) {
                                                            val breakTimeError =
                                                                state.validationErrors["breakInterval"]
                                                                    ?: state.validationErrors["breakDuration"]
                                                                    ?: ""
                                                            if (breakTimeError.isNotEmpty()) {
                                                                ErrorText(
                                                                    text = breakTimeError,
                                                                    modifier = Modifier.padding(
                                                                        start = 68.dp
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                    ) {
                        if (isEditMode) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                SillokButton(
                                    text = "취소",
                                    onClick = {
                                        if (!state.isLoading) {
                                            viewModel.onEvent(MeetingFormEvent.CancelClicked)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    backgroundColor = gray500,
                                    textColor = primaryTextColor
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                SillokButton(
                                    text = "저장",
                                    onClick = {
                                        if (!state.isLoading) {
                                            viewModel.onEvent(MeetingFormEvent.ValidateForm)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        if (!isEditMode) {
                            SillokButton(
                                text = "새 회의 등록",
                                onClick = {
                                    if (!state.isLoading) {
                                        viewModel.onEvent(MeetingFormEvent.ValidateForm)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 시스템 뒤로가기 처리
            BackHandler {
                if (state.showDuplicationDialog) {
                    // 중복 다이얼로그가 열려있으면 다이얼로그 닫기
                    viewModel.onEvent(MeetingFormEvent.DuplicationDialogDismissed)
                } else {
                    // 그 외에는 취소 동작과 동일하게 처리
                    viewModel.onEvent(MeetingFormEvent.CancelClicked)
                }
            }
        }

        // 취소 확인 다이얼로그 (모드별 문구 분기) - 오버레이로 표시
        val dialogMessage = if (isEditMode) {
            "수정된 내용이 저장되지 않습니다.\n정말로 나가시겠습니까?"
        } else {
            "작성 중인 내용이 있습니다.\n정말로 취소하시겠습니까?"
        }
        SillokDialog(
            visible = state.showCancelDialog,
            message = dialogMessage,
            confirmText = "예",
            cancelText = "아니요",
            onConfirm = {
                viewModel.onEvent(MeetingFormEvent.CancelConfirmed)
            },
            onDismiss = {
                viewModel.onEvent(MeetingFormEvent.CancelDismissed)
            }
        )

        // 중복 시간 체크 다이얼로그
        val duplicationDialogMessage = if (isEditMode) {
            "기존 회의와 겹치는 일정이 있습니다\n그래도 회의를 수정하시겠습니까?"
        } else {
            "기존 회의와 겹치는 일정이 있습니다\n그래도 회의를 생성하시겠습니까?"
        }
        SillokDialog(
            visible = state.showDuplicationDialog,
            message = duplicationDialogMessage,
            confirmText = "취소",
            cancelText = "네",
            onConfirm = {
                viewModel.onEvent(MeetingFormEvent.DuplicationDialogDismissed)
            },
            onDismiss = {
                viewModel.onEvent(MeetingFormEvent.DuplicationDialogConfirmed)
            }
        )

        // 팀 검색 다이얼로그
        TeamSearchDialog(
            visible = state.showTeamSearchDialog,
            teams = state.teams,
            searchText = state.teamSearchText,
            selectedTeam = state.selectedTeam,
            onDismiss = {
                viewModel.onEvent(MeetingFormEvent.DismissTeamSearchDialog)
            },
            onSearchTextChange = { text ->
                viewModel.onEvent(MeetingFormEvent.TeamSearchTextChanged(text))
            },
            onTeamSelected = { team ->
                viewModel.onEvent(MeetingFormEvent.TeamSelected(team))
            },
            onInviteClick = {
                viewModel.onEvent(MeetingFormEvent.InviteTeamMembers)
            }
        )

        // 팀 멤버 선택 다이얼로그
        TeamMemberSelectionDialog(
            visible = state.showTeamMemberSelectionDialog,
            team = state.selectedTeam,
            members = state.teamMembersForSelection,
            selectedMemberIds = state.selectedTeamMembers,
            onDismiss = {
                viewModel.onEvent(MeetingFormEvent.DismissTeamMemberSelectionDialog)
            },
            onMemberToggle = { memberId ->
                viewModel.onEvent(MeetingFormEvent.TeamMemberToggled(memberId))
            },
            onSelectAll = {
                viewModel.onEvent(MeetingFormEvent.SelectAllTeamMembers)
            },
            onConfirm = {
                viewModel.onEvent(MeetingFormEvent.ConfirmTeamMemberSelection)
            }
        )

        // 팀 멤버 선택 결과 다이얼로그
        TeamMemberSelectionResultDialog(
            visible = state.showTeamMemberSelectionResultDialog,
            team = state.selectedTeamForResult,
            onDismiss = {
                viewModel.onEvent(MeetingFormEvent.DismissTeamMemberSelectionResultDialog)
            }
        )
    }
}