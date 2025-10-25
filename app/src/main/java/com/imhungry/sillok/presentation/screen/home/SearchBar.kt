package com.imhungry.sillok.presentation.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.sillok.R
import com.imhungry.sillok.ui.theme.beige
import com.imhungry.sillok.ui.theme.gray200

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    onFocusChange: (Boolean) -> Unit = {},
    focusRequester: FocusRequester = remember { FocusRequester() },
    onMenuClick: () -> Unit = {},
    text: String = "",
    onTextChange: (String) -> Unit = {}
) {

    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(beige)
            .padding(start = 16.dp, end = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 메뉴 아이콘(사이드바 열기)
            Image(
                painter = painterResource(id = R.drawable.menu),
                contentDescription = "메뉴",
                modifier = Modifier
                    .size(20.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onMenuClick() }
            )

            Spacer(modifier = Modifier.width(12.dp))

            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        onFocusChange(focusState.isFocused)
                    },
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            text = "제목, 참석자로 검색",
                            style = MaterialTheme.typography.bodyLarge,
                            color = gray200,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Image(
                painter = painterResource(id = R.drawable.search),
                contentDescription = "검색",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}