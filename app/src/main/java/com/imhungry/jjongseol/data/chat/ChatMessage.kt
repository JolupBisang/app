package com.imhungry.jjongseol.data.chat

data class ChatMessage(
    val sender: String,
    val message: String,
    val timestamp: String,
    val isMe: Boolean,
    val profileImageUrl: String? = null
)


