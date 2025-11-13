package com.imhungry.sillok.domain.usecase.folder

import com.imhungry.sillok.domain.model.folder.AddMeetingsToFolderRequest
import com.imhungry.sillok.domain.repository.folder.MeetingMinutesFolderRepository
import javax.inject.Inject

class AddMeetingsToFolderUseCase @Inject constructor(
    private val repository: MeetingMinutesFolderRepository
) {
    suspend operator fun invoke(request: AddMeetingsToFolderRequest) =
        repository.addMeetingsToFolder(request)
}

