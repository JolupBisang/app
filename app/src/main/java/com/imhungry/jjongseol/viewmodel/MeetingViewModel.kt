package com.imhungry.jjongseol.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.data.model.error.ApiError
import com.imhungry.jjongseol.data.model.meeting.MeetingReq
import com.imhungry.jjongseol.data.network.api.MeetingApi
import com.imhungry.jjongseol.feature.meeting.MeetingSseSubscriber
import com.imhungry.jjongseol.feature.meeting.MeetingStreamController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeetingViewModel @Inject constructor(
    application: Application,
    private val meetingApi: MeetingApi,
    val streamController: MeetingStreamController,
    val sseSubscriber: MeetingSseSubscriber
) : AndroidViewModel(application) {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun setError(apiError: ApiError) {
        _errorMessage.value = when (apiError.message) {
            "만료된 토큰입니다." -> "TOKEN_EXPIRED"
            else -> apiError.message ?: "알 수 없는 오류 발생"
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun createMeeting(
        meetingReq: MeetingReq,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = meetingApi.createMeeting(meetingReq)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("에러 발생: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                onError("예외 발생: ${e.message}")
            }
        }
    }
}
