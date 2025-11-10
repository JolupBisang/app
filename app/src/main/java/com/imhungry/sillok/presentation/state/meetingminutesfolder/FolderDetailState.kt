package com.imhungry.sillok.presentation.state.meetingminutesfolder

import com.imhungry.sillok.presentation.screen.meetingminutesfolder.FolderMeetingItem

data class FolderDetailState(
    val folderName: String = "",
    val meetings: List<FolderMeetingItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
