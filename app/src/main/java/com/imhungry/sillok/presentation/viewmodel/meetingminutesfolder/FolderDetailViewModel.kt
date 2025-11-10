package com.imhungry.sillok.presentation.viewmodel.meetingminutesfolder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.presentation.screen.meetingminutesfolder.FolderMeetingItem
import com.imhungry.sillok.presentation.state.meetingminutesfolder.FolderDetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderDetailViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(FolderDetailState())
    val state: StateFlow<FolderDetailState> = _state.asStateFlow()

    fun loadFolderDetail(folderId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                // 임시 데이터 (추후 실제 API 호출로 대체)
                val dummyData = getDummyFolderDetail(folderId)
                _state.update {
                    it.copy(
                        folderName = dummyData.folderName,
                        meetings = dummyData.meetings,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "폴더 정보를 불러오는데 실패했습니다."
                    )
                }
            }
        }
    }

    private fun getDummyFolderDetail(folderId: Long): FolderDetailData {
        return when (folderId) {
            1L -> FolderDetailData(
                folderName = "cho비상회의",
                meetings = listOf(
                    FolderMeetingItem(id = 1L, title = "회의 제목", date = "2025.1.24", isSelected = true),
                    FolderMeetingItem(id = 2L, title = "회의 제목", date = "2025.1.25", isSelected = true),
                    FolderMeetingItem(id = 3L, title = "회의 제목", date = "2025.1.26", isSelected = true),
                    FolderMeetingItem(id = 4L, title = "회의 제목", date = "2025.1.27", isSelected = true),
                    FolderMeetingItem(id = 5L, title = "회의 제목", date = "2025.1.28", isSelected = false),
                    FolderMeetingItem(id = 6L, title = "회의 제목", date = "2025.1.29", isSelected = false),
                    FolderMeetingItem(id = 7L, title = "회의 제목", date = "2025.1.30", isSelected = true),
                    FolderMeetingItem(id = 8L, title = "회의 제목", date = "2025.2.1", isSelected = false),
                    FolderMeetingItem(id = 9L, title = "회의 제목", date = "2025.2.2", isSelected = false),
                    FolderMeetingItem(id = 10L, title = "회의 제목", date = "2025.2.3", isSelected = false),
                    FolderMeetingItem(id = 11L, title = "회의 제목", date = "2025.2.4", isSelected = false),
                    FolderMeetingItem(id = 12L, title = "회의 제목", date = "2025.2.5", isSelected = false),
                    FolderMeetingItem(id = 13L, title = "회의 제목", date = "2025.2.6", isSelected = false),
                    FolderMeetingItem(id = 14L, title = "회의 제목", date = "2025.2.7", isSelected = false),
                    FolderMeetingItem(id = 15L, title = "회의 제목", date = "2025.2.8", isSelected = false)
                )
            )
            2L -> FolderDetailData(
                folderName = "개발팀 주간회의",
                meetings = listOf(
                    FolderMeetingItem(id = 21L, title = "주간 회의 1", date = "2025.1.20", isSelected = false),
                    FolderMeetingItem(id = 22L, title = "주간 회의 2", date = "2025.1.27", isSelected = false),
                    FolderMeetingItem(id = 23L, title = "주간 회의 3", date = "2025.2.3", isSelected = false)
                )
            )
            else -> FolderDetailData(
                folderName = "폴더 이름",
                meetings = listOf(
                    FolderMeetingItem(id = 1L, title = "회의 제목", date = "2025.1.24", isSelected = false)
                )
            )
        }
    }

    fun toggleMeetingSelection(meetingId: Long, isSelected: Boolean) {
        _state.update { state ->
            state.copy(
                meetings = state.meetings.map { meeting ->
                    if (meeting.id == meetingId) {
                        meeting.copy(isSelected = isSelected)
                    } else {
                        meeting
                    }
                }
            )
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private data class FolderDetailData(
        val folderName: String,
        val meetings: List<FolderMeetingItem>
    )
}
