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
    private val _chatCache = mutableMapOf<String, MutableList<Chat>>()
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats

    private val _currentRoomId = MutableStateFlow<String?>(null)
    val currentRoomId: StateFlow<String?> = _currentRoomId.asStateFlow()

    private val _hasSendMessage = MutableStateFlow(false)
    val hasSendMessage: StateFlow<Boolean> = _hasSendMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isLoadingDelete = MutableStateFlow(false)
    val isLoadingDelete: StateFlow<Boolean> = _isLoadingDelete

    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoom>> = _chatRooms.asStateFlow()

    private var lastUserMessageTime: Timestamp? = null
    private var isWaitingAssistant by mutableStateOf(false)

    private fun observeChats(roomId: String) {
        listenerRegistration?.remove()

        viewModelScope.launch {
            if (_chatCache[roomId] == null) {
                val existingChats = chatRepository.getChatOnce(roomId)
                _chatCache[roomId] = existingChats.toMutableList()
                _chats.value = existingChats
            } else {
                _chats.value = _chatCache[roomId] ?: emptyList()
            }
        }

        listenerRegistration = chatRepository.observeChats(roomId) { newChats ->
            val currentChats = _chatCache[roomId]?.toMutableList() ?: mutableListOf()
            val existingIds = currentChats.map { it.id }.toSet()
            val filteredNewChats = newChats.filter { it.id !in existingIds }
            currentChats += filteredNewChats
            _chatCache[roomId] = currentChats
            _chats.value = currentChats

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
        _hasSendMessage.value = true

        viewModelScope.launch {
            val roomId = _currentRoomId.value ?: run {
                val newRoomId = chatRepository.createChatRoom(text)
                _currentRoomId.value = newRoomId
                newRoomId
            }

            observeChats(roomId)
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

    fun deleteChatRoom(roomId: String) {
        viewModelScope.launch {
            _isLoadingDelete.value = true
            try {
                chatRepository.deleteChatRoom(roomId)
                if (_currentRoomId.value == roomId) {
                    clearChat()
                }
                val rooms = chatRepository.getChatRoom()
                _chatRooms.value = rooms
            } catch (_: Exception) {}
            finally {
                _isLoadingDelete.value = false
            }
        }
    }

    fun onRoomClick(roomId: String) {
        if (_currentRoomId.value == roomId) {
            return
        }
        listenerRegistration?.remove()
        _currentRoomId.value = roomId
        _chats.value = emptyList()
        observeChats(roomId)
    }

    fun fetchChatRooms() {
        viewModelScope.launch {
            val rooms = chatRepository.getChatRoom()
            _chatRooms.value = rooms
        }
    }

    fun setHasSendMessage(value: Boolean) {
        _hasSendMessage.value = value
    }

    fun clearChat() {
        _hasSendMessage.value = false
        _currentRoomId.value?.let { _chatCache.remove(it) }
        listenerRegistration?.remove()
        listenerRegistration = null
        _currentRoomId.value = null
        _chats.value = emptyList()
    }

    override fun onCleared() {
        listenerRegistration?.remove()
        super.onCleared()
    }
}