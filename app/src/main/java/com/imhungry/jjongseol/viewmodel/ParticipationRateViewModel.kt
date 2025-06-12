package com.imhungry.jjongseol.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.data.model.participationrate.response.ParticipationRateHistoryRes
import com.imhungry.jjongseol.data.repository.ParticipationRateRepository
import com.imhungry.jjongseol.data.repository.ParticipationRateResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParticipationRateViewModel @Inject constructor(
    private val repository: ParticipationRateRepository
) : ViewModel() {
    private val _participationRates = MutableStateFlow<List<ParticipationRateHistoryRes.UserParticipationRate>>(emptyList())
    val participationRates: StateFlow<List<ParticipationRateHistoryRes.UserParticipationRate>> = _participationRates

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadParticipationRates(meetingId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = repository.getParticipationRateHistory(meetingId)) {
                is ParticipationRateResult.Success -> {
                    _participationRates.value = result.data.userParticipationRates
                }
                is ParticipationRateResult.Error -> {
                    _errorMessage.value = result.message
                    _isLoading.value = false
                }
                is ParticipationRateResult.Exception -> {
                    _errorMessage.value = result.throwable.message ?: "네트워크 오류"
                    _isLoading.value = false
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }
}
