package com.imhungry.jjongseol.data.network.api

import com.imhungry.jjongseol.data.model.common.ApiResponse
import com.imhungry.jjongseol.data.model.agenda.AgendaListResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface AgendaApi {
    @GET("api/agendas/{meetingId}")
    suspend fun getAgendas(@Path("meetingId") meetingId: Long): ApiResponse<AgendaListResponse>
}
