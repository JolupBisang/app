package com.imhungry.sillok.presentation.screen.folder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.sillok.presentation.screen.folder.components.MonthPickerDialog
import com.imhungry.sillok.presentation.screen.folder.components.YearPickerDialog
import com.imhungry.sillok.presentation.screen.home.SearchBar
import com.imhungry.sillok.presentation.viewmodel.folder.FolderMeetinngAddViewModel
import com.imhungry.sillok.ui.components.BasicBox
import com.imhungry.sillok.ui.components.ScreenHeader
import com.imhungry.sillok.ui.components.SegmentedControl
import com.imhungry.sillok.ui.components.SillokButton
import com.imhungry.sillok.ui.components.TimeFilterType
import com.imhungry.sillok.ui.theme.border
import com.imhungry.sillok.ui.theme.green300
import com.imhungry.sillok.ui.theme.primaryBackground
import com.imhungry.sillok.ui.theme.primaryTextColor
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FolderMeetingAddScreen(
    folderId: Long,
    onBackClick: () -> Unit,
    onMeetingToggle: (Long, Boolean) -> Unit = { _, _ -> },
    onComplete: (String) -> Unit,
    viewModel: FolderMeetinngAddViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchFocused by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var isEditMode by remember { mutableStateOf(false) }
    var selectedFilterType by remember { mutableStateOf(TimeFilterType.ALL) }
    
    // 선택된 연도/월 상태
    val now = LocalDate.now()
    var selectedYear by remember { mutableStateOf(now.year) }
    var selectedMonth by remember { mutableStateOf(now.monthValue) }
    
    // 다이얼로그 표시 상태
    var showYearPicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    
    // 초기 로드 및 연도/월 변경 시 회의 목록 로드
    LaunchedEffect(Unit) {
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
    
    // 필터링된 회의 목록
    val filteredMeetings = remember(state.meetings, selectedFilterType, selectedYear, selectedMonth, searchText) {
        state.meetings.filter { meeting ->
            // 검색어 필터링
            val matchesSearch = searchText.isBlank() || 
                meeting.title.contains(searchText, ignoreCase = true)
            
            if (!matchesSearch) return@filter false
            
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
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 연도/월 선택 버튼
            when (selectedFilterType) {
                TimeFilterType.YEAR -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { showYearPicker = true }
                                .border(1.dp, border, RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedYear}년",
                                style = MaterialTheme.typography.bodyMedium,
                                color = green300,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                TimeFilterType.MONTH -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { showYearPicker = true }
                                .border(1.dp, border, RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedYear}년",
                                style = MaterialTheme.typography.bodyMedium,
                                color = green300,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Row(
                            modifier = Modifier
                                .clickable { showMonthPicker = true }
                                .border(1.dp, border, RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedMonth}월",
                                style = MaterialTheme.typography.bodyMedium,
                                color = green300,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                TimeFilterType.ALL -> {
                    // 전체 선택 시에는 표시하지 않음
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SearchBar(
                modifier = Modifier
                    .fillMaxWidth(),
                focusRequester = focusRequester,
                text = searchText,
                innerText = "회의 제목으로 검색",
                onTextChange = { searchText = it },
                onFocusChange = { isSearchFocused = it },
                onImeAction = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    isSearchFocused = false
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 회의 리스트
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                items(filteredMeetings) { meeting ->
                    FolderMeetingEditItem(
                        title = meeting.title,
                        date = meeting.date,
                        isSelected = meeting.isSelected,
                        onToggle = {
                            val newSelection = !meeting.isSelected
                            viewModel.toggleMeetingSelection(meeting.id, newSelection)
                            onMeetingToggle(meeting.id, newSelection)
                        }
                    )
                }
            }

            // 하단 버튼
            SillokButton(
                text = "추가하기",
                onClick = { 
                    // TODO: 선택된 회의록 추가 처리
                    onComplete("")
                },
                modifier = Modifier.fillMaxWidth()
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

