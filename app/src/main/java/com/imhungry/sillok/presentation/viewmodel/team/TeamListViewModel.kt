package com.imhungry.sillok.presentation.viewmodel.team

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.domain.model.team.TeamDetailSummary
import com.imhungry.sillok.presentation.state.team.TeamListState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamListViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(TeamListState())
    val state: StateFlow<TeamListState> = _state.asStateFlow()
    
    init {
        loadTeams()
    }
    
    fun loadTeams() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                // 임시 데이터 (추후 실제 API 호출로 대체)
                val dummyTeams = getDummyTeams()
                _state.update { 
                    it.copy(
                        teams = dummyTeams,
                        isLoading = false,
                        error = null
                    ) 
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "팀 목록을 불러오는데 실패했습니다."
                    ) 
                }
            }
        }
    }
    
    private fun getDummyTeams(): List<TeamDetailSummary> {
        return listOf(
            TeamDetailSummary(
                id = 1L,
                name = "cho비상회의",
                memberCount = 4,
                date = "2025.04.10",
                timeRange = "18:00~20:00"
            ),
            TeamDetailSummary(
                id = 2L,
                name = "개발팀 주간회의",
                memberCount = 8,
                date = "2025.04.11",
                timeRange = "14:00~16:00"
            ),
            TeamDetailSummary(
                id = 3L,
                name = "기획팀 회의",
                memberCount = 6,
                date = "2025.04.12",
                timeRange = "10:00~12:00"
            ),
            TeamDetailSummary(
                id = 4L,
                name = "디자인 리뷰",
                memberCount = 5,
                date = "2025.04.13",
                timeRange = "15:00~17:00"
            )
        )
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}