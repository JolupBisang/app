package com.imhungry.jjongseol.controller

import com.imhungry.jjongseol.data.network.FeedbackApi
import com.imhungry.jjongseol.data.network.ParticipationRateApi
import com.imhungry.jjongseol.data.network.SummaryApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

class TestDataSender @Inject constructor(
    private val summaryApi: SummaryApi,
    private val participationRateApi: ParticipationRateApi,
    private val feedbackApi: FeedbackApi
) {
    private var summaryJob: Job? = null
    private var participationJob: Job? = null
    private var feedbackJob: Job? = null

    fun startSummary(meetingId: Long, scope: CoroutineScope) {
        if (summaryJob?.isActive == true) return
        summaryJob = scope.launch {
            while (isActive) {
                runCatching { summaryApi.sendTestSummary(meetingId) }
                    .onFailure { println("요약 전송 실패: ${it.message}") }
                delay(5000)
            }
        }
    }

    fun stopSummary() {
        summaryJob?.cancel()
        summaryJob = null
    }

    fun startParticipation(meetingId: Long, scope: CoroutineScope) {
        if (participationJob?.isActive == true) return
        participationJob = scope.launch {
            while (isActive) {
                runCatching { participationRateApi.sendTestParticipationRate(meetingId) }
                    .onFailure { println("점유율 전송 실패: ${it.message}") }
                delay(5000)
            }
        }
    }

    fun stopParticipation() {
        participationJob?.cancel()
        participationJob = null
    }

    fun startFeedback(meetingId: Long, scope: CoroutineScope) {
        if (feedbackJob?.isActive == true) return
        feedbackJob = scope.launch {
            while (isActive) {
                runCatching { feedbackApi.sendTestFeedback(meetingId) }
                    .onFailure { println("피드백 전송 실패: ${it.message}") }
                delay(5000)
            }
        }
    }

    fun stopFeedback() {
        feedbackJob?.cancel()
        feedbackJob = null
    }
}