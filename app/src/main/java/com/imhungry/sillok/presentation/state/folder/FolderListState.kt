package com.imhungry.sillok.presentation.state.folder

import com.imhungry.sillok.domain.model.folder.MeetingMinutesFolderDetailSummary

data class FolderListState(
    val folders: List<MeetingMinutesFolderDetailSummary> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)