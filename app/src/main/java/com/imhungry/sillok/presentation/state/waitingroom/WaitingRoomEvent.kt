package com.imhungry.sillok.presentation.state.waitingroom

sealed class WaitingRoomEvent {
    data object MeetingStarted : WaitingRoomEvent()
    data class StartFailed(val message: String?) : WaitingRoomEvent()
}


