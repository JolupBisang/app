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
import com.imhungry.jjongseol.data.network.config.AppPrefs
import com.imhungry.jjongseol.ui.theme.SetNavigationBarColor
import com.imhungry.jjongseol.ui.theme.UserGreen1
import com.imhungry.jjongseol.ui.theme.blackColor

@Composable
fun LearningVoiceLastScreen(navController: NavController) {
    SetNavigationBarColor(blackColor)

    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxSize()
            .background(blackColor)
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 130.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.gif)
                        .decoderFactory(GifDecoder.Factory())
                        .build()
                ),
                contentDescription = "목소리 인식 완료 gif",
                modifier = Modifier.size(140.dp).fillMaxWidth()
            )

            Text(text = "목소리 학습 완료",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    modifier = Modifier.padding(top = 20.dp),
                    text = "회의실록이 당신의 목소리를 기억합니다.",
                    color = Color.White,
                    fontSize = 13.sp,
                )
                Text(
                    text = "이제 서비스를 사용할 준비가 끝났어요!",
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
                onClick = { AppPrefs(context).setVoiceTutorialCompleted()
                    navController.navigate("home") {
                        popUpTo(0)
                    } },
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
                    "완료",
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
