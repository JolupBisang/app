package com.imhungry.sillok.data.model.meeting

data class MeetingDetailResDto(
    val meetingId: Long,
    val title: String,
    val location: String,
    val scheduledStartTime: String,
    val targetTime: Int,
    val restInterval: Int,
    val restDuration: Int,
    val meetingStatus: String,
    val participants: List<ParticipantDto>,
    val isHost: Boolean
) {
    data class ParticipantDto(
        val userId: Long,
        val email: String,
        val role: MeetingRole,
    )
}

enum class MeetingRole {
    HOST,
    PARTICIPANT
}