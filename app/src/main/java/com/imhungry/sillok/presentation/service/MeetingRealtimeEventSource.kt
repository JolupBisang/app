package com.imhungry.sillok.presentation.service

import kotlinx.coroutines.flow.Flow

interface MeetingRealtimeEventSource {

    val events: Flow<ServiceEvent>

    suspend fun start(
        serverUrl: String,
        meetingId: Long,
        jwtToken: String
    )

    suspend fun stop()

    fun toggleMic()
}
