package com.imhungry.sillok.presentation.screen.folder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.sillok.domain.model.meeting.MeetingDetailSummary
import com.imhungry.sillok.presentation.screen.folder.components.MonthPickerDialog
import com.imhungry.sillok.presentation.screen.folder.components.YearPickerDialog
import com.imhungry.sillok.presentation.screen.home.SearchBar
import com.imhungry.sillok.presentation.viewmodel.folder.FolderMeetingAddViewModel
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.ui.components.ScreenHeader
import com.imhungry.sillok.ui.components.SegmentedControl
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.components.TimeFilterType
import com.imhungry.sillok.ui.theme.primaryBackground
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FolderMeetingAddScreen(
    folderId: Long,
    onBackClick: () -> Unit,
    onMeetingToggle: (Long, Boolean) -> Unit = { _, _ -> },
    onComplete: (String) -> Unit,
    viewModel: FolderMeetingAddViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchFocused by remember { mutableStateOf(false) }
    var selectedFilterType by remember { mutableStateOf(TimeFilterType.ALL) }
    
    // 선택된 연도/월 상태
    val now = LocalDate.now()
    var selectedYear by remember { mutableStateOf(now.year) }
    var selectedMonth by remember { mutableStateOf(now.monthValue) }
    
    // 다이얼로그 표시 상태
    var showYearPicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    
    // 폴더 ID 설정 및 초기 로드
    LaunchedEffect(folderId) {
        viewModel.setFolderId(folderId)
        viewModel.loadMeetings(selectedYear, selectedMonth)
    }
    
    LaunchedEffect(selectedYear, selectedMonth, selectedFilterType) {
        when (selectedFilterType) {
            TimeFilterType.YEAR -> {
                // 연도 선택 시 해당 연도의 모든 회의를 불러오기 위해 1월로 설정
                // 실제로는 12개월 모두 호출해야 하지만, 일단 1월로 호출하고 클라이언트에서 필터링
                viewModel.loadMeetings(selectedYear, 1)
            }
            TimeFilterType.MONTH -> {
                // 월 선택 시 해당 연도/월의 회의 불러오기
                viewModel.loadMeetings(selectedYear, selectedMonth)
            }
            TimeFilterType.ALL -> {
                // 전체 선택 시 현재 연도/월로 불러오기
                viewModel.loadMeetings(selectedYear, selectedMonth)
            }
        }
    }
    
    // 필터링된 회의 목록 (검색이 비활성화되어 있을 때만 사용)
    val filteredMeetings = remember(state.meetings, selectedFilterType, selectedYear, selectedMonth, state.searchQuery) {
        if (state.searchQuery.isNotBlank()) {
            // 검색이 활성화되어 있으면 빈 리스트 반환 (검색 결과를 별도로 표시)
            emptyList()
        } else {
            state.meetings.filter { meeting ->
                // 날짜 필터링
                when (selectedFilterType) {
                    TimeFilterType.YEAR -> {
                        // 선택된 연도의 회의만 표시
                        try {
                            val dateFormatter = DateTimeFormatter.ofPattern("yyyy.M.d")
                            val meetingDate = LocalDate.parse(meeting.date, dateFormatter)
                            meetingDate.year == selectedYear
                        } catch (e: Exception) {
                            false
                        }
                    }
                    TimeFilterType.MONTH -> {
                        // 선택된 연도/월의 회의만 표시
                        try {
                            val dateFormatter = DateTimeFormatter.ofPattern("yyyy.M.d")
                            val meetingDate = LocalDate.parse(meeting.date, dateFormatter)
                            meetingDate.year == selectedYear && meetingDate.monthValue == selectedMonth
                        } catch (e: Exception) {
                            false
                        }
                    }
                    TimeFilterType.ALL -> {
                        // 전체 회의 표시
                        true
                    }
                }
            }
        }
    }

    // 검색 상태에서 뒤로가기 처리
    BackHandler(enabled = state.searchQuery.isNotBlank() || state.searchText.isNotBlank()) {
        focusManager.clearFocus()
        keyboardController?.hide()
        isSearchFocused = false
        viewModel.clearSearch()
    }

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
                title = "회의 추가",
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(28.dp))

            SegmentedControl(
                selectedType = selectedFilterType,
                onTypeSelected = { selectedFilterType = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                selectedYear = if (selectedFilterType == TimeFilterType.YEAR || selectedFilterType == TimeFilterType.MONTH) selectedYear else null,
                selectedMonth = if (selectedFilterType == TimeFilterType.MONTH) selectedMonth else null,
                onYearClick = {
                    selectedFilterType = TimeFilterType.YEAR
                    showYearPicker = true
                },
                onMonthClick = {
                    selectedFilterType = TimeFilterType.MONTH
                    showMonthPicker = true
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SearchBar(
                modifier = Modifier
                    .fillMaxWidth(),
                focusRequester = focusRequester,
                text = state.searchText,
                innerText = "회의 제목으로 검색",
                onTextChange = { viewModel.onSearchTextChange(it) },
                onFocusChange = { isSearchFocused = it },
                onImeAction = {
                    viewModel.onSearchSubmit()
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    isSearchFocused = false
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 회의 리스트 (검색 결과 또는 필터링된 목록)
            MeetingListArea(
                searchQuery = state.searchQuery,
                searchPagingFlowState = viewModel.searchPagingFlow,
                isSearching = state.isSearching,
                filteredMeetings = filteredMeetings,
                selectedMeetingIds = state.meetings.filter { it.isSelected }.map { it.id }.toSet(),
                onMeetingToggle = { meetingId, isSelected ->
                    viewModel.toggleMeetingSelection(meetingId, isSelected)
                    onMeetingToggle(meetingId, isSelected)
                },
                onMeetingToggleFromSearch = { meetingSummary, isSelected ->
                    viewModel.toggleMeetingSelectionFromSearch(meetingSummary, isSelected)
                    onMeetingToggle(meetingSummary.id, isSelected)
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
            Spacer(Modifier.height(16.dp))

            // 하단 버튼
            SillokButton(
                text = "추가하기",
                onClick = { 
                    val selectedMeetingIds = state.meetings
                        .filter { it.isSelected }
                        .map { it.id }
                    if (selectedMeetingIds.isNotEmpty()) {
                        viewModel.addMeetings(
                            meetingIds = selectedMeetingIds,
                            onSuccess = {
                                onComplete("")
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.meetings.any { it.isSelected } && !state.isLoading
            )
        }
    }

    // 연도 선택 다이얼로그
    YearPickerDialog(
        visible = showYearPicker,
        selectedYear = selectedYear,
        onYearSelected = { year ->
            selectedYear = year
            showYearPicker = false
        },
        onDismiss = { showYearPicker = false }
    )

    // 월 선택 다이얼로그
    MonthPickerDialog(
        visible = showMonthPicker,
        selectedMonth = selectedMonth,
        onMonthSelected = { month ->
            selectedMonth = month
            showMonthPicker = false
        },
        onDismiss = { showMonthPicker = false }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MeetingListArea(
    searchQuery: String,
    searchPagingFlowState: StateFlow<Flow<PagingData<MeetingDetailSummary>>?>,
    isSearching: Boolean,
    filteredMeetings: List<FolderMeetingItem>,
    selectedMeetingIds: Set<Long>,
    onMeetingToggle: (Long, Boolean) -> Unit,
    onMeetingToggleFromSearch: (MeetingDetailSummary, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchPagingFlow by searchPagingFlowState.collectAsState()

    Box(modifier = modifier.fillMaxWidth()) {
        if (searchQuery.isNotBlank() && searchPagingFlow != null) {
            // 검색 결과 표시
            SearchResultListWrapper(
                modifier = Modifier.fillMaxSize(),
                searchPagingFlow = searchPagingFlow!!,
                isLoading = isSearching,
                selectedMeetingIds = selectedMeetingIds,
                onMeetingToggleFromSearch = onMeetingToggleFromSearch
            )
        } else {
            // 필터링된 회의 목록 표시
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredMeetings) { meeting ->
                    FolderMeetingEditItem(
                        title = meeting.title,
                        date = meeting.date,
                        isSelected = meeting.isSelected,
                        onToggle = {
                            val newSelection = !meeting.isSelected
                            onMeetingToggle(meeting.id, newSelection)
                        }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SearchResultListWrapper(
    searchPagingFlow: Flow<PagingData<MeetingDetailSummary>>,
    isLoading: Boolean,
    selectedMeetingIds: Set<Long>,
    onMeetingToggleFromSearch: (MeetingDetailSummary, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagingItems: LazyPagingItems<MeetingDetailSummary> =
        searchPagingFlow.collectAsLazyPagingItems()

    SearchResultList(
        modifier = modifier,
        pagingItems = pagingItems,
        isLoading = isLoading,
        selectedMeetingIds = selectedMeetingIds,
        onMeetingToggleFromSearch = onMeetingToggleFromSearch
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SearchResultList(
    pagingItems: LazyPagingItems<MeetingDetailSummary>,
    isLoading: Boolean,
    selectedMeetingIds: Set<Long>,
    onMeetingToggleFromSearch: (MeetingDetailSummary, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { it.id },
            contentType = pagingItems.itemContentType { "meeting" }
        ) { index ->
            val meetingSummary = pagingItems[index] ?: return@items
            
            // COMPLETED 상태인 회의만 표시
            if (meetingSummary.status != "COMPLETED") {
                return@items
            }

            // MeetingDetailSummary를 FolderMeetingItem으로 변환
            val dateString = try {
                val dateTime = LocalDateTime.parse(
                    meetingSummary.scheduledStartTime.take(19),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                )
                dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.d"))
            } catch (e: Exception) {
                ""
            }

            val isSelected = selectedMeetingIds.contains(meetingSummary.id)

            FolderMeetingEditItem(
                title = meetingSummary.title,
                date = dateString,
                isSelected = isSelected,
                onToggle = {
                    val newSelection = !isSelected
                    onMeetingToggleFromSearch(meetingSummary, newSelection)
                }
            )
        }
    }
}

