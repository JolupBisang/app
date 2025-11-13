package com.imhungry.sillok.presentation.state.folder

data class FolderFormState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val createdFolderId: Long? = null
)