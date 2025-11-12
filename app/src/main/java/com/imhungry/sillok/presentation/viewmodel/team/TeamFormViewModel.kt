package com.imhungry.sillok.presentation.viewmodel.team

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.team.CreateTeamRequest
import com.imhungry.sillok.domain.usecase.team.CreateTeamUseCase
import com.imhungry.sillok.presentation.state.team.TeamFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamFormViewModel @Inject constructor(
    private val createTeamUseCase: CreateTeamUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "TeamFormViewModel"
    }

    private val _state = MutableStateFlow(TeamFormState())
    val state: StateFlow<TeamFormState> = _state.asStateFlow()

    fun updateTeamName(teamName: String) {
        _state.update { current ->
            current.copy(
                teamName = teamName,
                showValidationErrors = false,
                error = null
            )
        }
    }

    fun updateEmailInput(emailInput: String) {
        _state.update { current ->
            current.copy(
                emailInput = emailInput,
                showValidationErrors = false,
                error = null
            )
        }
    }

    fun addEmail() {
        val trimmedEmail = _state.value.emailInput.trim()
        if (trimmedEmail.isNotEmpty() &&
            Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() &&
            !_state.value.memberEmails.contains(trimmedEmail)
        ) {
            _state.update { current ->
                current.copy(
                    memberEmails = current.memberEmails + trimmedEmail,
                    emailInput = "",
                    showValidationErrors = false,
                    error = null
                )
            }
        }
    }

    fun removeEmail(index: Int) {
        _state.update { current ->
            val updatedEmails = current.memberEmails.filterIndexed { i, _ -> i != index }
            current.copy(
                memberEmails = updatedEmails,
                showValidationErrors = false
            )
        }
    }

    fun showMemberInviteScreen() {
        val teamName = _state.value.teamName.trim()
        if (teamName.isNotEmpty()) {
            _state.update { it.copy(showMemberInvite = true, showValidationErrors = false) }
        } else {
            _state.update { it.copy(showValidationErrors = true) }
        }
    }

    fun hideMemberInviteScreen() {
        _state.update { it.copy(showMemberInvite = false) }
    }

    fun createTeam(onSuccess: (Long) -> Unit) {
        val teamName = _state.value.teamName.trim()
        val memberEmails = _state.value.memberEmails

        // 유효성 검사
        if (teamName.isEmpty()) {
            _state.update { it.copy(showValidationErrors = true) }
            return
        }

        if (memberEmails.isEmpty()) {
            _state.update { it.copy(showValidationErrors = true) }
            return
        }

        _state.update { it.copy(isLoading = true, error = null, showValidationErrors = false) }

        viewModelScope.launch {
            try {
                val request = CreateTeamRequest(
                    teamName = teamName,
                    memberEmails = memberEmails
                )

                when (val result = createTeamUseCase(request)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "팀 생성 성공: teamId=${result.data}")
                        _state.update { it.copy(isLoading = false) }
                        onSuccess(result.data)
                    }

                    is ApiResult.Failure -> {
                        Log.e(TAG, "팀 생성 실패: ${result.message}")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "팀 생성 예외 발생: ${e.message}", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "팀 생성 중 오류가 발생했습니다."
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
