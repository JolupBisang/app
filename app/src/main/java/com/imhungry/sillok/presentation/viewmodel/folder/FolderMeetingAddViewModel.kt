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
import com.imhungry.sillok.presentation.state.folder.FolderMeetingAddState
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.imhungry.sillok.data.paging.MeetingFilterPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderMeetingAddViewModel @Inject constructor(
    private val getMeetingSummaryListUseCase: GetMeetingSummaryListUseCase,
    private val addMeetingsToFolderUseCase: AddMeetingsToFolderUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(FolderMeetingAddState())
    val state: StateFlow<FolderMeetingAddState> = _state.asStateFlow()

    companion object {
        private const val TAG = "FolderMeetingAddViewModel"
    }

    private var currentFolderId: Long = 0L
    
    // 필터링/검색 결과 Paging Flow
    private val _pagingFlow = MutableStateFlow<Flow<PagingData<MeetingDetailSummary>>?>(null)
    val pagingFlow: StateFlow<Flow<PagingData<MeetingDetailSummary>>?> = _pagingFlow.asStateFlow()
    
    // 선택된 회의 ID 목록 (검색/필터 결과와 무관하게 관리)
    private val _selectedMeetingIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedMeetingIds: StateFlow<Set<Long>> = _selectedMeetingIds.asStateFlow()

    fun setFolderId(folderId: Long) {
        currentFolderId = folderId
    }
    
    // 필터링 및 검색 파라미터 업데이트
    fun updateFilters(year: Int?, month: Int?, title: String?) {
        val searchText = title?.takeIf { it.isNotBlank() }
        _state.update { 
            it.copy(
                searchText = searchText ?: "",
                searchQuery = searchText ?: ""
            ) 
        }
        refreshPagingFlow(year, month, searchText)
    }
    
    private fun refreshPagingFlow(year: Int?, month: Int?, title: String?) {
        val pagingFlow = Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                MeetingFilterPagingSource(
                    getMeetingSummaryListUseCase = getMeetingSummaryListUseCase,
                    year = year,
                    month = month,
                    title = title
                )
            }
        ).flow.cachedIn(viewModelScope)
        
        _pagingFlow.value = pagingFlow
    }

    fun toggleMeetingSelection(meetingId: Long, isSelected: Boolean) {
        _selectedMeetingIds.update { currentIds ->
            if (isSelected) {
                currentIds + meetingId
            } else {
                currentIds - meetingId
            }
        }
    }

    fun addMeetings(onSuccess: () -> Unit) {
        val meetingIds = _selectedMeetingIds.value.toList()
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
        _state.update { 
            it.copy(
                searchQuery = query
            ) 
        }
        // 필터 업데이트는 Screen에서 처리
    }

    fun clearSearch() {
        _state.update { 
            it.copy(
                searchText = "",
                searchQuery = ""
            ) 
        }
        // 필터 업데이트는 Screen에서 처리
    }

}