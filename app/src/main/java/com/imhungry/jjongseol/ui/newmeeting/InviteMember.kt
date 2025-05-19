package com.imhungry.jjongseol.ui.newmeeting.invite

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.jjongseol.ui.theme.md_theme_button_color_blue
import com.imhungry.jjongseol.data.network.api.UserApi
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun SearchScreen(
    selectedEmails: MutableState<List<String>>,
    userApi: UserApi
) {
    var query by remember { mutableStateOf("") }
    var matchedEmail by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                matchedEmail = null
                if (it.isNotBlank()) {
                    scope.launch {
                        try {
                            val response = userApi.getUserByEmail(it)
                            matchedEmail = response.data.email
                        } catch (e: HttpException) {
                            if (e.code() == 404) {
                                matchedEmail = null
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(15.dp)),
            singleLine = true,
            textStyle = TextStyle(fontSize = 16.sp),
            placeholder = {
                Text("이름, 이메일, 팀으로 검색",
                    style = TextStyle(color = Color.LightGray)
                )
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color.Black,
                cursorColor = Color.Black,
                backgroundColor = Color.White,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            )
        )

        if (matchedEmail != null && !selectedEmails.value.contains(matchedEmail)) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))) {
                Text(
                    text = matchedEmail!!,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedEmails.value = selectedEmails.value + matchedEmail!!
                            query = ""
                            matchedEmail = null
                        }
                        .padding(8.dp),
                    color = Color.DarkGray,
                    fontSize = 13.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedEmails.value.forEach { email ->
                Chip(email, onRemove = {
                    selectedEmails.value = selectedEmails.value - email
                })
            }
        }
    }
}

@Composable
fun Chip(text: String, onRemove: () -> Unit) {
    Surface(
        modifier = Modifier.padding(4.dp).clickable { onRemove() },
        color = Color.LightGray,
        shape = RoundedCornerShape(50)
    ) {
        CustomStyledText(text)
    }
}


@Composable
fun CustomStyledText(text: String) {
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = md_theme_button_color_blue, fontSize = 12.sp, fontWeight = FontWeight.Bold)) {
            append("X ")
        }
        withStyle(style = SpanStyle(color = Color.Black, fontSize = 12.sp)) {
            append(text)
        }
    }

    Text(
        text = annotatedString,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
}