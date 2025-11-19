package com.imhungry.sillok.presentation.service

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class RealMeetingRealtimeEventSource @Inject constructor(
    private val app: Application
) : MeetingRealtimeEventSource {

    companion object {
        private const val TAG = "RealMeetingRealtimeES"
    }

    override val events: Flow<ServiceEvent>
        @RequiresApi(Build.VERSION_CODES.O)
        get() = MeetingInProgressService.serviceEvents

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun start(
        serverUrl: String,
        meetingId: Long,
        jwtToken: String
    ) {
        Log.d(
            TAG,
            "[start] Foreground Service 시작: serverUrl=$serverUrl, meetingId=$meetingId"
        )

        val intent = Intent(app, MeetingInProgressService::class.java).apply {
            action = MeetingInProgressService.ACTION_START
            putExtra(MeetingInProgressService.EXTRA_SERVER_URL, serverUrl)
            putExtra(MeetingInProgressService.EXTRA_MEETING_ID, meetingId)
            putExtra(MeetingInProgressService.EXTRA_JWT_TOKEN, jwtToken)
        }
        ContextCompat.startForegroundService(app, intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun stop() {
        Log.d(TAG, "[stop] Service 중지 요청")
        val intent = Intent(app, MeetingInProgressService::class.java).apply {
            action = MeetingInProgressService.ACTION_STOP
        }
        app.startService(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun toggleMic() {
        Log.d(TAG, "[toggleMic] 마이크 토글 요청")
        val intent = Intent(app, MeetingInProgressService::class.java).apply {
            action = MeetingInProgressService.ACTION_TOGGLE_MIC
        }
        app.startService(intent)
    }
}
