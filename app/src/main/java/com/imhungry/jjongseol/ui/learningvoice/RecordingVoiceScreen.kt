package com.imhungry.jjongseol.ui.learningvoice
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.imhungry.jjongseol.ui.theme.UserGreen1

@Composable
fun RecordingVoiceScreen(navController: NavController) {
    val scripts = listOf(
        "많고 많은 사람 중에\n그대 한 사람",
        "너무 맑고 초롱한\n그 중 하나 별이여",
        "그대만큼 사랑스러운\n사람을 본 일 없다"
    )

    var currentIndex by remember { mutableStateOf(0) }
    var isRecording by remember { mutableStateOf(false) }
    var hasRecorded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp, vertical = 40.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${currentIndex + 1}/${scripts.size}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally) {
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

            when {
                !isRecording && !hasRecorded -> {
                    Button(
                        onClick = {
                            isRecording = true
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
