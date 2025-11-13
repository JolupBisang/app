package com.imhungry.sillok.domain.usecase.folder

import com.imhungry.sillok.domain.repository.folder.MeetingMinutesFolderRepository
import javax.inject.Inject

class GetAllFoldersUseCase @Inject constructor(
    private val repository: MeetingMinutesFolderRepository
) {
    suspend operator fun invoke() =
        repository.getAllFolders()
}

