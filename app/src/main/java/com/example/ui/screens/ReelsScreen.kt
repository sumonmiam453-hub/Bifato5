package com.example.ui.screens

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.entities.CommentEntity
import com.example.data.local.entities.PostEntity
import com.example.ui.components.CommentBottomSheet
import com.example.util.SoundManager
import kotlinx.coroutines.delay

data class ReelItem(
    val id: Long,
    val creatorName: String,
    val creatorAvatar: String,
    val caption: String,
    val audioTrackName: String = "",
    val videoUrl: String,
    var likesCount: Int,
    var commentsCount: Int,
    var sharesCount: Int,
    var viewsCount: String = "",
    var isLiked: Boolean = false,
    var isFollowing: Boolean = false,
    var isVerified: Boolean = false
)

fun isReelVideoPost(imageUrl: String?, content: String): Boolean {
    if (imageUrl.isNullOrBlank()) return false
    val cleanUrl = imageUrl.replace("[VIDEO]", "").trim()
    if (imageUrl.contains("[VIDEO]")) return true
    return cleanUrl.endsWith(".mp4") || cleanUrl.endsWith(".mov") || cleanUrl.endsWith(".mkv") ||
            cleanUrl.endsWith(".webm") || cleanUrl.endsWith(".avi") || cleanUrl.contains("googlevideo")
}

private val DEFAULT_SAMPLE_REELS = listOf(
    ReelItem(
        id = 9001L,
        creatorName = "Tarek Rahman",
        creatorAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80",
        caption = "Exploring the breathtaking sunset view over the mountains! Nature is always healing and magical ✨🏔️ #travel #sunset #nature #explore",
        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
        likesCount = 14200,
        commentsCount = 389,
        sharesCount = 1250,
        isLiked = false
    ),
    ReelItem(
        id = 9002L,
        creatorName = "Nusrat Jahan",
        creatorAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=300&q=80",
        caption = "Morning vibe with good music and positive energy. Have a productive day everyone! ☕🌸 #lifestyle #morning #vibes #happiness",
        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
        likesCount = 28900,
        commentsCount = 642,
        sharesCount = 3410,
        isLiked = true
    ),
    ReelItem(
        id = 9003L,
        creatorName = "Tech Bangla Review",
        creatorAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=300&q=80",
        caption = "Check out this next-generation drone footage and high-speed motion capture! Amazing technology progression in 2026 🚁🔥 #technology #drone #techbangla #gadgets",
        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
        likesCount = 9850,
        commentsCount = 192,
        sharesCount = 820,
        isLiked = false
    ),
    ReelItem(
        id = 9004L,
        creatorName = "Maruf Hossain",
        creatorAvatar = "drawable/img_user_avatar",
        caption = "Coding late at night and crafting clean mobile user experiences! Never stop building what you love 💻⚡ #developer #android #compose #codinglife",
        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyblazes.mp4",
        likesCount = 35600,
        commentsCount = 912,
        sharesCount = 4500,
        isLiked = true
    )
)

@Composable
fun ReelsScreen(
    posts: List<PostEntity> = emptyList(),
    onVisitProfile: (String, String) -> Unit = { _, _ -> },
    onVideoPlayed: (WatchHistoryItem) -> Unit = {},
    onDeletePost: (Long) -> Unit = {},
    onShareClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showCommentSheet by remember { mutableStateOf(false) }
    var activeReelId by remember { mutableStateOf<Long?>(null) }
    val reelsList = remember { mutableStateListOf<ReelItem>() }

    var showDoubleTapHearts by remember { mutableStateOf(false) }
    var heartTriggerId by remember { mutableLongStateOf(0L) }

    LaunchedEffect(heartTriggerId) {
        if (heartTriggerId > 0L) {
            showDoubleTapHearts = true
            delay(800)
            showDoubleTapHearts = false
        }
    }

    // Sync user uploaded video posts + sample feed
    LaunchedEffect(posts) {
        val userUploadedReels = mutableListOf<ReelItem>()

        // 1. Process posts with videos
        posts.filter { isReelVideoPost(it.imageUrl, it.content) }.forEach { post ->
            val cleanMedia = post.imageUrl?.replace("[VIDEO]", "")?.trim() ?: ""
            userUploadedReels.add(
                ReelItem(
                    id = post.id,
                    creatorName = post.authorName,
                    creatorAvatar = post.authorAvatarUrl,
                    caption = post.content.replace("[VIDEO]", "").trim(),
                    videoUrl = cleanMedia,
                    likesCount = post.likesCount,
                    commentsCount = post.commentsCount,
                    sharesCount = post.sharesCount,
                    isLiked = post.userReaction != null
                )
            )
        }

        reelsList.clear()
        reelsList.addAll(userUploadedReels)

        DEFAULT_SAMPLE_REELS.forEach { sample ->
            if (reelsList.none { it.id == sample.id }) {
                reelsList.add(sample)
            }
        }

        // Realtime Firebase posts sync
        try {
            com.example.data.FirebaseManager.getPostsFlow().collect { fbPosts ->
                fbPosts.filter { isReelVideoPost(it.imageUrl, it.content) }.forEach { post ->
                    val cleanMedia = post.imageUrl?.replace("[VIDEO]", "")?.trim() ?: ""
                    if (reelsList.none { it.id == post.id }) {
                        reelsList.add(
                            0,
                            ReelItem(
                                id = post.id,
                                creatorName = post.authorName,
                                creatorAvatar = post.authorAvatarUrl,
                                caption = post.content.replace("[VIDEO]", "").trim(),
                                videoUrl = cleanMedia,
                                likesCount = post.likesCount,
                                commentsCount = post.commentsCount,
                                sharesCount = post.sharesCount,
                                isLiked = false
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {}
    }

    val commentsForReels = remember { mutableStateListOf<CommentEntity>() }

    if (reelsList.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("No reels available", color = Color.White)
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { reelsList.size })

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { pageIndex ->
            val reel = reelsList.getOrNull(pageIndex)
            if (reel != null) {
                val isCurrentlyActive = pagerState.currentPage == pageIndex

                ReelVideoItemCard(
                    reel = reel,
                    isActive = isCurrentlyActive,
                    onDoubleTapLike = {
                        if (!reel.isLiked) {
                            val newLikes = reel.likesCount + 1
                            reelsList[pageIndex] = reel.copy(isLiked = true, likesCount = newLikes)
                        }
                        SoundManager.playLikeSound()
                        heartTriggerId = System.currentTimeMillis()
                    },
                    onLikeClick = {
                        SoundManager.playLikeSound()
                        val updatedLiked = !reel.isLiked
                        val newLikes = reel.likesCount + if (updatedLiked) 1 else -1
                        reelsList[pageIndex] = reel.copy(isLiked = updatedLiked, likesCount = newLikes)
                    },
                    onCommentClick = {
                        activeReelId = reel.id
                        showCommentSheet = true
                    },
                    onShareClick = {
                        onShareClick(reel.id)
                    },
                    onVisitProfile = { name, avatar ->
                        onVisitProfile(name, avatar)
                    }
                )
            }
        }

        // Double Tap Floating Love Heart Overlay
        DoubleLoveHeartOverlay(
            visible = showDoubleTapHearts,
            modifier = Modifier.fillMaxSize()
        )
    }

    if (showCommentSheet && activeReelId != null) {
        val filteredComments = commentsForReels.filter { it.postId == activeReelId }
        CommentBottomSheet(
            comments = filteredComments,
            onAddComment = { text ->
                val newComment = CommentEntity(
                    id = System.currentTimeMillis(),
                    postId = activeReelId!!,
                    authorName = "Maruf Hossain",
                    authorAvatarUrl = "drawable/img_user_avatar",
                    content = text,
                    timeAgo = "Just now"
                )
                commentsForReels.add(0, newComment)
                val idx = reelsList.indexOfFirst { it.id == activeReelId }
                if (idx != -1) {
                    reelsList[idx] = reelsList[idx].copy(commentsCount = reelsList[idx].commentsCount + 1)
                }
            },
            onDismiss = { showCommentSheet = false }
        )
    }
}

@Composable
fun ReelVideoItemCard(
    reel: ReelItem,
    isActive: Boolean,
    onDoubleTapLike: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onVisitProfile: (String, String) -> Unit
) {
    var isPlaying by remember(isActive) { mutableStateOf(isActive) }
    var isCaptionExpanded by remember { mutableStateOf(false) }
    var showPlayPauseIndicator by remember { mutableStateOf(false) }
    var indicatorIsPlaying by remember { mutableStateOf(true) }

    // Pipe Scrubber States
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubProgress by remember { mutableFloatStateOf(0f) }
    var videoDurationMs by remember { mutableIntStateOf(30000) }
    var seekToRequestMs by remember { mutableIntStateOf(-1) }

    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var hasPlaybackError by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                videoViewRef?.stopPlayback()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(isActive) {
        isPlaying = isActive
        if (!isActive) {
            try {
                videoViewRef?.pause()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(isPlaying, isScrubbing) {
        if (isPlaying && !isScrubbing) {
            while (isPlaying && !isScrubbing) {
                delay(200)
                if (currentProgress < 1f) {
                    currentProgress += (200f / videoDurationMs.coerceAtLeast(10000))
                } else {
                    currentProgress = 0f
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(reel.id) {
                detectTapGestures(
                    onDoubleTap = {
                        onDoubleTapLike()
                    },
                    onTap = {
                        isPlaying = !isPlaying
                        indicatorIsPlaying = isPlaying
                        showPlayPauseIndicator = true
                    }
                )
            }
    ) {
        val cleanUrl = remember(reel.videoUrl) {
            reel.videoUrl.replace("[VIDEO]", "").replace("[GIF]", "").trim()
        }

        val isVideoFormat = cleanUrl.startsWith("content://") ||
                cleanUrl.startsWith("file://") ||
                cleanUrl.startsWith("http://") ||
                cleanUrl.startsWith("https://") ||
                cleanUrl.contains(".mp4") ||
                cleanUrl.contains("video")

        if (isVideoFormat && !hasPlaybackError) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        videoViewRef = this
                        setOnErrorListener { _, _, _ ->
                            hasPlaybackError = true
                            true
                        }
                        try {
                            if (cleanUrl.isNotBlank()) {
                                setVideoURI(Uri.parse(cleanUrl))
                            }
                        } catch (e: Exception) {
                            hasPlaybackError = true
                        }
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            videoDurationMs = mp.duration.takeIf { it > 1000 } ?: 30000
                            try {
                                mp.setVolume(1f, 1f)
                            } catch (_: Exception) {}
                            if (isActive && isPlaying) start()
                        }
                    }
                },
                update = { videoView ->
                    videoViewRef = videoView
                    try {
                        if (seekToRequestMs >= 0) {
                            videoView.seekTo(seekToRequestMs)
                            seekToRequestMs = -1
                        }
                        if (isActive && isPlaying) {
                            if (!videoView.isPlaying) videoView.start()
                        } else {
                            if (videoView.isPlaying) videoView.pause()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AsyncImage(
                model = cleanUrl.ifBlank { "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=800&q=80" },
                contentDescription = reel.caption,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Subtle Bottom Gradient Scrim for text readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f), Color.Black.copy(alpha = 0.9f))
                    )
                )
        )

        // Center Tap Play/Pause Animated Feedback Indicator
        LaunchedEffect(showPlayPauseIndicator) {
            if (showPlayPauseIndicator) {
                delay(650)
                showPlayPauseIndicator = false
            }
        }

        AnimatedVisibility(
            visible = showPlayPauseIndicator,
            enter = scaleIn(initialScale = 0.6f) + fadeIn(),
            exit = scaleOut(targetScale = 1.2f) + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (indicatorIsPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (indicatorIsPlaying) "Playing" else "Paused",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        // ==========================================
        // LEFT-BOTTOM SECTION: PROFILE PIC + ID NAME + TITLE
        // ==========================================
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 24.dp, end = 86.dp)
        ) {
            // Profile Picture + ID Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .clickable { onVisitProfile(reel.creatorName, reel.creatorAvatar) }
            ) {
                // Profile Picture (Circular)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                ) {
                    if (reel.creatorAvatar.startsWith("drawable/")) {
                        Image(
                            painter = painterResource(id = R.drawable.img_user_avatar),
                            contentDescription = reel.creatorName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AsyncImage(
                            model = reel.creatorAvatar,
                            contentDescription = reel.creatorName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Creator ID Name
                Text(
                    text = reel.creatorName,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Title / Caption with "more..."
            if (reel.caption.isNotBlank()) {
                val isLongCaption = reel.caption.length > 50
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = reel.caption,
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 13.5.sp,
                        lineHeight = 18.sp,
                        maxLines = if (isCaptionExpanded) 8 else 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (isLongCaption) isCaptionExpanded = !isCaptionExpanded
                            }
                    )

                    if (isLongCaption) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCaptionExpanded) " less" else " ...more",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            modifier = Modifier.clickable { isCaptionExpanded = !isCaptionExpanded }
                        )
                    }
                }
            }
        }

        // ==========================================
        // RIGHT-SIDE SECTION: STRICTLY LIKE, COMMENT, SHARE ONLY
        // ==========================================
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Like Option
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onLikeClick() },
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                        .testTag("reel_like_button")
                ) {
                    Icon(
                        imageVector = if (reel.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (reel.isLiked) Color(0xFFE41E3F) else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = formatCount(reel.likesCount),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 2. Comment Option
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onCommentClick() },
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                        .testTag("reel_comment_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    text = formatCount(reel.commentsCount),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 3. Share Option
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onShareClick() },
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                        .testTag("reel_share_button")
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_bold_share),
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = formatCount(reel.sharesCount),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ==========================================
        // BOTTOM PIPE VIDEO DURATION BAR (Seekbar with Drag & Tap)
        // ==========================================
        val displayProgress = if (isScrubbing) scrubProgress else currentProgress

        BoxWithConstraints(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(18.dp)
                .pointerInput(reel.id) {
                    detectTapGestures(
                        onPress = { offset ->
                            val progress = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                            currentProgress = progress
                            seekToRequestMs = (progress * videoDurationMs).toInt()
                        }
                    )
                }
                .pointerInput(reel.id) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isScrubbing = true
                            scrubProgress = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            scrubProgress = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            isScrubbing = false
                            currentProgress = scrubProgress
                            seekToRequestMs = (scrubProgress * videoDurationMs).toInt()
                        },
                        onDragCancel = {
                            isScrubbing = false
                        }
                    )
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            val fullWidth = maxWidth

            // Pipe Background Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isScrubbing) 5.dp else 3.dp)
                    .background(Color.White.copy(alpha = 0.25f))
            ) {
                // Active Played Pipe Progress
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(fullWidth * displayProgress)
                        .background(Color.White)
                )
            }

            // Pipe Thumb (visible when dragging)
            if (isScrubbing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset { IntOffset(x = ((fullWidth.toPx() * displayProgress) - 5.dp.toPx()).toInt(), y = 0) }
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}

@Composable
fun DoubleLoveHeartOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val scaleAnim by animateFloatAsState(
        targetValue = if (visible) 1.2f else 0f,
        animationSpec = spring(),
        label = "burstScale"
    )
    val alphaAnim by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(),
        label = "burstAlpha"
    )

    if (visible || alphaAnim > 0.01f) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_love_heart_filled),
                contentDescription = "Love Heart",
                modifier = Modifier
                    .size(90.dp)
                    .graphicsLayer {
                        scaleX = scaleAnim
                        scaleY = scaleAnim
                        alpha = alphaAnim
                    }
            )
        }
    }
}
