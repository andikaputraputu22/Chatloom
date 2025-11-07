package com.nandikacreativestudio.chatloom.ui.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nandikacreativestudio.chatloom.models.ChatRoom

@Composable
fun RoomPopupMenu(
    chatRoom: ChatRoom?,
    onDismiss: () -> Unit,
    onAddToFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Log.e("Anjay", "Hasilnya: ${chatRoom?.isOnFavorite}")
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .shadow(2.dp, shape = RoundedCornerShape(8.dp))
                .background(
                    color = colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(vertical = 10.dp, horizontal = 24.dp)
                        .clickable(
                            onClick = {
                                onDismiss()
                                onAddToFavorite()
                            },
                            indication = null,
                            interactionSource = remember {
                                MutableInteractionSource()
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (chatRoom?.isOnFavorite == true)
                            "Remove from Favorite" else "Add to Favorite",
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(vertical = 10.dp, horizontal = 24.dp)
                        .clickable(
                            onClick = {
                                onDismiss()
                                onDelete()
                            },
                            indication = null,
                            interactionSource = remember {
                                MutableInteractionSource()
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Delete",
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}