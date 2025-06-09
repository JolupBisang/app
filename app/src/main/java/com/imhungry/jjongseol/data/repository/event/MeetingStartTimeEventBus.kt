package com.imhungry.jjongseol.data.repository.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object MeetingStartTimeEventBus {
    private val _startTimeFlow = MutableSharedFlow<Pair<Long, Long>>(replay = 1)
    val startTimeFlow = _startTimeFlow.asSharedFlow()

    fun send(meetingId: Long, startTime: Long) {
        _startTimeFlow.tryEmit(meetingId to startTime)
    }
}
