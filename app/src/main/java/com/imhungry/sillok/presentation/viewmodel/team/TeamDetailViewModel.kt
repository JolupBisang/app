package com.imhungry.sillok.presentation.viewmodel.team

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.data.local.TeamDescriptionStore
import com.imhungry.sillok.data.local.UserStore
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.team.AddTeamMemberRequest
import com.imhungry.sillok.domain.usecase.team.AddTeamMemberUseCase
import com.imhungry.sillok.domain.usecase.team.GetTeamDetailUseCase
import com.imhungry.sillok.domain.usecase.team.GetTeamMembersUseCase
import com.imhungry.sillok.presentation.state.team.TeamDetailState
import com.imhungry.sillok.presentation.state.team.TeamMember
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamDetailViewModel @Inject constructor(
    private val getTeamDetailUseCase: GetTeamDetailUseCase,
    private val getTeamMembersUseCase: GetTeamMembersUseCase,
    private val addTeamMemberUseCase: AddTeamMemberUseCase,
    private val teamDescriptionStore: TeamDescriptionStore,
    private val userStore: UserStore
) : ViewModel() {
    companion object {
        private const val TAG = "TeamDetailViewModel"
    }

    private val _state = MutableStateFlow(TeamDetailState())
    val state: StateFlow<TeamDetailState> = _state.asStateFlow()

    private var currentTeamId: Long? = null

    fun loadTeamDetail(teamId: Long) {
        currentTeamId = teamId
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                // 현재 사용자 ID 가져오기
                val currentUser = userStore.user.first()
                val currentUserId = currentUser?.id

                // DataStore에서 한줄소개 가져오기
                val savedDescription = teamDescriptionStore.getTeamDescription(teamId) ?: ""

                // 팀 상세 정보 가져오기
                when (val teamResult = getTeamDetailUseCase(teamId)) {
                    is ApiResult.Success -> {
                        val team = teamResult.data
                        Log.d(TAG, "팀 상세 정보 로드 성공: teamId=$teamId, teamName=${team.teamName}")

                        // 팀 멤버 목록 가져오기
                        when (val membersResult = getTeamMembersUseCase(teamId)) {
                            is ApiResult.Success -> {
                                val domainMembers = membersResult.data
                                val stateMembers = domainMembers.map { domainMember ->
                                    TeamMember(
                                        id = domainMember.id,
                                        nickname = domainMember.name,
                                        email = domainMember.email,
                                        profileImage = domainMember.pictureURL.takeIf { it.isNotBlank() }
                                    )
                                }
                                Log.d(TAG, "팀 멤버 목록 로드 성공: ${stateMembers.size}명")

                                _state.update {
                                    it.copy(
                                        teamName = team.teamName,
                                        teamDescription = savedDescription,
                                        members = stateMembers,
                                        currentUserId = currentUserId,
                                        isLoading = false,
                                        error = null
                                    )
                                }
                            }

                            is ApiResult.Failure -> {
                                Log.e(TAG, "팀 멤버 목록 로드 실패: ${membersResult.message}")
                                _state.update {
                                    it.copy(
                                        teamName = team.teamName,
                                        teamDescription = savedDescription,
                                        members = emptyList(),
                                        currentUserId = currentUserId,
                                        isLoading = false,
                                        error = membersResult.message
                                    )
                                }
                            }
                        }
                    }

                    is ApiResult.Failure -> {
                        Log.e(TAG, "팀 상세 정보 로드 실패: ${teamResult.message}")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = teamResult.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "팀 정보 로드 예외 발생: ${e.message}", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "팀 정보를 불러오는데 실패했습니다."
                    )
                }
            }
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
        val teamId = currentTeamId
        if (teamId != null) {
            // 상태를 먼저 업데이트하여 즉시 UI 반영
            _state.update { state ->
                state.copy(teamDescription = description)
            }
            // DataStore 저장은 백그라운드에서 처리
            viewModelScope.launch {
                try {
                    teamDescriptionStore.saveTeamDescription(teamId, description)
                    Log.d(TAG, "팀 한줄소개 저장 성공: teamId=$teamId")
                } catch (e: Exception) {
                    Log.e(TAG, "팀 한줄소개 저장 실패: ${e.message}", e)
                }
            }
        }
    }

    fun addTeamMember(email: String, onResult: (Boolean, String?) -> Unit) {
        val teamId = currentTeamId
        if (teamId == null) {
            onResult(false, "팀 정보를 불러오지 못했습니다.")
            return
        }

        viewModelScope.launch {
            try {
                val request = AddTeamMemberRequest(memberEmail = email)
                when (val result = addTeamMemberUseCase(teamId, request)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "팀 멤버 추가 성공: teamId=$teamId, email=$email")
                        // 멤버 목록 새로고침
                        loadTeamDetail(teamId)
                        onResult(true, null)
                    }
                    is ApiResult.Failure -> {
                        Log.e(TAG, "팀 멤버 추가 실패: ${result.message}")
                        onResult(false, result.message)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "팀 멤버 추가 예외 발생: ${e.message}", e)
                onResult(false, e.localizedMessage ?: "멤버 추가 중 오류가 발생했습니다.")
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
