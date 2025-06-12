package com.imhungry.jjongseol.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.data.model.agenda.dto.AgendaDto
import com.imhungry.jjongseol.data.repository.AgendaRepository
import com.imhungry.jjongseol.data.repository.AgendaResult
import com.imhungry.jjongseol.data.model.agenda.AgendaItem
import com.imhungry.jjongseol.data.repository.AgendaSocketEventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val agendaRepository: AgendaRepository
) : ViewModel() {
    private val _agendaItems = MutableStateFlow<List<AgendaItem>>(emptyList())
    val agendaItems: StateFlow<List<AgendaItem>> = _agendaItems

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var currentMeetingId: Long? = null

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            AgendaSocketEventRepository.agendaUpdates.collect { updateDto ->
                updateAgendaFromSocket(updateDto.agendaId, updateDto.content, updateDto.isCompleted)
            }
        }
    }

    fun loadAgendas(meetingId: Long) {
        currentMeetingId = meetingId
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = agendaRepository.getAgendas(meetingId)) {
                is AgendaResult.Success -> {
                    _agendaItems.value = result.data.agendaDetails.map {
                        AgendaItem(
                            id = it.agendaId,
                            text = it.content,
                            isCompleted = it.isCompleted,
                            isPlaceholder = false
                        )
                    }
                }
                is AgendaResult.Error -> {
                    _errorMessage.value = result.errorResponse?.message ?: result.message
                }
                is AgendaResult.Exception -> {
                    _errorMessage.value = result.throwable.message ?: "네트워크 오류"
                }

                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun addPlaceholderItem() {
        val current = _agendaItems.value
        if (current.any { it.isPlaceholder }) return

        val updated = current.toMutableList()
        updated.add(
            AgendaItem(id = null, text = "", isPlaceholder = true)
        )
        _agendaItems.value = updated
    }
    fun addAgenda(content: String) {
        val meetingId = currentMeetingId ?: return
        viewModelScope.launch {
            when (val result = agendaRepository.addAgenda(meetingId, content)) {
                is AgendaResult.Success -> {
                    val newItem = AgendaItem(
                        id = result.data.agendaId,
                        text = content,
                        isCompleted = false,
                        isPlaceholder = false
                    )
                    val updated = _agendaItems.value.toMutableList().apply {
                        removeAll { it.isPlaceholder || it.id == null || it.text == content }
                        add(newItem)
                    }
                    _agendaItems.value = updated
                }
                is AgendaResult.Error, is AgendaResult.Exception -> {
                    _errorMessage.value = when (result) {
                        is AgendaResult.Error -> result.errorResponse?.message ?: result.message
                        is AgendaResult.Exception -> result.throwable.message ?: "네트워크 오류"
                        else -> "알 수 없는 오류"
                    }
                }

                else -> {}
            }
        }
    }

    fun onEditAgendaItem(item: AgendaItem, newText: String) {
        val updatedList = _agendaItems.value.toMutableList()
        val index = updatedList.indexOfFirst { it == item }
        if (index != -1) {
            updatedList[index] = item.copy(text = newText)
            _agendaItems.value = updatedList
        }
    }


    fun removeAgendaByText(text: String) {
        _agendaItems.value = _agendaItems.value.filterNot { it.text == text }
    }

    fun deleteAgenda(agendaId: Long) {
        viewModelScope.launch {
            when (val result = agendaRepository.deleteAgenda(agendaId)) {
                is AgendaResult.Success -> {
                    _agendaItems.value = _agendaItems.value.filterNot { it.id == agendaId }
                }
                is AgendaResult.Error, is AgendaResult.Exception -> {
                    _errorMessage.value = when (result) {
                        is AgendaResult.Error -> result.errorResponse?.message ?: result.message
                        is AgendaResult.Exception -> result.throwable.message ?: "네트워크 오류"
                        else -> "알 수 없는 오류"
                    }
                }

                else -> {}
            }
        }
    }

    fun editAgenda(agendaId: Long, newText: String) {
        viewModelScope.launch {
            when (val result = agendaRepository.updateAgenda(agendaId, newText)) {
                is AgendaResult.Success -> {
                    _agendaItems.value = _agendaItems.value.map {
                        if (it.id == agendaId) it.copy(text = newText) else it
                    }
                }
                is AgendaResult.Error, is AgendaResult.Exception -> {
                    _errorMessage.value = when (result) {
                        is AgendaResult.Error -> result.errorResponse?.message ?: result.message
                        is AgendaResult.Exception -> result.throwable.message ?: "네트워크 오류"
                        else -> "알 수 없는 오류"
                    }
                }

                else -> {}
            }
        }
    }

    fun restoreAgendas(agendas: List<String>) {
        val restoredItems = agendas.map { text ->
            AgendaItem(
                id = null,
                text = text,
                isCompleted = false,
                isPlaceholder = false
            )
        }
        _agendaItems.value = restoredItems
    }

    fun onToggleAgenda(index: Int) {
        val agendas = _agendaItems.value.toMutableList()
        if (index in agendas.indices) {
            val item = agendas[index]
            val newIsCompleted = !item.isCompleted

            agendas[index] = item.copy(isCompleted = newIsCompleted)
            _agendaItems.value = agendas

            viewModelScope.launch {
                if (item.id != null) {
                    when (val result = agendaRepository.changeAgendaStatus(item.id, newIsCompleted)) {
                        is AgendaResult.Success -> {  }
                        is AgendaResult.Error, is AgendaResult.Exception -> {
                            agendas[index] = item
                            _agendaItems.value = agendas
                            _errorMessage.value = when (result) {
                                is AgendaResult.Error -> result.errorResponse?.message ?: result.message
                                is AgendaResult.Exception -> result.throwable.message ?: "네트워크 오류"
                                else -> "알 수 없는 오류"
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    fun updateAgendaFromSocket(agendaId: Long, content: String, isCompleted: Boolean) {
        _agendaItems.value = _agendaItems.value.map {
            if (it.id == agendaId) it.copy(text = content, isCompleted = isCompleted)
            else it
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
