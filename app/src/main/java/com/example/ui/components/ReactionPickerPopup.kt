package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.util.SoundManager
import kotlinx.coroutines.delay

data class FacebookReaction(
    val type: String,
    val emoji: String,
    val label: String,
    val color: Color
)

val facebookReactions = listOf(
    FacebookReaction("LIKE", "👍", "Like", Color(0xFF1877F2)),
    FacebookReaction("LOVE", "❤️", "Love", Color(0xFFF33E58)),
    FacebookReaction("HAHA", "😆", "Haha", Color(0xFFF7B125)),
    FacebookReaction("WOW", "😮", "Wow", Color(0xFFF7B125)),
    FacebookReaction("SAD", "😢", "Sad", Color(0xFFF7B125)),
    FacebookReaction("ANGRY", "😡", "Angry", Color(0xFFE41E3F))
)

@Composable
fun ReactionPickerPopup(
    isVisible: Boolean,
    onReactionSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        val density = LocalDensity.current
        val yOffset = with(density) { (-45).dp.roundToPx() }
        val xOffset = with(density) { 0.dp.roundToPx() }

        // Staggered sequential appearance state for each reaction
        var visibleCount by remember { mutableStateOf(0) }

        LaunchedEffect(isVisible) {
            visibleCount = 0
            facebookReactions.indices.forEach { index ->
                delay(40L) // Stagger 40ms delay per emoji
                visibleCount = index + 1
            }
        }

        Popup(
            alignment = Alignment.TopCenter,
            offset = IntOffset(x = xOffset, y = yOffset),
            onDismissRequest = onDismiss,
            properties = PopupProperties(focusable = true)
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 10.dp,
                    modifier = modifier.padding(horizontal = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        facebookReactions.forEachIndexed { index, reaction ->
                            val isEmojiVisible = index < visibleCount
                            val scale by animateFloatAsState(
                                targetValue = if (isEmojiVisible) 1f else 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "reaction_scale_$index"
                            )

                            var isPressed by remember { mutableStateOf(false) }
                            val pressScale by animateFloatAsState(
                                targetValue = if (isPressed) 1.35f else scale,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "press_scale_$index"
                            )

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .scale(pressScale)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                    .clickable {
                                        isPressed = true
                                        SoundManager.playLikeSound()
                                        onReactionSelected(reaction.type)
                                        onDismiss()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = reaction.emoji,
                                    fontSize = 24.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
