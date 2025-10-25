package com.imhungry.sillok.domain.repository.summary

import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.summary.Summary

interface SummaryRepository {
    suspend fun getSummaries(
        meetingId: Long,
        isRecap: Boolean = false,
        page: Int = 0,
        size: Int = 30
    ): ApiResult<List<Summary>>
}
