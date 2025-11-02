package com.imhungry.sillok.presentation.screen.home

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.sillok.R
import com.imhungry.sillok.domain.model.meeting.MeetingDetailSummary
import com.imhungry.sillok.presentation.viewmodel.home.HomeViewModel
import com.imhungry.sillok.ui.components.CustomDrawer
import com.imhungry.sillok.ui.components.HomeBasicBox
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.components.rememberDrawerState
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.sideBar
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    onNavigateToCreateMeeting: () -> Unit = {},
    onNavigateToMeetingDetail: (Long) -> Unit = {},
    onNavigagteToMeetingInProgress: (Long) -> Unit = {},
    onNavigateToMeetingMinutes: (Long) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val homeState by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchFocused by remember { mutableStateOf(false) }
    var showOngoingMeeting by remember { mutableStateOf(true) }
    var ongoingIndex by remember { mutableStateOf(0) }
    val drawerState = rememberDrawerState()

    if (homeState.searchText.isNotBlank()) {
        BackHandler {
            focusManager.clearFocus()
            keyboardController?.hide()
            isSearchFocused = false
            viewModel.onSearchTextChange("")
        }
    }
    
    if (drawerState.isOpen) {
        BackHandler {
            drawerState.close()
        }
    }

    CustomDrawer(
        drawerState = drawerState,
        drawerWidth = 280.dp,
        edgeThreshold = 50.dp,
        swipeThreshold = 0.1f,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(sideBar)
            ) {
                Sidebar(
                    userName = homeState.userName,
                    profileImage = homeState.profileImage,
                    onNewMeeting = {
                        drawerState.close()
                        viewModel.onCleared()
                        onNavigateToCreateMeeting()
                    },
                    onTeamManagement = {
                        drawerState.close()
                        // TODO: 팀 관리 화면으로 이동
                    },
                    onFeedbackHistory = {
                        drawerState.close()
                        // TODO: 피드백 기록 화면으로 이동
                    },
                    onMeetingFolder = {
                        drawerState.close()
                        // TODO: 회의록 폴더 화면으로 이동
                    }
                )
            }
        }
    ) {
        HomeBasicBox(
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
                        // 화면을 누르면 검색창의 포커스 해제
                        if (isSearchFocused) {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            isSearchFocused = false
                        }
                    }
            ) {
                // 상단 영역
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.menu),
                            contentDescription = "메뉴",
                            modifier = Modifier
                                .size(20.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { drawerState.open() }
                        )
                        Spacer(Modifier.weight(1f))
                        Image(
                            painter = painterResource(id = R.drawable.alarm),
                            contentDescription = "알림",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    val ongoingList = homeState.ongoingMeetings
                    if (ongoingList.isNotEmpty() && showOngoingMeeting) {
                        val current = ongoingList.getOrNull(ongoingIndex)
                        if (current != null) {
                            OngoingMeetingNotification(
                                meeting = current,
                                onJoinMeeting = {
                                    onNavigagteToMeetingInProgress(current.id)
                                },
                                onDeclineMeeting = {},
                                onDismiss = {
                                    if (ongoingIndex < ongoingList.lastIndex) {
                                        ongoingIndex += 1
                                    } else {
                                        showOngoingMeeting = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    SearchBar(
                        modifier = Modifier.fillMaxWidth(),
                        onFocusChange = { focused ->
                            isSearchFocused = focused
                        },
                        focusRequester = focusRequester,
                        onMenuClick = {
                            drawerState.open()
                        },
                        text = homeState.searchText,
                        onTextChange = { viewModel.onSearchTextChange(it) }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    if (homeState.searchText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        SearchResultList(
                            results = homeState.searchResults,
                            isLoading = homeState.isSearching,
                            onItemClick = { meeting ->
                                viewModel.onCleared()
                                when (meeting.status) {
                                    "WAITING" -> onNavigateToMeetingDetail(meeting.id)
                                    "IN_PROGRESS" -> onNavigagteToMeetingInProgress(meeting.id)
                                    "COMPLETED" -> onNavigateToMeetingMinutes(meeting.id)
                                    else -> onNavigateToMeetingDetail(meeting.id)
                                }
                            }
                        )
                    } else {
                        Column {
//                            CalendarView(
//                                selectedDate = selectedDate,
//                                onDateSelected = { date ->
//                                    selectedDate = date
//                                },
//                                onTodayClick = {
//                                    selectedDate = LocalDate.now()
//                                },
//                                scheduledMeetings = scheduledMeetings,
//                                pastMeetings = pastMeetings,
//                                modifier = Modifier.fillMaxWidth()
//                            )
//
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            SelectedDateMeetingList(
//                                selectedDate = selectedDate,
//                                scheduledMeetings = scheduledMeetings,
//                                pastMeetings = pastMeetings,
//                                onMeetingItemClick = onMeetingItemClick,
//                                modifier = Modifier.fillMaxSize()
//                            )
//                        }
                            MeetingScheduleView(
                                onMeetingItemClick = { meeting ->
                                    viewModel.onCleared()
                                    when (meeting.status) {
                                        "WAITING" -> onNavigateToMeetingDetail(meeting.id)
                                        "IN_PROGRESS" -> onNavigagteToMeetingInProgress(meeting.id)
                                        "COMPLETED" -> onNavigateToMeetingMinutes(meeting.id)
                                        else -> onNavigateToMeetingDetail(meeting.id)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                scheduledMeetings = homeState.scheduledMeetings,
                                pastMeetings = homeState.pastMeetings
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val ongoingList = homeState.ongoingMeetings

                    Spacer(modifier = Modifier.height(16.dp))

                    if (ongoingList.isNotEmpty() && showOngoingMeeting) {
                        val current = ongoingList.getOrNull(ongoingIndex)
                        if (current != null) {
                            SillokButton(
                                text = "현재 진행 중인 회의 참여하기",
                                onClick = {
                                    viewModel.onCleared()
                                    onNavigateToCreateMeeting()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    SillokButton(
                        text = "새 회의 만들기",
                        backgroundColor = primaryTextColor,
                        onClick = {
                            viewModel.onCleared()
                            onNavigateToCreateMeeting()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultList(
    results: List<MeetingDetailSummary>,
    isLoading: Boolean,
    onItemClick: (MeetingDetailSummary) -> Unit
) {
    if (isLoading) return
    if (results.isEmpty()) return
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp)
    ) {
        items(results) { meeting ->
            MeetingItem(meeting = meeting) { onItemClick(meeting) }
        }
    }
}