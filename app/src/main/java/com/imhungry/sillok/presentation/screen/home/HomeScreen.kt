package com.imhungry.sillok.presentation.screen.home

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
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
import com.imhungry.sillok.domain.model.meeting.MeetingDetailSummary
import com.imhungry.sillok.presentation.state.home.HomeState
import com.imhungry.sillok.presentation.state.home.MeetingUi
import com.imhungry.sillok.presentation.viewmodel.home.HomeViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
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
    onNavigateToLogin: () -> Unit = {},
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

    val userName = homeState.userName
    val profileImage = homeState.profileImage

    // 사용자 조회 실패 시 로그인 화면으로 이동
    LaunchedEffect(homeState.shouldNavigateToLogin) {
        if (homeState.shouldNavigateToLogin) {
            viewModel.clearNavigateToLogin()
            onNavigateToLogin()
        }
    }

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
        searchQuery = homeState.searchQuery,
        isDrawerOpen = materialDrawerState.currentValue == DrawerValue.Open,
        onExitDialogDismiss = { viewModel.dismissExitDialog() },
        onSearchClose = {
            clearSearchFocus(focusManager, keyboardController) { isSearchFocused = false }
            viewModel.clearSearch()
        },
        onDrawerClose = {
            scope.launch { materialDrawerState.close() }
        },
        onExitDialogShow = { viewModel.showExitDialog() }
    )

//    ModalNavigationDrawer(
//        drawerState = materialDrawerState,
//        drawerContent = {
//            Sidebar(
//                userName = userName,
//                profileImage = profileImage,
//                onNewMeeting = onNewMeetingClick,
//                onTeamManagement = onTeamManagementClick,
//                //onFeedbackHistory = onFeedbackHistoryClick,
//                onMeetingFolder = onMeetingFolderClick,
//                onLogout = onNavigateToLogin
//            )
//        }
//    ) {
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
                viewModel.loadHomeDataForMonth2(year, month)
            },
            onJoinOngoingMeeting = { meeting ->
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
//    }
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
    searchQuery: String,
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

            searchText.isNotBlank() || searchQuery.isNotBlank() -> {
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
                isLoading = homeState.isLoading,
                onMenuClick = onMenuClick,
                onNotificationClick = onNotificationClick,
                onJoinOngoingMeeting = onJoinOngoingMeeting,
                onJoinScheduledMeeting = onJoinScheduledMeeting,
                focusRequester = focusRequester,
                onSearchFocusChange = onSearchFocusChange,
                searchText = homeState.searchText,
                onSearchTextChange = onSearchTextChange,
                searchQuery = homeState.searchQuery,
                isSearching = homeState.isSearching,
                meetings = homeState.meetings,
                onMeetingItemClick = onMeetingItemClick,
                onMonthChanged = onMonthChanged
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
    isLoading: Boolean,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onJoinOngoingMeeting: (MeetingUi) -> Unit,
    onJoinScheduledMeeting: (MeetingUi) -> Unit,
    focusRequester: FocusRequester,
    onSearchFocusChange: (Boolean) -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    searchQuery: String,
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
            isLoading = isLoading,
            onJoinOngoingMeeting = onJoinOngoingMeeting,
            onJoinScheduledMeeting = onJoinScheduledMeeting
        )

        Spacer(Modifier.height(16.dp))

        SearchArea(
            focusRequester = focusRequester,
            onSearchFocusChange = onSearchFocusChange,
            searchText = searchText,
            onSearchTextChange = onSearchTextChange,
            onSearchSubmit = { viewModel.onSearchSubmit() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        MeetingListArea(
            searchText = searchText,
            searchQuery = searchQuery,
            searchPagingFlowState = viewModel.searchPagingFlow,
            isSearching = isSearching,
            meetings = meetings,
            onMeetingItemClick = onMeetingItemClick,
            onMonthChanged = onMonthChanged,
            onRefresh = { viewModel.refresh() },
            isLoading = isLoading
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
//        Image(
//            painter = painterResource(id = R.drawable.logo),
//            contentDescription = "로고",
//            modifier = Modifier
//                .size(40.dp)
//                .clickable(
//                    interactionSource = remember { MutableInteractionSource() },
//                    indication = null
//                ) {  }
//        )
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
    isLoading: Boolean,
    onJoinOngoingMeeting: (MeetingUi) -> Unit,
    onJoinScheduledMeeting: (MeetingUi) -> Unit
) {
    // dismissed가 false인 회의만 필터링
    val availableOngoing = ongoingList.filter { !it.dismissed }
    val availableUpcoming = upcomingList.filter { !it.dismissed }

    // 로딩이 완료된 후에만 알림 표시/숨김 처리
    LaunchedEffect(isLoading, availableOngoing.size) {
        if (!isLoading) {
            if (availableOngoing.isEmpty() && notificationState.showOngoingMeeting.value) {
                notificationState.showOngoingMeeting.value = false
            } else if (availableOngoing.isNotEmpty() && !notificationState.showOngoingMeeting.value) {
                notificationState.showOngoingMeeting.value = true
            }
        }
    }

    LaunchedEffect(isLoading, availableUpcoming.size) {
        if (!isLoading) {
            if (availableUpcoming.isEmpty() && notificationState.showScheduledMeeting.value) {
                notificationState.showScheduledMeeting.value = false
            } else if (availableUpcoming.isNotEmpty() && !notificationState.showScheduledMeeting.value) {
                notificationState.showScheduledMeeting.value = true
            }
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
    onSearchTextChange: (String) -> Unit,
    onSearchSubmit: () -> Unit = {}
) {
    SearchBar(
        modifier = Modifier.fillMaxWidth(),
        onFocusChange = onSearchFocusChange,
        focusRequester = focusRequester,
        text = searchText,
        onTextChange = onSearchTextChange,
        onImeAction = onSearchSubmit
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MeetingListArea(
    searchText: String,
    searchQuery: String,
    searchPagingFlowState: StateFlow<Flow<PagingData<MeetingDetailSummary>>?>,
    isSearching: Boolean,
    meetings: List<MeetingUi>,
    onMeetingItemClick: (MeetingUi) -> Unit,
    onMonthChanged: (Int, Int) -> Unit,
    onRefresh: () -> Unit,
    isLoading: Boolean
) {
    val searchPagingFlow by searchPagingFlowState.collectAsState()

    Box(modifier = Modifier.fillMaxWidth()) {
        if (searchQuery.isNotBlank() && searchPagingFlow != null) {
            // collectAsLazyPagingItems()는 항상 같은 composition에서 호출되어야 함
            // 조건부 렌더링 내부에서 호출하지 않고, 별도 컴포저블로 분리
            SearchResultListWrapper(
                modifier = Modifier.fillMaxSize(),
                searchPagingFlow = searchPagingFlow!!,
                isLoading = isSearching,
                onItemClick = onMeetingItemClick
            )
        } else {
            MeetingScheduleView(
                onMeetingItemClick = onMeetingItemClick,
                onMonthChanged = onMonthChanged,
                meetings = meetings,
                onRefresh = onRefresh,
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SearchResultListWrapper(
    searchPagingFlow: Flow<PagingData<MeetingDetailSummary>>,
    isLoading: Boolean,
    onItemClick: (MeetingUi) -> Unit,
    modifier: Modifier = Modifier
) {
    // collectAsLazyPagingItems()는 항상 호출되어야 함 (조건부가 아님)
    val pagingItems: LazyPagingItems<MeetingDetailSummary> =
        searchPagingFlow.collectAsLazyPagingItems()

    SearchResultList(
        modifier = modifier,
        pagingItems = pagingItems,
        isLoading = isLoading,
        onItemClick = onItemClick
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SearchResultList(
    pagingItems: LazyPagingItems<MeetingDetailSummary>,
    isLoading: Boolean,
    onItemClick: (MeetingUi) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { it.id },
            contentType = pagingItems.itemContentType { "meeting" }
        ) { index ->
            val meetingSummary = pagingItems[index] ?: return@items

            val meeting = MeetingUi.from(meetingSummary)

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

            val isLastItem = index == pagingItems.itemCount - 1
            MeetingListItem(
                meeting = meeting,
                onClick = { onItemClick(meeting) },
                backgroundColor = backgroundColor,
                borderColor = borderColor,
                borderWith = borderWith,
                titleColor = titleColor,
                timeColor = timeColor,
                modifier = if (isLastItem) Modifier.padding(bottom = 62.dp) else Modifier
            )
        }
    }
}