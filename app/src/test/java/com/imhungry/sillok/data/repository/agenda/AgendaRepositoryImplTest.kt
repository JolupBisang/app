package com.imhungry.sillok.data.repository.agenda

// 이 파일은 AgendaRepositoryImpl에 대한 순수 단위 테스트입니다.
// - AgendaApi는 mockk로 모킹하며, 실제 네트워크/Retrofit은 관여하지 않습니다.
// - 분기 처리, 매핑(AgendaMapper를 통한 DTO → 도메인), 오류의 ApiResult 전파를 검증합니다.
// - DI나 Android 런타임 없이 저장소 로직을 빠르게 피드백하기 위한 테스트입니다.

import com.imhungry.sillok.data.mapper.agenda.AgendaMapper
import com.imhungry.sillok.data.model.agenda.*
import com.imhungry.sillok.data.remote.agenda.AgendaApi
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.agenda.Agenda
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AgendaRepositoryImplTest {

    private val api: AgendaApi = mockk()
    private val mapper = AgendaMapper()
    private val repository = AgendaRepositoryImpl(api, mapper)

    @Test
    fun `getAgendas - 성공적으로 목록을 반환한다`() = runTest {
        // 목적: 성공 시 매핑된 도메인 리스트 반환을 검증
        // given
        val meetingId = 1L
        val dto = AgendaDetailResDto(
            agendas = listOf(
                AgendaListInfoRes(id = 10L, content = "a", isCompleted = false),
                AgendaListInfoRes(id = 11L, content = "b", isCompleted = true)
            )
        )
        coEvery { api.getAgendas(meetingId) } returns Response.success(dto)

        // when
        val result = repository.getAgendas(meetingId)

        // then
        assertTrue(result is ApiResult.Success)
        val data = (result as ApiResult.Success).data
        assertEquals(2, data.size)
        assertEquals(Agenda(10L, "a", false), data[0])
        assertEquals(Agenda(11L, "b", true), data[1])
    }

    @Test
    fun `getAgendas - HTTP 에러면 Failure를 반환한다`() = runTest {
        // 목적: HTTP 에러가 ApiResult.Failure로 매핑됨을 검증
        // given
        val meetingId = 1L
        val errorBody = "not found".toResponseBody("text/plain".toMediaType())
        coEvery { api.getAgendas(meetingId) } returns Response.error(404, errorBody)

        // when
        val result = repository.getAgendas(meetingId)

        // then
        assertTrue(result is ApiResult.Failure)
    }

    @Test
    fun `getAgendas - 예외 발생 시 Failure를 반환한다`() = runTest {
        // 목적: 호출 중 예외 발생 시 ApiResult.Failure로 매핑됨을 검증
        // given
        val meetingId = 1L
        coEvery { api.getAgendas(meetingId) } throws RuntimeException("boom")

        // when
        val result = repository.getAgendas(meetingId)

        // then
        assertTrue(result is ApiResult.Failure)
    }

    @Test
    fun `addAgenda - 성공적으로 생성된 ID를 반환한다`() = runTest {
        // 목적: 생성된 agendaId가 정상 파싱됨을 검증
        // given
        val meetingId = 1L
        val content = "new agenda"
        val res = AgendaCreateResDto(
            meetingId = meetingId,
            agendaDetails = listOf(AgendaDetailInfoRes(agendaId = 123L, content = content))
        )
        coEvery { api.addAgenda(meetingId, AgendaCreateReqDto(content)) } returns Response.success(res)

        // when
        val result = repository.addAgenda(meetingId, content)

        // then
        assertTrue(result is ApiResult.Success)
        assertEquals(123L, (result as ApiResult.Success).data)
    }

    @Test
    fun `addAgenda - 응답 파싱 실패 시 Failure를 반환한다`() = runTest {
        // 목적: 필수 필드 누락 등으로 파싱 실패 시 ApiResult.Failure가 반환됨을 검증
        // given
        val meetingId = 1L
        val content = "new agenda"
        val res = AgendaCreateResDto(
            meetingId = meetingId,
            agendaDetails = listOf(AgendaDetailInfoRes(agendaId = null, content = content))
        )
        coEvery { api.addAgenda(meetingId, AgendaCreateReqDto(content)) } returns Response.success(res)

        // when
        val result = repository.addAgenda(meetingId, content)

        // then
        assertTrue(result is ApiResult.Failure)
    }

    @Test
    fun `updateAgenda - 성공적으로 업데이트된 ID를 반환한다`() = runTest {
        // 목적: 성공 시 업데이트된 ID 반환을 검증
        // given
        val meetingId = 1L
        val agendaId = 22L
        val content = "updated"
        val res = AgendaUpdateResDto(agendaId = agendaId)
        coEvery { api.updateAgenda(meetingId, agendaId, AgendaUpdateReqDto(content)) } returns Response.success(res)

        // when
        val result = repository.updateAgenda(meetingId, agendaId, content)

        // then
        assertTrue(result is ApiResult.Success)
        assertEquals(agendaId, (result as ApiResult.Success).data)
    }

    @Test
    fun `changeAgendaStatus - 성공적으로 상태를 반환한다`() = runTest {
        // 목적: 성공 시 완료 상태(Boolean) 반환을 검증
        // given
        val meetingId = 1L
        val agendaId = 22L
        val isCompleted = true
        val res = AgendaChangeStatusResDto(meetingId = meetingId, agendaId = agendaId, isCompleted = isCompleted)
        coEvery { api.changeAgendaStatus(meetingId, agendaId, AgendaStatusReqDto(isCompleted)) } returns Response.success(res)

        // when
        val result = repository.changeAgendaStatus(meetingId, agendaId, isCompleted)

        // then
        assertTrue(result is ApiResult.Success)
        assertEquals(isCompleted, (result as ApiResult.Success).data)
    }

    @Test
    fun `deleteAgenda - 성공적으로 완료된다`() = runTest {
        // 목적: 성공 시 ApiResult.Success(Unit) 반환을 검증
        // given
        val meetingId = 1L
        val agendaId = 22L
        val res = AgendaDeletionResDto(meetingId = meetingId, agendaId = agendaId)
        coEvery { api.deleteAgenda(meetingId, agendaId) } returns Response.success(res)

        // when
        val result = repository.deleteAgenda(meetingId, agendaId)

        // then
        assertTrue(result is ApiResult.Success)
    }
}


