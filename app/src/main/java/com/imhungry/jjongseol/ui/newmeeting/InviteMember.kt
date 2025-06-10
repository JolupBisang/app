package com.imhungry.jjongseol.ui.newmeeting.invite

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imhungry.jjongseol.data.model.meeting.request.ParticipantAddReq
import com.imhungry.jjongseol.data.network.api.MeetingUserApi
import com.imhungry.jjongseol.ui.theme.md_theme_button_color_blue
import com.imhungry.jjongseol.data.network.api.UserApi
import com.imhungry.jjongseol.ui.theme.BasicBackGround
import com.imhungry.jjongseol.ui.theme.UserGray
import com.imhungry.jjongseol.ui.theme.UserGreen2
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun SearchScreen(
    selectedEmails: MutableState<List<String>>,
    userApi: UserApi,
    enabled: Boolean,
    hostEmail: String
) {
    var query by remember { mutableStateOf("") }
    var matchedEmail by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    matchedEmail = null
                    errorMessage = null
                },
                singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp),
                placeholder = {
                    Text("이름, 이메일, 팀으로 검색", color = Color.LightGray, fontSize = 13.sp)
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 0.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.Black,
                    cursorColor = Color.Black,
                    backgroundColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,

                ),
                enabled = enabled
            )

            Box(
                modifier = Modifier
                    .width(50.dp)
                    .fillMaxHeight()
                    .clickable(enabled = enabled) {
                        if (query.isNotBlank() && enabled) {
                            scope.launch {
                                try {
                                    val response = userApi.getUserByEmail(query)
                                    if (response.isSuccessful) {
                                        matchedEmail = response.body()?.data?.email
                                        errorMessage = null
                                    } else if (response.code() == 404) {
                                        matchedEmail = null
                                        errorMessage = "사용자를 찾을 수 없습니다."
                                    } else {
                                        errorMessage = "오류가 발생했습니다: ${response.code()}"
                                    }
                                } catch (e: HttpException) {
                                    errorMessage = "네트워크 오류: ${e.message}"
                                } catch (e: Exception) {
                                    errorMessage = "예외 발생: ${e.message}"
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "검색하기",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Black
                )
            }
        }

        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        val emailToAdd = matchedEmail
        if (!emailToAdd.isNullOrBlank() && !selectedEmails.value.contains(emailToAdd)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .border(0.5.dp, Color.Gray, RoundedCornerShape(10.dp))
                    .then(if (enabled) Modifier.clickable {
                        selectedEmails.value = selectedEmails.value + emailToAdd
                        query = ""
                        matchedEmail = null
                        errorMessage = null
                    } else Modifier)
            ) {
                Text(
                    text = emailToAdd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    color = Color.DarkGray,
                    fontSize = 13.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            selectedEmails.value.forEach { email ->
                Chip(email, enabled = enabled, isHostParticipant = email == hostEmail, onRemove = {
                    selectedEmails.value = selectedEmails.value - email
                })
            }

        }
    }
}

@Composable
fun Chip(
    text: String,
    enabled: Boolean,
    isHostParticipant: Boolean,
    onRemove: () -> Unit
) {
    val canRemove = enabled && !isHostParticipant
    val chipColor = if (isHostParticipant) UserGreen2 else BasicBackGround

    Surface(
        modifier = Modifier
            .padding(4.dp)
            .then(if (canRemove) Modifier.clickable { onRemove() } else Modifier),
        color = chipColor,
        shape = RoundedCornerShape(20)
    ) {
        CustomStyledText(text = text, showX = canRemove)
    }
}

@Composable
fun CustomStyledText(text: String, showX: Boolean) {
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = Color.Black, fontSize = 12.sp)) {
            append(text)
        }
        if (showX) {
            withStyle(
                style = SpanStyle(
                    color = Color.DarkGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(" X")
            }
        }
    }

    Text(
        text = annotatedString,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
}