package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.summary.response.SummaryListRes
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SummaryApi {
    @GET("/api/v1/meetings/{meetingId}/summary")
    suspend fun getSummaries(
        @Path("meetingId") meetingId: Long,
        @Query("isRecap") isRecap: Boolean = false
    ): Response<SummaryListRes>
}
