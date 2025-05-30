package com.imhungry.jjongseol.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.data.repository.AgendaRepository
import com.imhungry.jjongseol.data.repository.AgendaResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AgendaUiItem(
    val agendaId: Long,
    val content: String,
    val isCompleted: Boolean
)

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val agendaRepository: AgendaRepository
) : ViewModel() {
    private val _agendaItems = MutableStateFlow<List<AgendaUiItem>>(emptyList())
    val agendaItems: StateFlow<List<AgendaUiItem>> = _agendaItems

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var currentMeetingId: Long? = null

    fun loadAgendas(meetingId: Long) {
        currentMeetingId = meetingId
        viewModelScope.launch {
            when (val result = agendaRepository.getAgendas(meetingId)) {
                is AgendaResult.Success -> {
                    _agendaItems.value = result.data.agendaDetails.map {
                        AgendaUiItem(
                            agendaId = it.agendaId,
                            content = it.content,
                            isCompleted = it.isCompleted
                        )
                    }
                }
                is AgendaResult.Error -> {
                    _errorMessage.value = result.errorResponse?.message ?: result.message
                }
                is AgendaResult.Exception -> {
                    _errorMessage.value = result.throwable.message ?: "네트워크 오류"
                }
            }
        }
    }

    fun onToggleAgenda(index: Int) {
        val agendas = _agendaItems.value
        if (index in agendas.indices) {
            val item = agendas[index]
            viewModelScope.launch {
                when (val result = agendaRepository.changeAgendaStatus(item.agendaId, !item.isCompleted)) {
                    is AgendaResult.Success -> {
                        // 상태 변경 성공 시, 목록 다시 불러오기 (최신 상태 반영)
                        currentMeetingId?.let { loadAgendas(it) }
                    }
                    is AgendaResult.Error -> {
                        _errorMessage.value = result.errorResponse?.errorId ?: result.message
                    }
                    is AgendaResult.Exception -> {
                        _errorMessage.value = result.throwable.message ?: "네트워크 오류"
                    }
                }
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
