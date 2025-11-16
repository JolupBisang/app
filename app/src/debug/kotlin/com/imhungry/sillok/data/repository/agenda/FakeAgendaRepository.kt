package com.imhungry.sillok.data.repository.agenda

import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.agenda.Agenda
import com.imhungry.sillok.domain.repository.agenda.AgendaRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * debug 전용 AgendaRepository
 * - 서버 요청 없이 UI / ViewModel 테스트 가능
 * - 더미 아젠다 리스트를 제공하여 회의 진행 화면 테스트에 유용
 */
@Singleton
class FakeAgendaRepository @Inject constructor() : AgendaRepository {

    companion object {
        // 더미 아젠다 내용 목록
        private val DUMMY_AGENDA_CONTENTS = listOf(
            "프로젝트 목표와 범위 합의",
            "역할 분담 및 일정 수립",
            "기술 스택 및 아키텍처 논의",
            "다음 회의 일정 결정"
        )
    }

    override suspend fun getAgendas(meetingId: Long): ApiResult<List<Agenda>> {
        // 더미 아젠다 리스트 생성
        // 첫 번째 아젠다는 완료 상태, 나머지는 미완료 상태
        val agendas = DUMMY_AGENDA_CONTENTS.mapIndexed { index, content ->
            Agenda(
                agendaId = meetingId * 100L + index + 1, // 고유 ID 생성
                content = content,
                isCompleted = index == 0 // 첫 번째만 완료 상태
            )
        }
        return ApiResult.Success(agendas)
    }

    override suspend fun addAgenda(meetingId: Long, content: List<String>): ApiResult<List<Long>> {
        // 신규 Agenda ID 리스트를 가짜로 반환
        // meetingId를 기반으로 고유한 ID 생성
        val baseId = meetingId * 100L + DUMMY_AGENDA_CONTENTS.size + 1
        val fakeIds = content.indices.map { baseId + it }
        return ApiResult.Success(fakeIds)
    }

    override suspend fun updateAgenda(
        meetingId: Long,
        agendaId: Long,
        content: String
    ): ApiResult<Long> {
        // 성공했다고 가정하고 동일 ID 반환
        return ApiResult.Success(agendaId)
    }

    override suspend fun changeAgendaStatus(
        meetingId: Long,
        agendaId: Long,
        isCompleted: Boolean
    ): ApiResult<Boolean> {
        // 항상 성공
        return ApiResult.Success(true)
    }

    override suspend fun deleteAgenda(meetingId: Long, agendaId: Long): ApiResult<Unit> {
        // 항상 성공
        return ApiResult.Success(Unit)
    }
}
