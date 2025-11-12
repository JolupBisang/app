package com.imhungry.sillok.data.model.meeting

data class MeetingDetailResDto(
    val meetingId: Long,
    val title: String,
    val location: String,
    val scheduledStartTime: String,
    val actualStartTime: String? = null,
    val scheduledEndTime: String? = null,
    val targetTime: Int,
    val restInterval: Int,
    val restDuration: Int,
    val meetingStatus: String,
    val participants: List<ParticipantDto>,
    val agendas: List<AgendaDto>,
    val teamNames: List<String>,
    val isHost: Boolean
) {
    data class ParticipantDto(
        val userId: Long,
        val email: String,
        val role: MeetingRole,
    )
    
    data class AgendaDto(
        val agendaId: Long,
        val content: String,
        val isCompleted: Boolean
    )
}

enum class MeetingRole {
    HOST,
    PARTICIPANT
}