package com.imhungry.sillok.presentation.screen.meetingdetail

import android.os.Build
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.screen.meetingform.components.AgendaInputField
import com.imhungry.sillok.presentation.screen.meetingform.components.BreakTimeInputField
import com.imhungry.sillok.presentation.screen.meetingform.components.DateInputField
import com.imhungry.sillok.presentation.screen.meetingform.components.EmailInputFieldWithAutocomplete
import com.imhungry.sillok.presentation.screen.meetingform.components.InputField
import com.imhungry.sillok.presentation.screen.meetingform.components.LabelText
import com.imhungry.sillok.presentation.screen.meetingform.components.SelectedEmailsList
import com.imhungry.sillok.presentation.screen.meetingform.components.TimeInputField
import com.imhungry.sillok.presentation.viewmodel.meetingdetail.MeetingDetailViewModel
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.ui.components.ScreenHeader
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.components.SillokButtonRow
import com.imhungry.sillok.ui.components.SillokDialog
import com.imhungry.sillok.ui.theme.green300
import com.imhungry.sillok.ui.theme.inverse
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.secondaryButton
import com.imhungry.sillok.ui.theme.tertiary

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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                LabelText(
                                    text = "팀"
                                )
                                if (state.teamNames.isNotEmpty()) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        state.teamNames.forEach { teamName ->
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .border(1.dp, green300, RoundedCornerShape(12.dp))
                                                    .background(green300)
                                                    .padding(start = 12.dp, end = 12.dp, top = 3.dp, bottom = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = teamName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = inverse
                                                )
                                            }
                                        }
                                    }
                                }
                            }
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
            message = "이 회의를 숨기시겠습니까?\n\n다른 참석자는 이 회의를 계속 볼 수 있습니다.",
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