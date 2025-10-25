package com.imhungry.sillok.domain.model.meeting

enum class MeetingStatus {
    WAITING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    companion object {
        fun from(value: String?): MeetingStatus? = try {
            if (value.isNullOrBlank()) null else valueOf(value)
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}


