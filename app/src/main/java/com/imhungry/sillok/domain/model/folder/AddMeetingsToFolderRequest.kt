package com.imhungry.sillok.domain.model.folder

data class AddMeetingsToFolderRequest(
    val folderId: Long,
    val meetingIds: List<Long>
)

