package com.nandikacreativestudio.chatloom.models

data class ChatResponse(
    val id: String,
    val choices: List<ChatChoice>
)
