package com.nandikacreativestudio.chatloom.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.nandikacreativestudio.chatloom.models.Chat
import com.nandikacreativestudio.chatloom.models.ChatRoom
import com.nandikacreativestudio.chatloom.repository.ChatRepository
import com.nandikacreativestudio.chatloom.utils.Constants
import com.nandikacreativestudio.chatloom.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private var listenerRegistration: ListenerRegistration? = null
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats

    private val _currentRoomId = MutableStateFlow<String?>(null)
    val currentRoomId: StateFlow<String?> = _currentRoomId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoom>> = _chatRooms.asStateFlow()

    private var lastUserMessageTime: Timestamp? = null
    private var isWaitingAssistant by mutableStateOf(false)

    private fun observeChats(roomId: String) {
        listenerRegistration?.remove()
        listenerRegistration = chatRepository.observeChats(roomId) { newChats ->
            _chats.value += newChats

            val assistantReply = lastUserMessageTime?.let { lastUser ->
                newChats.any {
                    it.role == "assistant" && (it.timestamp?.seconds ?: 0) > lastUser.seconds
                }
            } ?: false

            if (assistantReply) {
                isWaitingAssistant = false
            }
        }
    }

    fun sendUserMessage(text: String) {
        lastUserMessageTime = Timestamp.now()
        isWaitingAssistant = true
        _isLoading.value = true
        viewModelScope.launch {
            val roomId = _currentRoomId.value ?: run {
                val newRoomId = chatRepository.createChatRoom(text)
                _currentRoomId.value = newRoomId
                observeChats(newRoomId)
                newRoomId
            }

            chatRepository.insertChatToFirebase(
                roomId = roomId,
                text = text,
                role = Constants.ROLE_USER
            )
            when (val result = chatRepository.fetchChatCompletion(roomId, text)) {
                is Result.Success -> {
                    _isLoading.value = false
                }
                is Result.Error -> {
                    _isLoading.value = false
                }
                else -> {
                    _isLoading.value = false
                }
            }
        }
    }

    fun fetchChatRooms() {
        viewModelScope.launch {
            val rooms = chatRepository.getChatRoom()
            _chatRooms.value = rooms
        }
    }

    fun setCurrentRoomId(id: String?) {
        _currentRoomId.value = id
    }

    fun clearChat() {
        _chats.value = emptyList()
    }

    override fun onCleared() {
        listenerRegistration?.remove()
        super.onCleared()
    }
}