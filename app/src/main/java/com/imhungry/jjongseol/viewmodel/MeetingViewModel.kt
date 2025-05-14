package com.imhungry.jjongseol.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.imhungry.jjongseol.data.model.error.ApiError
import com.imhungry.jjongseol.data.model.meeting.MeetingDetailRes
import com.imhungry.jjongseol.data.model.meeting.MeetingReq
import com.imhungry.jjongseol.data.network.api.MeetingApi
import com.imhungry.jjongseol.data.network.client.handleHttpException
import com.imhungry.jjongseol.feature.meeting.MeetingSseSubscriber
import com.imhungry.jjongseol.feature.meeting.MeetingStreamController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class MeetingViewModel @Inject constructor(
    application: Application,
    private val meetingApi: MeetingApi,
    val streamController: MeetingStreamController,
    val sseSubscriber: MeetingSseSubscriber
) : BaseAndroidViewModel(application) {

    init {
        streamController.onWebSocketErrorMessage = { msg ->
            setError(ApiError(msg, null))
        }
    }

    private val _meetingDetail = MutableStateFlow<MeetingDetailRes?>(null)
    val meetingDetail: StateFlow<MeetingDetailRes?> = _meetingDetail

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

    suspend fun loadMeetingDetail(meetingId: Long): Boolean {
        return try {
            val result = meetingApi.getMeetingDetail(meetingId)
            _meetingDetail.value = result.data
            true
        } catch (e: HttpException) {
            setError(handleHttpException(e))
            false
        } catch (e: Exception) {
            setError(ApiError(message = e.message, errorId = null))
            false
        }
    }

    fun initializeSession(
        meetingId: Long,
        timeProvider: () -> String,
        scope: CoroutineScope
    ) {
        val token = getJwtToken()

        if (streamController.startStreamingSafely(meetingId, token)) {
            streamController.resumeEncoding()
        }

        /*sseSubscriber.apply {
            subscribeToSummary(meetingId, timeProvider)
            subscribeToParticipationRate(meetingId)
            subscribeToFeedback(meetingId, timeProvider)
        }

        streamController.startSendingTestData(meetingId, scope)*/
    }

    fun cleanupSession() {
        streamController.stopStreaming()
        streamController.stopSendingTestData()
        streamController.resetMicState()
        //sseSubscriber.stopSse()
    }

    private fun getJwtToken(): String {
        val prefs = getApplication<Application>().getSharedPreferences("auth", Context.MODE_PRIVATE)
        return prefs.getString("jwt_token", "") ?: ""
    }
}
