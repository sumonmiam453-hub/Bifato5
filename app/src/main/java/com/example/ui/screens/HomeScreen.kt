package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.PostEntity
import com.example.data.local.entities.StoryEntity
import com.example.ui.components.PostComposerCard
import com.example.ui.components.PostItemCard
import com.example.ui.components.StoriesCarousel
import com.example.util.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    posts: List<PostEntity>,
    stories: List<StoryEntity>,
    userAvatarUrl: String?,
    userName: String = "Maruf Hossain",
    uploadProgress: Float? = null,
    onComposerClick: () -> Unit,
    onStoryClick: (Int) -> Unit,
    onAddStoryClick: () -> Unit,
    onReactionSelect: (Long, String) -> Unit,
    onCommentClick: (Long) -> Unit,
    onShareClick: (Long) -> Unit,
    onSaveToggle: (Long) -> Unit,
    onDeletePost: (Long) -> Unit,
    onVisitProfile: (String, String) -> Unit = { _, _ -> },
    onRefresh: () -> Unit = {},
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Strictly Text & Image Posts - No Video Posts in Home Feed
    val feedPosts = remember(posts) {
        posts.filter { post ->
            val url = (post.imageUrl ?: "").trim()
            val isVideo = url.contains("[VIDEO]", ignoreCase = true) ||
                    url.endsWith(".mp4", ignoreCase = true) ||
                    url.endsWith(".mov", ignoreCase = true) ||
                    url.endsWith(".mkv", ignoreCase = true) ||
                    url.endsWith(".webm", ignoreCase = true) ||
                    url.endsWith(".avi", ignoreCase = true) ||
                    url.endsWith(".3gp", ignoreCase = true) ||
                    (url.contains("video", ignoreCase = true) && !url.contains("image", ignoreCase = true) && !url.endsWith(".jpg") && !url.endsWith(".png") && !url.endsWith(".webp") && !url.endsWith(".jpeg"))
            !isVideo
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                SoundManager.playRefreshSound()
                onRefresh()
                delay(1000)
                isRefreshing = false
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("home_feed_list")
        ) {
            // 1. Post Composer Box ("What's on your mind?")
            item {
                PostComposerCard(
                    userAvatarUrl = userAvatarUrl,
                    onComposerClick = onComposerClick
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 6.dp)
            }

            // 2. Stories Carousel Bar
            item {
                if (isLoading) {
                    com.example.ui.components.ShimmerStoriesRow()
                } else {
                    StoriesCarousel(
                        stories = stories,
                        userAvatarUrl = userAvatarUrl,
                        onAddStoryClick = onAddStoryClick,
                        onStoryClick = onStoryClick
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 6.dp)
            }

            // Upload Progress Bar
            if (uploadProgress != null) {
                item {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (uploadProgress >= 1f) "Upload Complete" else "Uploading...",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            )
                            Text(
                                text = "${(uploadProgress * 100).toInt()}%",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 8.dp))
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = { uploadProgress },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            color = com.example.ui.theme.FacebookBlue,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 6.dp)
                }
            }

        // 3. News Feed Posts (Strictly Text & Image Posts - No Video Posts in Home Feed)
        if (isLoading) {
            items(3) {
                com.example.ui.components.ShimmerPostCard()
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 4.dp)
            }
        } else if (feedPosts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "There are no posts in your feed yet.\nCreate your first post from a new account!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 15.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(feedPosts, key = { it.id }) { post ->
                PostItemCard(
                    post = post,
                    onReactionSelect = { reaction -> onReactionSelect(post.id, reaction) },
                    onCommentClick = { onCommentClick(post.id) },
                    onShareClick = { onShareClick(post.id) },
                    onSaveToggle = { onSaveToggle(post.id) },
                    onDeletePost = { onDeletePost(post.id) },
                    onVisitProfile = onVisitProfile,
                    currentUserName = userName
                )
            }
        }
    }
}
}
