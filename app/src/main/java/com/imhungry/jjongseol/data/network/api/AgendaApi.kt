package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.agenda.AgendaChangeStatusRes
import com.imhungry.jjongseol.data.model.common.ApiResponse
import com.imhungry.jjongseol.data.model.agenda.AgendaListResponse
import com.imhungry.jjongseol.data.model.agenda.AgendaStatusReq
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface AgendaApi {
    @GET("api/agendas/{meetingId}")
    suspend fun getAgendas(@Path("meetingId") meetingId: Long): ApiResponse<AgendaListResponse>

    @PATCH("api/agendas/{agendaId}")
    suspend fun changeAgendaStatus(
        @Path("agendaId") agendaId: Long,
        @Body request: AgendaStatusReq
    ): ApiResponse<AgendaChangeStatusRes>
}
