package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.response.SliceResponse
import com.imhungry.jjongseol.data.model.segment.response.SegmentListRes
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SegmentApi {
    @GET("/api/segment/{meetingId}")
    suspend fun getSegments(
        @Path("meetingId") meetingId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 40,
        @Query("sort") sort: String = "segmentOrder,DESC"
    ): Response<SliceResponse<SegmentListRes>>
}
