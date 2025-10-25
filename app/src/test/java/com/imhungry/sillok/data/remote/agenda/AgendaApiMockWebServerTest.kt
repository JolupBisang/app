package com.imhungry.sillok.data.remote.agenda

// 이 파일은 MockWebServer를 사용해 Retrofit 기반 AgendaApi의 "계약"을 검증합니다.
// - HTTP 경로/메서드/헤더/바디와 JSON 직렬화·역직렬화를 확인합니다.
// - 성공/클라이언트·서버 에러/타임아웃 등 네트워크 경계 상황을 포함합니다.
// - JVM에서 실행되어(에뮬레이터 불필요) API 인터페이스 안정성을 빠르게 확인할 수 있습니다.

import com.google.gson.Gson
import com.imhungry.sillok.data.model.agenda.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class AgendaApiMockWebServerTest {

    private lateinit var server: MockWebServer
    private lateinit var api: AgendaApi
    private val gson = Gson()

    private fun createApi(client: OkHttpClient? = null): AgendaApi {
        // MockWebServer의 baseUrl로 Retrofit을 구성합니다. 필요 시 커스텀 OkHttpClient를 주입합니다.
        val builder = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
        if (client != null) builder.client(client)
        return builder.build().create(AgendaApi::class.java)
    }

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = createApi()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getAgendas_returnsList_on200() = runTest {
        val body = gson.toJson(
            AgendaDetailResDto(
                agendas = listOf(
                    AgendaListInfoRes(1, "a", false),
                    AgendaListInfoRes(2, "b", true)
                )
            )
        )
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val resp = api.getAgendas(10)

        val recorded = server.takeRequest()
        assertEquals("/api/v1/meetings/10/agendas", recorded.path)
        assertEquals("GET", recorded.method)
        assertEquals(true, resp.isSuccessful)
        assertEquals(2, resp.body()!!.agendas.size)
    }

    @Test
    fun addAgenda_sendsBody_andParses_on200() = runTest {
        val resBody = gson.toJson(
            AgendaCreateResDto(
                meetingId = 10,
                agendaDetails = listOf(AgendaDetailInfoRes(123, "abc"))
            )
        )
        server.enqueue(MockResponse().setResponseCode(200).setBody(resBody))

        val resp = api.addAgenda(10, AgendaCreateReqDto("abc"))

        val recorded = server.takeRequest()
        assertEquals("/api/v1/meetings/10/agendas", recorded.path)
        assertEquals("POST", recorded.method)
        // Body JSON 검사: 요청 페이로드가 예상 스키마인지 확인합니다.
        assertEquals("{\"content\":\"abc\"}", recorded.body.readUtf8())
        assertEquals(true, resp.isSuccessful)
        assertEquals(123L, resp.body()!!.agendaDetails.first().agendaId)
    }

    @Test
    fun changeAgendaStatus_parses_on200() = runTest {
        val resBody = gson.toJson(AgendaChangeStatusResDto(10, 20, true))
        server.enqueue(MockResponse().setResponseCode(200).setBody(resBody))

        val resp = api.changeAgendaStatus(10, 20, AgendaStatusReqDto(true))

        val recorded = server.takeRequest()
        assertEquals("/api/v1/meetings/10/agendas/20/status", recorded.path)
        assertEquals("PATCH", recorded.method)
        assertEquals(true, resp.isSuccessful)
        assertEquals(true, resp.body()!!.isCompleted)
    }

    @Test
    fun updateAgenda_parses_on200() = runTest {
        val resBody = gson.toJson(AgendaUpdateResDto(agendaId = 99))
        server.enqueue(MockResponse().setResponseCode(200).setBody(resBody))

        val resp = api.updateAgenda(10, 20, AgendaUpdateReqDto("hi"))

        val recorded = server.takeRequest()
        assertEquals("/api/v1/meetings/10/agendas/20", recorded.path)
        assertEquals("PATCH", recorded.method)
        assertEquals(true, resp.isSuccessful)
        assertEquals(99L, resp.body()!!.agendaId)
    }

    @Test
    fun deleteAgenda_ok_on200() = runTest {
        val resBody = gson.toJson(AgendaDeletionResDto(meetingId = 10, agendaId = 20))
        server.enqueue(MockResponse().setResponseCode(200).setBody(resBody))

        val resp = api.deleteAgenda(10, 20)

        val recorded = server.takeRequest()
        assertEquals("/api/v1/meetings/10/agendas/20", recorded.path)
        assertEquals("DELETE", recorded.method)
        assertEquals(true, resp.isSuccessful)
    }

    @Test
    fun getAgendas_returnsError_on4xx() = runTest {
        server.enqueue(MockResponse().setResponseCode(404).setBody("not found"))

        val resp = api.getAgendas(99)
        val recorded = server.takeRequest()
        assertEquals("/api/v1/meetings/99/agendas", recorded.path)
        assertEquals(false, resp.isSuccessful)
        assertEquals(404, resp.code())
    }

    @Test
    fun addAgenda_sendsAuthorizationHeader_whenClientInterceptorAddsIt() = runTest {
        val client = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val req = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer TEST_TOKEN")
                    .build()
                chain.proceed(req)
            })
            .build()
        api = createApi(client)

        val resBody = gson.toJson(
            AgendaCreateResDto(
                meetingId = 10,
                agendaDetails = listOf(AgendaDetailInfoRes(1, "x"))
            )
        )
        server.enqueue(MockResponse().setResponseCode(200).setBody(resBody))

        val resp = api.addAgenda(10, AgendaCreateReqDto("x"))

        val recorded = server.takeRequest()
        assertEquals("Bearer TEST_TOKEN", recorded.getHeader("Authorization"))
        assertTrue(resp.isSuccessful)
    }

    @Test
    fun getAgendas_timesOut_whenServerDoesNotRespond() = runTest {
        val timeoutClient = OkHttpClient.Builder()
            .readTimeout(100, TimeUnit.MILLISECONDS)
            .writeTimeout(100, TimeUnit.MILLISECONDS)
            .connectTimeout(100, TimeUnit.MILLISECONDS)
            .build()
        api = createApi(timeoutClient)

        server.enqueue(
            MockResponse()
                .setSocketPolicy(SocketPolicy.NO_RESPONSE)
        )

        var thrown: Throwable? = null
        try {
            api.getAgendas(1)
        } catch (t: Throwable) {
            thrown = t
        }
        assertNotNull(thrown)
    }
}


