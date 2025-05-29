package com.nandikacreativestudio.chatloom.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nandikacreativestudio.chatloom.models.Chat

@Composable
fun ChatLayout(
    chat: Chat,
    isUser: Boolean,
    userBubbleColor: Color
) {
    val bubbleColor = if (isUser) userBubbleColor else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = bubbleColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .then(
                    if (isUser) {
                        Modifier
                            .padding(vertical = 10.dp, horizontal = 16.dp)
                            .widthIn(max = 300.dp)
                    } else {
                        Modifier
                            .padding(vertical = 10.dp)
                    }
                )
        ) {
            Text(
                text = chat.text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}