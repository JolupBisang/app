package com.imhungry.jjongseol.ui.profilecard

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import java.util.Calendar
import androidx.compose.ui.res.painterResource
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.theme.md_theme_button_color_blue

@Composable
fun MakeProfile(navController: NavController) {
    val userName = remember { mutableStateOf("") }
    val userRole = remember { mutableStateOf("") }
    val communicatingWay = remember { mutableStateOf("") }
    val problemWay = remember { mutableStateOf("") }
    val helpWay = remember { mutableStateOf("") }
    val myNameIs = remember { mutableStateOf("") }
    val hobby = remember { mutableStateOf("") }


    ConstraintLayout(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
    ) {
        val scrollList = createRef()

        LazyColumn(
            modifier = Modifier
                .padding(30.dp)
                .fillMaxSize()
                .constrainAs(scrollList) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                    height = Dimension.fillToConstraints
                },
        ) {
            item {
                Column( modifier = Modifier
                    .fillMaxHeight()){
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(top = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "프로필 카드 만들기",
                            style = TextStyle(
                                color = Color.Gray,
                                fontSize = 30.sp,
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(bottom = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "당신을 소개해주세요!",
                            style = TextStyle(
                                color = Color.Gray,
                                fontSize = 20.sp,
                            )
                        )
                    }
                }

            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.profilepicture),
                        contentDescription = "프로필 사진",
                        modifier = Modifier.weight(1f)
                    )
                    Column(modifier = Modifier.weight(2f)) {
                        Box(
                            modifier = Modifier
                                .height(30.dp)
                                .padding(start = 8.dp, end = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        )
                        {
                            Text(
                                "이름",
                                style = TextStyle(
                                    color = Color.DarkGray,
                                    fontSize = 15.sp,
                                )
                            )
                        }

                        OutlinedTextField(
                            value = userName.value,
                            onValueChange = { userName.value = it },
                            modifier = Modifier
                                .padding(8.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(15.dp)),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 16.sp),
                            placeholder = {
                                Text(
                                    "이름",
                                    style = TextStyle(
                                        color = Color.LightGray
                                    )
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
                    }
                    Column(modifier = Modifier.weight(2f)) {
                        Box(
                            modifier = Modifier
                                .height(30.dp)
                                .padding(start = 8.dp, end = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        )
                        {
                            Text(
                                "역할",
                                style = TextStyle(
                                    color = Color.DarkGray,
                                    fontSize = 15.sp,
                                )
                            )
                        }

                        OutlinedTextField(
                            value = userRole.value,
                            onValueChange = { userRole.value = it },
                            modifier = Modifier
                                .padding(8.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(15.dp)),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 16.sp),
                            placeholder = {
                                Text(
                                    "역할",
                                    style = TextStyle(
                                        color = Color.LightGray
                                    )
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
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(2f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                                .padding(start = 8.dp, end = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        )
                        {
                            Text(
                                "생일",
                                style = TextStyle(
                                    color = Color.DarkGray,
                                    fontSize = 15.sp,
                                )
                            )
                        }
                        DatePickerField()
                    }
                    Column(modifier = Modifier.weight(1f)){
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                                .padding(start = 8.dp, end = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        )
                        {
                            Text(
                                "MBTI",
                                style = TextStyle(
                                    color = Color.DarkGray,
                                    fontSize = 15.sp,
                                )
                            )
                        }
                        MBTIPickerField()
                    }
                }
            }


            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .padding(start = 8.dp, end = 8.dp),
                    contentAlignment = Alignment.CenterStart
                )
                {
                    Text("내가 선호하는 소통 방식",
                        style = TextStyle(
                            color = Color.DarkGray,
                            fontSize = 15.sp,
                        )
                    )
                }

                OutlinedTextField(
                    value = communicatingWay.value,
                    onValueChange = { communicatingWay.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(15.dp)),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 16.sp),
                    placeholder = {
                        Text("직설적, 논리적, 팩트 기반",
                            style = TextStyle(
                                color = Color.LightGray
                            )
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
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .padding(start = 8.dp, end = 8.dp),
                    contentAlignment = Alignment.CenterStart
                )
                {
                    Text("문제 해결 방식",
                        style = TextStyle(
                            color = Color.DarkGray,
                            fontSize = 15.sp,
                        )
                    )
                }

                OutlinedTextField(
                    value = problemWay.value,
                    onValueChange = { problemWay.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(15.dp)),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 16.sp),
                    placeholder = {
                        Text("충분한 시간 필요 vs 즉각 대화",
                            style = TextStyle(
                                color = Color.LightGray
                            )
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
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .padding(start = 8.dp, end = 8.dp),
                    contentAlignment = Alignment.CenterStart
                )
                {
                    Text("이런 도움을 드릴 수 있어요",
                        style = TextStyle(
                            color = Color.DarkGray,
                            fontSize = 15.sp,
                        )
                    )
                }

                OutlinedTextField(
                    value = helpWay.value,
                    onValueChange = { helpWay.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(15.dp)),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 16.sp),
                    placeholder = {
                        Text("텍스트",
                            style = TextStyle(
                                color = Color.LightGray
                            )
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
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .padding(start = 8.dp, end = 8.dp),
                    contentAlignment = Alignment.CenterStart
                )
                {
                    Text("자기 소개",
                        style = TextStyle(
                            color = Color.DarkGray,
                            fontSize = 15.sp,
                        )
                    )
                }

                OutlinedTextField(
                    value = myNameIs.value,
                    onValueChange = { myNameIs.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(15.dp)),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 16.sp),
                    placeholder = {
                        Text(".",
                            style = TextStyle(
                                color = Color.LightGray
                            )
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
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .padding(start = 8.dp, end = 8.dp),
                    contentAlignment = Alignment.CenterStart
                )
                {
                    Text("취미",
                        style = TextStyle(
                            color = Color.DarkGray,
                            fontSize = 15.sp,
                        )
                    )
                }

                OutlinedTextField(
                    value = hobby.value,
                    onValueChange = { hobby.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(15.dp)),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 16.sp),
                    placeholder = {
                        Text("취미",
                            style = TextStyle(
                                color = Color.LightGray
                            )
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
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(modifier = Modifier.fillMaxWidth().weight(1f)
                        .padding(start = 8.dp, end = 8.dp, top = 15.dp, bottom = 8.dp)
                        .height(60.dp)
                        .border(BorderStroke(0.dp, Color.Transparent)),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Transparent,
                            contentColor = Color.Black
                        ),
                        elevation = null,
                        onClick = {
                            navController.navigate("home")
                        }) {
                        Text(
                            "건너 뛰기",
                            style = TextStyle(color = md_theme_button_color_blue, fontSize = 25.sp)
                        )
                    }
                    Button(modifier = Modifier.fillMaxWidth().weight(1f)
                        .padding(start = 8.dp, end = 8.dp, top = 15.dp, bottom = 8.dp)
                        .height(60.dp)
                        .border(BorderStroke(0.dp, Color.Transparent)),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Transparent,
                            contentColor = Color.Black
                        ),
                        elevation = null,
                        onClick = {
                            navController.navigate("completeProfile")
                        }) {
                        Text(
                            "작성 완료",
                            style = TextStyle(color = md_theme_button_color_blue, fontSize = 25.sp)
                        )
                    }
                }
            }


        }
    }
}

@Composable
fun MBTIPickerField() {
    var expanded by remember { mutableStateOf(false) }
    val mbtiTypes = listOf("INTJ", "INTP", "ENTJ", "ENTP",
        "INFJ", "INFP", "ENFJ", "ENFP",
        "ISTJ", "ISFJ", "ESTJ", "ESFJ",
        "ISTP", "ISFP", "ESTP", "ESFP")
    var selectedMBTI by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row(modifier = Modifier
            .clickable { expanded = !expanded }
            .padding(8.dp)
            .height(55.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(15.dp)),

            verticalAlignment = Alignment.CenterVertically) {
            Text(text = selectedMBTI, modifier = Modifier.weight(1f).padding(start = 10.dp))
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Dropdown Menu",
                modifier = Modifier.clickable { expanded = !expanded }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 250.dp)
            ) {
                mbtiTypes.forEach { mbti ->
                    DropdownMenuItem(text = { Text(mbti,modifier = Modifier.padding(start = 8.dp)) },onClick = {
                        selectedMBTI = mbti
                        expanded = false
                    })
                }
            }
        }
    }
}


@Composable
fun DatePickerField() {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val dateText = remember { mutableStateOf("YYYY / MM / DD") }
    val textColor = if (dateText.value == "YYYY / MM / DD") Color.LightGray else Color.Black

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            dateText.value = "${selectedYear} / ${selectedMonth + 1} / $selectedDay"
        }, year, month, day
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(15.dp))
            .clickable {
                datePickerDialog.show()
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = dateText.value,
            style = TextStyle(
                color = textColor,
                fontSize = 16.sp
            ),
            modifier = Modifier
                .padding(10.dp)
                .padding(start = 5.dp)
        )
    }
}
