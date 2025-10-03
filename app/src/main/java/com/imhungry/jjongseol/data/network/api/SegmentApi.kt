package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.segment.response.SegmentListRes
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SegmentApi {
    @GET("/api/v1/meeting/{meetingId}/segments")
    suspend fun getSegments(
        @Path("meetingId") meetingId: Long
    ): Response<SegmentListRes>
}
