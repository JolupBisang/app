package com.imhungry.sillok.data.repository.agenda

// 이 파일은 JVM에서 수행하는 슬라이스 통합 테스트입니다.
// - Hilt 없이 RepositoryImpl + Retrofit + Gson + MockWebServer를 직접 연결합니다.
// - HTTP 응답부터 Repository의 ApiResult 매핑까지 끝단 흐름을 검증합니다.
// - androidTest보다 빠르고 Android 런타임/DI에 독립적입니다.

import com.google.gson.Gson
import com.imhungry.sillok.data.mapper.agenda.AgendaMapper
import com.imhungry.sillok.data.model.agenda.*
import com.imhungry.sillok.data.remote.agenda.AgendaApi
import com.imhungry.sillok.data.util.ApiResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
class AgendaRepositoryImplNetworkTest {

    private lateinit var server: MockWebServer
    private lateinit var api: AgendaApi
    private lateinit var repository: AgendaRepositoryImpl
    private val gson = Gson()

    @Before
    fun setUp() {
        // MockWebServer를 시작하고 Retrofit API를 생성한 뒤, 실제 Mapper로 Repository를 구성합니다.
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AgendaApi::class.java)
        repository = AgendaRepositoryImpl(api, AgendaMapper())
    }

    @After
    fun tearDown() {
        // 각 테스트 종료 후 서버 리소스를 정리합니다.
        server.shutdown()
    }

    @Test
    fun getAgendas_success() = runTest {
        val body = gson.toJson(
            AgendaDetailResDto(
                agendas = listOf(
                    AgendaListInfoRes(1, "a", false),
                    AgendaListInfoRes(2, "b", true)
                )
            )
        )
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val result = repository.getAgendas(10)

        assertTrue(result is ApiResult.Success)
        val data = (result as ApiResult.Success).data
        assertEquals(2, data.size)
        val recorded = server.takeRequest()
        assertEquals("/api/v1/meetings/10/agendas", recorded.path)
        assertEquals("GET", recorded.method)
    }

    @Test
    fun addAgenda_success() = runTest {
        val body = gson.toJson(
            AgendaCreateResDto(
                meetingId = 10,
                agendaDetails = listOf(AgendaDetailInfoRes(99, "x"))
            )
        )
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val result = repository.addAgenda(10, "x")
        assertTrue(result is ApiResult.Success)
        assertEquals(99, (result as ApiResult.Success).data)
        val recorded = server.takeRequest()
        assertEquals("/api/v1/meetings/10/agendas", recorded.path)
        assertEquals("POST", recorded.method)
    }

    @Test
    fun updateAgenda_success() = runTest {
        val body = gson.toJson(AgendaUpdateResDto(agendaId = 7))
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val result = repository.updateAgenda(10, 7, "y")
        assertTrue(result is ApiResult.Success)
        assertEquals(7, (result as ApiResult.Success).data)
        val recorded = server.takeRequest()
        assertEquals("/api/v1/meetings/10/agendas/7", recorded.path)
        assertEquals("PATCH", recorded.method)
    }

    @Test
    fun changeAgendaStatus_success() = runTest {
        val body = gson.toJson(AgendaChangeStatusResDto(10, 7, true))
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val result = repository.changeAgendaStatus(10, 7, true)
        assertTrue(result is ApiResult.Success)
        assertEquals(true, (result as ApiResult.Success).data)
        val recorded = server.takeRequest()
        assertEquals("/api/v1/meetings/10/agendas/7/status", recorded.path)
        assertEquals("PATCH", recorded.method)
    }

    @Test
    fun deleteAgenda_success() = runTest {
        val body = gson.toJson(AgendaDeletionResDto(meetingId = 10, agendaId = 7))
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val result = repository.deleteAgenda(10, 7)
        assertTrue(result is ApiResult.Success)
        val recorded = server.takeRequest()
        assertEquals("/api/v1/meetings/10/agendas/7", recorded.path)
        assertEquals("DELETE", recorded.method)
    }
}


