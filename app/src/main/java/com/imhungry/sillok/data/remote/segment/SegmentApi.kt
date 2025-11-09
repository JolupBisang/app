package com.imhungry.sillok.data.remote.segment

import com.imhungry.sillok.data.model.segment.SegmentListResDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SegmentApi {
    @GET("/api/v1/meetings/{meetingId}/segments")
    suspend fun getSegments(
        @Path("meetingId") meetingId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 40
    ): Response<SegmentListResDto>
}
