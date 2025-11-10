package com.imhungry.sillok.presentation.screen.home

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.sillok.R
import com.imhungry.sillok.presentation.state.home.HomeState
import com.imhungry.sillok.presentation.state.home.MeetingUi
import com.imhungry.sillok.presentation.viewmodel.home.HomeViewModel
import com.imhungry.sillok.ui.components.BasicBoxWithGradientBurshBackground
import com.imhungry.sillok.ui.components.ExitDialog
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.components.SillokInfoDialog
import com.imhungry.sillok.ui.theme.gradientBrush2
import com.imhungry.sillok.ui.theme.gradientBrush3
import com.imhungry.sillok.ui.theme.green300
import com.imhungry.sillok.ui.theme.lightMeetingOutline
import com.imhungry.sillok.ui.theme.meetingOutline
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.primaryTextColor
import com.imhungry.sillok.ui.theme.sideBar
import com.imhungry.sillok.ui.theme.tertiary
import kotlinx.coroutines.launch
import androidx.compose.material3.rememberDrawerState as rememberMaterialDrawerState

private const val TAG = "HomeScreen"

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    onNavigateToCreateMeeting: () -> Unit = {},
    onNavigateToMeetingDetail: (Long) -> Unit = {},
    onNavigagteToMeetingInProgress: (Long) -> Unit = {},
    onNavigateToMeetingMinutes: (Long) -> Unit = {},
    onNavigateToNotificationHistory: () -> Unit = {},
    onNavigateToTeamList: () -> Unit = {},
    onNavigateToMeetingMinutesFolder: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val homeState by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchFocused by remember { mutableStateOf(false) }

    val notificationState = rememberNotificationState()
    val materialDrawerState = rememberMaterialDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navigationHandlers = rememberNavigationHandlers(
        onNavigateToCreateMeeting = onNavigateToCreateMeeting,
        onNavigateToMeetingDetail = onNavigateToMeetingDetail,
        onNavigagteToMeetingInProgress = onNavigagteToMeetingInProgress,
        onNavigateToMeetingMinutes = onNavigateToMeetingMinutes
    )

    // drawerContent에 전달할 값들을 추출하여 불필요한 재구성 방지
    val userName = homeState.userName
    val profileImage = homeState.profileImage
    
    // 콜백들을 메모이제이션
    val onNewMeetingClick = remember(scope, materialDrawerState, onNavigateToCreateMeeting) {
        {
            scope.launch { materialDrawerState.close() }
            onNavigateToCreateMeeting()
        }
    }
    
    val onTeamManagementClick = remember(scope, materialDrawerState, onNavigateToTeamList) {
        {
            scope.launch { materialDrawerState.close() }
            onNavigateToTeamList()
        }
    }
    
    val onFeedbackHistoryClick = remember(scope, materialDrawerState) {
        {
            scope.launch { materialDrawerState.close() }
        }
    }
    
    val onMeetingFolderClick = remember(scope, materialDrawerState, onNavigateToMeetingMinutesFolder) {
        {
            scope.launch { materialDrawerState.close() }
            onNavigateToMeetingMinutesFolder()
        }
    }

    ExitDialog(
        visible = homeState.showExitDialog,
        onConfirm = {
            viewModel.dismissExitDialog()
        },
        onDismiss = { viewModel.dismissExitDialog() }
    )

    SillokInfoDialog(
        visible = homeState.showGeneratingMeetingNoteDialog,
        message = "회의록을 생성하는 중입니다",
        confirmText = "확인",
        onConfirm = {
            viewModel.dismissGeneratingMeetingNoteDialog()
        }
    )

    HandleBackPress(
        showExitDialog = homeState.showExitDialog,
        searchText = homeState.searchText,
        isDrawerOpen = materialDrawerState.currentValue == DrawerValue.Open,
        onExitDialogDismiss = { viewModel.dismissExitDialog() },
        onSearchClose = {
            clearSearchFocus(focusManager, keyboardController) { isSearchFocused = false }
            viewModel.onSearchTextChange("")
        },
        onDrawerClose = {
            scope.launch { materialDrawerState.close() }
        },
        onExitDialogShow = { viewModel.showExitDialog() }
    )

    ModalNavigationDrawer(
        drawerState = materialDrawerState,
        drawerContent = {
            Sidebar(
                userName = userName,
                profileImage = profileImage,
                onNewMeeting = onNewMeetingClick,
                onTeamManagement = onTeamManagementClick,
                //onFeedbackHistory = onFeedbackHistoryClick,
                onMeetingFolder = onMeetingFolderClick
            )
        }
    ) {
        HomeContent(
            homeState = homeState,
            notificationState = notificationState,
            viewModel = viewModel,
            focusRequester = focusRequester,
            onSearchFocusChange = { isSearchFocused = it },
            onSearchTextChange = { viewModel.onSearchTextChange(it) },
            onMeetingItemClick = { meeting ->
                viewModel.onMeetingClick(meeting.id) { meetingId ->
                    // meetingId로 MeetingUi를 찾아서 navigate
                    val targetMeeting = homeState.meetings.find { it.id == meetingId }
                        ?: homeState.searchResults.find { it.id == meetingId }
                        ?: meeting
                    navigationHandlers.navigateToMeeting(targetMeeting)
                }
            },
            onMonthChanged = { year, month ->
                viewModel.loadHomeDataForMonth(year, month)
            },
            onJoinOngoingMeeting = { meeting ->
                viewModel.onCleared()
                onNavigagteToMeetingInProgress(meeting.id)
            },
            onJoinScheduledMeeting = { meeting ->
                viewModel.onMeetingClick(meeting.id) { meetingId ->
                    // meetingId로 MeetingUi를 찾아서 navigate
                    val targetMeeting = homeState.meetings.find { it.id == meetingId }
                        ?: homeState.upcomingMeetings.find { it.id == meetingId }
                        ?: meeting
                    navigationHandlers.navigateToMeeting(targetMeeting)
                }
            },
            onCreateMeeting = {
                onNavigateToCreateMeeting()
            },
            onMenuClick = {
                scope.launch { materialDrawerState.open() }
            },
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
        showScheduledMeeting = mutableStateOf(true)
    )
}

private data class NotificationState(
    val showOngoingMeeting: MutableState<Boolean>,
    val showScheduledMeeting: MutableState<Boolean>
)

@Composable
private fun rememberNavigationHandlers(
    onNavigateToCreateMeeting: () -> Unit,
    onNavigateToMeetingDetail: (Long) -> Unit,
    onNavigagteToMeetingInProgress: (Long) -> Unit,
    onNavigateToMeetingMinutes: (Long) -> Unit
) = remember(
    onNavigateToCreateMeeting,
    onNavigateToMeetingDetail,
    onNavigagteToMeetingInProgress,
    onNavigateToMeetingMinutes
) {
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
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?,
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
    homeState: HomeState,
    notificationState: NotificationState,
    viewModel: HomeViewModel,
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

    BasicBoxWithGradientBurshBackground(
        statusBarColor = primaryBackground,
        navigationBarColor = primaryBackground,
        backgroundColor = primaryBackground,
        gradientBrush = gradientBrush,
        isLoading = homeState.isLoading
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
                viewModel = viewModel,
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
    viewModel: HomeViewModel,
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
            viewModel = viewModel,
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
    viewModel: HomeViewModel,
    onJoinOngoingMeeting: (MeetingUi) -> Unit,
    onJoinScheduledMeeting: (MeetingUi) -> Unit
) {
    // dismissed가 false인 회의만 필터링
    val availableOngoing = ongoingList.filter { !it.dismissed }
    val availableUpcoming = upcomingList.filter { !it.dismissed }

    // 리스트가 비어있으면 알림 숨기기
    LaunchedEffect(availableOngoing.isEmpty()) {
        if (availableOngoing.isEmpty() && notificationState.showOngoingMeeting.value) {
            notificationState.showOngoingMeeting.value = false
        }
    }

    LaunchedEffect(availableUpcoming.isEmpty()) {
        if (availableUpcoming.isEmpty() && notificationState.showScheduledMeeting.value) {
            notificationState.showScheduledMeeting.value = false
        }
    }

    when {
        availableOngoing.isNotEmpty() && notificationState.showOngoingMeeting.value -> {
            OngoingMeetingNotification(
                meetings = ongoingList,
                onJoinMeeting = { meeting -> onJoinOngoingMeeting(meeting) },
                onDismiss = { meeting -> viewModel.dismissOngoingMeeting(meeting.id) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        availableUpcoming.isNotEmpty() && notificationState.showScheduledMeeting.value -> {
            ScheduledMeetingNotification(
                meetings = upcomingList,
                onJoinMeeting = { meeting -> onJoinScheduledMeeting(meeting) },
                onDismiss = { meeting -> viewModel.dismissScheduledMeeting(meeting.id) },
                modifier = Modifier.fillMaxWidth()
            )
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
    onJoinOngoingMeeting: (MeetingUi) -> Unit,
    onCreateMeeting: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(16.dp))

        if (ongoingList.isNotEmpty() && showOngoingMeeting) {
            // 항상 첫 번째 요소 사용
            val current = ongoingList.firstOrNull()
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
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(results) { meeting ->
            val backgroundColor: Color
            val borderColor: Color
            val borderWith: Dp
            val titleColor: Color
            val timeColor: Color

            when (meeting.status) {
                "IN_PROGRESS" -> {
                    // 진행 중인 회의
                    backgroundColor = Color.White
                    borderColor = green300
                    borderWith = 2.dp
                    titleColor = primaryTextColor
                    timeColor = primaryTextColor
                }

                "WAITING" -> {
                    // 예정된 회의
                    backgroundColor = Color.White
                    borderColor = meetingOutline
                    borderWith = 1.dp
                    titleColor = primaryTextColor
                    timeColor = tertiary
                }

                else -> {
                    // 그 외 (COMPLETED 등)
                    backgroundColor = Color.Transparent
                    borderColor = lightMeetingOutline
                    borderWith = 1.dp
                    titleColor = tertiary
                    timeColor = tertiary
                }
            }

            MeetingListItem(
                meeting = meeting,
                onClick = { onItemClick(meeting) },
                backgroundColor = backgroundColor,
                borderColor = borderColor,
                borderWith = borderWith,
                titleColor = titleColor,
                timeColor = timeColor
            )
        }
    }
}