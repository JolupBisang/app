package com.imhungry.sillok.presentation.screen.meetingdetail

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.sillok.presentation.viewmodel.meetingdetail.MeetingDetailViewModel
import com.imhungry.sillok.presentation.screen.meetingform.components.AgendaInputField
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.presentation.screen.meetingform.components.BreakTimeInputField
import com.imhungry.sillok.presentation.screen.meetingform.components.DateInputField
import com.imhungry.sillok.presentation.screen.meetingform.components.EmailInputFieldWithAutocomplete
import com.imhungry.sillok.presentation.screen.meetingform.components.InputField
import com.imhungry.sillok.ui.components.ScreenHeader
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.components.SillokButtonRow
import com.imhungry.sillok.presentation.screen.meetingform.components.TimeInputField
import com.imhungry.sillok.presentation.state.meetingform.MeetingFormEvent
import com.imhungry.sillok.ui.theme.primaryBackground

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingDetailScreen(
    meetingId: Long,
    onEditMeeting: () -> Unit,
    onStartMeeting: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: MeetingDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(Unit) {
        //viewModel.loadMeetingDetail(meetingId)
        viewModel.loadDummyMeetingDetail()
    }

    BackHandler { onBackClick() }

    BasicBox(
        statusBarColor = primaryBackground,
        navigationBarColor = primaryBackground,
        backgroundColor = primaryBackground
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ScreenHeader(
                title = "회의 정보",
                onBackClick = { onBackClick() }
            )

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        InputField(
                            label = "제목",
                            value = state.title,
                            onValueChange = {},
                            isReadOnly = true
                        )
                    }

                    item {
                        EmailInputFieldWithAutocomplete(
                            label = "참석자",
                            participantEmails = state.participantEmails,
                            onValueChange = {},
                            onEmailSelected = {},
                            onEmailSubmitted = {},
                            onEmailRemoved = {},
                            isReadOnly = true,
                            hostEmail = state.hostEmail
                        )
                    }

                    item {
                        DateInputField(
                            label = "일시",
                            value = state.date,
                            onValueChange = {},
                            placeholder = "YYYY / MM / DD",
                            isReadOnly = true
                        )
                    }

                    item {
                        TimeInputField(
                            label = "시간",
                            startTime = state.startTime,
                            endTime = state.endTime,
                            duration = state.targetTime,
                            onStartTimeChange = { /* 사용되지 않음 */ },
                            onEndTimeChange = { /* 사용되지 않음 */ },
                            onDurationChange = { /* 사용되지 않음 */ },
                            isReadOnly = true
                        )
                    }

                    item {
                        InputField(
                            label = "장소",
                            value = state.location,
                            onValueChange = {},
                            isReadOnly = true
                        )
                    }

                    item {
                        AgendaInputField(
                            label = "아젠다",
                            agendas = state.agendas,
                            onAgendaChanged = { index, agenda -> /* 사용되지 않음 */ },
                            onAgendaAdded = { /* 사용되지 않음 */ },
                            onAgendaRemoved = { index -> /* 사용되지 않음 */ },
                            isReadOnly = true
                        )
                    }

                    item {
                        Column {
                            BreakTimeInputField(
                                label = "쉬는시간",
                                breakInterval = state.breakInterval,
                                breakDuration = state.breakDuration,
                                onBreakIntervalChanged = { /* 사용되지 않음 */ },
                                onBreakDurationChanged = { /* 사용되지 않음 */ },
                                isReadOnly = true
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                ) {
                    if (state.isHost) {
                        SillokButtonRow(
                            onBack = {
                                onBackClick()
                            },
                            onEnter = {
                                onStartMeeting()
                            },
                            onModify = {
                                onEditMeeting()
                            }
                        )
                    } else {
                        SillokButton(
                            text = "입장",
                            onClick = { onStartMeeting() },
                        )
                    }
                }
            }
        }
    }
}