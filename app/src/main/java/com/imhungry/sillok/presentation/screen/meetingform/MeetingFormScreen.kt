package com.imhungry.sillok.presentation.screen.meetingform

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.BackHandler
import com.imhungry.sillok.presentation.state.meetingform.MeetingData
import com.imhungry.sillok.presentation.state.meetingform.MeetingFormEvent
import com.imhungry.sillok.presentation.viewmodel.meetingform.MeetingFormViewModel
import com.imhungry.sillok.presentation.screen.meetingform.components.AgendaInputField
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.presentation.screen.meetingform.components.BreakTimeInputField
import com.imhungry.sillok.presentation.screen.meetingform.components.DateInputField
import com.imhungry.sillok.presentation.screen.meetingform.components.EmailInputFieldWithAutocomplete
import com.imhungry.sillok.presentation.screen.meetingform.components.ErrorText
import com.imhungry.sillok.presentation.screen.meetingform.components.InputField
import com.imhungry.sillok.ui.components.ScreenHeader
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.components.SillokDialog
import com.imhungry.sillok.presentation.screen.meetingform.components.TimeInputField
import com.imhungry.sillok.ui.theme.gray400
import com.imhungry.sillok.ui.theme.gray500
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.primaryTextColor
import java.time.LocalDate
import java.time.LocalTime

// YYYYMMDD 형식의 문자열을 LocalDate로 파싱하는 헬퍼 함수
@RequiresApi(Build.VERSION_CODES.O)
private fun parseDateFromYYYYMMDD(dateString: String): LocalDate {
    return try {
        if (dateString.length == 8) {
            val year = dateString.substring(0, 4).toInt()
            val month = dateString.substring(4, 6).toInt()
            val day = dateString.substring(6, 8).toInt()
            LocalDate.of(year, month, day)
        } else {
            LocalDate.now() // 기본값으로 현재 날짜 반환
        }
    } catch (e: Exception) {
        LocalDate.now() // 파싱 실패 시 현재 날짜 반환
    }
}

// HHMM 형식의 문자열을 LocalTime으로 파싱하는 헬퍼 함수
@RequiresApi(Build.VERSION_CODES.O)
private fun parseTimeFromHHMM(timeString: String): LocalTime {
    return try {
        if (timeString.length == 4) {
            val hour = timeString.substring(0, 2).toInt()
            val minute = timeString.substring(2, 4).toInt()
            LocalTime.of(hour, minute)
        } else {
            LocalTime.now() // 기본값으로 현재 시간 반환
        }
    } catch (e: Exception) {
        LocalTime.now() // 파싱 실패 시 현재 시간 반환
    }
}


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
            backgroundColor = primaryBackground
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
                                 onValueChange = { viewModel.onEvent(MeetingFormEvent.TitleChanged(it)) },
                                 placeholder = "${state.userName}님의 회의",
                                 textFieldValue = state.titleTextFieldValue,
                                 onTextFieldValueChange = { viewModel.onEvent(MeetingFormEvent.TitleTextFieldValueChanged(it)) },
                                 onImeDone = { participantsFocusRequester.requestFocus() }
                             )
                            // 제목 에러 메시지
                            if (state.showValidationErrors && state.validationErrors.containsKey("title")) {
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
                                        .padding(top = 38.dp, start = 65.dp)
                                        .zIndex(999f)
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color(0xFFE7E7E7), RoundedCornerShape(4.dp)),
                                        shape = RoundedCornerShape(4.dp),
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
                                                    Text(
                                                        text = email,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Normal
                                                    )
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
                                            onValueChange = { viewModel.onEvent(MeetingFormEvent.ParticipantsChanged(it)) },
                                            placeholder = "이메일",
                                            emailSuggestions = state.emailSuggestions,
                                            showEmailSuggestions = state.showEmailSuggestions,
                                            isSearching = state.isSearching,
                                            participantEmails = state.participantEmails,
                                            onEmailSelected = { email ->
                                                viewModel.onEvent(MeetingFormEvent.ParticipantEmailSelected(email))
                                            },
                                            onEmailSubmitted = { email ->
                                                viewModel.onEvent(MeetingFormEvent.ParticipantEmailSelected(email))
                                            },
                                            onEmailRemoved = { index ->
                                                viewModel.onEvent(MeetingFormEvent.ParticipantEmailRemoved(index))
                                            },
                                            modifier = Modifier.focusRequester(participantsFocusRequester)
                                        )
                                        // 참석자 에러 메시지
                                        if (state.showValidationErrors && state.validationErrors.containsKey("participants")) {
                                            ErrorText(
                                                text = state.validationErrors["participants"] ?: "",
                                                modifier = Modifier.padding(start = 68.dp)
                                            )
                                        }
                                    }

                                    Column {
                                        DateInputField(
                                            label = "일시",
                                            value = state.date,
                                            onValueChange = { viewModel.onEvent(MeetingFormEvent.DateChanged(it)) },
                                            placeholder = "YYYY / MM / DD"
                                        )
                                        // 날짜 에러 메시지
                                        if (state.showValidationErrors && state.validationErrors.containsKey("date")) {
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
                                            onStartTimeChange = { viewModel.onEvent(MeetingFormEvent.StartTimeChanged(it)) },
                                            onEndTimeChange = { viewModel.onEvent(MeetingFormEvent.EndTimeChanged(it)) },
                                            onDurationChange = { viewModel.onEvent(MeetingFormEvent.DurationChanged(it)) },
                                            showTimePicker = state.showTimePicker,
                                            onTimePickerDismiss = { viewModel.onEvent(MeetingFormEvent.ClearFocus) },
                                            onStartTimeClick = { viewModel.onEvent(MeetingFormEvent.TimePickerShown) },
                                            onEndTimeClick = { viewModel.onEvent(MeetingFormEvent.TimePickerShown) }
                                        )
                                        // 시간 에러 메시지
                                        if (state.showValidationErrors && (state.validationErrors.containsKey("startTime") || state.validationErrors.containsKey("endTime"))) {
                                            val timeError = state.validationErrors["startTime"] ?: state.validationErrors["endTime"] ?: ""
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
                                                    .padding(top = 38.dp, start = 65.dp)
                                                    .zIndex(999f)
                                            ) {
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .border(1.dp, Color(0xFFE7E7E7), RoundedCornerShape(4.dp)),
                                                    shape = RoundedCornerShape(4.dp),
                                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                                    colors = CardDefaults.cardColors(containerColor = primaryBackground)
                                                ) {
                                                    LazyColumn(
                                                        modifier = Modifier.heightIn(max = 216.dp)
                                                    ) {
                                                        items(state.locationSuggestions) { place ->
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
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
                                                                        fontWeight = FontWeight.Normal
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
                                                         onValueChange = { viewModel.onEvent(MeetingFormEvent.LocationChanged(it)) },
                                                         placeholder = "장소",
                                                         textFieldValue = state.locationTextFieldValue,
                                                         onTextFieldValueChange = { viewModel.onEvent(MeetingFormEvent.LocationTextFieldValueChanged(it)) }
                                                     )
                                                    // 장소 에러 메시지
                                                    if (state.showValidationErrors && state.validationErrors.containsKey("location")) {
                                                        ErrorText(
                                                            text = state.validationErrors["location"] ?: "",
                                                            modifier = Modifier.padding(start = 68.dp)
                                                        )
                                                    }
                                                }

                                                Column {
                                                    AgendaInputField(
                                                        label = "아젠다",
                                                        agendas = state.agendas,
                                                        onAgendaChanged = { index, agenda ->
                                                            viewModel.onEvent(MeetingFormEvent.AgendaChanged(index, agenda))
                                                        },
                                                        onAgendaAdded = {
                                                            viewModel.onEvent(MeetingFormEvent.AgendaAdded)
                                                        },
                                                        onAgendaRemoved = { index ->
                                                            viewModel.onEvent(MeetingFormEvent.AgendaRemoved(index))
                                                        }
                                                    )
                                                    // 아젠다 에러 메시지
                                                    if (state.showValidationErrors && state.validationErrors.containsKey("agendas")) {
                                                        ErrorText(
                                                            text = state.validationErrors["agendas"] ?: "",
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
                                                            viewModel.onEvent(MeetingFormEvent.BreakIntervalChanged(interval))
                                                        },
                                                        onBreakDurationChanged = { duration ->
                                                            viewModel.onEvent(MeetingFormEvent.BreakDurationChanged(duration))
                                                        }
                                                    )

                                                    // 쉬는 시간 에러 메시지
                                                    if (state.showValidationErrors && (state.validationErrors.containsKey("breakInterval") || state.validationErrors.containsKey("breakDuration"))) {
                                                        val breakTimeError = state.validationErrors["breakInterval"] ?: state.validationErrors["breakDuration"] ?: ""
                                                        if (breakTimeError.isNotEmpty()) {
                                                            ErrorText(
                                                                text = breakTimeError,
                                                                modifier = Modifier.padding(start = 68.dp)
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
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        // 장소 자동완성 드롭다운이 나타날 때만 Spacer 표시
                        if (state.showLocationSuggestions) {
                            Spacer(modifier = Modifier.height(220.dp))
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
                                    viewModel.onEvent(MeetingFormEvent.CancelClicked)
                                },
                                modifier = Modifier.weight(1f),
                                backgroundColor = gray500,
                                textColor = primaryTextColor
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            SillokButton(
                                text = "저장",
                                onClick = {
                                    viewModel.onEvent(MeetingFormEvent.ValidateForm)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    if (!isEditMode) {
                        SillokButton(
                            text = "새 회의 등록",
                            onClick = {
                                viewModel.onEvent(MeetingFormEvent.ValidateForm)
                            }
                        )
                    }
                }
            }
        }
            
            // 시스템 뒤로가기 → 취소 동작과 동일하게 처리
            BackHandler {
                viewModel.onEvent(MeetingFormEvent.CancelClicked)
            }
        }

        // 취소 확인 다이얼로그 (모드별 문구 분기) - 오버레이로 표시
        if (state.showCancelDialog) {
            val dialogMessage = if (isEditMode) {
                "수정된 내용이 저장되지 않습니다.\n정말로 나가시겠습니까?"
            } else {
                "작성 중인 내용이 있습니다.\n정말로 취소하시겠습니까?"
            }
            SillokDialog(
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
        }
    }
}