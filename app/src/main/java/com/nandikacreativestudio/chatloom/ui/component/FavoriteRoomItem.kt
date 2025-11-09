package com.nandikacreativestudio.chatloom.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nandikacreativestudio.chatloom.models.ChatRoom

@Composable
fun FavoriteRoomItem(
    room: ChatRoom,
    onClick: () -> Unit,
    onRemoveFavorite: (ChatRoom) -> Unit
) {
    var selectedRoomToRemoveFavorite by remember {
        mutableStateOf<ChatRoom?>(null)
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 2.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = room.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            IconButton(onClick = { selectedRoomToRemoveFavorite = room }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Remove"
                )
            }
        }

        selectedRoomToRemoveFavorite?.let { room ->
            ConfirmationDialog(
                title = "Remove Favorite",
                message = "Are you sure want to remove this room from favorite?",
                confirmText = "Remove",
                confirmColor = Color.Red,
                onConfirm = {
                    onRemoveFavorite(room)
                    selectedRoomToRemoveFavorite = null
                },
                onDismiss = { selectedRoomToRemoveFavorite = null }
            )
        }
    }
}