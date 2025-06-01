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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadAgendas(meetingId: Long) {
        currentMeetingId = meetingId
        viewModelScope.launch {
            _isLoading.value = true
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
            _isLoading.value = false
        }
    }

    /*fun onToggleAgenda(index: Int) {
        val agendas = _agendaItems.value
        if (index in agendas.indices) {
            val item = agendas[index]
            viewModelScope.launch {
                when (val result = agendaRepository.changeAgendaStatus(item.agendaId, !item.isCompleted)) {
                    is AgendaResult.Success -> {
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
    }*/

    fun onToggleAgenda(index: Int) {
        val agendas = _agendaItems.value.toMutableList()
        if (index in agendas.indices) {
            val item = agendas[index]
            val newIsCompleted = !item.isCompleted

            agendas[index] = item.copy(isCompleted = newIsCompleted)
            _agendaItems.value = agendas

            viewModelScope.launch {
                when (val result = agendaRepository.changeAgendaStatus(item.agendaId, newIsCompleted)) {
                    is AgendaResult.Success -> {
                    }
                    is AgendaResult.Error, is AgendaResult.Exception -> {
                        agendas[index] = item
                        _agendaItems.value = agendas
                        _errorMessage.value = when (result) {
                            is AgendaResult.Error -> result.errorResponse?.message ?: result.message
                            is AgendaResult.Exception -> result.throwable.message ?: "네트워크 오류"
                            else -> "알 수 없는 오류"
                        }
                    }
                }
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
