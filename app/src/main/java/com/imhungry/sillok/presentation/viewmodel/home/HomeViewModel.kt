package com.imhungry.sillok.presentation.viewmodel.home

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imhungry.sillok.data.local.DismissedMeetingStore
import com.imhungry.sillok.data.local.GeneratingMeetingNoteStore
import com.imhungry.sillok.data.local.TokenStore
import com.imhungry.sillok.data.local.UserStore
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.meeting.MeetingDetailSummary
import com.imhungry.sillok.domain.model.meeting.MeetingStatus
import com.imhungry.sillok.domain.model.user.User
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
import java.util.Calendar
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMeetingSummaryListUseCase: GetMeetingSummaryListUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val dismissedMeetingStore: DismissedMeetingStore,
    private val generatingMeetingNoteStore: GeneratingMeetingNoteStore,
    private val tokenStore: TokenStore,
    private val userStore: UserStore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var currentYearMonth: Pair<Int, Int>? = null

    init {
        viewModelScope.launch {
            loadUserProfileInternal()
            loadInitialData()
            loadOngoingAndUpcomingMeetings()
        }
    }

    private suspend fun loadUserProfileInternal() {
        try {
            when (val result = getMyProfileUseCase()) {
                is ApiResult.Success -> {
                    val user = result.data
                    Log.d(TAG, "사용자 프로필 로드 성공: id=${user.id}, nickname=${user.nickname}, pictureURL=${user.pictureURL}")
                    updateUserInfo(user)
                }
                is ApiResult.Failure -> {
                    Log.e(TAG, "사용자 프로필 로드 실패: ${result.message}")
                    // 실패 시 빈 상태로 처리
                    updateUserInfo(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "사용자 프로필 로드 예외 발생: ${e.message}", e)
            updateUserInfo(null)
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            loadUserProfileInternal()
        }
    }

    private fun updateUserInfo(user: User?) {
        _state.update { current ->
            current.copy(
                userName = user?.nickname?.takeIf { it.isNotBlank() } ?: current.userName,
                profileImage = user?.pictureURL?.takeIf { it.isNotBlank() } ?: ""
            )
        }
    }

    private fun loadInitialData() {
        Log.d(TAG, "loadInitialData 호출")
        loadHomeData()
        //loadDummyHomeState()
    }

    fun refresh() {
        viewModelScope.launch {
            loadUserProfile()
            loadInitialData()
            // 진행 중/예정 회의는 달과 관계없이 별도로 로드
            loadOngoingAndUpcomingMeetings()
        }
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

    // ========================================
    // 공개 메서드
    // ========================================

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshData() {
        loadHomeData()
        // 진행 중/예정 회의는 달과 관계없이 별도로 로드
        loadOngoingAndUpcomingMeetings()
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun dismissOngoingMeeting(meetingId: Long) {
        // 상태를 먼저 업데이트하여 즉시 UI 반영 (알림에서만 숨김)
        _state.update { current ->
            current.copy(
                ongoingMeetings = current.ongoingMeetings.map {
                    if (it.id == meetingId) it.copy(dismissed = true) else it
                }
            )
        }
        // DataStore 저장은 백그라운드에서 처리 (알림용 dismiss만 저장)
        viewModelScope.launch {
            dismissedMeetingStore.addDismissedOngoingMeeting(meetingId)
        }
    }

    fun dismissScheduledMeeting(meetingId: Long) {
        // 상태를 먼저 업데이트하여 즉시 UI 반영 (알림에서만 숨김)
        _state.update { current ->
            current.copy(
                upcomingMeetings = current.upcomingMeetings.map {
                    if (it.id == meetingId) it.copy(dismissed = true) else it
                }
            )
        }
        // DataStore 저장은 백그라운드에서 처리 (알림용 dismiss만 저장)
        viewModelScope.launch {
            dismissedMeetingStore.addDismissedScheduledMeeting(meetingId)
            // dismiss 후에도 진행 중/예정 회의 목록을 다시 로드하여 최신 상태 유지
            loadOngoingAndUpcomingMeetings()
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
            Log.d(TAG, "loadHomeDataForMonth 시작: year=$year, month=$month")
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                currentYearMonth = year to month
                when (val result = getMeetingSummaryListUseCase(year, month)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "loadHomeDataForMonth 성공: 회의 수=${result.data.size}")
                        handleSuccessResult(result.data, year, month)
                    }

                    is ApiResult.Failure -> {
                        Log.e(TAG, "loadHomeDataForMonth 실패: ${result.message}")
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
    fun loadHomeDataForMonth2(year: Int, month: Int) {
        viewModelScope.launch {
            Log.d(TAG, "loadHomeDataForMonth 시작: year=$year, month=$month")
            try {
                currentYearMonth = year to month
                when (val result = getMeetingSummaryListUseCase(year, month)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "loadHomeDataForMonth 성공: 회의 수=${result.data.size}")
                        handleSuccessResult(result.data, year, month)
                    }

                    is ApiResult.Failure -> {
                        Log.e(TAG, "loadHomeDataForMonth 실패: ${result.message}")
                        handleFailureResult(result.message)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadHomeDataForMonth 예외 발생: ${e.message}", e)
                handleException(e)
            }
        }
    }

    /**
     * 진행 중인 회의와 예정된 회의를 달과 관계없이 로드합니다.
     * 현재 달과 다음 3개월의 데이터를 가져와서 진행 중/예정 회의만 필터링합니다.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadOngoingAndUpcomingMeetings() {
        viewModelScope.launch {
            try {
                val allOngoing = mutableListOf<MeetingDetailSummary>()
                val allUpcoming = mutableListOf<MeetingDetailSummary>()

                // 현재 달과 다음 3개월의 데이터를 가져옴
                for (i in 0..3) {
                    val targetCalendar = Calendar.getInstance().apply {
                        add(Calendar.MONTH, i)
                    }
                    val year = targetCalendar.get(Calendar.YEAR)
                    val month = targetCalendar.get(Calendar.MONTH) + 1

                    when (val result = getMeetingSummaryListUseCase(year, month)) {
                        is ApiResult.Success -> {
                            val ongoing = result.data.filter { 
                                MeetingStatus.from(it.status) == MeetingStatus.IN_PROGRESS 
                            }
                            val upcoming = result.data.filter { 
                                MeetingStatus.from(it.status) == MeetingStatus.WAITING 
                            }
                            allOngoing.addAll(ongoing)
                            allUpcoming.addAll(upcoming)
                        }
                        is ApiResult.Failure -> {
                            Log.e(TAG, "loadOngoingAndUpcomingMeetings 실패 (year=$year, month=$month): ${result.message}")
                        }
                    }
                }

                // DataStore에서 숨긴 회의 ID 가져오기
                val dismissedMeetingIds = dismissedMeetingStore.getDismissedMeetingIds()
                val dismissedOngoingIds = dismissedMeetingStore.getDismissedOngoingMeetingIds()
                val dismissedScheduledIds = dismissedMeetingStore.getDismissedScheduledMeetingIds()

                // 숨긴 회의 제외
                val filteredOngoing = allOngoing.filter { !dismissedMeetingIds.contains(it.id) }
                val filteredUpcoming = allUpcoming.filter { !dismissedMeetingIds.contains(it.id) }

                // 중복 제거 (같은 ID가 여러 달에 있을 수 있음)
                val uniqueOngoing = filteredOngoing.distinctBy { it.id }
                val uniqueUpcoming = filteredUpcoming.distinctBy { it.id }

                val ongoingUis = uniqueOngoing.map {
                    MeetingUi.from(it).copy(dismissed = dismissedOngoingIds.contains(it.id))
                }
                val upcomingUis = uniqueUpcoming.map {
                    MeetingUi.from(it).copy(dismissed = dismissedScheduledIds.contains(it.id))
                }

                Log.d(TAG, "loadOngoingAndUpcomingMeetings 완료: ongoing=${ongoingUis.size}, upcoming=${upcomingUis.size}")

                // 진행 중/예정 회의만 업데이트 (meetings는 변경하지 않음)
                _state.update { current ->
                    current.copy(
                        ongoingMeetings = ongoingUis,
                        upcomingMeetings = upcomingUis
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadOngoingAndUpcomingMeetings 예외 발생: ${e.message}", e)
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
        Log.d(TAG, "handleSuccessResult: 전체 회의 수=${summaries.size}, 숨긴 회의 수=${dismissedMeetingIds.size}")

        // 숨긴 회의 제외
        val filteredSummaries = summaries.filter { !dismissedMeetingIds.contains(it.id) }
        Log.d(TAG, "handleSuccessResult: 필터링 후 회의 수=${filteredSummaries.size}")

        // 달력 표시용 meetings만 업데이트 (ongoing/upcoming은 별도로 관리)
        val meetingUis = filteredSummaries.map { summary ->
            val isDismissed = dismissedMeetingIds.contains(summary.id)
            MeetingUi.from(summary).copy(dismissed = isDismissed)
        }

        logMeetingLoadResult(year, month, summaries.size, 0, 0)

        Log.d(TAG, "handleSuccessResult: 최종 meetings 수=${meetingUis.size}")
        // meetings만 업데이트 (ongoingMeetings, upcomingMeetings는 변경하지 않음)
        _state.update { current ->
            current.copy(
                isLoading = false,
                meetings = meetingUis,
                error = null
            )
        }
    }


    private fun logMeetingLoadResult(
        year: Int,
        month: Int,
        total: Int,
        ongoing: Int,
        upcoming: Int
    ) {
        val past = total - ongoing - upcoming
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
            val normalizedText = rawText.trim().lowercase()
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

        return emptyList()
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
