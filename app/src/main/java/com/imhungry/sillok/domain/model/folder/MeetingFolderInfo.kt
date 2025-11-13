package com.imhungry.sillok.domain.model.folder

data class MeetingFolderInfo(
    val folderId: Long,
    val folderName: String,
    val meetingName: String?,
    val scheduledStartTime: String?,
    val scheduledEndTime: String?
)

