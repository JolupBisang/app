package com.imhungry.sillok.presentation.viewmodel.home

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging
import com.imhungry.sillok.data.local.UserStore
import com.imhungry.sillok.data.util.ApiResult
import com.imhungry.sillok.domain.model.meeting.MeetingDetailSummary
import com.imhungry.sillok.domain.model.meeting.MeetingStatus
import com.imhungry.sillok.domain.usecase.meeting.GetMeetingSummaryListUseCase
import com.imhungry.sillok.presentation.state.home.HomeState
import com.imhungry.sillok.presentation.state.home.MeetingUi
import com.imhungry.sillok.presentation.util.NotificationHelper
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
    private val userStore: UserStore,
    private val notificationHistoryStore: com.imhungry.sillok.data.local.NotificationHistoryStore,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    companion object {
        private const val TAG = "HomeViewModel"
        private const val SEARCH_LIMIT = 25L
        private const val DEFAULT_MEETING_TITLE = "회의"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_MEETINGS = "meetings"
    }

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()
    
    private var currentYearMonth: Pair<Int, Int>? = null
    private var hasNewMeetingListener: ListenerRegistration? = null
    private var meetingStartedListener: ListenerRegistration? = null
    private var previousUpcomingMeetingIds: Set<Long> = emptySet()

    init {
        initializeNotificationHelper()
        observeUserChanges()
        loadInitialData()
        observeUpcomingMeetingsForNotification()
    }

    private fun initializeNotificationHelper() {
        NotificationHelper.createNotificationChannel(context)
        NotificationHelper.setHistoryStore(notificationHistoryStore)
    }

    private fun observeUserChanges() {
        viewModelScope.launch {
            userStore.user.collect { user ->
                updateUserInfo(user)
                handleUserAuthState(user)
            }
        }
    }

    private fun updateUserInfo(user: com.imhungry.sillok.domain.model.user.User?) {
        _state.update { current ->
            current.copy(
                userName = user?.nickname ?: current.userName,
                profileImage = user?.profileImage ?: current.profileImage
            )
        }
    }

    private fun handleUserAuthState(user: com.imhungry.sillok.domain.model.user.User?) {
        val uid = user?.id?.toString()
        if (!uid.isNullOrBlank()) {
            attachFirestoreListeners(uid)
            getFCMToken(uid)
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
        // loadHomeData()
        loadDummyHomeState()
    }

    private fun observeUpcomingMeetingsForNotification() {
        viewModelScope.launch {
            state.collect { s ->
                if (s.upcomingMeetings.isNotEmpty() && previousUpcomingMeetingIds.isEmpty()) {
                    previousUpcomingMeetingIds = s.upcomingMeetings.map { it.id }.toSet()
                }
            }
        }
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
        loadHomeDataWithNotification()
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
        snapshot: com.google.firebase.firestore.DocumentSnapshot
    ) {
        val startedMeetingId = snapshot.getLong("startedMeetingId") ?: 0L
        if (startedMeetingId > 0L) {
            Log.d(TAG, "회의 시작 감지: meetingId=$startedMeetingId")
            viewModelScope.launch {
                fetchMeetingTitleAndShowDialog(db, uid, startedMeetingId)
                resetMeetingStartedFlag(db, uid)
            }
        }
    }

    private suspend fun fetchMeetingTitleAndShowDialog(
        db: FirebaseFirestore,
        uid: String,
        startedMeetingId: Long
    ) {
        val meetingTitle = try {
            val meetingDoc = db.collection(COLLECTION_MEETINGS)
                .document(startedMeetingId.toString())
                .get()
                .await()
            meetingDoc.getString("title") ?: DEFAULT_MEETING_TITLE
        } catch (e: Exception) {
            Log.e(TAG, "회의 정보 조회 실패", e)
            null
        }

        showMeetingStartedDialog(startedMeetingId, meetingTitle)
    }

    private fun showMeetingStartedDialog(meetingId: Long, title: String?) {
        _state.update {
            it.copy(
                showMeetingStartedDialog = true,
                pendingMeetingId = meetingId,
                pendingMeetingTitle = title
            )
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

    fun dismissMeetingStartedDialog() {
        _state.update {
            it.copy(
                showMeetingStartedDialog = false,
                pendingMeetingId = null,
                pendingMeetingTitle = null
            )
        }
    }

    fun showExitDialog() {
        Log.d(TAG, "종료 다이얼로그 표시 요청")
        _state.update { it.copy(showExitDialog = true) }
        Log.d(TAG, "종료 다이얼로그 상태: ${_state.value.showExitDialog}")
    }

    fun dismissExitDialog() {
        _state.update { it.copy(showExitDialog = false) }
    }

    // ========================================
    // FCM 토큰 관리
    // ========================================

    private fun getFCMToken(uid: String) {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                if (token.isNotEmpty()) {
                    saveFCMTokenToFirestore(uid, token)
                }
            } catch (e: Exception) {
                Log.e(TAG, "FCM 토큰 가져오기 실패", e)
            }
        }
    }

    private fun saveFCMTokenToFirestore(uid: String, token: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(COLLECTION_USERS).document(uid)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d(TAG, "FCM 토큰 저장 성공: $token")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "FCM 토큰 저장 실패", e)
            }
    }

    // ========================================
    // 생명주기 관리
    // ========================================

    public override fun onCleared() {
        super.onCleared()
        detachAllListeners()
    }

    // ========================================
    // 공개 메서드
    // ========================================

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshData() {
        loadHomeData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadHomeDataForMonth(year: Int, month: Int) {
        loadHomeDataForMonth(year, month, showNotification = false)
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
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
        loadHomeDataWithNotification(showNotification = false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadHomeDataWithNotification(showNotification: Boolean = true) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        loadHomeDataForMonth(year, month, showNotification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadHomeDataForMonth(year: Int, month: Int, showNotification: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                currentYearMonth = year to month
                when (val result = getMeetingSummaryListUseCase(year, month)) {
                    is ApiResult.Success -> {
                        handleSuccessResult(result.data, year, month, showNotification)
                    }
                    is ApiResult.Failure -> {
                        handleFailureResult(result.message)
                    }
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleSuccessResult(
        summaries: List<MeetingDetailSummary>,
        year: Int,
        month: Int,
        showNotification: Boolean
    ) {
        val (ongoing, upcoming, meetings) = categorizeMeetings(summaries)
        
        logMeetingLoadResult(year, month, summaries.size, ongoing.size, upcoming.size)
        
        val meetingUis = convertToMeetingUis(ongoing, upcoming, meetings)
        
        if (showNotification) {
            checkAndNotifyNewMeetings(meetingUis.upcoming)
        }
        
        updateStateWithMeetings(meetingUis)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun categorizeMeetings(summaries: List<MeetingDetailSummary>): Triple<List<MeetingDetailSummary>, List<MeetingDetailSummary>, List<MeetingDetailSummary>> {
        val ongoing = summaries.filter { MeetingStatus.from(it.status) == MeetingStatus.IN_PROGRESS }
        val upcoming = summaries.filter { MeetingStatus.from(it.status) == MeetingStatus.WAITING }
        val meetings = summaries
        return Triple(ongoing, upcoming, meetings)
    }

    private fun convertToMeetingUis(
        ongoing: List<MeetingDetailSummary>,
        upcoming: List<MeetingDetailSummary>,
        meetings: List<MeetingDetailSummary>
    ): MeetingUis {
        return MeetingUis(
            ongoing = ongoing.map { MeetingUi.from(it) },
            upcoming = upcoming.map { MeetingUi.from(it) },
            all = meetings.map { MeetingUi.from(it) }
        )
    }

    private data class MeetingUis(
        val ongoing: List<MeetingUi>,
        val upcoming: List<MeetingUi>,
        val all: List<MeetingUi>
    )

    private fun logMeetingLoadResult(year: Int, month: Int, total: Int, ongoing: Int, upcoming: Int) {
        val past = total - ongoing - upcoming
        Log.d(TAG, "${year}-${month} 달 로드 성공: 전체=$total, 예정=$upcoming, 진행=$ongoing, 종료=$past")
    }

    private fun checkAndNotifyNewMeetings(currentUpcoming: List<MeetingUi>) {
        if (previousUpcomingMeetingIds.isEmpty()) {
            previousUpcomingMeetingIds = currentUpcoming.map { it.id }.toSet()
            return
        }

        val currentIds = currentUpcoming.map { it.id }.toSet()
        val newMeetings = currentUpcoming.filter { it.id !in previousUpcomingMeetingIds }

        if (newMeetings.isNotEmpty()) {
            val latestMeeting = newMeetings.first()
            NotificationHelper.showNewMeetingNotification(
                context = context,
                meetingId = latestMeeting.id,
                meetingTitle = latestMeeting.title
            )
            Log.d(TAG, "새로운 회의 알림 표시: ${latestMeeting.title} (ID: ${latestMeeting.id})")
        }

        previousUpcomingMeetingIds = currentIds
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

        val titleResults = searchByTitle(db, start, end)
        val participantResults = searchByParticipants(db, query)

        val merged = (titleResults + participantResults).distinctBy { it.id }
        return merged.map { MeetingUi.from(it) }
    }

    private suspend fun searchByTitle(
        db: FirebaseFirestore,
        start: String,
        end: String
    ): List<MeetingDetailSummary> {
        val snapshot = db.collection(COLLECTION_MEETINGS)
            .orderBy("title")
            .startAt(start)
            .endAt(end)
            .limit(SEARCH_LIMIT)
            .get()
            .await()

        return snapshot.documents.mapNotNull { mapMeetingDoc(it.data ?: emptyMap()) }
    }

    private suspend fun searchByParticipants(
        db: FirebaseFirestore,
        query: String
    ): List<MeetingDetailSummary> {
        val (start, end) = buildPrefixRange(query)
        
        val userSnapshot = db.collection(COLLECTION_USERS)
            .orderBy("email")
            .startAt(start)
            .endAt(end)
            .limit(SEARCH_LIMIT)
            .get()
            .await()

        val candidateEmails = userSnapshot.documents.mapNotNull { it.getString("email") }.toSet()

        val participantResults = mutableListOf<MeetingDetailSummary>()
        for (email in candidateEmails) {
            val meetingSnapshot = db.collection(COLLECTION_MEETINGS)
                .whereArrayContains("participants", email)
                .limit(SEARCH_LIMIT)
                .get()
                .await()
            participantResults += meetingSnapshot.documents.mapNotNull { 
                mapMeetingDoc(it.data ?: emptyMap()) 
            }
        }

        return participantResults
    }

    private fun normalize(text: String): String = text.trim().lowercase()

    private fun buildPrefixRange(query: String): Pair<String, String> {
        return query to (query + "\uf8ff")
    }

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

    // ========================================
    // 더미 데이터 (개발용)
    // ========================================

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadDummyHomeState() {
        viewModelScope.launch {
            val meetings = createDummyMeetings()
            val ongoingSummaries = createDummyOngoingMeetings()

            val ongoingUi = ongoingSummaries.map { MeetingUi.from(it) }
            val upcomingUi = meetings.map { MeetingUi.from(it) }

            _state.update {
                it.copy(
                    userName = it.userName,
                    isLoading = false,
                    error = null,
                    meetings = upcomingUi,
                    ongoingMeetings = ongoingUi,
                    upcomingMeetings = upcomingUi
                )
            }
        }
    }

    private fun createDummyMeetings(): List<MeetingDetailSummary> {
        return listOf(
            MeetingDetailSummary(1001L, "프로덕트 킥오프 회의", "2025-11-01T10:00:00", 60, "WAITING"),
            MeetingDetailSummary(1002L, "디자인 리뷰", "2025-11-027T15:30:00", 45, "COMPLETED"),
            MeetingDetailSummary(1001L, "프로덕트 킥오프 회의", "2025-11-03T10:00:00", 60, "CANCELED"),
            MeetingDetailSummary(1002L, "디자인 리뷰", "2025-11-04T15:30:00", 45, "IN_PROGRESS"),
            MeetingDetailSummary(1001L, "프로덕트 킥오프 회의", "2025-10-25T10:00:00", 60, "IN_PROGRESS"),
            MeetingDetailSummary(1002L, "디자인 리뷰", "2025-10-27T15:30:00", 45, "IN_PROGRESS"),
            MeetingDetailSummary(1001L, "프로덕트 킥오프 회의", "2025-10-25T10:00:00", 60, "IN_PROGRESS"),
            MeetingDetailSummary(1002L, "디자인 리뷰", "2025-10-27T15:30:00", 45, "WAITING"),
            MeetingDetailSummary(1001L, "프로덕트 킥오프 회의", "2025-10-25T10:00:00", 60, "WAITING")
        )
    }

    private fun createDummyOngoingMeetings(): List<MeetingDetailSummary> {
        return listOf(
            MeetingDetailSummary(1101L, "기술 공유 세션", "2025-10-22T13:00:00", 50, "IN_PROGRESS"),
            MeetingDetailSummary(1102L, "기술 공유 세션2", "2025-10-22T13:00:00", 50, "IN_PROGRESS")
        )
    }
}
