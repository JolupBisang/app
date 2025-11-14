package com.imhungry.sillok.presentation.state.meetingform

import androidx.compose.ui.text.input.TextFieldValue

sealed class MeetingFormEvent {
    data class TitleChanged(val title: String) : MeetingFormEvent()
    data class TitleTextFieldValueChanged(val textFieldValue: TextFieldValue) : MeetingFormEvent()
    data class ParticipantsChanged(val participants: String) : MeetingFormEvent()
    data class ParticipantEmailSelected(val email: String) : MeetingFormEvent()
    data class ParticipantEmailRemoved(val email: String) : MeetingFormEvent()
    data class DateChanged(val date: String) : MeetingFormEvent()
    data class StartTimeChanged(val startTime: String) : MeetingFormEvent()
    data class EndTimeChanged(val endTime: String) : MeetingFormEvent()
    data class DurationChanged(val duration: String) : MeetingFormEvent()
    data class LocationChanged(val location: String) : MeetingFormEvent()
    data class LocationTextFieldValueChanged(val textFieldValue: TextFieldValue) :
        MeetingFormEvent()

    data class LocationSelected(val location: String) : MeetingFormEvent()
    data class AgendaChanged(val index: Int, val agenda: String) : MeetingFormEvent()
    object AgendaAdded : MeetingFormEvent()
    data class AgendaRemoved(val index: Int) : MeetingFormEvent()
    data class BreakIntervalChanged(val breakInterval: String) : MeetingFormEvent()
    data class BreakDurationChanged(val breakDuration: String) : MeetingFormEvent()

    object CreateMeeting : MeetingFormEvent()
    object BackClicked : MeetingFormEvent()
    object CancelClicked : MeetingFormEvent() // 취소 버튼 클릭
    object CancelConfirmed : MeetingFormEvent() // 취소 확인
    object CancelDismissed : MeetingFormEvent() // 취소 다이얼로그 닫기
    object ClearFocus : MeetingFormEvent()
    object TimePickerShown : MeetingFormEvent()

    // 검증 관련 이벤트
    object ValidateForm : MeetingFormEvent()
    object ClearValidationErrors : MeetingFormEvent()
    data class ShowValidationError(val field: String, val message: String) : MeetingFormEvent()
    object FormValidationSuccess : MeetingFormEvent()

    // API 성공 이벤트
    data class MeetingCreated(val meetingId: Long) : MeetingFormEvent()
    object MeetingUpdated : MeetingFormEvent()

    // 중복 시간 체크 관련 이벤트
    object DuplicationDialogConfirmed : MeetingFormEvent()
    object DuplicationDialogDismissed : MeetingFormEvent()

    // 팀 검색 다이얼로그 관련 이벤트
    object ShowTeamSearchDialog : MeetingFormEvent()
    object DismissTeamSearchDialog : MeetingFormEvent()
    data class TeamSearchTextChanged(val text: String) : MeetingFormEvent()
    data class TeamSelected(val team: TeamInfo) : MeetingFormEvent()
    object InviteTeamMembers : MeetingFormEvent()

    // 팀 멤버 선택 다이얼로그 관련 이벤트
    object ShowTeamMemberSelectionDialog : MeetingFormEvent()
    object DismissTeamMemberSelectionDialog : MeetingFormEvent()
    data class TeamMemberToggled(val memberId: Long) : MeetingFormEvent()
    object SelectAllTeamMembers : MeetingFormEvent()
    object ConfirmTeamMemberSelection : MeetingFormEvent()
    data class RemoveSelectedTeam(val teamId: Long) : MeetingFormEvent()

    // 팀 멤버 선택 결과 다이얼로그 관련 이벤트
    data class ShowTeamMemberSelectionResultDialog(val teamId: Long) : MeetingFormEvent()
    object DismissTeamMemberSelectionResultDialog : MeetingFormEvent()
}