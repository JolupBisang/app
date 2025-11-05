package com.imhungry.sillok.presentation.state.meetingminutesfolder

import com.imhungry.sillok.domain.model.meetingminutesfolder.MeetingMinutesFolderDetailSummary

data class MeetingMinutesFolderState (
    val folders: List<MeetingMinutesFolderDetailSummary> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)