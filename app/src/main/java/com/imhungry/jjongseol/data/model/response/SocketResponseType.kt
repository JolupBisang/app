package com.imhungry.jjongseol.data.model.response

enum class SocketResponseType {
    CONNECTION_ESTABLISHED,
    LAST_PROCESSED_CHUNK_ID,
    ERROR,
    TRANSLATED_TEXT,
    FEEDBACK,
    PARTICIPATION_RATE,
    SUMMARY,
    COMPLETION_SCHEDULED,
    MEETING_COMPLETED,
    DIARIZED_SEGMENT
}
