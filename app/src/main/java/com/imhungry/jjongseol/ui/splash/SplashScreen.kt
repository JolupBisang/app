package com.imhungry.jjongseol.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.SilRokNavigation
import com.imhungry.jjongseol.ui.login.GoogleLoginButton
import com.imhungry.jjongseol.ui.theme.BasicBackGround
import com.imhungry.jjongseol.ui.theme.Pretend
import com.imhungry.jjongseol.viewmodel.SplashViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinish: (SilRokNavigation) -> Unit
) {
    val viewModel: SplashViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        delay(1500)
        val destination = viewModel.getNavigationDestination()
        onFinish(destination)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BasicBackGround),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1.6f))

        Image(
            painter = painterResource(id = R.drawable.logo2),
            contentDescription = "logo",
            modifier = Modifier.size(113.dp)
        )

        Spacer(modifier = Modifier.weight(0.7f))

        Text(
            text = stringResource(R.string.login_guidance),
            textAlign = TextAlign.Center,
            fontFamily = Pretend,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.alpha(0f)
        )

        GoogleLoginButton(
            modifier = Modifier
                .padding(top = 12.dp)
                .width(280.dp)
                .height(48.dp)
                .alpha(0f),
            onClick = { }
        )

        Spacer(modifier = Modifier.weight(1.6f))
    }
}
