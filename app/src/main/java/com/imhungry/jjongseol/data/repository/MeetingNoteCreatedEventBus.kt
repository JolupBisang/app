package com.imhungry.jjongseol.data.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object MeetingNoteCreatedEventBus {
    private val _flow = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    val flow = _flow.asSharedFlow()
    fun send(meetingId: Long) { _flow.tryEmit(meetingId) }
}