package com.imhungry.jjongseol.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.imhungry.jjongseol.data.model.agenda.AgendaDto
import com.imhungry.jjongseol.data.model.error.ApiError
import com.imhungry.jjongseol.data.network.client.handleHttpException
import com.imhungry.jjongseol.data.repository.AgendaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val agendaRepository: AgendaRepository,
    private val context: Application,
) : BaseAndroidViewModel(context) {
    private val _agendaItems = MutableStateFlow<List<AgendaDto>>(emptyList())
    val agendaItems: StateFlow<List<AgendaDto>> = _agendaItems

    private val _checkedStates = MutableStateFlow<List<Boolean>>(emptyList())
    val checkedStates: StateFlow<List<Boolean>> = _checkedStates

    private var loadedMeetingId: Long? = null

    suspend fun loadAgendas(meetingId: Long) {
        try {
            val agendas = agendaRepository.getAgendas(meetingId)

            val prefs = context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("agenda_list_$meetingId", Gson().toJson(agendas)).apply()

            val checked = loadCheckedStatesFromPrefs(meetingId, agendas)
            _agendaItems.value = agendas
            _checkedStates.value = checked
            loadedMeetingId = meetingId
        } catch (e: HttpException) {
            setError(handleHttpException(e))
        } catch (e: Exception) {
            setError(ApiError(message = e.message, errorId = null))
        }
    }

    fun saveAgendaCompletionStatusToServer(agendaId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                agendaRepository.changeAgendaStatus(agendaId, isCompleted)
            } catch (e: HttpException) {
                setError(handleHttpException(e))
            } catch (e: Exception) {
                setError(ApiError(message = e.message, errorId = null))
            }
        }
    }

    fun saveAllAgendaStatusesToServer() {
        viewModelScope.launch {
            coroutineScope {
                agendaItems.value.forEachIndexed { index, item ->
                    launch {
                        val isCompleted = checkedStates.value.getOrElse(index) { false }
                        agendaRepository.changeAgendaStatus(item.agendaId, isCompleted)
                    }
                }
            }
        }
    }

    fun toggleAgendaChecked(index: Int) {
        val currentList = _checkedStates.value.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = !currentList[index]
            _checkedStates.value = currentList
            saveCheckedStatesToPrefs(currentList)
        }
    }

    private fun saveCheckedStatesToPrefs(states: List<Boolean>) {
        val agendas = _agendaItems.value
        val map = agendas.mapIndexed { index, dto ->
            dto.agendaId.toString() to states.getOrNull(index).toString()
        }.toMap()

        val prefs = context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("checked_map_${loadedMeetingId ?: -1}", Gson().toJson(map)).apply()
    }

    private fun loadCheckedStatesFromPrefs(meetingId: Long, agendas: List<AgendaDto>): List<Boolean> {
        val prefs = context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("checked_map_$meetingId", null)
        val map: Map<String, String> = if (json != null)
            Gson().fromJson(json, object : TypeToken<Map<String, String>>() {}.type)
        else emptyMap()

        return agendas.map { dto ->
            map[dto.agendaId.toString()]?.toBooleanStrictOrNull() ?: dto.isCompleted
        }
    }
}
