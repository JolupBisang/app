package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.agenda.dto.AgendaDto
import com.imhungry.jjongseol.data.model.agenda.request.AgendaCreateReq
import com.imhungry.jjongseol.data.model.agenda.request.AgendaStatusReq
import com.imhungry.jjongseol.data.model.agenda.request.AgendaUpdateReq
import com.imhungry.jjongseol.data.model.agenda.response.AgendaChangeStatusRes
import com.imhungry.jjongseol.data.model.agenda.response.AgendaDetailRes
import com.imhungry.jjongseol.data.model.response.SuccessResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
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

    @POST("/api/meetings/{meetingId}/agendas")
    suspend fun addAgenda(
        @Path("meetingId") meetingId: Long,
        @Body req: AgendaCreateReq
    ): Response<SuccessResponse<AgendaDto>>

    @DELETE("/api/agendas/{agendaId}")
    suspend fun deleteAgenda(
        @Path("agendaId") agendaId: Long
    ): Response<SuccessResponse<Unit>>

    @PATCH("/api/agendas/content/{agendaId}")
    suspend fun updateAgenda(
        @Path("agendaId") agendaId: Long,
        @Body req: AgendaUpdateReq
    ): Response<SuccessResponse<Long>>

}