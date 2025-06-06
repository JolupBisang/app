package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.agenda.request.AgendaStatusReq
import com.imhungry.jjongseol.data.model.agenda.response.AgendaChangeStatusRes
import com.imhungry.jjongseol.data.model.agenda.response.AgendaDetailRes
import com.imhungry.jjongseol.data.model.response.SuccessResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface AgendaApi {
    @PATCH("/api/agendas/{agendaId}")
    suspend fun changeAgendaStatus(
        @Path("agendaId") agendaId: Long,
        @Body agendaStatusReq: AgendaStatusReq
    ): Response<SuccessResponse<AgendaChangeStatusRes>>

    @GET("/api/meetings/{meetingId}/agendas")
    suspend fun getAgendas(
        @Path("meetingId") meetingId: Long
    ): Response<SuccessResponse<AgendaDetailRes>>
}