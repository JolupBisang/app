package com.imhungry.sillok.presentation.viewmodel.team

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.team.TeamDetailSummary
import com.imhungry.sillok.domain.model.team.TeamListItem
import com.imhungry.sillok.domain.usecase.team.GetMyTeamsUseCase
import com.imhungry.sillok.domain.usecase.team.GetTeamMembersUseCase
import com.imhungry.sillok.presentation.state.team.TeamListState
import com.imhungry.sillok.presentation.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
class TeamListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getMyTeamsUseCase: GetMyTeamsUseCase,
    private val getTeamMembersUseCase: GetTeamMembersUseCase
) : ViewModel() {
    companion object {
        private const val TAG = "TeamListViewModel"
    }

    private val _state = MutableStateFlow(TeamListState())
    val state: StateFlow<TeamListState> = _state.asStateFlow()

    init {
        loadTeams()
    }

    fun loadTeams() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                when (val result = getMyTeamsUseCase()) {
                    is ApiResult.Success -> {
                        val teams = convertToTeamDetailSummaries(result.data)
                        Log.d(TAG, "팀 목록 로드 성공: ${teams.size}개")
                        _state.update {
                            it.copy(
                                teams = teams,
                                isLoading = false,
                                error = null
                            )
                        }
                    }

                    is ApiResult.Failure -> {
                        Log.e(TAG, "팀 목록 로드 실패: ${result.message}")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "팀 목록 로드 예외 발생: ${e.message}", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "팀 목록을 불러오는데 실패했습니다."
                    )
                }
            }
        }
    }

    private suspend fun convertToTeamDetailSummaries(teamListItems: List<TeamListItem>): List<TeamDetailSummary> = coroutineScope {
        val now = LocalDateTime.now()
        
        // 각 팀의 멤버 수를 병렬로 가져오기
        val memberCounts = teamListItems.map { item ->
            async {
                when (val result = getTeamMembersUseCase(item.teamId)) {
                    is ApiResult.Success -> result.data.size
                    is ApiResult.Failure -> {
                        Log.e(TAG, "팀 ${item.teamId} 멤버 조회 실패: ${result.message}")
                        0
                    }
                }
            }
        }.map { it.await() }
        
        // 팀 정보와 멤버 수를 결합
        teamListItems.mapIndexed { index, item ->
            val date = item.scheduledStartTime?.let { DateTimeUtils.localIsoToDateString(it) }
            val startTime = item.scheduledStartTime?.let { DateTimeUtils.localIsoToTimeString(it) }
            val endTime = item.scheduledEndTime?.let { DateTimeUtils.localIsoToTimeString(it) }
            val timeRange = if (startTime != null && endTime != null) {
                "$startTime~$endTime"
            } else null
            
            // scheduledStartTime이 지났는지 확인
            val isPast = item.scheduledStartTime?.let { scheduledStartTime ->
                try {
                    val scheduledStart = LocalDateTime.parse(
                        scheduledStartTime,
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    )
                    scheduledStart.isBefore(now)
                } catch (e: Exception) {
                    Log.e(TAG, "시간 파싱 실패: $scheduledStartTime", e)
                    false
                }
            } ?: false
            
            TeamDetailSummary(
                id = item.teamId,
                name = item.teamName,
                memberCount = memberCounts[index],
                date = date,
                timeRange = timeRange,
                meetingName = item.meetingName,
                isPast = isPast
            )
        }
    }

    fun refresh() {
        loadTeams()
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}