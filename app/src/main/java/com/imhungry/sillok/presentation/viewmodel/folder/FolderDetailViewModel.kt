package com.imhungry.sillok.presentation.viewmodel.folder

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.folder.FolderMeetingInfo
import com.imhungry.sillok.domain.model.folder.RemoveMeetingsFromFolderRequest
import com.imhungry.sillok.domain.usecase.folder.GetFolderMeetingsUseCase
import com.imhungry.sillok.domain.usecase.folder.RemoveMeetingsFromFolderUseCase
import com.imhungry.sillok.presentation.screen.folder.FolderMeetingItem
import com.imhungry.sillok.presentation.state.folder.FolderDetailState
import com.imhungry.sillok.presentation.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class FolderDetailViewModel @Inject constructor(
    private val getFolderMeetingsUseCase: GetFolderMeetingsUseCase,
    private val removeMeetingsFromFolderUseCase: RemoveMeetingsFromFolderUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(FolderDetailState())
    val state: StateFlow<FolderDetailState> = _state.asStateFlow()

    companion object {
        private const val TAG = "FolderDetailViewModel"
    }

    private var currentFolderId: Long = 0L
    private var currentFolderName: String = ""

    fun loadFolderDetail(folderId: Long, folderName: String = "") {
        currentFolderId = folderId
        currentFolderName = folderName
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                when (val result = getFolderMeetingsUseCase(folderId)) {
                    is ApiResult.Success -> {
                        val meetings = convertToFolderMeetingItems(result.data.meetings)
                        _state.update {
                            it.copy(
                                folderName = folderName.ifEmpty { "폴더 이름" },
                                meetings = meetings,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is ApiResult.Failure -> {
                        Log.e(TAG, "폴더 회의 목록 로드 실패: ${result.message}")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "폴더 회의 목록 로드 예외 발생: ${e.message}", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "폴더 정보를 불러오는데 실패했습니다."
                    )
                }
            }
        }
    }

    private fun convertToFolderMeetingItems(meetings: List<FolderMeetingInfo>): List<FolderMeetingItem> {
        return meetings.map { meeting ->
            val date = DateTimeUtils.localIsoToDateStringWithoutDayOfWeek(meeting.scheduledStartTime)
            FolderMeetingItem(
                id = meeting.meetingId,
                title = meeting.title,
                date = date,
                isSelected = false
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

    fun removeMeetings(meetingIds: List<Long>, onSuccess: () -> Unit) {
        if (meetingIds.isEmpty() || currentFolderId == 0L) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val request = RemoveMeetingsFromFolderRequest(
                    folderId = currentFolderId,
                    meetingIds = meetingIds
                )
                when (val result = removeMeetingsFromFolderUseCase(request)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "회의 삭제 성공: ${result.data}")
                        // 삭제 성공 후 회의 목록 새로고침
                        loadFolderDetail(currentFolderId, currentFolderName)
                        onSuccess()
                    }
                    is ApiResult.Failure -> {
                        Log.e(TAG, "회의 삭제 실패: ${result.message}")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "회의 삭제 예외 발생: ${e.message}", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "회의 삭제 중 오류가 발생했습니다."
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
