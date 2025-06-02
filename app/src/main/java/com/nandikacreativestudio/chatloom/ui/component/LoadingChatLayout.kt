package com.nandikacreativestudio.chatloom.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun LoadingChatLayout() {
    val colors = MaterialTheme.colorScheme
    val dotCount = 3
    val animaTables = remember {
        List(dotCount) { Animatable(0f) }
    }

    LaunchedEffect(Unit) {
        animaTables.forEachIndexed { index, anim ->
            launch {
                kotlinx.coroutines.delay(index * 100L)
                while (true) {
                    anim.animateTo(1f, animationSpec = tween(300))
                    anim.animateTo(0f, animationSpec = tween(300))
                }
            }
        }
    }

    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            animaTables.forEach { anim ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .graphicsLayer {
                            translationY = -8 * anim.value
                        }
                        .background(
                            color = colors.primary,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}