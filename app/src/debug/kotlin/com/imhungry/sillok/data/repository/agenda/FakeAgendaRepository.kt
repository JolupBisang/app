package com.imhungry.sillok.data.repository.agenda

import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.agenda.Agenda
import com.imhungry.sillok.domain.repository.agenda.AgendaRepository
import javax.inject.Inject
import javax.inject.Singleton

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
