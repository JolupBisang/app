package com.imhungry.sillok.domain.repository.folder

import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.folder.AddMeetingsToFolderRequest
import com.imhungry.sillok.domain.model.folder.CreateMeetingFolderRequest
import com.imhungry.sillok.domain.model.folder.DeleteMeetingFoldersRequest
import com.imhungry.sillok.domain.model.folder.MeetingFolderDetail
import com.imhungry.sillok.domain.model.folder.MeetingFolderList
import com.imhungry.sillok.domain.model.folder.RemoveMeetingsFromFolderRequest

interface MeetingMinutesFolderRepository {
    suspend fun createFolder(request: CreateMeetingFolderRequest): ApiResult<Long>
    suspend fun deleteFolders(request: DeleteMeetingFoldersRequest): ApiResult<List<Long>>
    suspend fun getAllFolders(): ApiResult<MeetingFolderList>
    suspend fun getFolderMeetings(folderId: Long): ApiResult<MeetingFolderDetail>
    suspend fun addMeetingsToFolder(request: AddMeetingsToFolderRequest): ApiResult<List<Long>>
    suspend fun removeMeetingsFromFolder(request: RemoveMeetingsFromFolderRequest): ApiResult<List<Long>>
}

