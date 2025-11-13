package com.imhungry.sillok.presentation.viewmodel.folder

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.folder.DeleteMeetingFoldersRequest
import com.imhungry.sillok.domain.model.folder.MeetingFolderInfo
import com.imhungry.sillok.domain.model.folder.MeetingMinutesFolderDetailSummary
import com.imhungry.sillok.domain.usecase.folder.DeleteMeetingFoldersUseCase
import com.imhungry.sillok.domain.usecase.folder.GetAllFoldersUseCase
import com.imhungry.sillok.presentation.state.folder.FolderListState
import com.imhungry.sillok.presentation.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class FolderListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getAllFoldersUseCase: GetAllFoldersUseCase,
    private val deleteMeetingFoldersUseCase: DeleteMeetingFoldersUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(FolderListState())
    val state: StateFlow<FolderListState> = _state.asStateFlow()

    companion object {
        private const val TAG = "FolderListViewModel"
    }

    init {
        loadFolders()
    }

    fun loadFolders() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                when (val result = getAllFoldersUseCase()) {
                    is ApiResult.Success -> {
                        val folders = convertToDetailSummary(result.data.folders)
                        _state.update {
                            it.copy(
                                folders = folders,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is ApiResult.Failure -> {
                        Log.e(TAG, "폴더 목록 로드 실패: ${result.message}")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "폴더 목록 로드 예외 발생: ${e.message}", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "회의록 폴더 목록을 불러오는데 실패했습니다."
                    )
                }
            }
        }
    }

    private fun convertToDetailSummary(folders: List<MeetingFolderInfo>): List<MeetingMinutesFolderDetailSummary> {
        return folders.map { folder ->
            val date = if (folder.scheduledStartTime != null) {
                DateTimeUtils.localIsoToDateStringWithoutDayOfWeek(folder.scheduledStartTime)
            } else {
                ""
            }

            val timeRange = if (folder.scheduledStartTime != null && folder.scheduledEndTime != null) {
                val start = DateTimeUtils.localIsoToTimeString(folder.scheduledStartTime)
                val end = DateTimeUtils.localIsoToTimeString(folder.scheduledEndTime)
                "$start~$end"
            } else {
                ""
            }

            val isPast = calculateIsPast(folder.scheduledEndTime)

            MeetingMinutesFolderDetailSummary(
                id = folder.folderId,
                name = folder.folderName,
                meetingName = folder.meetingName,
                date = date,
                timeRange = timeRange,
                isPast = isPast
            )
        }
    }

    private fun calculateIsPast(scheduledEndTime: String?): Boolean {
        if (scheduledEndTime.isNullOrBlank()) return false
        return try {
            val endTime = LocalDateTime.parse(
                scheduledEndTime.take(19),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
            )
            val now = LocalDateTime.now()
            endTime.isBefore(now)
        } catch (e: Exception) {
            Log.e(TAG, "날짜 파싱 실패: $scheduledEndTime", e)
            false
        }
    }

    fun deleteFolders(folderIds: List<Long>, onSuccess: () -> Unit) {
        if (folderIds.isEmpty()) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val request = DeleteMeetingFoldersRequest(folderIds = folderIds)
                when (val result = deleteMeetingFoldersUseCase(request)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "폴더 삭제 성공: ${result.data}")
                        // 삭제 성공 후 폴더 목록 새로고침
                        loadFolders()
                        onSuccess()
                    }
                    is ApiResult.Failure -> {
                        Log.e(TAG, "폴더 삭제 실패: ${result.message}")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "폴더 삭제 예외 발생: ${e.message}", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "폴더 삭제 중 오류가 발생했습니다."
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}