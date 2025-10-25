package com.imhungry.sillok.domain.model.meeting

data class Meeting(
    val meetingId: Long,
    val title: String,
    val location: String,
    val scheduledStartTime: String,
    val targetTime: Int,
    val restInterval: Int,
    val restDuration: Int,
    val meetingStatus: String,
    val participants: List<Participant>,
    val isHost: Boolean
) {
    data class Participant(
        val userId: Long,
        val email: String
    )
}
