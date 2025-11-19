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
    folderName: String = "",
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
    
    // 폴더 ID 설정
    LaunchedEffect(folderId) {
        viewModel.setFolderId(folderId)
    }
    
    // 년도만 선택했을 때 현재 월로 자동 업데이트
    LaunchedEffect(selectedFilterType, selectedYear) {
        if (selectedFilterType == TimeFilterType.YEAR && selectedYear == now.year) {
            selectedMonth = now.monthValue
        }
    }
    
    // 필터 및 검색 파라미터 계산
    val filterYear = remember(selectedFilterType, selectedYear) {
        when (selectedFilterType) {
            TimeFilterType.YEAR, TimeFilterType.MONTH -> selectedYear
            TimeFilterType.ALL -> null
        }
    }
    
    val filterMonth = remember(selectedFilterType, selectedMonth) {
        when (selectedFilterType) {
            TimeFilterType.MONTH -> selectedMonth
            TimeFilterType.YEAR -> {
                // 년도만 선택하면 현재 월로 자동 설정
                now.monthValue
            }
            TimeFilterType.ALL -> null
        }
    }
    
    val searchTitle = remember(state.searchQuery) {
        state.searchQuery.takeIf { it.isNotBlank() }
    }
    
    // 필터/검색 변경 시 Paging Flow 업데이트
    LaunchedEffect(filterYear, filterMonth, searchTitle) {
        viewModel.updateFilters(filterYear, filterMonth, searchTitle)
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
                title = folderName.ifEmpty { "회의 추가" },
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
                selectedMonth = if (selectedFilterType == TimeFilterType.MONTH) {
                    selectedMonth
                } else if (selectedFilterType == TimeFilterType.YEAR) {
                    // 년도만 선택했을 때도 현재 월 표시
                    if (selectedYear == now.year) now.monthValue else selectedMonth
                } else {
                    null
                },
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

            // 회의 리스트 (Paging 사용)
            MeetingListArea(
                pagingFlowState = viewModel.pagingFlow,
                selectedMeetingIds = viewModel.selectedMeetingIds,
                onMeetingToggle = { meetingId, isSelected ->
                    viewModel.toggleMeetingSelection(meetingId, isSelected)
                    onMeetingToggle(meetingId, isSelected)
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
            Spacer(Modifier.height(16.dp))

            // 하단 버튼
            val selectedMeetingIds by viewModel.selectedMeetingIds.collectAsState()
            SillokButton(
                text = "추가하기",
                onClick = { 
                    viewModel.addMeetings(
                        onSuccess = {
                            onComplete("")
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedMeetingIds.isNotEmpty() && !state.isLoading
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
    pagingFlowState: StateFlow<Flow<PagingData<MeetingDetailSummary>>?>,
    selectedMeetingIds: StateFlow<Set<Long>>,
    onMeetingToggle: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagingFlow by pagingFlowState.collectAsState()
    val selectedIds by selectedMeetingIds.collectAsState()

    if (pagingFlow != null) {
        val pagingItems: LazyPagingItems<MeetingDetailSummary> =
            pagingFlow!!.collectAsLazyPagingItems()

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

                val isSelected = selectedIds.contains(meetingSummary.id)

                FolderMeetingEditItem(
                    title = meetingSummary.title,
                    date = dateString,
                    isSelected = isSelected,
                    onToggle = {
                        val newSelection = !isSelected
                        onMeetingToggle(meetingSummary.id, newSelection)
                    }
                )
            }
        }
    }
}

