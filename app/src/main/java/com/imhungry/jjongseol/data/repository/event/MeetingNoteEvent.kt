package com.imhungry.jjongseol.data.repository.event

sealed class MeetingNoteEvent(val meetingId: Long) {
    class Created(meetingId: Long): MeetingNoteEvent(meetingId)
    class Completed(meetingId: Long): MeetingNoteEvent(meetingId)
}
