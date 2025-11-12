package com.imhungry.sillok.presentation.viewmodel.home

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.imhungry.sillok.data.local.DismissedMeetingStore
import com.imhungry.sillok.data.local.GeneratingMeetingNoteStore
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.meeting.MeetingDetailSummary
import com.imhungry.sillok.domain.model.meeting.MeetingStatus
import com.imhungry.sillok.domain.model.user.User
import com.imhungry.sillok.domain.usecase.meeting.GetMeetingDetailUseCase
import com.imhungry.sillok.domain.usecase.meeting.GetMeetingSummaryListUseCase
import com.imhungry.sillok.domain.usecase.user.GetMyProfileUseCase
import com.imhungry.sillok.presentation.state.home.HomeState
import com.imhungry.sillok.presentation.state.home.MeetingUi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMeetingSummaryListUseCase: GetMeetingSummaryListUseCase,
    private val getMeetingDetailUseCase: GetMeetingDetailUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val dismissedMeetingStore: DismissedMeetingStore,
    private val generatingMeetingNoteStore: GeneratingMeetingNoteStore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
        private const val SEARCH_LIMIT = 25L
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_MEETINGS = "meetings"
    }

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var currentYearMonth: Pair<Int, Int>? = null
    private var hasNewMeetingListener: ListenerRegistration? = null
    private var meetingStartedListener: ListenerRegistration? = null

    init {
        loadUserProfile()
        loadInitialData()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                when (val result = getMyProfileUseCase()) {
                    is ApiResult.Success -> {
                        val user = result.data
                        Log.d(TAG, "사용자 프로필 로드 성공: id=${user.id}, nickname=${user.nickname}, pictureURL=${user.pictureURL}")
                        updateUserInfo(user)
                        handleUserAuthState(user)
                    }
                    is ApiResult.Failure -> {
                        Log.e(TAG, "사용자 프로필 로드 실패: ${result.message}")
                        // 실패 시 빈 상태로 처리
                        updateUserInfo(null)
                        handleUserAuthState(null)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "사용자 프로필 로드 예외 발생: ${e.message}", e)
                updateUserInfo(null)
                handleUserAuthState(null)
            }
        }
    }

    private fun updateUserInfo(user: User?) {
        _state.update { current ->
            current.copy(
                userName = user?.nickname?.takeIf { it.isNotBlank() } ?: current.userName,
                // 빈 문자열을 null로 변환하여 UI에서 placeholder가 표시되도록 함
                profileImage = user?.pictureURL?.takeIf { it.isNotBlank() } ?: ""
            )
        }
    }

    private fun handleUserAuthState(user: User?) {
        val uid = user?.id?.toString()
        if (!uid.isNullOrBlank()) {
            attachFirestoreListeners(uid)
        } else {
            detachAllListeners()
        }
    }

    private fun attachFirestoreListeners(uid: String) {
        attachHasNewMeetingListener(uid)
        attachMeetingStartedListener(uid)
    }

    private fun detachAllListeners() {
        detachHasNewMeetingListener()
        detachMeetingStartedListener()
    }

    private fun loadInitialData() {
        Log.d(TAG, "loadInitialData 호출")
        loadHomeData()
        //loadDummyHomeState()
    }

    fun refresh() {
        loadUserProfile()
        loadInitialData()
    }

    // ========================================
    // Firestore 리스너 관리
    // ========================================

    private fun attachHasNewMeetingListener(uid: String) {
        hasNewMeetingListener?.remove()
        val db = FirebaseFirestore.getInstance()
        hasNewMeetingListener = db.collection(COLLECTION_USERS).document(uid)
            .addSnapshotListener { snapshot, _ ->
                val hasNewMeeting = snapshot?.getBoolean("hasNewMeeting") ?: false
                if (hasNewMeeting) {
                    handleNewMeetingDetected(db, uid)
                }
            }
    }

    private fun handleNewMeetingDetected(db: FirebaseFirestore, uid: String) {
        loadHomeData()
        resetHasNewMeetingFlag(db, uid)
        _state.update { it.copy(hasNewMeeting = false) }
    }

    private fun resetHasNewMeetingFlag(db: FirebaseFirestore, uid: String) {
        db.collection(COLLECTION_USERS).document(uid)
            .update(mapOf("hasNewMeeting" to false, "updatedAt" to System.currentTimeMillis()))
    }

    private fun detachHasNewMeetingListener() {
        hasNewMeetingListener?.remove()
        hasNewMeetingListener = null
    }

    private fun attachMeetingStartedListener(uid: String) {
        meetingStartedListener?.remove()
        val db = FirebaseFirestore.getInstance()
        meetingStartedListener = db.collection(COLLECTION_USERS).document(uid)
            .addSnapshotListener { snapshot, _ ->
                val meetingStarted = snapshot?.getBoolean("meetingStarted") ?: false
                if (meetingStarted) {
                    handleMeetingStarted(db, uid, snapshot)
                }
            }
    }

    private fun handleMeetingStarted(
        db: FirebaseFirestore,
        uid: String,
        snapshot: DocumentSnapshot
    ) {
        val startedMeetingId = snapshot.getLong("startedMeetingId") ?: 0L
        if (startedMeetingId > 0L) {
            Log.d(TAG, "회의 시작 감지: meetingId=$startedMeetingId")
            // 홈 데이터 새로고침
            loadHomeData()
            // 플래그 리셋
            resetMeetingStartedFlag(db, uid)
        }
    }

    private fun resetMeetingStartedFlag(db: FirebaseFirestore, uid: String) {
        db.collection(COLLECTION_USERS).document(uid)
            .update(mapOf("meetingStarted" to false, "updatedAt" to System.currentTimeMillis()))
    }

    private fun detachMeetingStartedListener() {
        meetingStartedListener?.remove()
        meetingStartedListener = null
    }

    // ========================================
    // Dialog 관리
    // ========================================

    fun showExitDialog() {
        _state.update { it.copy(showExitDialog = true) }
    }

    fun dismissExitDialog() {
        _state.update { it.copy(showExitDialog = false) }
    }

    fun dismissGeneratingMeetingNoteDialog() {
        _state.update { it.copy(showGeneratingMeetingNoteDialog = false) }
    }

    fun onMeetingClick(meetingId: Long, onNavigate: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                val generatingMeetingNoteId = generatingMeetingNoteStore.getGeneratingMeetingNoteId()

                if (generatingMeetingNoteId != null && generatingMeetingNoteId == meetingId) {
                    // 회의록 생성 중이면 다이얼로그 표시
                    _state.update { it.copy(showGeneratingMeetingNoteDialog = true) }
                } else {
                    // 회의록 생성 중이 아니면 바로 이동
                    onNavigate(meetingId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "회의록 생성 상태 확인 실패: ${e.message}")
                // 에러 발생 시에도 이동 허용
                onNavigate(meetingId)
            }
        }
    }

    // ========================================
    // 생명주기 관리
    // ========================================

    public override fun onCleared() {
        super.onCleared()
        //detachAllListeners()
    }

    // ========================================
    // 공개 메서드
    // ========================================

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshData() {
        loadHomeData()
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun dismissOngoingMeeting(meetingId: Long) {
        // 상태를 먼저 업데이트하여 즉시 UI 반영
        _state.update { current ->
            current.copy(
                ongoingMeetings = current.ongoingMeetings.map {
                    if (it.id == meetingId) it.copy(dismissed = true) else it
                }
            )
        }
        // DataStore 저장은 백그라운드에서 처리
        viewModelScope.launch {
            dismissedMeetingStore.addDismissedOngoingMeeting(meetingId)
        }
    }

    fun dismissScheduledMeeting(meetingId: Long) {
        // 상태를 먼저 업데이트하여 즉시 UI 반영
        _state.update { current ->
            current.copy(
                upcomingMeetings = current.upcomingMeetings.map {
                    if (it.id == meetingId) it.copy(dismissed = true) else it
                }
            )
        }
        // DataStore 저장은 백그라운드에서 처리
        viewModelScope.launch {
            dismissedMeetingStore.addDismissedScheduledMeeting(meetingId)
        }
    }

    fun onSearchTextChange(text: String) {
        _state.update { it.copy(searchText = text) }
        if (text.isBlank()) {
            _state.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }
        performSearch(text)
    }

    // ========================================
    // 회의 데이터 로딩
    // ========================================

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadHomeData() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        loadHomeDataForMonth(year, month)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadHomeDataForMonth(year: Int, month: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                currentYearMonth = year to month
                when (val result = getMeetingSummaryListUseCase(year, month)) {
                    is ApiResult.Success -> {
                        handleSuccessResult(result.data, year, month)
                    }

                    is ApiResult.Failure -> {
                        handleFailureResult(result.message)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadHomeDataForMonth 예외 발생: ${e.message}", e)
                handleException(e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun handleSuccessResult(
        summaries: List<MeetingDetailSummary>,
        year: Int,
        month: Int
    ) {
        // DataStore에서 숨긴 회의 ID 가져오기
        val dismissedMeetingIds = dismissedMeetingStore.getDismissedMeetingIds()

        // 숨긴 회의 제외
        val filteredSummaries = summaries.filter { !dismissedMeetingIds.contains(it.id) }

        val (ongoing, upcoming, meetings) = categorizeMeetings(filteredSummaries)

        logMeetingLoadResult(year, month, summaries.size, ongoing.size, upcoming.size)

        // DataStore에서 dismiss 정보 가져오기 (알림용)
        val dismissedOngoingIds = dismissedMeetingStore.getDismissedOngoingMeetingIds()
        val dismissedScheduledIds = dismissedMeetingStore.getDismissedScheduledMeetingIds()

        val meetingUis = convertToMeetingUis(
            ongoing,
            upcoming,
            meetings,
            dismissedOngoingIds,
            dismissedScheduledIds
        )

        updateStateWithMeetings(meetingUis)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun categorizeMeetings(summaries: List<MeetingDetailSummary>): Triple<List<MeetingDetailSummary>, List<MeetingDetailSummary>, List<MeetingDetailSummary>> {
        val ongoing =
            summaries.filter { MeetingStatus.from(it.status) == MeetingStatus.IN_PROGRESS }
        val upcoming = summaries.filter { MeetingStatus.from(it.status) == MeetingStatus.WAITING }
        val meetings = summaries
        return Triple(ongoing, upcoming, meetings)
    }

    private fun convertToMeetingUis(
        ongoing: List<MeetingDetailSummary>,
        upcoming: List<MeetingDetailSummary>,
        meetings: List<MeetingDetailSummary>,
        dismissedOngoingIds: Set<Long>,
        dismissedScheduledIds: Set<Long>
    ): MeetingUis {
        return MeetingUis(
            ongoing = ongoing.map {
                MeetingUi.from(it).copy(dismissed = dismissedOngoingIds.contains(it.id))
            },
            upcoming = upcoming.map {
                MeetingUi.from(it).copy(dismissed = dismissedScheduledIds.contains(it.id))
            },
            all = meetings.map {
                val isDismissed = dismissedOngoingIds.contains(it.id) ||
                        dismissedScheduledIds.contains(it.id)
                MeetingUi.from(it).copy(dismissed = isDismissed)
            }
        )
    }

    private data class MeetingUis(
        val ongoing: List<MeetingUi>,
        val upcoming: List<MeetingUi>,
        val all: List<MeetingUi>
    )

    private fun logMeetingLoadResult(
        year: Int,
        month: Int,
        total: Int,
        ongoing: Int,
        upcoming: Int
    ) {
        val past = total - ongoing - upcoming
    }

    private fun updateStateWithMeetings(meetingUis: MeetingUis) {
        _state.update {
            it.copy(
                isLoading = false,
                meetings = meetingUis.all,
                ongoingMeetings = meetingUis.ongoing,
                upcomingMeetings = meetingUis.upcoming,
                error = null
            )
        }
    }

    private fun handleFailureResult(message: String?) {
        _state.update {
            it.copy(
                isLoading = false,
                error = message ?: "데이터를 불러오지 못했습니다."
            )
        }
    }

    private fun handleException(e: Exception) {
        _state.update {
            it.copy(
                isLoading = false,
                error = e.localizedMessage ?: "오류가 발생했습니다."
            )
        }
    }

    // ========================================
    // 검색 기능
    // ========================================

    private fun performSearch(rawText: String) {
        viewModelScope.launch {
            val normalizedText = normalize(rawText)
            _state.update { it.copy(isSearching = true) }
            try {
                val searchResults = searchMeetings(normalizedText)
                _state.update { it.copy(searchResults = searchResults, isSearching = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isSearching = false, error = e.localizedMessage) }
            }
        }
    }

    private suspend fun searchMeetings(query: String): List<MeetingUi> {
        val db = FirebaseFirestore.getInstance()
        val (start, end) = buildPrefixRange(query)

        // Firestore에서 회의 ID만 수집
        val titleResultIds = searchByTitleIds(db, start, end)
        val participantResultIds = searchByParticipantsIds(db, query)

        val mergedIds = (titleResultIds + participantResultIds).distinct()

        // 숨긴 회의 제외
        val dismissedMeetingIds = dismissedMeetingStore.getDismissedMeetingIds()
        val filteredIds = mergedIds.filter { !dismissedMeetingIds.contains(it) }

        // 각 회의 ID에 대해 GetMeetingDetailUseCase 호출하여 완전한 정보 가져오기
        val meetingSummaries = filteredIds.mapNotNull { meetingId ->
            when (val result = getMeetingDetailUseCase(meetingId)) {
                is ApiResult.Success -> {
                    val meeting = result.data
                    MeetingDetailSummary(
                        id = meeting.meetingId,
                        title = meeting.title,
                        scheduledStartTime = meeting.scheduledStartTime,
                        targetTime = meeting.targetTime,
                        status = meeting.meetingStatus
                    )
                }

                is ApiResult.Failure -> {
                    Log.e(TAG, "회의 상세 정보 조회 실패: meetingId=$meetingId, error=${result.message}")
                    null
                }
            }
        }

        return meetingSummaries.map { MeetingUi.from(it) }
    }

    private suspend fun searchByTitleIds(
        db: FirebaseFirestore,
        start: String,
        end: String
    ): List<Long> {
        val snapshot = db.collection(COLLECTION_MEETINGS)
            .orderBy("title")
            .startAt(start)
            .endAt(end)
            .limit(SEARCH_LIMIT)
            .get()
            .await()

        return snapshot.documents.mapNotNull {
            (it.data?.get("meetingId") as? Number)?.toLong()
        }
    }

    private suspend fun searchByParticipantsIds(
        db: FirebaseFirestore,
        query: String
    ): List<Long> {
        val (start, end) = buildPrefixRange(query)

        val userSnapshot = db.collection(COLLECTION_USERS)
            .orderBy("email")
            .startAt(start)
            .endAt(end)
            .limit(SEARCH_LIMIT)
            .get()
            .await()

        val candidateEmails = userSnapshot.documents.mapNotNull { it.getString("email") }.toSet()

        val participantResultIds = mutableSetOf<Long>()
        for (email in candidateEmails) {
            val meetingSnapshot = db.collection(COLLECTION_MEETINGS)
                .whereArrayContains("participants", email)
                .limit(SEARCH_LIMIT)
                .get()
                .await()
            meetingSnapshot.documents.forEach { doc ->
                (doc.data?.get("meetingId") as? Number)?.toLong()?.let {
                    participantResultIds.add(it)
                }
            }
        }

        return participantResultIds.toList()
    }

    private fun normalize(text: String): String = text.trim().lowercase()

    private fun buildPrefixRange(query: String): Pair<String, String> {
        return query to (query + "\uf8ff")
    }

    // ========================================
    // 더미 데이터 (개발용)
    // ========================================

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadDummyHomeState() {
        viewModelScope.launch {
            val meetings = createDummyMeetings()
            val ongoingSummaries = createDummyOngoingMeetings()
            val searchMeetings = createDummySearchResults()

            val ongoingUi = ongoingSummaries.map { MeetingUi.from(it) }
            val upcomingUi = meetings.map { MeetingUi.from(it) }
            val searchUi = searchMeetings.map { MeetingUi.from(it) }

            _state.update {
                it.copy(
                    userName = "조은경",
                    isLoading = false,
                    error = null,
                    meetings = upcomingUi,
                    ongoingMeetings = ongoingUi,
                    upcomingMeetings = upcomingUi,
                    searchResults = searchUi
                )
            }
        }
    }

    private fun createDummyMeetings(): List<MeetingDetailSummary> {
        return listOf(
            MeetingDetailSummary(2L, "프로덕트 킥오프 회의", "2025-11-01T10:00:00", 60, "WAITING"),
            MeetingDetailSummary(3L, "디자인 리뷰", "2025-11-027T15:30:00", 45, "COMPLETED"),
            MeetingDetailSummary(4L, "프로덕트 킥오프 회의", "2025-11-03T10:00:00", 60, "CANCELED"),
            MeetingDetailSummary(5L, "디자인 리뷰", "2025-11-04T15:30:00", 45, "IN_PROGRESS"),
            MeetingDetailSummary(6L, "프로덕트 킥오프 회의", "2025-10-25T10:00:00", 60, "IN_PROGRESS"),
            MeetingDetailSummary(2L, "프로덕트 킥오프 회의", "2025-11-01T10:00:00", 60, "WAITING"),
            MeetingDetailSummary(3L, "디자인 리뷰", "2025-11-027T15:30:00", 45, "COMPLETED"),
            MeetingDetailSummary(4L, "프로덕트 킥오프 회의", "2025-11-03T10:00:00", 60, "CANCELED"),
            MeetingDetailSummary(5L, "디자인 리뷰", "2025-11-04T15:30:00", 45, "IN_PROGRESS"),
            MeetingDetailSummary(6L, "프로덕트 킥오프 회의", "2025-10-25T10:00:00", 60, "IN_PROGRESS"),
            MeetingDetailSummary(7L, "디자인 리뷰", "2025-10-27T15:30:00", 45, "IN_PROGRESS"),
            MeetingDetailSummary(8L, "프로덕트 킥오프 회의", "2025-10-25T10:00:00", 60, "IN_PROGRESS"),
            MeetingDetailSummary(9L, "디자인 리뷰", "2025-10-27T15:30:00", 45, "WAITING"),
            MeetingDetailSummary(10L, "프로덕트 킥오프 회의", "2025-10-25T10:00:00", 60, "WAITING")
        )
    }

    private fun createDummyOngoingMeetings(): List<MeetingDetailSummary> {
        return listOf(
            MeetingDetailSummary(11L, "기술 공유 세션", "2025-10-22T13:00:00", 50, "IN_PROGRESS"),
            MeetingDetailSummary(12L, "기술 공유 세션2", "2025-10-22T13:00:00", 50, "IN_PROGRESS")
        )
    }

    private fun createDummySearchResults(): List<MeetingDetailSummary> {
        return listOf(
            MeetingDetailSummary(2L, "프로덕트 킥오프 회의", "2025-11-01T10:00:00", 60, "WAITING"),
            MeetingDetailSummary(3L, "디자인 리뷰", "2025-11-027T15:30:00", 45, "COMPLETED"),
            MeetingDetailSummary(2L, "프로덕트 킥오프 회의", "2025-11-01T10:00:00", 60, "WAITING"),
            MeetingDetailSummary(3L, "디자인 리뷰", "2025-11-027T15:30:00", 45, "COMPLETED"),
            MeetingDetailSummary(4L, "프로덕트 킥오프 회의", "2025-11-03T10:00:00", 60, "CANCELED"),
            MeetingDetailSummary(5L, "디자인 리뷰", "2025-11-04T15:30:00", 45, "IN_PROGRESS"),
            MeetingDetailSummary(6L, "프로덕트 킥오프 회의", "2025-10-25T10:00:00", 60, "IN_PROGRESS"),
            MeetingDetailSummary(7L, "디자인 리뷰", "2025-10-27T15:30:00", 45, "IN_PROGRESS"),
            MeetingDetailSummary(8L, "프로덕트 킥오프 회의", "2025-10-25T10:00:00", 60, "IN_PROGRESS"),
            MeetingDetailSummary(9L, "디자인 리뷰", "2025-10-27T15:30:00", 45, "WAITING"),
            MeetingDetailSummary(10L, "프로덕트 킥오프 회의", "2025-10-25T10:00:00", 60, "WAITING")
        )
    }
}
