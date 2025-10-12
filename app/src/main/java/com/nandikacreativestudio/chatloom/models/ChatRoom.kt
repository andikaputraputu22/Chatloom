package com.nandikacreativestudio.chatloom.models

import com.google.firebase.Timestamp

data class ChatRoom(
    val id: String = "",
    val title: String = "",
    val createdAt: Timestamp? = null,
    val ownerId: String = ""
)
