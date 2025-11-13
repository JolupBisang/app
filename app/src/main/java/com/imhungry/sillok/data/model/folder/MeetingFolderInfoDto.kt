package com.imhungry.sillok.data.model.folder

data class MeetingFolderInfoDto(
    val folderId: Long,
    val folderName: String,
    val meetingName: String?,
    val scheduledStartTime: String?,
    val scheduledEndTime: String?
)

