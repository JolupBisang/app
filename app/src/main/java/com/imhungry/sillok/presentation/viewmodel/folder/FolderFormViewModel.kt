package com.imhungry.sillok.presentation.viewmodel.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.folder.CreateMeetingFolderRequest
import com.imhungry.sillok.domain.usecase.folder.CreateMeetingFolderUseCase
import com.imhungry.sillok.presentation.state.folder.FolderFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderFormViewModel @Inject constructor(
    private val createMeetingFolderUseCase: CreateMeetingFolderUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(FolderFormState())
    val state: StateFlow<FolderFormState> = _state.asStateFlow()

    fun createFolder(folderName: String) {
        if (folderName.trim().isEmpty()) {
            _state.update { it.copy(error = "폴더 이름을 입력해주세요.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isSuccess = false) }

            val result = createMeetingFolderUseCase(
                CreateMeetingFolderRequest(name = folderName.trim())
            )

            _state.update {
                when (result) {
                    is ApiResult.Success -> {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            createdFolderId = result.data,
                            error = null
                        )
                    }
                    is ApiResult.Failure -> {
                        it.copy(
                            isLoading = false,
                            isSuccess = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun resetSuccess() {
        _state.update { it.copy(isSuccess = false, createdFolderId = null) }
    }
}