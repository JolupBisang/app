package com.imhungry.jjongseol.data.model.meeting.dto

data class FeedbackDto(
    val timestamp: String,
    val comment: String,
    val isRead: Boolean = false
)