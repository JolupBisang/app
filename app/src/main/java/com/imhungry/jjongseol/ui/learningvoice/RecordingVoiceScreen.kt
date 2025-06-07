package com.imhungry.jjongseol.ui.learningvoice
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.data.network.config.AppPrefs
import com.imhungry.jjongseol.ui.theme.Pretend
import com.imhungry.jjongseol.ui.theme.SetNavigationBarColor
import com.imhungry.jjongseol.ui.theme.UserGreen1
import com.imhungry.jjongseol.ui.theme.blackColor
import com.imhungry.jjongseol.ui.theme.danger
import com.imhungry.jjongseol.viewmodel.AudioViewModel
import java.io.File
import android.Manifest

@Composable
fun RecordingVoiceScreen(
    navController: NavController,
    viewModel: AudioViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isVoiceTutorialCompleted = remember { AppPrefs(context).isVoiceTutorialCompleted() }

    var permissionGranted by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }

    val requiredPermissions = remember {
        buildList {
            add(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        permissionGranted = requiredPermissions.all {
            perms[it] == true
        }
        permissionRequested = true
    }


    LaunchedEffect(isVoiceTutorialCompleted) {
        if (isVoiceTutorialCompleted) {
            navController.navigate("home") {
                popUpTo(0)
            }
        }
    }

    val scripts = listOf(
        "많고 많은 사람 중에\n그대 한 사람",
        "너무 맑고 초롱한\n그 중 하나 별이여",
        "그대만큼 사랑스러운\n사람을 본 일 없다"
    )

    var currentIndex by remember { mutableStateOf(0) }
    var isRecording by remember { mutableStateOf(false) }
    var hasRecorded by remember { mutableStateOf(false) }
    var audioFilePath by remember { mutableStateOf<String?>(null) }
    var showRetry by remember { mutableStateOf(false) }
    var recorder: MediaRecorder? by remember { mutableStateOf(null) }
    var recordStartTime by remember { mutableStateOf(0L) }
    var recordedDuration by remember { mutableStateOf(0L) }

    fun startRecording(context: Context) {
        val fileName = "voice_${System.currentTimeMillis()}.m4a"
        val file = File(context.cacheDir, fileName)
        audioFilePath = file.absolutePath

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(16000)
            setAudioChannels(1)
            setOutputFile(audioFilePath)
            prepare()
            start()
        }
        recordStartTime = System.currentTimeMillis()
    }

    fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
        }
        recorder = null
        recordedDuration = System.currentTimeMillis() - recordStartTime
        if (recordedDuration < 500) {
            showRetry = true
            hasRecorded = false
            // 잘못된 파일 삭제
            audioFilePath?.let {
                val f = File(it)
                if (f.exists()) f.delete()
            }
            audioFilePath = null
        } else {
            showRetry = false
            hasRecorded = true
        }
    }

    SetNavigationBarColor(blackColor)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(blackColor)
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 40.dp),
            //verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${currentIndex + 1}/${scripts.size}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(40.dp))
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(if (showRetry) R.drawable.warning else R.drawable.voiceing)
                        .decoderFactory(GifDecoder.Factory())
                        .build()
                ),
                contentDescription = if (showRetry) "녹음 실패 경고 gif" else "목소리 인식 중 gif",
                modifier = Modifier.size(200.dp)
            )
            Text(
                text = "휴대폰 마이크에 대고 다음과 같이 말씀해주세요.",
                color = Color.LightGray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = scripts[currentIndex],
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                showRetry -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "녹음 시간이 너무 짧습니다.\n다시 녹음해주시길 바랍니다.",
                            color = danger,
                            fontSize = 13.sp,
                            fontFamily = Pretend,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(
                            onClick = {
                                showRetry = false
                                hasRecorded = false
                                audioFilePath?.let {
                                    val oldFile = File(it)
                                    if (oldFile.exists()) oldFile.delete()
                                }
                                audioFilePath = null
                                // 재녹음 상태로 전환
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = UserGreen1,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(13.dp),
                            elevation = null
                        ) {
                            Text("재녹음", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                !isRecording && !hasRecorded -> {
                    Button(
                        onClick = {
                            isRecording = true
                            hasRecorded = false
                            startRecording(context)
                            //녹음 시작 로직
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = UserGreen1,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(13.dp),
                        elevation = null
                    ) {
                        Text("시작", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                isRecording -> {
                    Button(
                        onClick = {
                            isRecording = false
                            hasRecorded = true
                            stopRecording()
                            //녹음 중지 로직
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Gray,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(13.dp),
                        elevation = null
                    ) {
                        Text("중지", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                !isRecording && hasRecorded -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                audioFilePath?.let {
                                    val oldFile = File(it)
                                    if (oldFile.exists()) oldFile.delete()
                                }
                                audioFilePath = null
                                hasRecorded = false
                                //재녹음
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(55.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.White,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(13.dp),
                            elevation = null
                        ) {
                            Text("재녹음", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        }

                        Button(
                            onClick = {
                                audioFilePath?.let {
                                    val audioFile = File(it)
                                    viewModel.uploadAudio(audioFile)
                                }
                                if (currentIndex < scripts.size - 1) {
                                    currentIndex += 1
                                    hasRecorded = false
                                } else {
                                    navController.navigate("LearningVoiceLast")
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(55.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = UserGreen1,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(13.dp),
                            elevation = null
                        ) {
                            Text("다음", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
