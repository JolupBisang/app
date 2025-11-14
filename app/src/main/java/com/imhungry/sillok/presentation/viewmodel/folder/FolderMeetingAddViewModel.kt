package com.imhungry.sillok.presentation.viewmodel.folder

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.folder.AddMeetingsToFolderRequest
import com.imhungry.sillok.domain.model.meeting.MeetingDetailSummary
import com.imhungry.sillok.domain.usecase.folder.AddMeetingsToFolderUseCase
import com.imhungry.sillok.domain.usecase.meeting.GetMeetingSummaryListUseCase
import com.imhungry.sillok.domain.usecase.meeting.SearchMeetingsUseCase
import com.imhungry.sillok.presentation.screen.folder.FolderMeetingItem
import com.imhungry.sillok.presentation.state.folder.FolderMeetingAddState
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.imhungry.sillok.data.paging.MeetingPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class FolderMeetingAddViewModel @Inject constructor(
    private val getMeetingSummaryListUseCase: GetMeetingSummaryListUseCase,
    private val searchMeetingsUseCase: SearchMeetingsUseCase,
    private val addMeetingsToFolderUseCase: AddMeetingsToFolderUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(FolderMeetingAddState())
    val state: StateFlow<FolderMeetingAddState> = _state.asStateFlow()

    companion object {
        private const val TAG = "FolderMeetingAddViewModel"
    }

    private var currentFolderId: Long = 0L
    
    // 검색 결과 Paging Flow
    private val _searchPagingFlow = MutableStateFlow<Flow<PagingData<MeetingDetailSummary>>?>(null)
    val searchPagingFlow: StateFlow<Flow<PagingData<MeetingDetailSummary>>?> = _searchPagingFlow.asStateFlow()

    fun setFolderId(folderId: Long) {
        currentFolderId = folderId
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadMeetings(year: Int, month: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            when (val result = getMeetingSummaryListUseCase(year, month)) {
                is ApiResult.Success -> {
                    // COMPLETED 상태인 회의만 필터링
                    val completedMeetings = result.data.filter { summary ->
                        summary.status == "COMPLETED"
                    }
                    val meetings = completedMeetings.map { summary ->
                        convertToFolderMeetingItem(summary)
                    }
                    _state.update {
                        it.copy(
                            meetings = meetings,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is ApiResult.Failure -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun toggleMeetingSelection(meetingId: Long, isSelected: Boolean) {
        _state.update { state ->
            val existingMeeting = state.meetings.find { it.id == meetingId }
            if (existingMeeting != null) {
                // 기존 회의의 선택 상태만 업데이트
                state.copy(
                    meetings = state.meetings.map { meeting ->
                        if (meeting.id == meetingId) {
                            meeting.copy(isSelected = isSelected)
                        } else {
                            meeting
                        }
                    }
                )
            } else {
                // 검색 결과에서 선택한 회의는 state.meetings에 없으므로 그냥 무시
                // (검색 결과에서 선택한 회의는 별도로 처리하지 않음)
                state
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toggleMeetingSelectionFromSearch(meetingSummary: MeetingDetailSummary, isSelected: Boolean) {
        _state.update { state ->
            val existingMeeting = state.meetings.find { it.id == meetingSummary.id }
            if (existingMeeting != null) {
                // 기존 회의의 선택 상태만 업데이트
                state.copy(
                    meetings = state.meetings.map { meeting ->
                        if (meeting.id == meetingSummary.id) {
                            meeting.copy(isSelected = isSelected)
                        } else {
                            meeting
                        }
                    }
                )
            } else {
                // 검색 결과에서 선택한 회의를 state.meetings에 추가
                val newMeeting = convertToFolderMeetingItem(meetingSummary).copy(isSelected = isSelected)
                state.copy(
                    meetings = state.meetings + newMeeting
                )
            }
        }
    }

    fun addMeetings(meetingIds: List<Long>, onSuccess: () -> Unit) {
        if (meetingIds.isEmpty() || currentFolderId == 0L) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val request = AddMeetingsToFolderRequest(
                    folderId = currentFolderId,
                    meetingIds = meetingIds
                )
                when (val result = addMeetingsToFolderUseCase(request)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "회의 추가 성공: ${result.data}")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = null
                            )
                        }
                        onSuccess()
                    }
                    is ApiResult.Failure -> {
                        Log.e(TAG, "회의 추가 실패: ${result.message}")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "회의 추가 예외 발생: ${e.message}", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "회의 추가 중 오류가 발생했습니다."
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    // ========================================
    // 검색 기능
    // ========================================

    fun onSearchTextChange(text: String) {
        _state.update { it.copy(searchText = text) }
    }
    
    fun onSearchSubmit() {
        val query = _state.value.searchText.trim()
        if (query.isBlank()) {
            _state.update { 
                it.copy(
                    searchQuery = "",
                    isSearching = false
                ) 
            }
            _searchPagingFlow.value = null
            return
        }
        performSearch(query)
    }

    fun clearSearch() {
        _state.update { 
            it.copy(
                searchText = "",
                searchQuery = "",
                isSearching = false
            ) 
        }
        _searchPagingFlow.value = null
    }

    private fun performSearch(query: String) {
        _state.update { 
            it.copy(
                searchQuery = query,
                isSearching = true,
                isLoading = true
            ) 
        }
        
        // Paging Flow 생성
        val pagingFlow = Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                MeetingPagingSource(
                    searchMeetingsUseCase = searchMeetingsUseCase,
                    query = query
                )
            }
        ).flow.cachedIn(viewModelScope)
        
        _searchPagingFlow.value = pagingFlow
        _state.update { 
            it.copy(
                isSearching = false,
                isLoading = false
            ) 
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertToFolderMeetingItem(summary: MeetingDetailSummary): FolderMeetingItem {
        val dateString = try {
            val dateTime = LocalDateTime.parse(
                summary.scheduledStartTime.take(19),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
            )
            dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.d"))
        } catch (e: Exception) {
            ""
        }

        return FolderMeetingItem(
            id = summary.id,
            title = summary.title,
            date = dateString,
            isSelected = false
        )
    }
}