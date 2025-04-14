import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.imhungry.jjongseol.ui.theme.md_theme_button_color_blue

@Composable
fun SearchScreen() {
    val itemList = listOf(
        "yujin@example.com"
        ,"yujin123@example.com"
        ,"wonyoung@example.com"
        ,"wonyoung2025@example.com"
        ,"eunkyung@example.com"
        ,"ek_kyung@example.com"
        ,"jian_k@example.com"
        ,"ji_an@example.com"
        ,"sangjeong@example.com"
        ,"s_jeong@example.com"
        ,"yujin3000@example.com"
        ,"yujin_office@example.com"
        ,"wonyoung_star@example.com"
        ,"wonyoung1999@example.com"
        ,"eunkyung_pro@example.com"
        ,"ek_love@example.com"
        ,"jian_music@example.com"
        ,"ji_an_life@example.com"
        ,"sangjeong_tech@example.com"
        ,"s_jeong123@example.com"
        ,"sangjeong_new@example.com"
        ,"jian_hobby@example.com"
        ,"eunkyung_joy@example.com"
        ,"won_2027@example.com"
        ,"yu_jin2025@example.com"
    )
    var query by remember { mutableStateOf("") }
    var selectedEmails by remember { mutableStateOf(listOf<String>()) }

    Column() {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
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

        if (query.isNotEmpty()) {
            val filteredList = itemList.filter { it.startsWith(query, ignoreCase = true) && !selectedEmails.contains(it) }
            LazyColumn(modifier = Modifier.heightIn(max = 120.dp)
                .fillMaxWidth()
                .padding(start = 15.dp, end = 15.dp, bottom = 8.dp)
                .border(1.dp, Color.Gray,RoundedCornerShape(10.dp)),) {
                items(filteredList) { item ->
                    Text(
                        text = item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 15.dp)
                            .clickable {
                                selectedEmails = selectedEmails + item
                                query = ""
                            }
                            .padding(8.dp),
                        color = Color.DarkGray,
                        fontSize = 13.sp
                    )

                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedEmails.forEach { email ->
                Chip(email, onRemove = { selectedEmails = selectedEmails - email })
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