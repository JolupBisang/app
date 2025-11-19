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
import com.imhungry.sillok.domain.usecase.team.DeleteTeamUseCase
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
    val getTeamMembersUseCase: GetTeamMembersUseCase,
    private val deleteTeamUseCase: DeleteTeamUseCase
) : ViewModel() {
    companion object {
        private const val TAG = "TeamListViewModel"
    }

    private val _state = MutableStateFlow(TeamListState())
    val state: StateFlow<TeamListState> = _state.asStateFlow()
    
    // 검색어 상태
    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery: StateFlow<String?> = _searchQuery.asStateFlow()

    init {
        loadTeams()
    }
    
    fun updateSearchQuery(query: String?) {
        val searchName = query?.takeIf { it.isNotBlank() }
        _searchQuery.value = searchName
        loadTeams(searchName)
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

    fun loadTeams(name: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                // 큰 size로 모든 데이터를 한 번에 가져오기
                val allTeams = mutableListOf<TeamListItem>()
                var currentPage = 0
                var hasNext = true
                
                while (hasNext && currentPage < 50) { // 최대 50페이지까지만
                    when (val result = getMyTeamsUseCase(name, page = currentPage, size = 1000)) {
                        is ApiResult.Success -> {
                            val (teams, hasMore) = result.data
                            if (teams.isEmpty()) {
                                hasNext = false
                                break
                            }
                            allTeams.addAll(teams)
                            hasNext = hasMore
                            currentPage++
                            if (!hasNext) break
                        }
                        is ApiResult.Failure -> {
                            Log.e(TAG, "팀 목록 로드 실패: ${result.message}")
                            hasNext = false
                            break
                        }
                    }
                }
                
                val teams = convertToTeamDetailSummaries(allTeams)
                Log.d(TAG, "팀 목록 로드 성공: ${teams.size}개")
                _state.update {
                    it.copy(
                        teams = teams,
                        isLoading = false,
                        error = null
                    )
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
    
    fun refresh() {
        val currentQuery = _searchQuery.value
        loadTeams(currentQuery)
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    fun deleteTeams(teamIds: Set<Long>, onSuccess: () -> Unit = {}) {
        if (teamIds.isEmpty()) {
            Log.w(TAG, "삭제할 팀이 선택되지 않았습니다")
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                val failedDeletions = mutableListOf<Long>()
                val currentQuery = _searchQuery.value
                
                // 선택된 모든 팀을 순차적으로 삭제
                teamIds.forEach { teamId ->
                    when (val result = deleteTeamUseCase(teamId)) {
                        is ApiResult.Success -> {
                            Log.d(TAG, "팀 삭제 성공: teamId=$teamId")
                        }
                        is ApiResult.Failure -> {
                            Log.e(TAG, "팀 삭제 실패: teamId=$teamId, error=${result.message}")
                            failedDeletions.add(teamId)
                        }
                    }
                }
                
                // 삭제 결과에 따라 처리
                if (failedDeletions.isEmpty()) {
                    // 모든 삭제 성공
                    Log.d(TAG, "모든 팀 삭제 성공: ${teamIds.size}개")
                    // 목록 새로고침
                    loadTeams(currentQuery)
                    onSuccess()
                } else {
                    // 일부 실패
                    val errorMessage = if (failedDeletions.size == teamIds.size) {
                        "팀 삭제에 실패했습니다."
                    } else {
                        "${teamIds.size - failedDeletions.size}개 팀 삭제 완료, ${failedDeletions.size}개 실패"
                    }
                    Log.w(TAG, errorMessage)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                    // 성공한 항목은 목록에서 제거되도록 새로고침
                    loadTeams(currentQuery)
                }
            } catch (e: Exception) {
                Log.e(TAG, "팀 삭제 예외 발생: ${e.message}", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "팀 삭제 중 오류가 발생했습니다."
                    )
                }
            }
        }
    }
}