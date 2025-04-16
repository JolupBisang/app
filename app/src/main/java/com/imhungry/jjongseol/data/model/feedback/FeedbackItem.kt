package com.imhungry.jjongseol.data.model.feedback

data class FeedbackItem(
    val text: String,
    val time: String,
    val isRead: Boolean = false
)