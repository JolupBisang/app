package com.imhungry.sillok.presentation.viewmodel.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.presentation.state.team.TeamDetailState
import com.imhungry.sillok.presentation.state.team.TeamMember
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamDetailViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(TeamDetailState())
    val state: StateFlow<TeamDetailState> = _state.asStateFlow()

    fun loadTeamDetail(teamId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                // 임시 데이터 (추후 실제 API 호출로 대체)
                val dummyData = getDummyTeamDetail(teamId)
                _state.update {
                    it.copy(
                        teamName = dummyData.teamName,
                        teamDescription = dummyData.teamDescription,
                        members = dummyData.members,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "팀 정보를 불러오는데 실패했습니다."
                    )
                }
            }
        }
    }

    private fun getDummyTeamDetail(teamId: Long): TeamDetailData {
        return when (teamId) {
            1L -> TeamDetailData(
                teamName = "cho비상회의",
                teamDescription = "긴급 상황 대응을 위한 비상 회의 팀입니다.",
                members = listOf(
                    TeamMember(id = 1L, nickname = "김철수"),
                    TeamMember(id = 2L, nickname = "이영희"),
                    TeamMember(id = 3L, nickname = "박민수"),
                    TeamMember(id = 4L, nickname = "최지은"),
                    TeamMember(id = 5L, nickname = "정동욱"),
                    TeamMember(id = 6L, nickname = "한소영"),
                    TeamMember(id = 7L, nickname = "강태현"),
                    TeamMember(id = 8L, nickname = "윤서연"),
                    TeamMember(id = 9L, nickname = "임준호"),
                    TeamMember(id = 10L, nickname = "조수진")
                )
            )
            2L -> TeamDetailData(
                teamName = "개발팀 주간회의",
                teamDescription = "개발팀의 주간 진행 상황을 공유하는 회의입니다.",
                members = listOf(
                    TeamMember(id = 11L, nickname = "개발자1"),
                    TeamMember(id = 12L, nickname = "개발자2"),
                    TeamMember(id = 13L, nickname = "개발자3"),
                    TeamMember(id = 14L, nickname = "개발자4"),
                    TeamMember(id = 15L, nickname = "개발자5"),
                    TeamMember(id = 16L, nickname = "개발자6"),
                    TeamMember(id = 17L, nickname = "개발자7"),
                    TeamMember(id = 18L, nickname = "개발자8")
                )
            )
            else -> TeamDetailData(
                teamName = "팀 이름",
                teamDescription = "팀 설명",
                members = listOf(
                    TeamMember(id = 1L, nickname = "멤버1"),
                    TeamMember(id = 2L, nickname = "멤버2"),
                    TeamMember(id = 3L, nickname = "멤버3")
                )
            )
        }
    }

    fun removeMember(memberId: Long) {
        _state.update { state ->
            state.copy(
                members = state.members.filter { it.id != memberId }
            )
        }
    }

    fun updateTeamDescription(description: String) {
        _state.update { state ->
            state.copy(teamDescription = description)
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private data class TeamDetailData(
        val teamName: String,
        val teamDescription: String,
        val members: List<TeamMember>
    )
}
