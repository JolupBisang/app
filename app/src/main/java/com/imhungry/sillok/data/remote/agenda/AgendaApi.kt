package com.imhungry.sillok.data.remote.agenda

import com.imhungry.sillok.data.model.agenda.AgendaChangeStatusResDto
import com.imhungry.sillok.data.model.agenda.AgendaCreateReqDto
import com.imhungry.sillok.data.model.agenda.AgendaCreateResDto
import com.imhungry.sillok.data.model.agenda.AgendaDeletionResDto
import com.imhungry.sillok.data.model.agenda.AgendaDetailResDto
import com.imhungry.sillok.data.model.agenda.AgendaStatusReqDto
import com.imhungry.sillok.data.model.agenda.AgendaUpdateReqDto
import com.imhungry.sillok.data.model.agenda.AgendaUpdateResDto
import retrofit2.http.*
import retrofit2.Response

interface AgendaApi {
    @GET("/api/v1/meetings/{meetingId}/agendas")
    suspend fun getAgendas(
        @Path("meetingId") meetingId: Long
    ): Response<AgendaDetailResDto>

    @POST("/api/v1/meetings/{meetingId}/agendas")
    suspend fun addAgenda(
        @Path("meetingId") meetingId: Long,
        @Body request: AgendaCreateReqDto
    ): Response<AgendaCreateResDto>

    @PATCH("/api/v1/meetings/{meetingId}/agendas/{agendaId}")
    suspend fun updateAgenda(
        @Path("meetingId") meetingId: Long,
        @Path("agendaId") agendaId: Long,
        @Body request: AgendaUpdateReqDto
    ): Response<AgendaUpdateResDto>

    @PATCH("/api/v1/meetings/{meetingId}/agendas/{agendaId}/status")
    suspend fun changeAgendaStatus(
        @Path("meetingId") meetingId: Long,
        @Path("agendaId") agendaId: Long,
        @Body request: AgendaStatusReqDto
    ): Response<AgendaChangeStatusResDto>

    @DELETE("/api/v1/meetings/{meetingId}/agendas/{agendaId}")
    suspend fun deleteAgenda(
        @Path("meetingId") meetingId: Long,
        @Path("agendaId") agendaId: Long
    ): Response<AgendaDeletionResDto>
}
