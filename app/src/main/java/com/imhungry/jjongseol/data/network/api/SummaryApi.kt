package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.response.SliceResponse
import com.imhungry.jjongseol.data.model.summary.response.SummaryListRes
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SummaryApi {
    @GET("/api/summary/{meetingId}")
    suspend fun getSummaries(
        @Path("meetingId") meetingId: Long,
        @Query("isRecap") isRecap: Boolean = false,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 30,
        @Query("sort") sort: String = "timestamp,ASC"
    ): Response<SliceResponse<SummaryListRes>>
}
