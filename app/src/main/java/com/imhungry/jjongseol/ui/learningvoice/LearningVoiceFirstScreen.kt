package com.imhungry.jjongseol.ui.learningvoice

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.theme.SetNavigationBarColor
import com.imhungry.jjongseol.ui.theme.UserGreen1
import com.imhungry.jjongseol.ui.theme.blackColor
import com.imhungry.jjongseol.ui.theme.greenTitle1
import com.imhungry.jjongseol.ui.theme.greenTitle2
import com.imhungry.jjongseol.ui.theme.greenTitle3
import com.imhungry.jjongseol.ui.theme.greenTitle4
import com.imhungry.jjongseol.ui.theme.greenTitle5
import com.imhungry.jjongseol.ui.theme.whiteColor

@Composable
fun LearningVoiceFirstScreen(navController: NavController) {
    SetNavigationBarColor(blackColor)

    Box(
        modifier = Modifier.fillMaxSize()
            .background(blackColor)
            .navigationBarsPadding()
    ) {
        val gradientColors = listOf(greenTitle1, greenTitle2, greenTitle3, greenTitle4, greenTitle5)

        Column(modifier = Modifier.fillMaxWidth().padding(top = 130.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.gif)
                        .decoderFactory(GifDecoder.Factory())
                        .build()
                ),
                contentDescription = "목소리 인식 시작 gif",
                modifier = Modifier.size(140.dp).fillMaxWidth()
            )
            Row (verticalAlignment = Alignment.CenterVertically){
                Text(
                    text = "당신의 목소리",
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            colors = gradientColors
                        ),
                        fontSize = 30.sp, fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "를",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "알려주세요",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    modifier = Modifier.padding(top = 20.dp),
                    text = "이 설정을 통해 회의 중 발화자를 구분할 수 있습니다.",
                    color = Color.White,
                    fontSize = 13.sp,
                )
                Text(
                    text = "조용한 환경에서 평소처럼 말해주시길 바랍니다.",
                    color = Color.White,
                    fontSize = 13.sp,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { navController.navigate("RecordingVoice") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .border(1.dp, UserGreen1, RoundedCornerShape(13.dp)),
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = UserGreen1,
                    contentColor = Color.White
                ),
                elevation = null
            ) {
                Text(
                    "다음",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}
