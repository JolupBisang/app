package com.imhungry.jjongseol.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.data.repository.AudioRepository
import com.imhungry.jjongseol.data.repository.AudioResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val audioRepository: AudioRepository
) : ViewModel() {

    var uploadResult by mutableStateOf<AudioResult<Unit>?>(null)
        private set

    fun uploadAudio(file: java.io.File) {
        viewModelScope.launch {
            uploadResult = audioRepository.uploadAudio(file)
        }
    }
}
