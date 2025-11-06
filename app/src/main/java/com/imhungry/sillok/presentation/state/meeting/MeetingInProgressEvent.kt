package com.imhungry.sillok.presentation.state.meeting

sealed class MeetingInProgressEvent {
    data object NavigateToHome : MeetingInProgressEvent()
}

