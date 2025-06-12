package com.imhungry.jjongseol.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.data.model.feedback.response.FeedbackListRes
import com.imhungry.jjongseol.data.repository.FeedbackRepository
import com.imhungry.jjongseol.data.repository.FeedbackResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val repository: FeedbackRepository
) : ViewModel() {
    private val _feedbacks = MutableStateFlow<List<FeedbackListRes>>(emptyList())
    val feedbacks: StateFlow<List<FeedbackListRes>> = _feedbacks

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var hasNextPage = true
    private var currentPage = 0

    fun loadFeedbacks(meetingId: Long, reset: Boolean = false) {
        if (reset) {
            _feedbacks.value = emptyList()
            currentPage = 0
            hasNextPage = true
        }
        if (!hasNextPage) return

        viewModelScope.launch {
            _isLoading.value = true
            when (val result = repository.getFeedbacks(meetingId, currentPage)) {
                is FeedbackResult.Success -> {
                    val slice = result.data
                    _feedbacks.value = _feedbacks.value + slice.content
                    hasNextPage = !slice.last
                    currentPage += 1
                }
                is FeedbackResult.Error -> _errorMessage.value = result.message
                is FeedbackResult.Exception -> _errorMessage.value = result.throwable.message ?: "네트워크 오류"
            }
            _isLoading.value = false
        }
    }

    fun loadAllFeedbacks(meetingId: Long) {
        _feedbacks.value = emptyList()
        var page = 0
        var hasNextPage = true

        viewModelScope.launch {
            _isLoading.value = true
            while (hasNextPage) {
                when (val result = repository.getFeedbacks(meetingId, page)) {
                    is FeedbackResult.Success -> {
                        val slice = result.data
                        _feedbacks.value = _feedbacks.value + slice.content
                        hasNextPage = !slice.last
                        page += 1
                    }
                    is FeedbackResult.Error -> {
                        _errorMessage.value = result.message
                        hasNextPage = false
                        _isLoading.value = false
                    }
                    is FeedbackResult.Exception -> {
                        _errorMessage.value = result.throwable.message ?: "네트워크 오류"
                        hasNextPage = false
                        _isLoading.value = false
                    }
                }
            }
            _isLoading.value = false
        }
    }
}
