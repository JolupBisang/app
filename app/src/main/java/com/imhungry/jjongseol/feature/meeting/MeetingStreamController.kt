package com.imhungry.jjongseol.feature.meeting

import android.content.Context
import com.imhungry.jjongseol.feature.audio.StreamingController
import com.imhungry.jjongseol.feature.audio.devtool.TestDataSender
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class MeetingStreamController @Inject constructor(
    private val streamingController: StreamingController,
    private val testDataSender: TestDataSender,
    @ApplicationContext private val context: Context
) {
    private val _micEnabled = MutableStateFlow(true)
    val micEnabled: StateFlow<Boolean> = _micEnabled.asStateFlow()

    fun startStreaming() = streamingController.startStreamingService()
    fun stopStreaming() = streamingController.stopStreamingService()
    fun pauseEncoding() = streamingController.pauseEncoding()
    fun resumeEncoding() = streamingController.resumeEncoding()

    fun toggleMic(enabled: Boolean) {
        _micEnabled.value = enabled
        if (enabled) resumeEncoding() else pauseEncoding()
        saveMicState(enabled)
    }

    fun loadMicState() {
        val prefs = context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
        _micEnabled.value = prefs.getBoolean("mic_enabled", true)
    }

    private fun saveMicState(enabled: Boolean) {
        val prefs = context.getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("mic_enabled", enabled).apply()
    }

    fun startSendingTestData(meetingId: Long, scope: CoroutineScope) {
        testDataSender.startSummary(meetingId, scope)
        testDataSender.startParticipation(meetingId, scope)
        testDataSender.startFeedback(meetingId, scope)
    }

    fun stopSendingTestData() {
        testDataSender.stopSummary()
        testDataSender.stopParticipation()
        testDataSender.stopFeedback()
    }
}
