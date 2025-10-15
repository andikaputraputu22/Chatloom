package com.nandikacreativestudio.chatloom.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nandikacreativestudio.chatloom.api.ApiService
import com.nandikacreativestudio.chatloom.models.Chat
import com.nandikacreativestudio.chatloom.models.ChatResponse
import com.nandikacreativestudio.chatloom.models.ChatRoom
import com.nandikacreativestudio.chatloom.models.request.ChatMessage
import com.nandikacreativestudio.chatloom.models.request.ChatRequest
import com.nandikacreativestudio.chatloom.utils.Constants
import com.nandikacreativestudio.chatloom.utils.Result
import com.nandikacreativestudio.chatloom.utils.SharedPreferencesManager
import com.nandikacreativestudio.chatloom.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val apiService: ApiService,
    private val utils: Utils,
    private val firebaseAuth: FirebaseAuth,
    private val sharedPreferencesManager: SharedPreferencesManager
) {

    private val db = Firebase.firestore
    private val existingIds = mutableSetOf<String>()

    suspend fun fetchChatCompletion(
        roomId: String,
        chat: String
    ): Result<ChatResponse> = withContext(Dispatchers.IO) {
        val messages = listOf(
            ChatMessage(
                role = "developer",
                content = "You are a helpful assistant."
            ),
            ChatMessage(
                role = "user",
                content = chat
            )
        )
        val request = ChatRequest(
            model = "gpt-4.1-mini",
            messages = messages
        )
        try {
            val response = apiService.getChatCompletion(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    insertChatToFirebase(
                        roomId,
                        body.choices.first().message.content,
                        Constants.ROLE_ASSISTANT
                    )
                    Result.Success(body)
                } else {
                    Result.Error("Response body is null")
                }
            } else {
                Result.Error("Failed to fetch data: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}")
        }
    }

    suspend fun insertChatToFirebase(
        roomId: String,
        text: String,
        role: Int
    ) {
        val chat = hashMapOf(
            "text" to text,
            "role" to utils.getRole(role),
            "timestamp" to FieldValue.serverTimestamp()
        )
        db.collection("chat_rooms")
            .document(roomId)
            .collection("chats")
            .add(chat)
            .await()
    }

    fun observeChats(
        roomId: String,
        onChats: (List<Chat>) -> Unit
    ): ListenerRegistration {
        return db.collection("chat_rooms")
            .document(roomId)
            .collection("chats")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val newChats = value?.documentChanges
                    ?.filter { it.type == DocumentChange.Type.ADDED }
                    ?.mapNotNull { change ->
                        val doc = change.document
                        val chat = doc.toObject(Chat::class.java).copy(id = doc.id)
                        if (existingIds.add(chat.id)) chat else null
                    } ?: emptyList()

                if (newChats.isNotEmpty()) {
                    onChats(newChats)
                }
            }
    }

    suspend fun createChatRoom(title: String): String {
        val user = firebaseAuth.currentUser
        val room = hashMapOf(
            "title" to title,
            "createdAt" to FieldValue.serverTimestamp(),
            "ownerId" to (user?.uid ?: "guest")
        )
        val docRef = db.collection("chat_rooms")
            .add(room)
            .await()

        if (user?.uid == null) {
            sharedPreferencesManager.addRoom(docRef.id)
        }
        return docRef.id
    }

    suspend fun getChatRoom(): List<ChatRoom> {
        val user = firebaseAuth.currentUser ?: return emptyList()
        val snapshot = db.collection("chat_rooms")
            .whereEqualTo("ownerId", user.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val title = doc.getString("title") ?: return@mapNotNull null
            val createdAt = doc.getTimestamp("createdAt")
            ChatRoom(
                id = doc.id,
                title = title,
                createdAt = createdAt
            )
        }
    }

    suspend fun getChatOnce(roomId: String): List<Chat> {
        return db.collection("chat_rooms")
            .document(roomId)
            .collection("chats")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .await()
            .documents.mapNotNull { doc ->
                doc.toObject(Chat::class.java)?.copy(id = doc.id)
            }
    }

    suspend fun deleteChatRoom(roomId: String) {
        val roomRef = db.collection("chat_rooms").document(roomId)
        val chatsRef = roomRef.collection("chats")
        while (true) {
            val snapshot = chatsRef.limit(500).get().await()
            if (snapshot.isEmpty) break

            val batch = db.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
        }
        roomRef.delete().await()
    }

    private suspend fun deleteChatRooms(listRoom: List<String>) {
        for (roomId in listRoom) {
            try {
                deleteChatRoom(roomId)
            } catch (_: Exception) {}
        }
    }

    suspend fun deleteGuestRoom() {
        val roomList = sharedPreferencesManager.getRoomList()
        if (roomList.isEmpty()) return

        try {
            deleteChatRooms(roomList)
        } catch (_: Exception) {}
        finally {
            sharedPreferencesManager.clearRooms()
        }
    }
}