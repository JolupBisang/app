package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.agenda.request.AgendaCreateReq
import com.imhungry.jjongseol.data.model.agenda.request.AgendaStatusReq
import com.imhungry.jjongseol.data.model.agenda.request.AgendaUpdateReq
import com.imhungry.jjongseol.data.model.agenda.response.AgendaStatusChangeRes
import com.imhungry.jjongseol.data.model.agenda.response.AgendaListRes
import com.imhungry.jjongseol.data.model.agenda.response.AgendaCreationRes
import com.imhungry.jjongseol.data.model.agenda.response.AgendaDeletionRes
import com.imhungry.jjongseol.data.model.agenda.response.AgendaUpdateRes
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface AgendaApi {
    @PATCH("/api/v1/meetings/{meetingId}/agendas/{agendaId}/status")
    suspend fun changeAgendaStatus(
        @Path("meetingId") meetingId: Long,
        @Path("agendaId") agendaId: Long,
        @Body agendaStatusReq: AgendaStatusReq
    ): Response<AgendaStatusChangeRes>

    @GET("/api/v1/meetings/{meetingId}/agendas")
    suspend fun getAgendas(
        @Path("meetingId") meetingId: Long
    ): Response<AgendaListRes>

    @POST("/api/v1/meetings/{meetingId}/agendas")
    suspend fun addAgenda(
        @Path("meetingId") meetingId: Long,
        @Body req: AgendaCreateReq
    ): Response<AgendaCreationRes>

    @DELETE("/api/v1/meetings/{meetingId}/agendas/{agendaId}")
    suspend fun deleteAgenda(
        @Path("meetingId") meetingId: Long,
        @Path("agendaId") agendaId: Long
    ): Response<AgendaDeletionRes>

    @PATCH("/api/v1/meetings/{meetingId}/agendas/{agendaId}")
    suspend fun updateAgenda(
        @Path("meetingId") meetingId: Long,
        @Path("agendaId") agendaId: Long,
        @Body req: AgendaUpdateReq
    ): Response<AgendaUpdateRes>

}