package com.imhungry.sillok.data.model.realtime

enum class SocketResponseType {
    ERROR,
    DIARIZED_SEGMENT,
    COMPLETION_SCHEDULED,
    MEETING_COMPLETED,
    CONNECTION_ESTABLISHED,
    AGENDA_UPDATED,
    MEETING_NOTE_CREATED
}