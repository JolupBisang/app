package com.imhungry.jjongseol.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.data.model.segment.response.SegmentListRes
import com.imhungry.jjongseol.data.repository.SegmentRepository
import com.imhungry.jjongseol.data.repository.SegmentResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SegmentViewModel @Inject constructor(
    private val segmentRepository: SegmentRepository
) : ViewModel() {
    private val _segments = MutableStateFlow<List<SegmentListRes>>(emptyList())
    val segments: StateFlow<List<SegmentListRes>> = _segments

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var hasNextPage = true
    private var currentPage = 0

    fun loadSegments(meetingId: Long, reset: Boolean = false) {
        if (reset) {
            _segments.value = emptyList()
            currentPage = 0
            hasNextPage = true
        }
        if (!hasNextPage) return

        viewModelScope.launch {
            _isLoading.value = true
            when (val result = segmentRepository.getSegments(meetingId, currentPage)) {
                is SegmentResult.Success -> {
                    val slice = result.data
                    _segments.value = _segments.value + slice.content
                    hasNextPage = !(slice.last)
                    currentPage += 1
                }
                is SegmentResult.Error -> _errorMessage.value = result.message
                is SegmentResult.Exception -> _errorMessage.value = result.throwable.message ?: "네트워크 오류"
            }
            _isLoading.value = false
        }
    }

    fun loadAllSegments(meetingId: Long) {
        _segments.value = emptyList()
        var page = 0
        var hasNextPage = true

        viewModelScope.launch {
            _isLoading.value = true
            while (hasNextPage) {
                when (val result = segmentRepository.getSegments(meetingId, page)) {
                    is SegmentResult.Success -> {
                        val slice = result.data
                        _segments.value = _segments.value + slice.content
                        hasNextPage = !slice.last
                        page += 1
                    }
                    is SegmentResult.Error -> {
                        _errorMessage.value = result.message
                        hasNextPage = false
                        _isLoading.value = false
                    }
                    is SegmentResult.Exception -> {
                        _errorMessage.value = result.throwable.message ?: "네트워크 오류"
                        hasNextPage = false
                        _isLoading.value = false
                    }
                    else -> {
                        hasNextPage = false
                    }
                }
            }
            _isLoading.value = false
        }
    }
}
