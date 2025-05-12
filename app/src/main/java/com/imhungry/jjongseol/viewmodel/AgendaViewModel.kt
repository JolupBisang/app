package com.imhungry.jjongseol.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.imhungry.jjongseol.data.model.agenda.AgendaDto
import com.imhungry.jjongseol.data.model.agenda.AgendaUiModel
import com.imhungry.jjongseol.data.model.error.ApiError
import com.imhungry.jjongseol.data.network.client.handleHttpException
import com.imhungry.jjongseol.data.repository.AgendaRepository
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
) : BaseAndroidViewModel(context) {
    private val _agendaUiItems = MutableStateFlow<List<AgendaUiModel>>(emptyList())
    val agendaUiItems: StateFlow<List<AgendaUiModel>> = _agendaUiItems

    private var loadedMeetingId: Long? = null

    fun loadAgendas(meetingId: Long) {
        viewModelScope.launch {
            try {
                val agendas = agendaRepository.getAgendas(meetingId)
                val prefs = loadCheckedStatesFromPrefs(meetingId, agendas)
                _agendaUiItems.value = agendas.mapIndexed { i, dto ->
                    AgendaUiModel(dto, prefs.getOrElse(i) { dto.isCompleted })
                }
                loadedMeetingId = meetingId
            } catch (e: HttpException) {
                setError(handleHttpException(e)) // <-- 직접 호출
            } catch (e: Exception) {
                setError(ApiError(message = e.message, errorId = null))
            }
        }
    }

    fun toggleAgendaChecked(index: Int) {
        val updated = _agendaUiItems.value.toMutableList()
        val current = updated[index]
        val newValue = !current.isChecked
        val newModel = current.copy(isChecked = newValue)
        updated[index] = newModel
        _agendaUiItems.value = updated
        saveCheckedStatesToPrefs(updated)

        viewModelScope.launch {
            try {
                val success = agendaRepository.changeAgendaStatus(current.dto.agendaId, newValue)
                if (!success) throw Exception("상태 변경 실패")
            } catch (e: Exception) {
                updated[index] = current
                _agendaUiItems.value = updated
                setError(ApiError(message = e.message, errorId = null))
            }
        }
    }

    private fun saveCheckedStatesToPrefs(items: List<AgendaUiModel>) {
        val map = items.associate { it.dto.agendaId.toString() to it.isChecked.toString() }
        val prefs = context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("checked_map_${loadedMeetingId ?: -1}", Gson().toJson(map)).apply()
    }

    private fun loadCheckedStatesFromPrefs(meetingId: Long, agendas: List<AgendaDto>): List<Boolean> {
        val prefs = context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("checked_map_$meetingId", null)
        val map: Map<String, String> = if (json != null) Gson().fromJson(json, object : TypeToken<Map<String, String>>(){}.type) else emptyMap()

        return agendas.map { dto ->
            map[dto.agendaId.toString()]?.toBooleanStrictOrNull() ?: dto.isCompleted
        }
    }
}
