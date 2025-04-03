package com.imhungry.jjongseol.ui.newmeeting

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.imhungry.jjongseol.R
import com.imhungry.jjongseol.ui.theme.md_theme_button_color_blue

@Composable
fun CompletedNewMeeting(navController: NavController) {
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
            item{
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(top = 10.dp, bottom = 20.dp),
                    contentAlignment = Alignment.Center
                ){
                    Text("회의 생성",
                        style = TextStyle(
                            color = Color.Gray,
                            fontSize = 30.sp,
                        )
                    )
                }
            }

            item {
                Box( contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.newmeeting_completed),
                        contentDescription = "회의 생성 완료",
                        modifier = Modifier
                            .height(300.dp)
                            .width(300.dp)
                    )
                }
            }

            item{
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(top = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "성공적으로 생성되었어요!~",
                        style = TextStyle(
                            color = Color.Gray,
                            fontSize = 30.sp,
                        )
                    )
                }
            }
            item{
                Column(verticalArrangement = Arrangement.Center){
                    Button(modifier = Modifier.fillMaxWidth()
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
                            "홈으로 가기",
                            style = TextStyle(color = md_theme_button_color_blue, fontSize = 25.sp)
                        )
                    }
                }
            }
        }
    }
}