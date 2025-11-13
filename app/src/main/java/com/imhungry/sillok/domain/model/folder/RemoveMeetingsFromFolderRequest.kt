package com.imhungry.sillok.domain.model.folder

data class RemoveMeetingsFromFolderRequest(
    val folderId: Long,
    val meetingIds: List<Long>
)

