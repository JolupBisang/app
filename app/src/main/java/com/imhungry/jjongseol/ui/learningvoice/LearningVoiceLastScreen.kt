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
        Image(
            painter = painterResource(id = R.drawable.learningvoicelastscreen),
            contentDescription = "Learning Voice Last Screen Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

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
