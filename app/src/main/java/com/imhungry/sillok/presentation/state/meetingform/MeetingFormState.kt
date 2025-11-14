package com.imhungry.sillok.presentation.state.meetingform

import androidx.compose.ui.text.input.TextFieldValue
import com.imhungry.sillok.domain.model.places.PlaceSuggestion

data class TeamInfo(
    val id: Long,
    val name: String
)

data class MemberInfo(
    val id: Long,
    val name: String,
    val email: String,
    val profileImage: String?
)

data class SelectedTeamInfo(
    val teamId: Long,
    val teamName: String,
    val selectedMembers: List<MemberInfo>
)

data class MeetingFormState(
    val userName: String = "{이름}",
    val title: String = "",
    val titleTextFieldValue: TextFieldValue = TextFieldValue(""),
    val participants: String = "",
    val participantEmails: Set<String> = emptySet(),
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val duration: String = "",
    val location: String = "",
    val locationTextFieldValue: TextFieldValue = TextFieldValue(""),
    val agendas: List<String> = listOf(""), // 기본 아젠다 필드 하나 제공
    val breakInterval: String = "",
    val breakDuration: String = "",

    // 편집 모드 관련 상태
    val isEditMode: Boolean = false,
    val meetingId: Long? = null,

    // 이메일 자동완성 관련 상태
    val emailSuggestions: List<String> = emptyList(),
    val showEmailSuggestions: Boolean = false,
    val isSearching: Boolean = false,

    // 장소 자동완성 관련 상태
    val locationSuggestions: List<PlaceSuggestion> = emptyList(),
    val showLocationSuggestions: Boolean = false,
    val isLocationSearching: Boolean = false,

    // UI 상태
    val isLoading: Boolean = false,
    val error: String? = null,
    val showTimePicker: Boolean = false,
    val showCancelDialog: Boolean = false,
    val showDuplicationDialog: Boolean = false,

    // 필수 항목 검증 상태
    val validationErrors: Map<String, String> = emptyMap(),
    val showValidationErrors: Boolean = false,
    val isFormValid: Boolean = false,

    // 팀 및 멤버 리스트
    val teams: List<TeamInfo> = emptyList(),
    val members: List<MemberInfo> = emptyList(),

    // 팀 검색 다이얼로그
    val showTeamSearchDialog: Boolean = false,
    val teamSearchText: String = "",
    val selectedTeam: TeamInfo? = null,

    // 팀 멤버 선택 다이얼로그
    val showTeamMemberSelectionDialog: Boolean = false,
    val selectedTeamMembers: Set<Long> = emptySet(),
    val teamMembersForSelection: List<MemberInfo> = emptyList(),

    // 선택된 팀 목록 (팀 이름과 선택된 멤버 정보 포함)
    val selectedTeams: List<SelectedTeamInfo> = emptyList(),

    // 팀 멤버 선택 결과 다이얼로그
    val showTeamMemberSelectionResultDialog: Boolean = false,
    val selectedTeamForResult: SelectedTeamInfo? = null
)

// 기존 회의 데이터를 위한 데이터 클래스(편집 모드 초기화에 사용)
data class MeetingData(
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val location: String,
    val agendas: List<String>,
    val breakInterval: String,
    val breakDuration: String,
    val participants: List<String>
)