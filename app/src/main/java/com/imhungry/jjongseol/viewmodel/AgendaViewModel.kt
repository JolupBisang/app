package com.imhungry.jjongseol.viewmodel

import android.app.Application
import com.imhungry.jjongseol.data.model.agenda.AgendaDto
import com.imhungry.jjongseol.data.model.error.ApiError
import com.imhungry.jjongseol.data.network.client.handleHttpException
import com.imhungry.jjongseol.data.repository.AgendaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val agendaRepository: AgendaRepository,
    application: Application
) : BaseAndroidViewModel(application) {

    private val _agendaItems = MutableStateFlow<List<AgendaDto>>(emptyList())
    val agendaItems: StateFlow<List<AgendaDto>> = _agendaItems

    private var loadedMeetingId: Long? = null

    suspend fun loadAgendas(meetingId: Long) {
        try {
            val agendas = agendaRepository.getAgendas(meetingId)
            _agendaItems.value = agendas
            loadedMeetingId = meetingId
        } catch (e: HttpException) {
            setError(handleHttpException(e))
        } catch (e: Exception) {
            setError(ApiError(message = e.message, errorId = null))
        }
    }

    fun onToggleAgenda(index: Int) {
        val currentList = _agendaItems.value.toMutableList()
        val originalAgenda = currentList.getOrNull(index) ?: return

        val updatedAgenda = originalAgenda.copy(isCompleted = !originalAgenda.isCompleted)
        currentList[index] = updatedAgenda
        _agendaItems.value = currentList
    }

}
