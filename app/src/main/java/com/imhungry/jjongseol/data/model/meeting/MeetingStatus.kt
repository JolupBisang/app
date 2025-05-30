package com.imhungry.jjongseol.data.model.meeting

enum class MeetingStatus {
    WAITING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    companion object {
        fun from(value: String?): MeetingStatus? =
            values().find { it.name.equals(value, ignoreCase = true) }
    }
}
