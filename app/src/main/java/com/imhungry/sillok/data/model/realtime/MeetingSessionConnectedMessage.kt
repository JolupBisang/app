package com.imhungry.sillok.data.model.realtime

data class MeetingSessionConnectedMessage(
    val actualStartTime: String,
    val lastProcessedChunkId: Long
)