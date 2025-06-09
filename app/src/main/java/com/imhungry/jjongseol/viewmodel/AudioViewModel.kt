package com.imhungry.jjongseol.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.data.model.audio.dto.AudioInfo
import com.imhungry.jjongseol.data.repository.AudioRepository
import com.imhungry.jjongseol.data.repository.AudioResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val audioRepository: AudioRepository
) : ViewModel() {

    var uploadResult by mutableStateOf<AudioResult<Unit>?>(null)
        private set

    private val _audioList = MutableStateFlow<List<AudioInfo>>(emptyList())
    val audioList: StateFlow<List<AudioInfo>> = _audioList

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun uploadAudio(file: java.io.File) {
        viewModelScope.launch {
            uploadResult = audioRepository.uploadAudio(file)
        }
    }

    fun loadAudioList(meetingId: Long) {
        viewModelScope.launch {
            when (val result = audioRepository.getAudioList(meetingId)) {
                is AudioResult.Success -> {
                    _audioList.value = result.data.audioList
                }
                is AudioResult.Error -> {
                    _errorMessage.value = result.message
                }
                is AudioResult.Exception -> {
                    _errorMessage.value = result.throwable.message ?: "네트워크 오류"
                }
            }
        }
    }
}
