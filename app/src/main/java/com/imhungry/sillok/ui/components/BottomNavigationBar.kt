package com.imhungry.sillok.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.imhungry.sillok.R
import com.imhungry.sillok.ui.theme.primaryBackground

@Composable
fun BottomNavigationBar(
    onHomeClick: () -> Unit = {},
    onTeamClick: () -> Unit = {},
    onFloatingButtonClick: () -> Unit = {},
    onFolderClick: () -> Unit = {},
    onMyPageClick: () -> Unit = {},
    selectedRoute: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xB4FAFAF9),
                        primaryBackground
                    )
                )
            )
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            Image(
                painter = painterResource(id = R.drawable.home2),
                contentDescription = "홈",
                modifier = Modifier
                    .size(42.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onHomeClick() }
            )

            Image(
                painter = painterResource(id = R.drawable.team),
                contentDescription = "팀",
                modifier = Modifier
                    .size(42.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTeamClick() }
            )

            Image(
                painter = painterResource(id = R.drawable.plus),
                contentDescription = "추가",
                modifier = Modifier
                    .size(62.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onFloatingButtonClick() }
            )

            Image(
                painter = painterResource(id = R.drawable.folder),
                contentDescription = "폴더",
                modifier = Modifier
                    .size(42.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onFolderClick() }
            )

            Image(
                painter = painterResource(id = R.drawable.mypage),
                contentDescription = "마이페이지",
                modifier = Modifier
                    .size(42.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onMyPageClick() }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomNavigationBarPreview() {
    BottomNavigationBar(
        onHomeClick = {},
        onTeamClick = {},
        onFloatingButtonClick = {},
        onFolderClick = {},
        onMyPageClick = {},
        selectedRoute = "home"
    )
}

