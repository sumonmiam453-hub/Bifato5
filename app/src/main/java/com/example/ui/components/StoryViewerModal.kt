package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.ui.screens.sampleStoryMusicTracks
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.entities.StoryEntity
import com.example.util.SoundManager
import kotlinx.coroutines.delay

@Composable
fun StoryViewerModal(
    allStories: List<StoryEntity>,
    initialIndex: Int = 0,
    currentUserName: String = "Maruf Hossain",
    onDeleteStory: (Long) -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (allStories.isEmpty()) return

    // Active story index in list
    var currentIndex by remember { mutableIntStateOf(initialIndex.coerceIn(0, allStories.size - 1)) }
    val currentStory = allStories[currentIndex]

    // Find all stories for current author to display segmented pipes
    val authorStories = remember(currentStory.authorName) {
        val list = allStories.filter { it.authorName == currentStory.authorName }
        if (list.isEmpty()) listOf(currentStory) else list
    }
    val authorSegmentIndex = remember(currentIndex, authorStories) {
        authorStories.indexOf(currentStory).coerceAtLeast(0)
    }

    var progress by remember { mutableFloatStateOf(0f) }
    var replyText by remember { mutableStateOf("") }
    var showHeartAnimation by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Extract audio URL for this story
    val storyAudioUrl = remember(currentStory) {
        if (currentStory.storyImageUrl.contains(" [AUDIO:")) {
            currentStory.storyImageUrl.substringAfter(" [AUDIO:").substringBefore("]").trim()
        } else {
            // Assign working realistic track based on story ID
            val trackIndex = (currentStory.id.toInt().coerceAtLeast(0)) % sampleStoryMusicTracks.size
            sampleStoryMusicTracks[trackIndex].url
        }
    }

    val storyAudioTitle = remember(currentStory) {
        if (currentStory.storyImageUrl.contains(" [AUDIO:")) {
            "🎵 Story Music Attached"
        } else {
            val trackIndex = (currentStory.id.toInt().coerceAtLeast(0)) % sampleStoryMusicTracks.size
            "🎵 ${sampleStoryMusicTracks[trackIndex].title} • ${sampleStoryMusicTracks[trackIndex].artist}"
        }
    }

    // Auto play story music
    LaunchedEffect(currentStory, storyAudioUrl) {
        com.example.util.MusicPlayerManager.playTrack(context, "story_${currentStory.id}", storyAudioUrl)
    }

    // Stop story music when modal dismissed
    DisposableEffect(currentStory) {
        onDispose {
            com.example.util.MusicPlayerManager.stopTrack()
        }
    }

    // Auto progress story timer
    LaunchedEffect(currentIndex) {
        progress = 0f
        showHeartAnimation = false
        while (progress < 1f) {
            delay(50)
            progress += 0.015f
        }
        // Advance to next story
        if (currentIndex < allStories.size - 1) {
            currentIndex++
        } else {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Story Media (Image/Video frame)
            if (currentStory.storyImageUrl.startsWith("drawable/")) {
                val resId = when {
                    currentStory.storyImageUrl.contains("photo1") -> R.drawable.img_story_photo1
                    else -> R.drawable.img_story_photo1
                }
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = currentStory.authorName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = currentStory.storyImageUrl,
                    contentDescription = currentStory.authorName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Left / Right Touch Overlay for Facebook Story Navigation
            Row(modifier = Modifier.fillMaxSize()) {
                // Left 40%: Tap to go back
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectTapGestures {
                                if (currentIndex > 0) {
                                    currentIndex--
                                } else {
                                    onDismiss()
                                }
                            }
                        }
                )
                // Right 60%: Tap to go forward
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectTapGestures {
                                if (currentIndex < allStories.size - 1) {
                                    currentIndex++
                                } else {
                                    onDismiss()
                                }
                            }
                        }
                )
            }

            // Top Header: Segmented Progress Bar (Pipes) + Author Info + Close
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                // Segmented Progress Bar Pipes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    authorStories.forEachIndexed { idx, _ ->
                        val pipeProgress = when {
                            idx < authorSegmentIndex -> 1f
                            idx == authorSegmentIndex -> progress
                            else -> 0f
                        }
                        LinearProgressIndicator(
                            progress = { pipeProgress },
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.35f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (currentStory.authorAvatarUrl.startsWith("drawable/")) {
                            Image(
                                painter = painterResource(id = R.drawable.img_user_avatar),
                                contentDescription = currentStory.authorName,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            AsyncImage(
                                model = currentStory.authorAvatarUrl,
                                contentDescription = currentStory.authorName,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = currentStory.authorName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Story ${authorSegmentIndex + 1} of ${authorStories.size}",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.25f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = storyAudioTitle,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val author = currentStory.authorName
                        val isOwnStory = author.equals(currentUserName, ignoreCase = true) ||
                                author.equals("Maruf Hossain", ignoreCase = true) ||
                                author.equals("Manik Hossain", ignoreCase = true) ||
                                author.equals("Your Story", ignoreCase = true) ||
                                author.equals("Me", ignoreCase = true) ||
                                author.equals("You", ignoreCase = true) ||
                                (currentUserName.isNotBlank() && author.contains(currentUserName, ignoreCase = true))

                        if (isOwnStory) {
                            IconButton(
                                onClick = {
                                    onDeleteStory(currentStory.id)
                                    onDismiss()
                                },
                                modifier = Modifier.testTag("story_viewer_delete_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Story",
                                    tint = Color.Red,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("story_viewer_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }

            // Animated Heart Reaction overlay
            AnimatedVisibility(
                visible = showHeartAnimation,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Love",
                    tint = Color(0xFFF33E58),
                    modifier = Modifier.size(110.dp)
                )
            }

            // Bottom Reply Bar + Emoji Reactions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .padding(16.dp)
            ) {
                // Quick Emoji Reaction Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("👍", "❤️", "😆", "😮", "😢", "😡").forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 28.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    showHeartAnimation = true
                                    SoundManager.playStoryReactionSound()
                                    progress = 0.95f
                                }
                                .padding(4.dp)
                        )
                    }
                }

                // Reply Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("Send message to ${currentStory.authorName}...", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("story_reply_input"),
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.2f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.2f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                SoundManager.playCommentSound()
                                replyText = ""
                                if (currentIndex < allStories.size - 1) {
                                    currentIndex++
                                } else {
                                    onDismiss()
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Reply",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
