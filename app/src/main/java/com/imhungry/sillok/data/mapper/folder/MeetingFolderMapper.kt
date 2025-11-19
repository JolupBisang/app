package com.imhungry.sillok.data.mapper.folder

import com.imhungry.sillok.data.model.folder.MeetingFolderCreationReqDto
import com.imhungry.sillok.data.model.folder.MeetingFolderDeletionReqDto
import com.imhungry.sillok.data.model.folder.MeetingFolderDetailResDto
import com.imhungry.sillok.data.model.folder.MeetingFolderInfoDto
import com.imhungry.sillok.data.model.folder.MeetingFolderListResDto
import com.imhungry.sillok.data.model.folder.MeetingFolderUpdateReqDto
import com.imhungry.sillok.data.model.folder.MeetingInfoDto
import com.imhungry.sillok.domain.model.folder.AddMeetingsToFolderRequest
import com.imhungry.sillok.domain.model.folder.CreateMeetingFolderRequest
import com.imhungry.sillok.domain.model.folder.DeleteMeetingFoldersRequest
import com.imhungry.sillok.domain.model.folder.FolderMeetingInfo
import com.imhungry.sillok.domain.model.folder.MeetingFolderDetail
import com.imhungry.sillok.domain.model.folder.MeetingFolderInfo
import com.imhungry.sillok.domain.model.folder.MeetingFolderList
import com.imhungry.sillok.domain.model.folder.RemoveMeetingsFromFolderRequest
import javax.inject.Inject

class MeetingFolderMapper @Inject constructor() {
    fun toDto(request: CreateMeetingFolderRequest): MeetingFolderCreationReqDto {
        return MeetingFolderCreationReqDto(
            name = request.name
        )
    }

    fun toDto(request: DeleteMeetingFoldersRequest): MeetingFolderDeletionReqDto {
        return MeetingFolderDeletionReqDto(
            folderIds = request.folderIds
        )
    }

    fun toDto(request: AddMeetingsToFolderRequest): MeetingFolderUpdateReqDto {
        return MeetingFolderUpdateReqDto(
            meetingIds = request.meetingIds
        )
    }

    fun toDto(request: RemoveMeetingsFromFolderRequest): MeetingFolderUpdateReqDto {
        return MeetingFolderUpdateReqDto(
            meetingIds = request.meetingIds
        )
    }

    fun toDomain(dto: MeetingFolderDetailResDto): MeetingFolderDetail {
        return MeetingFolderDetail(
            meetings = dto.meetings.map { toDomain(it) }
        )
    }

    private fun toDomain(dto: MeetingInfoDto): FolderMeetingInfo {
        return FolderMeetingInfo(
            meetingId = dto.meetingId,
            title = dto.title,
            scheduledStartTime = dto.scheduledStartTime.take(19)
        )
    }

    fun toDomain(dto: MeetingFolderListResDto): MeetingFolderList {
        return MeetingFolderList(
            folders = dto.folders.map { toDomain(it) }
        )
    }

    private fun toDomain(dto: MeetingFolderInfoDto): MeetingFolderInfo {
        return MeetingFolderInfo(
            folderId = dto.folderId,
            folderName = dto.folderName,
            meetingName = dto.meetingName,
            scheduledStartTime = dto.scheduledStartTime?.take(19),
            scheduledEndTime = dto.scheduledEndTime?.take(19)
        )
    }
    
}

