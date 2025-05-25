package com.nandikacreativestudio.chatloom.models.request

data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>
)
