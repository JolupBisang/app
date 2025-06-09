package com.imhungry.jjongseol.data.repository.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object MeetingNoteEventBus {
    private val _flow = MutableSharedFlow<MeetingNoteEvent>(extraBufferCapacity = 1)
    val flow = _flow.asSharedFlow()
    fun send(event: MeetingNoteEvent) = _flow.tryEmit(event)
}