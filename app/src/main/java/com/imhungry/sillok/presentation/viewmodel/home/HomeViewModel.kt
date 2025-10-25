package com.imhungry.sillok.presentation.viewmodel.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.data.local.UserStore
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.meeting.MeetingDetailSummary
import com.imhungry.sillok.domain.model.meeting.MeetingStatus
import com.imhungry.sillok.domain.usecase.meeting.GetMeetingSummaryListUseCase
import com.imhungry.sillok.domain.usecase.user.GetMyProfileUseCase
import com.imhungry.sillok.presentation.state.home.HomeState
import com.imhungry.sillok.presentation.state.home.OngoingMeeting
import com.imhungry.sillok.presentation.util.DateTimeUtils.calcEndTimeIsoLocal
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import java.util.Calendar
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMeetingSummaryListUseCase: GetMeetingSummaryListUseCase,
    private val userStore: UserStore
) : ViewModel() {
    private val TAG = "HomeViewModel"

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()
    private var currentYearMonth: Pair<Int, Int>? = null
    private var hasMeetingListener: ListenerRegistration? = null

    init {
        // 사용자 닉네임 반영 및 hasMeeting 리스너 연결
        viewModelScope.launch {
            userStore.user.collect { user ->
                _state.update { current ->
                    current.copy(userName = user?.nickname ?: current.userName)
                }

                // 파이어베이스 users/{uid}의 hasMeeting 리스너 연결
                val uid = user?.id?.toString()
                if (!uid.isNullOrBlank()) {
                    attachHasMeetingListener(uid)
                }
            }
        }

        // 홈 데이터 초기 로드
        //loadHomeData()
        loadDummyHomeState()
        // hasMeeting 플래그 모니터링: true면 로드 후 false로 리셋
        viewModelScope.launch {
            state.collect { s ->
                if (s.hasMeeting) {
                    loadHomeData()
                    _state.update { it.copy(hasMeeting = false) }
                }
            }
        }
    }

    private fun attachHasMeetingListener(uid: String) {
        hasMeetingListener?.remove()
        val db = FirebaseFirestore.getInstance()
        hasMeetingListener = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, _ ->
                val hasMeeting = snapshot?.getBoolean("hasMeeting") ?: false
                if (hasMeeting) {
                    loadHomeData()
                    db.collection("users").document(uid)
                        .update(mapOf("hasMeeting" to false, "updatedAt" to System.currentTimeMillis()))
                }
                _state.update { it.copy(hasMeeting = hasMeeting) }
            }
    }

    public override fun onCleared() {
        super.onCleared()
        hasMeetingListener?.remove()
        hasMeetingListener = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadHomeData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH는 0부터 시작
                currentYearMonth = year to month

                var aggScheduled = emptyList<MeetingDetailSummary>()
                var aggPast = emptyList<MeetingDetailSummary>()
                var aggOngoingUi = emptyList<OngoingMeeting>()

                var curY = year
                var curM = month
                var monthsTried = 0
                val maxMonths = 12

                while ((aggScheduled.size + aggPast.size) < 8 && monthsTried < maxMonths) {
                    when (val result = getMeetingSummaryListUseCase(curY, curM)) {
                        is ApiResult.Success -> {
                            val summaries: List<MeetingDetailSummary> = result.data
                            val scheduled = summaries.filter { MeetingStatus.from(it.status) == MeetingStatus.WAITING }
                            val past = summaries.filter { MeetingStatus.from(it.status) == MeetingStatus.COMPLETED }
                            val ongoing = summaries.filter { MeetingStatus.from(it.status) == MeetingStatus.IN_PROGRESS }

                            Log.d(
                                TAG,
                                "월별 로드 성공: ${curY}-${curM} 전체=${summaries.size}, 예정=${scheduled.size}, 진행=${ongoing.size}, 종료=${past.size}\n"
                            )

                            val ongoingUi = ongoing.map {
                                OngoingMeeting(
                                    id = it.id,
                                    title = it.title,
                                    scheduledStartTime = it.scheduledStartTime,
                                    scheduledEndTime = calcEndTimeIsoLocal(it.scheduledStartTime, it.targetTime)
                                )
                            }

                            aggScheduled = aggScheduled + scheduled
                            aggPast = aggPast + past
                            aggOngoingUi = aggOngoingUi + ongoingUi
                        }
                        is ApiResult.Failure -> {
                        }
                    }

                    if (curM == 1) {
                        curY -= 1
                        curM = 12
                    } else {
                        curM -= 1
                    }
                    monthsTried += 1
                }

                currentYearMonth = curY to curM

                Log.d(
                    TAG,
                    "누적 결과: 예정=${aggScheduled.size}, 진행=${aggOngoingUi.size}, 종료=${aggPast.size}; 다음 기준=${curY}-${curM}"
                )

                _state.update {
                    it.copy(
                        isLoading = false,
                        scheduledMeetings = aggScheduled,
                        pastMeetings = aggPast,
                        ongoingMeetings = aggOngoingUi,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "오류가 발생했습니다."
                    )
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadPreviousMonth() {
        viewModelScope.launch {
            try {
                val (cy, cm) = currentYearMonth ?: run {
                    val cal = Calendar.getInstance()
                    cal.get(Calendar.YEAR) to (cal.get(Calendar.MONTH) + 1)
                }

                val prevYear: Int
                val prevMonth: Int
                if (cm == 1) {
                    prevYear = cy - 1
                    prevMonth = 12
                } else {
                    prevYear = cy
                    prevMonth = cm - 1
                }

                when (val result = getMeetingSummaryListUseCase(prevYear, prevMonth)) {
                    is ApiResult.Success -> {
                        val summaries: List<MeetingDetailSummary> = result.data
                        val scheduledNew = summaries.filter { MeetingStatus.from(it.status) == MeetingStatus.WAITING }
                        val pastNew = summaries.filter { MeetingStatus.from(it.status) == MeetingStatus.COMPLETED }
                        val ongoingNew = summaries.filter { MeetingStatus.from(it.status) == MeetingStatus.IN_PROGRESS }

                        // UI용 모델로 변환
                        val ongoingUiNew = ongoingNew.map {
                            OngoingMeeting(
                                id = it.id,
                                title = it.title,
                                scheduledStartTime = it.scheduledStartTime,
                                scheduledEndTime = calcEndTimeIsoLocal(it.scheduledStartTime, it.targetTime)
                            )
                        }

                        _state.update { current ->
                            current.copy(
                                scheduledMeetings = current.scheduledMeetings + scheduledNew,
                                pastMeetings = current.pastMeetings + pastNew,
                                ongoingMeetings = current.ongoingMeetings + ongoingUiNew
                            )
                        }

                        currentYearMonth = prevYear to prevMonth
                    }
                    is ApiResult.Failure -> {
                        _state.update {
                            it.copy(error = result.message ?: "이전 달 데이터를 불러오지 못했습니다.")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "이전 달 로드 중 예외 발생: ${e.localizedMessage}", e)
                _state.update { it.copy(error = e.localizedMessage ?: "오류가 발생했습니다.") }
            }
        }
    }

    fun refreshData() {
        loadHomeData()
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    // 검색 텍스트 변경 처리 및 Firestore 쿼리 실행
    fun onSearchTextChange(text: String) {
        _state.update { it.copy(searchText = text) }
        if (text.isBlank()) {
            _state.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }
        performSearch(text)
    }

    private fun normalize(s: String): String = s.trim().lowercase()

    private fun mapMeetingDoc(data: Map<String, Any?>): MeetingDetailSummary? {
        val id = (data["meetingId"] as? Number)?.toLong() ?: return null
        val title = data["title"] as? String ?: ""
        val scheduledStartTime = data["scheduledStartTime"] as? String ?: ""
        val targetTime = (data["targetTime"] as? Number)?.toInt() ?: 0
        val status = data["meetingStatus"] as? String ?: "WAITING"
        return MeetingDetailSummary(
            id = id,
            title = title,
            scheduledStartTime = scheduledStartTime,
            targetTime = targetTime,
            status = status
        )
    }

    private fun buildPrefixRange(query: String): Pair<String, String> {
        val start = query
        val end = query + "\uf8ff"
        return start to end
    }

    private fun performSearch(rawText: String) {
        viewModelScope.launch {
            val text = normalize(rawText)
            _state.update { it.copy(isSearching = true) }
            try {
                val db = FirebaseFirestore.getInstance()

                val (start, end) = buildPrefixRange(text)

                val titleSnap = db.collection("meetings")
                    .orderBy("title")
                    .startAt(start)
                    .endAt(end)
                    .limit(25)
                    .get()
                    .await()

                val titleResults = titleSnap.documents.mapNotNull { mapMeetingDoc(it.data ?: emptyMap()) }

                // users 에서 이메일 prefix 찾기
                val userSnap = db.collection("users")
                    .orderBy("email")
                    .startAt(start)
                    .endAt(end)
                    .limit(25)
                    .get()
                    .await()

                val candidateEmails = userSnap.documents.mapNotNull { it.getString("email") }.toSet()

                val participantResults = mutableListOf<MeetingDetailSummary>()
                // meetings 에서 participants 배열에 후보 이메일이 포함된 회의 조회 (전체 일치)
                for (email in candidateEmails) {
                    val pSnap = db.collection("meetings")
                        .whereArrayContains("participants", email)
                        .limit(25)
                        .get()
                        .await()
                    participantResults += pSnap.documents.mapNotNull { mapMeetingDoc(it.data ?: emptyMap()) }
                }

                // 통합 및 중복 제거
                val merged = (titleResults + participantResults).distinctBy { it.id }

                _state.update { it.copy(searchResults = merged, isSearching = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isSearching = false, error = e.localizedMessage) }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadDummyHomeState() {
        viewModelScope.launch {
            // 더미 회의 데이터 생성
            val scheduled = listOf(
                MeetingDetailSummary(
                    id = 1001L,
                    title = "프로덕트 킥오프 회의",
                    scheduledStartTime = "2025-10-25T10:00:00",
                    targetTime = 60,
                    status = "WAITING"
                ),
                MeetingDetailSummary(
                    id = 1002L,
                    title = "디자인 리뷰",
                    scheduledStartTime = "2025-10-27T15:30:00",
                    targetTime = 45,
                    status = "WAITING"
                ),
                MeetingDetailSummary(
                    id = 1001L,
                    title = "프로덕트 킥오프 회의",
                    scheduledStartTime = "2025-10-25T10:00:00",
                    targetTime = 60,
                    status = "WAITING"
                ),
                MeetingDetailSummary(
                    id = 1002L,
                    title = "디자인 리뷰",
                    scheduledStartTime = "2025-10-27T15:30:00",
                    targetTime = 45,
                    status = "WAITING"
                ),
                MeetingDetailSummary(
                    id = 1001L,
                    title = "프로덕트 킥오프 회의",
                    scheduledStartTime = "2025-10-25T10:00:00",
                    targetTime = 60,
                    status = "WAITING"
                ),
                MeetingDetailSummary(
                    id = 1002L,
                    title = "디자인 리뷰",
                    scheduledStartTime = "2025-10-27T15:30:00",
                    targetTime = 45,
                    status = "WAITING"
                ),
                MeetingDetailSummary(
                    id = 1001L,
                    title = "프로덕트 킥오프 회의",
                    scheduledStartTime = "2025-10-25T10:00:00",
                    targetTime = 60,
                    status = "WAITING"
                ),
                MeetingDetailSummary(
                    id = 1002L,
                    title = "디자인 리뷰",
                    scheduledStartTime = "2025-10-27T15:30:00",
                    targetTime = 45,
                    status = "WAITING"
                ),
                MeetingDetailSummary(
                    id = 1001L,
                    title = "프로덕트 킥오프 회의",
                    scheduledStartTime = "2025-10-25T10:00:00",
                    targetTime = 60,
                    status = "WAITING"
                ),
                MeetingDetailSummary(
                    id = 1002L,
                    title = "디자인 리뷰",
                    scheduledStartTime = "2025-10-27T15:30:00",
                    targetTime = 45,
                    status = "WAITING"
                ),
                MeetingDetailSummary(
                    id = 1001L,
                    title = "프로덕트 킥오프 회의",
                    scheduledStartTime = "2025-10-25T10:00:00",
                    targetTime = 60,
                    status = "WAITING"
                ),
                MeetingDetailSummary(
                    id = 1002L,
                    title = "디자인 리뷰",
                    scheduledStartTime = "2025-10-27T15:30:00",
                    targetTime = 45,
                    status = "WAITING"
                )
            )

            val past = listOf(
                MeetingDetailSummary(
                    id = 9001L,
                    title = "분기 회고",
                    scheduledStartTime = "2025-09-30T14:00:00",
                    targetTime = 90,
                    status = "COMPLETED"
                ),
                MeetingDetailSummary(
                    id = 9001L,
                    title = "분기 회고",
                    scheduledStartTime = "2025-09-30T14:00:00",
                    targetTime = 90,
                    status = "COMPLETED"
                ),
                MeetingDetailSummary(
                    id = 9001L,
                    title = "분기 회고",
                    scheduledStartTime = "2025-09-30T14:00:00",
                    targetTime = 90,
                    status = "COMPLETED"
                ),
                MeetingDetailSummary(
                    id = 9001L,
                    title = "분기 회고",
                    scheduledStartTime = "2025-09-30T14:00:00",
                    targetTime = 90,
                    status = "COMPLETED"
                ),
                MeetingDetailSummary(
                    id = 9001L,
                    title = "분기 회고",
                    scheduledStartTime = "2025-09-30T14:00:00",
                    targetTime = 90,
                    status = "COMPLETED"
                ),
                MeetingDetailSummary(
                    id = 9001L,
                    title = "분기 회고",
                    scheduledStartTime = "2025-09-30T14:00:00",
                    targetTime = 90,
                    status = "COMPLETED"
                ),
                MeetingDetailSummary(
                    id = 9001L,
                    title = "분기 회고",
                    scheduledStartTime = "2025-09-30T14:00:00",
                    targetTime = 90,
                    status = "COMPLETED"
                ),
                MeetingDetailSummary(
                    id = 9001L,
                    title = "분기 회고",
                    scheduledStartTime = "2025-09-30T14:00:00",
                    targetTime = 90,
                    status = "COMPLETED"
                ),
                MeetingDetailSummary(
                    id = 9001L,
                    title = "분기 회고",
                    scheduledStartTime = "2025-09-30T14:00:00",
                    targetTime = 90,
                    status = "COMPLETED"
                ),
                MeetingDetailSummary(
                    id = 9001L,
                    title = "분기 회고",
                    scheduledStartTime = "2025-09-30T14:00:00",
                    targetTime = 90,
                    status = "COMPLETED"
                ),
                MeetingDetailSummary(
                    id = 9001L,
                    title = "분기 회고",
                    scheduledStartTime = "2025-09-30T14:00:00",
                    targetTime = 90,
                    status = "COMPLETED"
                )
            )

            val ongoingSummaries = listOf(
                MeetingDetailSummary(
                    id = 1101L,
                    title = "기술 공유 세션",
                    scheduledStartTime = "2025-10-22T13:00:00",
                    targetTime = 50,
                    status = "IN_PROGRESS"
                )
            )

            val ongoingUi = ongoingSummaries.map {
                OngoingMeeting(
                    id = it.id,
                    title = it.title,
                    scheduledStartTime = it.scheduledStartTime,
                    scheduledEndTime = calcEndTimeIsoLocal(it.scheduledStartTime, it.targetTime)
                )
            }

            _state.update {
                it.copy(
                    userName = if (it.userName.isBlank()) "홍길동" else it.userName,
                    isLoading = false,
                    error = null,
                    scheduledMeetings = scheduled,
                    pastMeetings = past,
                    ongoingMeetings = ongoingUi
                )
            }
        }
    }
}