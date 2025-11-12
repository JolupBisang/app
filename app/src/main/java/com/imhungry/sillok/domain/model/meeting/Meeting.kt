package com.imhungry.sillok.domain.model.meeting

import com.imhungry.sillok.data.model.meeting.MeetingRole
import com.imhungry.sillok.domain.model.agenda.Agenda

data class Meeting(
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
    val participants: List<Participant>,
    val agendas: List<Agenda>,
    val teamNames: List<String>,
    val isHost: Boolean,
) {
    data class Participant(
        val userId: Long,
        val email: String,
        val role: MeetingRole
    )
}
