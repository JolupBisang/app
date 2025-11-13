package com.imhungry.sillok.domain.usecase.folder

import com.imhungry.sillok.domain.model.folder.DeleteMeetingFoldersRequest
import com.imhungry.sillok.domain.repository.folder.MeetingMinutesFolderRepository
import javax.inject.Inject

class DeleteMeetingFoldersUseCase @Inject constructor(
    private val repository: MeetingMinutesFolderRepository
) {
    suspend operator fun invoke(request: DeleteMeetingFoldersRequest) =
        repository.deleteFolders(request)
}

