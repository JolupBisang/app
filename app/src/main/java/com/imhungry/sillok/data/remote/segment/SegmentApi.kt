package com.imhungry.sillok.data.remote.segment

import com.imhungry.sillok.data.model.segment.SegmentListResDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface SegmentApi {
    @GET("/api/v1/meeting/{meetingId}/segments")
    suspend fun getSegments(
        @Path("meetingId") meetingId: Long
    ): Response<SegmentListResDto>
}
