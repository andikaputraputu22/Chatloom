package com.nandikacreativestudio.chatloom.models

import com.google.firebase.Timestamp

data class Chat(
    val id: String = "",
    val text: String = "",
    val role: String = "",
    val timestamp: Timestamp? = null
)
