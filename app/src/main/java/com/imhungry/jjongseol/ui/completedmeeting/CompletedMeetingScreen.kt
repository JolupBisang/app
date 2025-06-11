package com.imhungry.jjongseol.ui.completedmeeting

import CompletedMeetingRecordScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.firestore.FirebaseFirestore
import com.imhungry.jjongseol.data.model.feedback.response.FeedbackListRes
import com.imhungry.jjongseol.data.model.segment.response.SegmentListRes
import com.imhungry.jjongseol.data.model.summary.response.SummaryListRes
import com.imhungry.jjongseol.data.model.user.response.UserInfoResponse
import com.imhungry.jjongseol.data.network.config.AppPrefs
import com.imhungry.jjongseol.ui.completedmeeting.component.AudioPlayerBar
import com.imhungry.jjongseol.ui.completedmeeting.pager.CompletedMeetingFeedbackScreen
import com.imhungry.jjongseol.ui.completedmeeting.pager.CompletedMeetingSummaryScreen
import com.imhungry.jjongseol.ui.meeting.CustomHorizontalPagerIndicator
import com.imhungry.jjongseol.ui.theme.SetNavigationBarColor
import com.imhungry.jjongseol.ui.theme.primaryBackground
import com.imhungry.jjongseol.ui.theme.whiteColor
import com.imhungry.jjongseol.util.DateTimeUtils
import com.imhungry.jjongseol.viewmodel.AudioViewModel
import com.imhungry.jjongseol.viewmodel.FeedbackViewModel
import com.imhungry.jjongseol.viewmodel.MeetingViewModel
import com.imhungry.jjongseol.viewmodel.ParticipationRateViewModel
import com.imhungry.jjongseol.viewmodel.SegmentViewModel
import com.imhungry.jjongseol.viewmodel.SummaryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@Composable
fun CompletedMeetingScreen(
    navController: NavController,
    meetingId: Long
) {
    val context = LocalContext.current
    SetNavigationBarColor(whiteColor)

    CompletedMeetingContent(
        navController,
        meetingId = meetingId
    )
}
//
//val userInfos = listOf(
//    "유진",
//    "은경"
//)
//
//val texts = listOf(
//    "안녕하세요, 회의 시작하겠습니다.",
//    "네, 모두 들어오셨나요?",
//    "네, 다 모인 것 같습니다.",
//    "오늘 안건 공유해드릴게요.",
//    "우선 진행 상황부터 말씀드릴게요.",
//    "혹시 질문 있으신가요?",
//    "이번 안건에 대해 의견 있으신 분?",
//    "저 질문 있습니다.",
//    "네, 말씀하세요.",
//    "그 부분은 제가 답변드릴게요.",
//    "감사합니다, 이해됐어요.",
//    "추가로 공유할 내용 있으실까요?",
//    "없으시면 다음 안건으로 넘어가겠습니다.",
//    "네, 진행해주세요.",
//    "이 부분은 다음 회의에서 다루는 게 어떨까요?",
//    "좋은 생각입니다.",
//    "그럼 오늘 회의는 이만 마치겠습니다.",
//    "수고하셨습니다!",
//    "모두 수고하셨어요.",
//    "다음에 또 뵙겠습니다."
//)
//
//val dummySegments = mutableListOf<SegmentListRes>()
//val startTime = java.time.LocalTime.of(12, 0, 0)
//
//val summaryContents = listOf(
//    "회의가 정시에 시작되었고, 모두 참석함.",
//    "첫 번째 안건에 대한 논의가 활발하게 이루어짐.",
//    "중간에 간단한 휴식 시간을 가짐.",
//    "두 번째 안건에 대해 상반된 의견이 나옴.",
//    "합의점을 찾기 위해 다양한 대안이 제시됨.",
//    "결론적으로 일정 조정이 필요하다는 의견이 모임.",
//    "다음 회의 일정에 대한 논의가 이어짐.",
//    "참석자 모두 동의하에 회의록 초안을 확정함.",
//    "마지막으로 자유로운 의견 교환 시간을 가짐.",
//    "특이사항 없이 회의가 종료됨.",
//    "회식 일정 논의가 짧게 진행됨.",
//    "후속 작업 담당자를 지정함.",
//    "전체적인 회의 분위기가 원활했음.",
//    "질의응답 시간이 충분히 제공됨.",
//    "다음 회의 주제를 미리 공지하기로 함."
//)
//
//val summaryList = summaryContents.mapIndexed { i, content ->
//    val totalSeconds = i * 12
//    val micros = Random.nextInt(0, 1_000_000) // 0~999999
//    val hour = 12 + (totalSeconds / 3600)
//    val minute = (totalSeconds % 3600) / 60
//    val second = totalSeconds % 60
//    val timestamp = String.format("2025-06-10T%02d:%02d:%02d.%06d", hour, minute, second, micros)
//    SummaryListRes(
//        id = (i + 1).toLong(),
//        content = content,
//        isRecap = true,
//        timestamp = timestamp
//    )
//}
//
//
//val feedbackComments = listOf(
//    "발표 자료가 명확하고 이해하기 쉬웠습니다.",
//    "시간 관리가 다소 아쉬웠던 점이 있습니다.",
//    "참여자의 의견 수렴이 잘 이루어졌습니다.",
//    "핵심 내용이 더 강조되면 좋겠습니다.",
//    "목소리 톤이 일정해서 집중이 잘 됐습니다.",
//    "질의응답 시간이 충분하지 않았던 것 같습니다.",
//    "준비한 자료 외에도 유익한 정보가 많았습니다.",
//    "좀 더 천천히 진행해주시면 좋겠습니다.",
//    "참여 독려가 인상적이었습니다.",
//    "다음에는 실습 시간이 있으면 좋겠습니다.",
//    "토론 진행이 매끄러웠어요.",
//    "예상 질문에 대한 답변이 훌륭했습니다.",
//    "참석자의 이해도를 계속 체크해주셔서 좋았어요.",
//    "시각자료 활용이 눈에 띄었습니다.",
//    "마무리 멘트가 인상 깊었습니다."
//)
//
//val feedbackList = feedbackComments.mapIndexed { i, comment ->
//    val totalSeconds = i * 12  // 0, 12, 24, ... 168
//    val micros = Random.nextInt(0, 1_000_000)
//    val hour = 12 + (totalSeconds / 3600)
//    val minute = (totalSeconds % 3600) / 60
//    val second = totalSeconds % 60
//    val timestamp = String.format("2025-06-10T%02d:%02d:%02d.%06d", hour, minute, second, micros)
//    FeedbackListRes(
//        id = (i + 1).toLong(),
//        comment = comment,
//        timestamp = timestamp
//    )
//}


@OptIn(ExperimentalPagerApi::class)
@Composable
fun CompletedMeetingContent(
    navController: NavController,
    segmentViewModel: SegmentViewModel = hiltViewModel(),
    meetingViewModel: MeetingViewModel = hiltViewModel(),
    audioViewModel: AudioViewModel = hiltViewModel(),
    participationRateViewModel: ParticipationRateViewModel = hiltViewModel(),
    summaryViewModel: SummaryViewModel = hiltViewModel(),
    feedbackViewModel: FeedbackViewModel = hiltViewModel(),
    meetingId: Long
) {
    val context = LocalContext.current
    val segments by segmentViewModel.segments.collectAsState()
    val meetingDetail by meetingViewModel.meetingDetail.collectAsState()
    val audioList by audioViewModel.audioList.collectAsState()
    val errorMessage by audioViewModel.errorMessage.collectAsState()
    val participationRates by participationRateViewModel.participationRates.collectAsState()
    val feedbackList by feedbackViewModel.feedbacks.collectAsState()
    // 전체 요약
    val summaries by summaryViewModel.summaries.collectAsState()

    // 중간 요약
    val recapSummaryViewModel: SummaryViewModel = hiltViewModel(key = "recap")
    val recapSummaries by recapSummaryViewModel.summaries.collectAsState()
    var startTime by remember { mutableStateOf<String?>(null) }
    var endTime by remember { mutableStateOf<String?>(null) }
    var startMillis by remember { mutableStateOf<Long?>(null) }
    var endMillis by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(meetingId) {
        val db = FirebaseFirestore.getInstance()
        val meetingRef = db.collection("meetings").document(meetingId.toString())
        meetingRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val start = document.getString("startTime")
                val end = document.getString("endTime")
                startTime = start
                endTime = end
                startMillis = start?.let { DateTimeUtils.isoToMillis(it) }
                endMillis = endTime?.let { DateTimeUtils.isoToMillis(it) }
            }
        }
        meetingViewModel.loadMeetingDetail2(meetingId)
        segmentViewModel.loadSegments(meetingId, reset = true)
        audioViewModel.loadAudioList(meetingId)
        participationRateViewModel.loadParticipationRates(meetingId)
        summaryViewModel.loadSummaries(meetingId, isRecap = false, reset = true)
        recapSummaryViewModel.loadSummaries(meetingId, isRecap = true, reset = true)
        feedbackViewModel.loadFeedbacks(meetingId, reset = true)
    }

    val appPrefs = remember { AppPrefs(context) }
    val myProfile: UserInfoResponse? = appPrefs.loadMyProfile()
    val id: Long? = myProfile?.id
    val myAudioUrl = audioList.firstOrNull { it.userId == id }?.presignedUrl

    var playbackPosition by remember { mutableStateOf(0L) }
    var currentPosition by remember { mutableStateOf(0f) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    val duration = 180f

    LaunchedEffect(isPlaying, playbackSpeed) {
        while (isPlaying && currentPosition < duration) {
            delay(100L)
            currentPosition = (currentPosition + 0.1f * playbackSpeed).coerceAtMost(duration)
        }
    }
    val pagerState = rememberPagerState(initialPage = 1)
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryBackground)
    ) {

        if (startMillis != null && endTime != null && meetingDetail != null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                HorizontalPager(
                    count = 3,
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> meetingDetail?.let {
                            CompletedMeetingSummaryScreen(
                                summarys = recapSummaries,
                                fullSummary = summaries,
                                startMillis = startMillis!!,
                                endMillis = endMillis!!,
                                selectedTab = pagerState.currentPage,
                                onTabClick = { idx ->
                                    coroutineScope.launch { pagerState.animateScrollToPage(idx) }
                                },
                                onTimeClick = { seekMillis ->
                                    playbackPosition = seekMillis
                                    coroutineScope.launch { pagerState.animateScrollToPage(1) }
                                },
                                meetingDetail = it,
                                userParticipationRates = participationRates,
                                summaryViewModel = summaryViewModel,
                                meetingId = meetingId
                            )
                        }
                        1 -> meetingDetail?.let {
                            CompletedMeetingRecordScreen(
                                meetingId = meetingId,
                                navController = navController,
                                segments = segments,
                                startMillis = startMillis!!,
                                selectedTab = pagerState.currentPage,
                                onTabClick = { idx ->
                                    coroutineScope.launch { pagerState.animateScrollToPage(idx) }
                                },
                                playbackPosition = playbackPosition,
                                isPlaying = isPlaying,
                                onSeekToPosition = { newPosition ->
                                    playbackPosition = newPosition
                                },
                                meetingDetail = it,
                                currentUserId = id!!,
                                segmentViewModel = segmentViewModel,
                            )
                        }
                        2 -> CompletedMeetingFeedbackScreen(
                            feedbackList = feedbackList,
                            startMillis = startMillis!!,
                            selectedTab = pagerState.currentPage,
                            onTabClick = { idx ->
                                coroutineScope.launch { pagerState.animateScrollToPage(idx) }
                            },
                            onTimeClick = { seekMillis ->
                                playbackPosition = seekMillis
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            },
                            feedbackViewModel = feedbackViewModel,
                            meetingId = meetingId
                        )
                    }
                }
                CustomHorizontalPagerIndicator(
                    pagerState = pagerState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                )
            }
            if (!myAudioUrl.isNullOrEmpty()) {
                AudioPlayerBar(
                    audioUrl = myAudioUrl,
                    currentPosition = playbackPosition,
                    onPositionChange = { playbackPosition = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    onPlayingChanged = { isPlaying = it },
                    onExternalSeek = { seekToMillis ->
                        // AudioPlayerBar 외부에서 seekTo 요청이 왔을 때 처리
                        // (이 경우 CompletedMeetingRecordScreen의 onSeekToPosition이 currentPlaybackPosition을 업데이트하면 AudioPlayerBar의 LaunchedEffect(currentPosition)에서 자동으로 seekTo가 호출됨)
                    }
                )
            }
        }
    }
}