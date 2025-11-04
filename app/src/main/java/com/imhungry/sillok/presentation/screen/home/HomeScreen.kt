package com.imhungry.sillok.presentation.screen.home

import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.state.home.MeetingUi
import com.imhungry.sillok.presentation.viewmodel.home.HomeViewModel
import com.imhungry.sillok.ui.components.CustomDrawer
import com.imhungry.sillok.ui.components.ExitDialog
import com.imhungry.sillok.ui.components.HomeBasicBox
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.components.SillokDialog
import com.imhungry.sillok.ui.components.rememberDrawerState
import com.imhungry.sillok.ui.theme.gradientBrush2
import com.imhungry.sillok.ui.theme.gradientBrush3
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.sideBar
import com.imhungry.sillok.ui.components.SillokTextButton
import com.imhungry.sillok.ui.theme.tertiary

private const val TAG = "HomeScreen"

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    onNavigateToCreateMeeting: () -> Unit = {},
    onNavigateToMeetingDetail: (Long) -> Unit = {},
    onNavigagteToMeetingInProgress: (Long) -> Unit = {},
    onNavigateToMeetingMinutes: (Long) -> Unit = {},
    onNavigateToNotificationHistory: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val homeState by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchFocused by remember { mutableStateOf(false) }
    
    val notificationState = rememberNotificationState()
    val drawerState = rememberDrawerState()

    val navigationHandlers = rememberNavigationHandlers(
        onNavigateToCreateMeeting = onNavigateToCreateMeeting,
        onNavigateToMeetingDetail = onNavigateToMeetingDetail,
        onNavigagteToMeetingInProgress = onNavigagteToMeetingInProgress,
        onNavigateToMeetingMinutes = onNavigateToMeetingMinutes
    )

    MeetingStartedDialog(
        visible = homeState.showMeetingStartedDialog,
        meetingTitle = homeState.pendingMeetingTitle,
        pendingMeetingId = homeState.pendingMeetingId,
        onConfirm = { meetingId ->
            viewModel.dismissMeetingStartedDialog()
            onNavigagteToMeetingInProgress(meetingId)
        },
        onDismiss = { viewModel.dismissMeetingStartedDialog() }
    )

    ExitDialog(
        visible = homeState.showExitDialog,
        onConfirm = {
            viewModel.dismissExitDialog()
        },
        onDismiss = { viewModel.dismissExitDialog() }
    )

    HandleBackPress(
        showExitDialog = homeState.showExitDialog,
        searchText = homeState.searchText,
        isDrawerOpen = drawerState.isOpen,
        onExitDialogDismiss = { viewModel.dismissExitDialog() },
        onSearchClose = {
            clearSearchFocus(focusManager, keyboardController) { isSearchFocused = false }
            viewModel.onSearchTextChange("")
        },
        onDrawerClose = { drawerState.close() },
        onExitDialogShow = { viewModel.showExitDialog() }
    )

    CustomDrawer(
        drawerState = drawerState,
        drawerWidth = 280.dp,
        edgeThreshold = 50.dp,
        swipeThreshold = 0.1f,
        drawerContent = {
            DrawerContent(
                userName = homeState.userName,
                profileImage = homeState.profileImage,
                onNewMeeting = {
                    drawerState.close()
                    viewModel.onCleared()
                    onNavigateToCreateMeeting()
                },
                onTeamManagement = { drawerState.close() },
                onFeedbackHistory = { drawerState.close() },
                onMeetingFolder = { drawerState.close() }
            )
        }
    ) {
        HomeContent(
            homeState = homeState,
            notificationState = notificationState,
            focusRequester = focusRequester,
            onSearchFocusChange = { isSearchFocused = it },
            onSearchTextChange = { viewModel.onSearchTextChange(it) },
            onMeetingItemClick = { meeting ->
                viewModel.onCleared()
                navigationHandlers.navigateToMeeting(meeting)
            },
            onMonthChanged = { year, month ->
                viewModel.loadHomeDataForMonth(year, month)
            },
            onJoinOngoingMeeting = { meeting ->
                viewModel.onCleared()
                onNavigagteToMeetingInProgress(meeting.id)
            },
            onJoinScheduledMeeting = { meeting ->
                viewModel.onCleared()
                navigationHandlers.navigateToMeeting(meeting)
            },
            onCreateMeeting = {
                viewModel.onCleared()
                onNavigateToCreateMeeting()
            },
            onMenuClick = { drawerState.open() },
            onNotificationClick = onNavigateToNotificationHistory,
            onScreenClick = {
                if (isSearchFocused) {
                    clearSearchFocus(focusManager, keyboardController) { isSearchFocused = false }
                }
            }
        )
    }
}

@Composable
private fun rememberNotificationState() = remember {
    NotificationState(
        showOngoingMeeting = mutableStateOf(true),
        ongoingIndex = mutableStateOf(0),
        showScheduledMeeting = mutableStateOf(true),
        scheduledIndex = mutableStateOf(0)
    )
}

private data class NotificationState(
    val showOngoingMeeting: MutableState<Boolean>,
    val ongoingIndex: MutableState<Int>,
    val showScheduledMeeting: MutableState<Boolean>,
    val scheduledIndex: MutableState<Int>
)

@Composable
private fun rememberNavigationHandlers(
    onNavigateToCreateMeeting: () -> Unit,
    onNavigateToMeetingDetail: (Long) -> Unit,
    onNavigagteToMeetingInProgress: (Long) -> Unit,
    onNavigateToMeetingMinutes: (Long) -> Unit
) = remember(onNavigateToCreateMeeting, onNavigateToMeetingDetail, onNavigagteToMeetingInProgress, onNavigateToMeetingMinutes) {
    NavigationHandlers(
        navigateToMeeting = { meeting ->
            when (meeting.status) {
                "WAITING" -> onNavigateToMeetingDetail(meeting.id)
                "IN_PROGRESS" -> onNavigagteToMeetingInProgress(meeting.id)
                "COMPLETED" -> onNavigateToMeetingMinutes(meeting.id)
                else -> onNavigateToMeetingDetail(meeting.id)
            }
        }
    )
}

private data class NavigationHandlers(
    val navigateToMeeting: (MeetingUi) -> Unit
)

@Composable
private fun MeetingStartedDialog(
    visible: Boolean,
    meetingTitle: String?,
    pendingMeetingId: Long?,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val dialogMessage = if (meetingTitle != null) {
        "\"$meetingTitle\"\n회의가 시작되었습니다.\n참여하시겠습니까?"
    } else {
        "회의가 시작되었습니다.\n참여하시겠습니까?"
    }

    SillokDialog(
        visible = visible,
        message = dialogMessage,
        confirmText = "참여",
        cancelText = "나중에",
        onConfirm = {
            pendingMeetingId?.let { meetingId ->
                Log.d(TAG, "회의 참여 선택: meetingId=$meetingId")
                onConfirm(meetingId)
            }
        },
        onDismiss = {
            Log.d(TAG, "회의 참여 취소")
            onDismiss()
        }
    )
}

@Composable
private fun HandleBackPress(
    showExitDialog: Boolean,
    searchText: String,
    isDrawerOpen: Boolean,
    onExitDialogDismiss: () -> Unit,
    onSearchClose: () -> Unit,
    onDrawerClose: () -> Unit,
    onExitDialogShow: () -> Unit
) {
    BackHandler {
        when {
            showExitDialog -> {
                onExitDialogDismiss()
            }
            searchText.isNotBlank() -> {
                onSearchClose()
            }
            isDrawerOpen -> {
                onDrawerClose()
            }
            else -> {
                onExitDialogShow()
            }
        }
    }
}

private fun clearSearchFocus(
    focusManager: androidx.compose.ui.focus.FocusManager,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?,
    onFocusCleared: () -> Unit
) {
    focusManager.clearFocus()
    keyboardController?.hide()
    onFocusCleared()
}

@Composable
private fun DrawerContent(
    userName: String,
    profileImage: String,
    onNewMeeting: () -> Unit,
    onTeamManagement: () -> Unit,
    onFeedbackHistory: () -> Unit,
    onMeetingFolder: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(sideBar)
    ) {
        Sidebar(
            userName = userName,
            profileImage = profileImage,
            onNewMeeting = onNewMeeting,
            onTeamManagement = onTeamManagement,
            onFeedbackHistory = onFeedbackHistory,
            onMeetingFolder = onMeetingFolder
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HomeContent(
    homeState: com.imhungry.sillok.presentation.state.home.HomeState,
    notificationState: NotificationState,
    focusRequester: FocusRequester,
    onSearchFocusChange: (Boolean) -> Unit,
    onSearchTextChange: (String) -> Unit,
    onMeetingItemClick: (MeetingUi) -> Unit,
    onMonthChanged: (Int, Int) -> Unit,
    onJoinOngoingMeeting: (MeetingUi) -> Unit,
    onJoinScheduledMeeting: (MeetingUi) -> Unit,
    onCreateMeeting: () -> Unit,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onScreenClick: () -> Unit
) {
    val ongoingList = homeState.ongoingMeetings
    val upcomingList = homeState.upcomingMeetings

    val gradientBrush = rememberGradientBrush(
        showOngoingMeeting = notificationState.showOngoingMeeting.value,
        showScheduledMeeting = notificationState.showScheduledMeeting.value,
        ongoingList = ongoingList,
        upcomingList = upcomingList
    )

    HomeBasicBox(
        statusBarColor = primaryBackground,
        navigationBarColor = primaryBackground,
        backgroundColor = primaryBackground,
        gradientBrush = gradientBrush
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onScreenClick() }
        ) {
            TopContent(
                modifier = Modifier.weight(1f),
                ongoingList = ongoingList,
                upcomingList = upcomingList,
                notificationState = notificationState,
                onMenuClick = onMenuClick,
                onNotificationClick = onNotificationClick,
                onJoinOngoingMeeting = onJoinOngoingMeeting,
                onJoinScheduledMeeting = onJoinScheduledMeeting,
                focusRequester = focusRequester,
                onSearchFocusChange = onSearchFocusChange,
                searchText = homeState.searchText,
                onSearchTextChange = onSearchTextChange,
                searchResults = homeState.searchResults,
                isSearching = homeState.isSearching,
                meetings = homeState.meetings,
                onMeetingItemClick = onMeetingItemClick,
                onMonthChanged = onMonthChanged
            )

            BottomButtons(
                ongoingList = ongoingList,
                showOngoingMeeting = notificationState.showOngoingMeeting.value,
                ongoingIndex = notificationState.ongoingIndex.value,
                onJoinOngoingMeeting = onJoinOngoingMeeting,
                onCreateMeeting = onCreateMeeting
            )
        }
    }
}

@Composable
private fun rememberGradientBrush(
    showOngoingMeeting: Boolean,
    showScheduledMeeting: Boolean,
    ongoingList: List<MeetingUi>,
    upcomingList: List<MeetingUi>
) = remember(showOngoingMeeting, showScheduledMeeting, ongoingList.size, upcomingList.size) {
    when {
        ongoingList.isNotEmpty() && showOngoingMeeting -> gradientBrush2
        upcomingList.isNotEmpty() && showScheduledMeeting -> gradientBrush3
        else -> gradientBrush2
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TopContent(
    modifier: Modifier = Modifier,
    ongoingList: List<MeetingUi>,
    upcomingList: List<MeetingUi>,
    notificationState: NotificationState,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onJoinOngoingMeeting: (MeetingUi) -> Unit,
    onJoinScheduledMeeting: (MeetingUi) -> Unit,
    focusRequester: FocusRequester,
    onSearchFocusChange: (Boolean) -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    searchResults: List<MeetingUi>,
    isSearching: Boolean,
    meetings: List<MeetingUi>,
    onMeetingItemClick: (MeetingUi) -> Unit,
    onMonthChanged: (Int, Int) -> Unit
) {
    Column(modifier = modifier) {
        TopHeader(
            onMenuClick = onMenuClick,
            onNotificationClick = onNotificationClick
        )

        NotificationArea(
            ongoingList = ongoingList,
            upcomingList = upcomingList,
            notificationState = notificationState,
            onJoinOngoingMeeting = onJoinOngoingMeeting,
            onJoinScheduledMeeting = onJoinScheduledMeeting
        )

        Spacer(Modifier.height(16.dp))

        SearchArea(
            focusRequester = focusRequester,
            onSearchFocusChange = onSearchFocusChange,
            searchText = searchText,
            onSearchTextChange = onSearchTextChange
        )

        Spacer(modifier = Modifier.height(24.dp))

        MeetingListArea(
            searchText = searchText,
            searchResults = searchResults,
            isSearching = isSearching,
            meetings = meetings,
            onMeetingItemClick = onMeetingItemClick,
            onMonthChanged = onMonthChanged
        )
    }
}

@Composable
private fun TopHeader(
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit
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
                ) { onMenuClick() }
        )
        Spacer(Modifier.weight(1f))
        Image(
            painter = painterResource(id = R.drawable.alarm),
            contentDescription = "알림",
            modifier = Modifier
                .size(20.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onNotificationClick() }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun NotificationArea(
    ongoingList: List<MeetingUi>,
    upcomingList: List<MeetingUi>,
    notificationState: NotificationState,
    onJoinOngoingMeeting: (MeetingUi) -> Unit,
    onJoinScheduledMeeting: (MeetingUi) -> Unit
) {
    when {
        ongoingList.isNotEmpty() && notificationState.showOngoingMeeting.value -> {
            val current = ongoingList.getOrNull(notificationState.ongoingIndex.value)
            current?.let { meeting ->
                key(meeting.id) {
                    OngoingMeetingNotification(
                        meeting = meeting,
                        onJoinMeeting = { onJoinOngoingMeeting(meeting) },
                        onDeclineMeeting = {},
                        onDismiss = {
                            if (notificationState.ongoingIndex.value < ongoingList.lastIndex) {
                                notificationState.ongoingIndex.value += 1
                            } else {
                                notificationState.showOngoingMeeting.value = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        upcomingList.isNotEmpty() && notificationState.showScheduledMeeting.value -> {
            val current = upcomingList.getOrNull(notificationState.scheduledIndex.value)
            current?.let { meeting ->
                key(meeting.id) {
                    ScheduledMeetingNotification(
                        meeting = meeting,
                        onJoinMeeting = { onJoinScheduledMeeting(meeting) },
                        onDeclineMeeting = {},
                        onDismiss = {
                            if (notificationState.scheduledIndex.value < upcomingList.lastIndex) {
                                notificationState.scheduledIndex.value += 1
                            } else {
                                notificationState.showScheduledMeeting.value = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchArea(
    focusRequester: FocusRequester,
    onSearchFocusChange: (Boolean) -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit
) {
    SearchBar(
        modifier = Modifier.fillMaxWidth(),
        onFocusChange = onSearchFocusChange,
        focusRequester = focusRequester,
        text = searchText,
        onTextChange = onSearchTextChange
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MeetingListArea(
    searchText: String,
    searchResults: List<MeetingUi>,
    isSearching: Boolean,
    meetings: List<MeetingUi>,
    onMeetingItemClick: (MeetingUi) -> Unit,
    onMonthChanged: (Int, Int) -> Unit
) {
    if (searchText.isNotBlank()) {
        Spacer(modifier = Modifier.height(24.dp))
        SearchResultList(
            results = searchResults,
            isLoading = isSearching,
            onItemClick = onMeetingItemClick
        )
    } else {
        Column {
            MeetingScheduleView(
                onMeetingItemClick = onMeetingItemClick,
                onMonthChanged = onMonthChanged,
                meetings = meetings,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BottomButtons(
    ongoingList: List<MeetingUi>,
    showOngoingMeeting: Boolean,
    ongoingIndex: Int,
    onJoinOngoingMeeting: (MeetingUi) -> Unit,
    onCreateMeeting: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(16.dp))

        if (ongoingList.isNotEmpty() && showOngoingMeeting) {
            val current = ongoingList.getOrNull(ongoingIndex)
            current?.let { meeting ->
                SillokButton(
                    text = "현재 진행 중인 회의 참여하기",
                    onClick = { onJoinOngoingMeeting(meeting) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        SillokButton(
            text = "새 회의 만들기",
            backgroundColor = primaryTextColor,
            onClick = onCreateMeeting,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SearchResultList(
    results: List<MeetingUi>,
    isLoading: Boolean,
    onItemClick: (MeetingUi) -> Unit
) {
    if (isLoading || results.isEmpty()) return

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp)
    ) {
        items(results) { meeting ->
            MeetingItemUi(meeting = meeting) { onItemClick(meeting) }
        }
    }
}

@Composable
private fun MeetingItemUi(
    meeting: MeetingUi,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        SillokTextButton(
            text = "∘  ${meeting.title}",
            onClick = onClick
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            modifier = Modifier.width(80.dp),
            text = meeting.timeRange,
            style = MaterialTheme.typography.bodySmall,
            color = tertiary
        )
    }
}
