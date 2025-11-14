package com.imhungry.sillok.presentation.state.folder

import com.imhungry.sillok.presentation.screen.folder.FolderMeetingItem

data class FolderMeetingAddState(
    val meetings: List<FolderMeetingItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchText: String = "",
    val searchQuery: String = "",
    val isSearching: Boolean = false
)