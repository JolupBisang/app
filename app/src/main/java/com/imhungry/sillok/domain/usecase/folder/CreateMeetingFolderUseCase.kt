package com.imhungry.sillok.domain.usecase.folder

import com.imhungry.sillok.domain.model.folder.CreateMeetingFolderRequest
import com.imhungry.sillok.domain.repository.folder.MeetingMinutesFolderRepository
import javax.inject.Inject

class CreateMeetingFolderUseCase @Inject constructor(
    private val repository: MeetingMinutesFolderRepository
) {
    suspend operator fun invoke(request: CreateMeetingFolderRequest) =
        repository.createFolder(request)
}

