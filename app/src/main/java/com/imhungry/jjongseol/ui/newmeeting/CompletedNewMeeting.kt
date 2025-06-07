package com.imhungry.jjongseol.ui.newmeeting

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.theme.SetNavigationBarColor
import com.imhungry.jjongseol.ui.theme.md_theme_button_color_blue
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import com.imhungry.jjongseol.ui.theme.UserGreen1


@Composable
fun CompletedNewMeeting(navController: NavController) {
    SetNavigationBarColor(Color.White)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 130.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.gif)
                        .decoderFactory(GifDecoder.Factory())
                        .build()
                ),
                contentDescription = "회의 생성 완료 gif",
                modifier = Modifier
                    .size(200.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "회의 생성 완료",
                color = Color.Black,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "회의 일정이 성공적으로 생성되었습니다.!",
                color = Color.Black,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { navController.navigate("home") },
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
