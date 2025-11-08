package com.imhungry.sillok.presentation.screen.meetingdetail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.sillok.presentation.screen.meetingform.components.AgendaInputField
import com.imhungry.sillok.presentation.screen.meetingform.components.BreakTimeInputField
import com.imhungry.sillok.presentation.screen.meetingform.components.DateInputField
import com.imhungry.sillok.presentation.screen.meetingform.components.EmailInputFieldWithAutocomplete
import com.imhungry.sillok.presentation.screen.meetingform.components.InputField
import com.imhungry.sillok.presentation.screen.meetingform.components.TimeInputField
import com.imhungry.sillok.presentation.viewmodel.meetingdetail.MeetingDetailViewModel
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.ui.components.ScreenHeader
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.components.SillokButtonRow
import com.imhungry.sillok.ui.components.SillokDialog
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
        viewModel.loadMeetingDetail(meetingId)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        BasicBox(
            statusBarColor = primaryBackground,
            navigationBarColor = primaryBackground,
            backgroundColor = primaryBackground,
            isLoading = state.isLoading
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
                                label = "안건",
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
                                    viewModel.showDismissDialog()
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

        SillokDialog(
            visible = state.showDismissDialog,
            message = "이 회의를 목록에서 숨기시겠습니까?",
            confirmText = "예",
            cancelText = "취소",
            onConfirm = {
                viewModel.dismissMeeting()
                onBackClick()
            },
            onDismiss = {
                viewModel.dismissDismissDialog()
            }
        )
    }
}