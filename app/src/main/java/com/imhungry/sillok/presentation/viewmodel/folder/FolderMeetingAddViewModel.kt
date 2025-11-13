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
import com.imhungry.sillok.presentation.screen.folder.FolderMeetingItem
import com.imhungry.sillok.presentation.state.folder.FolderMeetingAddState
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val addMeetingsToFolderUseCase: AddMeetingsToFolderUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(FolderMeetingAddState())
    val state: StateFlow<FolderMeetingAddState> = _state.asStateFlow()

    companion object {
        private const val TAG = "FolderMeetingAddViewModel"
    }

    private var currentFolderId: Long = 0L

    fun setFolderId(folderId: Long) {
        currentFolderId = folderId
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadMeetings(year: Int, month: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            when (val result = getMeetingSummaryListUseCase(year, month)) {
                is ApiResult.Success -> {
                    val meetings = result.data.map { summary ->
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertToFolderMeetingItem(summary: MeetingDetailSummary): FolderMeetingItem {
        val dateString = try {
            val dateTime = LocalDateTime.parse(
                summary.scheduledStartTime.take(19),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
            )
            dateTime.format(DateTimeFormatter.ofPattern("yyyy.M.d"))
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