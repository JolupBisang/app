package com.imhungry.jjongseol.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.data.model.agenda.AgendaDto
import com.imhungry.jjongseol.data.model.error.ApiError
import com.imhungry.jjongseol.data.repository.AgendaRepository
import com.imhungry.jjongseol.util.handleHttpException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val agendaRepository: AgendaRepository,
    private val context: Application,
) : AndroidViewModel(context) {

    var onError: ((ApiError) -> Unit)? = null

    private val _agendaItems = MutableStateFlow<List<AgendaDto>>(emptyList())
    val agendaItems: StateFlow<List<AgendaDto>> = _agendaItems

    private val _checkedStates = MutableStateFlow<List<Boolean>>(emptyList())
    val checkedStates: StateFlow<List<Boolean>> = _checkedStates

    private var loadedMeetingId: Long? = null

    fun loadAgendas(meetingId: Long) {
        viewModelScope.launch {
            try {
                val agendas = agendaRepository.getAgendas(meetingId)
                _agendaItems.value = agendas
                _checkedStates.value = loadCheckedStatesFromPrefs(meetingId, agendas)
                loadedMeetingId = meetingId
            } catch (e: HttpException) {
                val apiError = handleHttpException(e)
                onError?.invoke(apiError)
            } catch (e: Exception) {
                onError?.invoke(ApiError(message = e.message, errorId = null))
            }

        }
    }

    fun toggleAgendaChecked(index: Int) {
        _checkedStates.value = _checkedStates.value.toMutableList().apply {
            this[index] = !this[index]
        }
        saveCheckedStatesToPrefs()
    }

    private fun loadCheckedStatesFromPrefs(meetingId: Long, agendas: List<AgendaDto>): List<Boolean> {
        val saved = context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
            .getString("checked_states_$meetingId", null)
        return saved?.split(",")?.map { it.toBooleanStrictOrNull() ?: false }
            ?.takeIf { it.size == agendas.size }
            ?: agendas.map { it.isCompleted }
    }

    private fun saveCheckedStatesToPrefs() {
        val states = _checkedStates.value.joinToString(",") { it.toString() }
        context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE).edit()
            .putString("checked_states_${loadedMeetingId ?: -1}", states)
            .apply()
    }
}
