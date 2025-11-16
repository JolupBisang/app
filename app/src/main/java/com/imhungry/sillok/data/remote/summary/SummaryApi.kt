package com.imhungry.sillok.data.remote.summary

import com.imhungry.sillok.data.model.summary.SummaryListResDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SummaryApi {
    @GET("/api/v1/meetings/{meetingId}/summary")
    suspend fun getSummaries(
        @Path("meetingId") meetingId: Long,
        @Query("isRecap") isRecap: Boolean = false,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 30,
        @Query("sort") sort: String = "generatedDateTime",
        @Query("direction") direction: String = "asc"
    ): Response<SummaryListResDto>
}
