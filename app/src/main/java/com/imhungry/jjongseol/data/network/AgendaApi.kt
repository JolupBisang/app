package com.imhungry.jjongseol.data.network

import com.imhungry.jjongseol.data.model.AgendaListResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface AgendaApi {
    @GET("api/agendas/{meetingId}")
    suspend fun getAgendas(@Path("meetingId") meetingId: Long): AgendaListResponse
}
