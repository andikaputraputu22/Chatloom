package com.nandikacreativestudio.chatloom.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nandikacreativestudio.chatloom.models.ChatResponse
import com.nandikacreativestudio.chatloom.repository.ChatRepository
import com.nandikacreativestudio.chatloom.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    var myChat: String = ""
    var chatResult by mutableStateOf<Result<ChatResponse>>(Result.Loading)

    fun fetchChat(chat: String) {
        myChat = chat
        viewModelScope.launch {
            chatResult = Result.Loading
            chatResult = chatRepository.fetchChatCompletion(chat)
        }
    }
}