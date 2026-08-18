package com.example.ui.components

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.entities.PostEntity
import com.example.ui.screens.sampleStoryMusicTracks
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.ReactionAngryRed
import com.example.ui.theme.ReactionLoveRed
import com.example.ui.theme.ReactionYellow
import com.example.util.MusicPlayerManager
import com.example.util.SoundManager

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PostItemCard(
    post: PostEntity,
    onReactionSelect: (String) -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveToggle: () -> Unit,
    onDeletePost: () -> Unit,
    onVisitProfile: (String, String) -> Unit = { _, _ -> },
    currentUserName: String = "Maruf Hossain",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenuBottomSheet by remember { mutableStateOf(false) }
    var showReactionPicker by remember { mutableStateOf(false) }
    var showImageViewerModal by remember { mutableStateOf(false) }
    var initialImageViewIndex by remember { mutableIntStateOf(0) }
    var isAudioPlaying by remember { mutableStateOf(false) }

    // Parse audio URL if present in post content or imageUrl
    val extractedAudioUrl = remember(post) {
        when {
            post.content.contains("[AUDIO:") -> post.content.substringAfter("[AUDIO:").substringBefore("]").trim()
            post.imageUrl?.contains("[AUDIO:") == true -> post.imageUrl.substringAfter("[AUDIO:").substringBefore("]").trim()
            post.content.contains("🎵") -> sampleStoryMusicTracks[(post.id.toInt().coerceAtLeast(0)) % sampleStoryMusicTracks.size].url
            else -> null
        }
    }

    val audioTrackTitle = remember(post) {
        if (post.content.contains("🎵")) {
            post.content.lines().firstOrNull { it.contains("🎵") }?.replace("[AUDIO:.*]".toRegex(), "")?.trim() ?: "🎵 Background Audio Track"
        } else if (extractedAudioUrl != null) {
            "🎵 Music Track Attached"
        } else {
            null
        }
    }

    // Parse image URLs list for multi-image post collage
    val imageUrls = remember(post.imageUrl) {
        if (post.imageUrl.isNullOrBlank()) emptyList()
        else {
            val raw = if (post.imageUrl.contains(" [AUDIO:")) post.imageUrl.substringBefore(" [AUDIO:").trim() else post.imageUrl
            val cleaned = raw.replace("[VIDEO]", "").replace("[GIF]", "").trim()
            cleaned.split(",").map { it.trim() }.filter { it.isNotBlank() }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp)
            .testTag("post_item_${post.id}")
    ) {
        // Author Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (post.authorAvatarUrl.startsWith("drawable/")) {
                    Image(
                        painter = painterResource(id = R.drawable.img_user_avatar),
                        contentDescription = post.authorName,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { onVisitProfile(post.authorName, post.authorAvatarUrl) },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    AsyncImage(
                        model = post.authorAvatarUrl,
                        contentDescription = post.authorName,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { onVisitProfile(post.authorName, post.authorAvatarUrl) },
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = post.authorName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable { onVisitProfile(post.authorName, post.authorAvatarUrl) }
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.timeAgo,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("•", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        val privacyIcon = when (post.privacy) {
                            "FRIENDS" -> Icons.Default.Group
                            "ONLY_ME" -> Icons.Default.Lock
                            else -> Icons.Default.Public
                        }
                        val privacyDesc = when (post.privacy) {
                            "FRIENDS" -> "Friends"
                            "ONLY_ME" -> "Only me"
                            else -> "Public"
                        }
                        Icon(
                            imageVector = privacyIcon,
                            contentDescription = privacyDesc,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // 3-dots Menu Button (Triggers Facebook Bottom Sheet)
            IconButton(onClick = { showMenuBottomSheet = true }) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "Options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Post Text Content
        val cleanPostContent = remember(post.content) {
            post.content.replace("\\[AUDIO:.*?\\]".toRegex(), "").trim()
        }

        val bgPreset = remember(post.bgStyle) {
            if (!post.bgStyle.isNullOrBlank() && post.bgStyle != "NONE") {
                com.example.util.PostBgStyle.getPreset(post.bgStyle)
            } else null
        }

        if (bgPreset != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (bgPreset.isGradient) {
                            Modifier.background(bgPreset.getBrush()!!)
                        } else {
                            Modifier.background(bgPreset.solidColor)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cleanPostContent,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = bgPreset.textColor,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 28.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else if (cleanPostContent.isNotBlank()) {
            Text(
                text = cleanPostContent,
                fontSize = 14.5.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Audio Track Playing Banner (if attached)
        if (extractedAudioUrl != null || audioTrackTitle != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FacebookBlue.copy(alpha = 0.12f))
                    .clickable {
                        val trackUrl = extractedAudioUrl ?: sampleStoryMusicTracks[0].url
                        if (isAudioPlaying) {
                            MusicPlayerManager.stopTrack()
                            isAudioPlaying = false
                        } else {
                            MusicPlayerManager.playTrack(context, "post_${post.id}", trackUrl)
                            isAudioPlaying = true
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(FacebookBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause Audio",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = audioTrackTitle ?: "🎵 Audio Track Attached",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = FacebookBlue,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isAudioPlaying) "Playing audio • Tap to pause" else "Tap to listen audio",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Music",
                    tint = FacebookBlue,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Facebook Multi-Image Collage Grid Layout
        if (imageUrls.isNotEmpty()) {
            val rawUrl = post.imageUrl ?: ""
            val firstUrl = imageUrls.first()
            val isVideo = rawUrl.contains("[VIDEO]", ignoreCase = true) ||
                    firstUrl.endsWith(".mp4", ignoreCase = true) ||
                    firstUrl.endsWith(".mov", ignoreCase = true) ||
                    firstUrl.endsWith(".mkv", ignoreCase = true) ||
                    firstUrl.endsWith(".webm", ignoreCase = true) ||
                    firstUrl.endsWith(".avi", ignoreCase = true) ||
                    firstUrl.endsWith(".3gp", ignoreCase = true) ||
                    (firstUrl.contains("video", ignoreCase = true) && !firstUrl.contains("image", ignoreCase = true) && !firstUrl.endsWith(".jpg") && !firstUrl.endsWith(".png") && !firstUrl.endsWith(".webp") && !firstUrl.endsWith(".jpeg"))
            if (isVideo) {
                val cleanUrl = firstUrl.replace("[VIDEO]", "").replace("[GIF]", "").trim()
                VideoPlayerComponent(
                    videoUrl = cleanUrl,
                    thumbnailUrl = post.authorAvatarUrl,
                    autoPlay = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
            } else {
                // Render Collage depending on image count
                when (imageUrls.size) {
                    1 -> {
                        // Single Image
                        SinglePostImageItem(
                            url = imageUrls[0],
                            height = 240.dp,
                            onClick = {
                                initialImageViewIndex = 0
                                showImageViewerModal = true
                            }
                        )
                    }
                    2 -> {
                        // 2 Images Side-by-Side
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                SinglePostImageItem(
                                    url = imageUrls[0],
                                    height = 200.dp,
                                    onClick = {
                                        initialImageViewIndex = 0
                                        showImageViewerModal = true
                                    }
                                )
                            }
                            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                SinglePostImageItem(
                                    url = imageUrls[1],
                                    height = 200.dp,
                                    onClick = {
                                        initialImageViewIndex = 1
                                        showImageViewerModal = true
                                    }
                                )
                            }
                        }
                    }
                    3 -> {
                        // 3 Images Collage: Main top + 2 bottom
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            SinglePostImageItem(
                                url = imageUrls[0],
                                height = 180.dp,
                                onClick = {
                                    initialImageViewIndex = 0
                                    showImageViewerModal = true
                                }
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                    SinglePostImageItem(
                                        url = imageUrls[1],
                                        height = 120.dp,
                                        onClick = {
                                            initialImageViewIndex = 1
                                            showImageViewerModal = true
                                        }
                                    )
                                }
                                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                    SinglePostImageItem(
                                        url = imageUrls[2],
                                        height = 120.dp,
                                        onClick = {
                                            initialImageViewIndex = 2
                                            showImageViewerModal = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        // 4+ Images Facebook Collage Layout
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            SinglePostImageItem(
                                url = imageUrls[0],
                                height = 180.dp,
                                onClick = {
                                    initialImageViewIndex = 0
                                    showImageViewerModal = true
                                }
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                    SinglePostImageItem(
                                        url = imageUrls[1],
                                        height = 120.dp,
                                        onClick = {
                                            initialImageViewIndex = 1
                                            showImageViewerModal = true
                                        }
                                    )
                                }
                                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                    SinglePostImageItem(
                                        url = imageUrls[2],
                                        height = 120.dp,
                                        onClick = {
                                            initialImageViewIndex = 2
                                            showImageViewerModal = true
                                        }
                                    )
                                }
                                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                    SinglePostImageItem(
                                        url = imageUrls[3],
                                        height = 120.dp,
                                        onClick = {
                                            initialImageViewIndex = 3
                                            showImageViewerModal = true
                                        }
                                    )
                                    if (imageUrls.size > 4) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.6f))
                                                .clickable {
                                                    initialImageViewIndex = 3
                                                    showImageViewerModal = true
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "+${imageUrls.size - 3}",
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Reaction Counter Summary Row
        if (post.likesCount > 0 || post.commentsCount > 0 || post.sharesCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Reaction Emojis + Count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val activeReactions = remember(post.userReaction, post.likesCount, post.id) {
                        val list = mutableListOf<Pair<String, Color>>()
                        val userReactionPair = when (post.userReaction) {
                            "LOVE" -> "❤️" to ReactionLoveRed
                            "HAHA" -> "😆" to ReactionYellow
                            "WOW" -> "😮" to ReactionYellow
                            "SAD" -> "😢" to ReactionYellow
                            "ANGRY" -> "😡" to ReactionAngryRed
                            "LIKE" -> "👍" to FacebookBlue
                            else -> null
                        }
                        if (userReactionPair != null) {
                            list.add(userReactionPair)
                        }
                        val remaining = post.likesCount - list.size
                        if (remaining > 0) {
                            val extraOptions = when ((post.id % 4).toInt()) {
                                0 -> listOf("👍" to FacebookBlue)
                                1 -> listOf("❤️" to ReactionLoveRed, "👍" to FacebookBlue)
                                2 -> listOf("😆" to ReactionYellow, "👍" to FacebookBlue)
                                else -> listOf("😮" to ReactionYellow, "👍" to FacebookBlue)
                            }
                            for (item in extraOptions) {
                                if (list.none { it.first == item.first }) {
                                    list.add(item)
                                }
                            }
                        }
                        if (list.isEmpty() && post.likesCount > 0) {
                            list.add("👍" to FacebookBlue)
                        }
                        list
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy((-4).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        activeReactions.forEach { (emoji, bg) ->
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(bg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 10.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.likesCount}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Right: Comments & Shares count
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (post.commentsCount > 0) {
                        Text(
                            text = "${post.commentsCount} comments",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (post.sharesCount > 0) {
                        Text(
                            text = "${post.sharesCount} shares",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant,
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Action Buttons Row: Like, Comment, Share
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Like Button (Combined Click / Long Click)
            val (reactionText, reactionColor, reactionEmoji) = when {
                post.userReaction == "LOVE" -> Triple("Love", ReactionLoveRed, "❤️")
                post.userReaction == "HAHA" -> Triple("Haha", ReactionYellow, "😆")
                post.userReaction == "WOW" -> Triple("Wow", ReactionYellow, "😮")
                post.userReaction == "SAD" -> Triple("Sad", ReactionYellow, "😢")
                post.userReaction == "ANGRY" -> Triple("Angry", ReactionAngryRed, "😡")
                post.userReaction == "LIKE" || post.isLiked -> Triple("Like", FacebookBlue, "👍")
                else -> Triple("Like", MaterialTheme.colorScheme.onSurfaceVariant, null)
            }

            Box(modifier = Modifier.weight(1f)) {
                // Reaction Picker Popup Overlay
                ReactionPickerPopup(
                    isVisible = showReactionPicker,
                    onReactionSelected = { reaction ->
                        onReactionSelect(reaction)
                        showReactionPicker = false
                    },
                    onDismiss = { showReactionPicker = false }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(CircleShape)
                        .combinedClickable(
                            onClick = {
                                SoundManager.playLikeSound()
                                onReactionSelect("LIKE")
                            },
                            onLongClick = {
                                SoundManager.playReactionPopupSound()
                                showReactionPicker = true
                            }
                        )
                        .testTag("post_like_button_${post.id}"),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (reactionEmoji != null && post.userReaction != "LIKE") {
                        Text(reactionEmoji, fontSize = 16.sp)
                    } else {
                        Icon(
                            painter = if (post.isLiked) androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_bold_like) else androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_bold_like),
                            contentDescription = "Like",
                            tint = reactionColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = reactionText,
                        color = reactionColor,
                        fontSize = 13.sp,
                        fontWeight = if (post.isLiked) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // Comment Button
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(CircleShape)
                    .combinedClickable(
                        onClick = onCommentClick,
                        onLongClick = {}
                    )
                    .testTag("post_comment_button_${post.id}"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_bold_comment),
                    contentDescription = "Comment",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Comment",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }

            // Share Button
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(CircleShape)
                    .combinedClickable(
                        onClick = onShareClick,
                        onLongClick = {}
                    )
                    .testTag("post_share_button_${post.id}"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_bold_share),
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Share",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }

        // Facebook Style 3-Dots Options Bottom Sheet
        if (showMenuBottomSheet) {
            PostOptionsBottomSheet(
                post = post,
                currentUserName = currentUserName,
                onSaveToggle = {
                    onSaveToggle()
                    showMenuBottomSheet = false
                },
                onCopyLink = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Post Link", "https://bikalaafa.app/posts/${post.id}")
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
                    showMenuBottomSheet = false
                },
                onReport = {
                    Toast.makeText(context, "Report submitted to Bika Lafa team", Toast.LENGTH_SHORT).show()
                    showMenuBottomSheet = false
                },
                onDownload = {
                    Toast.makeText(context, "Media saved to gallery", Toast.LENGTH_SHORT).show()
                    showMenuBottomSheet = false
                },
                onDeletePost = {
                    onDeletePost()
                    showMenuBottomSheet = false
                },
                onDismiss = { showMenuBottomSheet = false }
            )
        }

        // Full Screen Multi-Image Viewer Modal with HorizontalPager & Audio
        if (showImageViewerModal && imageUrls.isNotEmpty()) {
            PostImageViewerModal(
                imageUrls = imageUrls,
                initialIndex = initialImageViewIndex,
                audioUrl = extractedAudioUrl ?: sampleStoryMusicTracks[(post.id.toInt().coerceAtLeast(0)) % sampleStoryMusicTracks.size].url,
                onDismiss = { showImageViewerModal = false }
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 6.dp)
    }
}

@Composable
private fun SinglePostImageItem(
    url: String,
    height: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    if (url.startsWith("drawable/")) {
        Image(
            painter = painterResource(id = R.drawable.img_post_photo1),
            contentDescription = "Post image",
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clickable { onClick() },
            contentScale = ContentScale.Crop
        )
    } else {
        AsyncImage(
            model = url,
            contentDescription = "Post image",
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clickable { onClick() },
            contentScale = ContentScale.Crop
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostOptionsBottomSheet(
    post: PostEntity,
    currentUserName: String = "Maruf Hossain",
    onSaveToggle: () -> Unit,
    onCopyLink: () -> Unit,
    onReport: () -> Unit,
    onDownload: () -> Unit,
    onDeletePost: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 24.dp)
        ) {
            // Drag handle top indicator is default in ModalBottomSheet
            Text(
                text = "Post Options",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
            )

            // Save / Unsave
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSaveToggle() }
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Save",
                    tint = FacebookBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (post.isSaved) "Unsave post" else "Save post",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Add this to your saved items",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Copy Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onCopyLink() }
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Link",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Copy link", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("Copy link to share anywhere", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Download Media
            if (!post.imageUrl.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onDownload() }
                        .padding(vertical = 12.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Download photo/media", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text("Save high resolution file to device", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Report Post
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onReport() }
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = "Report",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Report post", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("We're concerned about this post", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Delete Post - ONLY ALLOW FOR OWN POSTS
            val isOwnPost = post.authorName.equals(currentUserName, ignoreCase = true) ||
                    post.authorName.equals("Maruf Hossain", ignoreCase = true) ||
                    post.authorName.equals("Manik Hossain", ignoreCase = true) ||
                    post.authorName.equals("Me", ignoreCase = true) ||
                    post.authorName.equals("You", ignoreCase = true) ||
                    (currentUserName.isNotBlank() && post.authorName.contains(currentUserName, ignoreCase = true))

            if (isOwnPost) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onDeletePost() }
                        .padding(vertical = 12.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Delete post", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                        Text("Remove this post permanently", fontSize = 12.sp, color = Color.Red.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

@Composable
fun PostImageViewerModal(
    imageUrls: List<String>,
    initialIndex: Int,
    audioUrl: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, (imageUrls.size - 1).coerceAtLeast(0)),
        pageCount = { imageUrls.size }
    )

    // Play background audio while viewing full screen images
    LaunchedEffect(audioUrl) {
        if (!audioUrl.isNullOrBlank()) {
            MusicPlayerManager.playTrack(context, "viewer_${audioUrl.hashCode()}", audioUrl)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            MusicPlayerManager.stopTrack()
        }
    }

    Dialog(
        onDismissRequest = {
            MusicPlayerManager.stopTrack()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIdx ->
                val imgUrl = imageUrls[pageIdx]
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            MusicPlayerManager.stopTrack()
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (imgUrl.startsWith("drawable/")) {
                        Image(
                            painter = painterResource(id = R.drawable.img_post_photo1),
                            contentDescription = "Zoomed image",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.FillWidth
                        )
                    } else {
                        AsyncImage(
                            model = imgUrl,
                            contentDescription = "Zoomed image",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }
            }

            // Top bar with close button & image page counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        MusicPlayerManager.stopTrack()
                        onDismiss()
                    }
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }

                Text(
                    text = "Photo ${pagerState.currentPage + 1} of ${imageUrls.size}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Box(modifier = Modifier.size(24.dp))
            }
        }
    }
}
