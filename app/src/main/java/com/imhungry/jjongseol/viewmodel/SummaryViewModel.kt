package com.imhungry.jjongseol.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.data.model.summary.response.SummaryListRes
import com.imhungry.jjongseol.data.repository.SummaryRepository
import com.imhungry.jjongseol.data.repository.SummaryResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val summaryRepository: SummaryRepository
) : ViewModel() {
    private val _summaries = MutableStateFlow<List<SummaryListRes>>(emptyList())
    val summaries: StateFlow<List<SummaryListRes>> = _summaries

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var hasNextPage = true
    private var currentPage = 0

    fun loadSummaries(meetingId: Long, isRecap: Boolean = false, reset: Boolean = false) {
        if (reset) {
            _summaries.value = emptyList()
            currentPage = 0
            hasNextPage = true
        }
        if (!hasNextPage) return

        viewModelScope.launch {
            _isLoading.value = true
            when (val result = summaryRepository.getSummaries(meetingId, isRecap, currentPage)) {
                is SummaryResult.Success -> {
                    val slice = result.data
                    _summaries.value = _summaries.value + slice.content
                    hasNextPage = !slice.last
                    currentPage += 1
                }
                is SummaryResult.Error -> _errorMessage.value = result.message
                is SummaryResult.Exception -> _errorMessage.value = result.throwable.message ?: "네트워크 오류"
            }
            _isLoading.value = false
        }
    }
}

