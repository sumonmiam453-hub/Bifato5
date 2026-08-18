package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.entities.PostEntity
import com.example.data.local.entities.StoryEntity
import com.example.ui.components.FollowersFollowingModal
import com.example.ui.components.FullScreenImageViewerModal
import com.example.ui.components.PostItemCard
import com.example.ui.components.ReelPlayerModal
import com.example.ui.theme.FacebookBlue
import com.example.util.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitedUserProfileScreen(
    userName: String,
    userAvatarUrl: String,
    allPosts: List<PostEntity>,
    onBackClick: () -> Unit,
    onOpenMessage: (String, String) -> Unit,
    onReactionSelect: (Long, String) -> Unit,
    onCommentClick: (Long) -> Unit,
    onShareClick: (Long) -> Unit,
    onSaveToggle: (Long) -> Unit,
    onDeletePost: (Long) -> Unit,
    currentUserName: String = "Maruf Hossain",
    stories: List<StoryEntity> = emptyList(),
    onStoryClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isFriend by remember { mutableStateOf(false) }
    var isFollowing by remember { mutableStateOf(false) }
    var isRequestSent by remember { mutableStateOf(false) }
    var selectedProfileTab by remember { mutableIntStateOf(0) }
    var showFollowersModal by remember { mutableStateOf(false) }
    var followersModalInitialTab by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var showAvatarOptionsSheet by remember { mutableStateOf(false) }
    var fullScreenViewerImage by remember { mutableStateOf<String?>(null) }
    var fullScreenViewerTitle by remember { mutableStateOf("Photo") }
    val avatarOptionsSheetState = rememberModalBottomSheetState()

    val coverPhotoUrl = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?auto=format&fit=crop&w=1000&q=80"
    val profileTabs = listOf("Posts", "Reels", "Photos", "About", "Friends")

    // Check if this visited user has any active story
    val userStoryIndex = remember(userName, stories) {
        stories.indexOfFirst { it.authorName.equals(userName, ignoreCase = true) }
    }
    val hasStory = userStoryIndex != -1

    val userPosts = remember(userName, allPosts) {
        val matched = allPosts.filter { it.authorName.equals(userName, ignoreCase = true) }
        if (matched.isNotEmpty()) matched
        else listOf(
            PostEntity(
                id = kotlin.math.abs(userName.hashCode().toLong() * 100).coerceAtLeast(10000),
                authorName = userName,
                authorAvatarUrl = userAvatarUrl,
                content = "Hello everyone! Welcome to my Facebook profile ✨ Glad to connect with you all!",
                likesCount = 48,
                commentsCount = 12,
                sharesCount = 5,
                timestamp = System.currentTimeMillis() - 86400000,
                timeAgo = "1 day ago"
            )
        )
    }

    var activePlayingReelIndex by remember { mutableStateOf<Int?>(null) }

    val userReels = remember(userPosts) {
        val list = mutableStateListOf<ProfileReelItem>()
        userPosts.forEachIndexed { _, post ->
            if (isReelVideoPost(post.imageUrl, post.content)) {
                list.add(
                    ProfileReelItem(
                        id = post.id,
                        title = post.content.replace("[VIDEO]", "").trim().ifBlank { "Reel video" },
                        thumbnailUrl = post.imageUrl!!.replace("[VIDEO]", "").trim(),
                        viewsCount = "${post.sharesCount * 14 + 1} views"
                    )
                )
            }
        }
        list
    }

    val userPhotos = remember(userPosts, userAvatarUrl) {
        val photosFromPosts = userPosts.mapNotNull { it.imageUrl }.filter { it.isNotBlank() }
        val all = mutableListOf<String>()
        if (userAvatarUrl.isNotBlank() && !userAvatarUrl.startsWith("drawable/")) all.add(userAvatarUrl)
        all.addAll(photosFromPosts)
        all.distinct()
    }

    var totalDragOffset by remember { mutableFloatStateOf(0f) }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                delay(800)
                isRefreshing = false
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { Toast.makeText(context, "Search in $userName's profile", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.Search, contentDescription = "Search Profile")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .pointerInput(selectedProfileTab) {
                        detectHorizontalDragGestures(
                            onDragStart = { totalDragOffset = 0f },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                totalDragOffset += dragAmount
                            },
                            onDragEnd = {
                                if (totalDragOffset < -45f && selectedProfileTab < profileTabs.size - 1) {
                                    selectedProfileTab++
                                    SoundManager.playClickSound()
                                } else if (totalDragOffset > 45f && selectedProfileTab > 0) {
                                    selectedProfileTab--
                                    SoundManager.playClickSound()
                                }
                            }
                        )
                    }
            ) {
                // Header: Cover Photo & Centered Avatar
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                        ) {
                            // Cover Image (Clickable -> View Cover Photo)
                            AsyncImage(
                                model = coverPhotoUrl,
                                contentDescription = "Cover photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clickable {
                                        fullScreenViewerImage = coverPhotoUrl
                                        fullScreenViewerTitle = "Cover photo"
                                    },
                                contentScale = ContentScale.Crop
                            )

                            // Centered Avatar Image (Clickable -> View Story if available or View Profile Picture)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .size(130.dp)
                                    .clickable {
                                        if (hasStory) {
                                            showAvatarOptionsSheet = true
                                        } else {
                                            fullScreenViewerImage = userAvatarUrl
                                            fullScreenViewerTitle = "Profile picture"
                                        }
                                    }
                            ) {
                                if (userAvatarUrl.startsWith("drawable/") || userAvatarUrl.isBlank()) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_user_avatar),
                                        contentDescription = "Profile picture",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .border(
                                                width = 4.dp,
                                                color = if (hasStory) FacebookBlue else MaterialTheme.colorScheme.surface,
                                                shape = CircleShape
                                            ),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    AsyncImage(
                                        model = userAvatarUrl,
                                        contentDescription = "Profile picture",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .border(
                                                width = 4.dp,
                                                color = if (hasStory) FacebookBlue else MaterialTheme.colorScheme.surface,
                                                shape = CircleShape
                                            ),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                // Story indicator badge if user has story
                                if (hasStory) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(FacebookBlue)
                                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoStories,
                                            contentDescription = "Story active",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Centered User Info & Action Buttons
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = userName,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Software Developer 💻 • Tech Enthusiast • Dhaka 📍",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                            )

                            // Followers / Posts / Following Stats Bar (Clickable)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Followers
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable {
                                        followersModalInitialTab = 0
                                        showFollowersModal = true
                                    }
                                ) {
                                    Text(
                                        text = if (isFriend || isFollowing) "1" else "0",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FacebookBlue
                                    )
                                    Text(
                                        text = "Followers",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(24.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                )

                                // Posts
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable { selectedProfileTab = 0 }
                                ) {
                                    Text(
                                        text = "${userPosts.size}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Posts",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(24.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                )

                                // Following
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable {
                                        followersModalInitialTab = 1
                                        showFollowersModal = true
                                    }
                                ) {
                                    Text(
                                        text = "0",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FacebookBlue
                                    )
                                    Text(
                                        text = "Following",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action Buttons Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val isSelf = userName.equals(currentUserName, ignoreCase = true) ||
                                        userName.equals("Me", ignoreCase = true) ||
                                        userName.equals("You", ignoreCase = true)

                                if (!isSelf) {
                                    // Follow / Friend Button
                                    Button(
                                        onClick = {
                                            SoundManager.playClickSound()
                                            if (isFriend || isFollowing) {
                                                isFriend = false
                                                isFollowing = false
                                                Toast.makeText(context, "Unfollowed $userName", Toast.LENGTH_SHORT).show()
                                            } else {
                                                isFriend = true
                                                isFollowing = true
                                                Toast.makeText(context, "You are now following $userName and added as friend!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isFriend || isFollowing) MaterialTheme.colorScheme.surfaceVariant else FacebookBlue
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isFriend || isFollowing) Icons.Default.People else Icons.Default.PersonAdd,
                                            contentDescription = null,
                                            tint = if (isFriend || isFollowing) MaterialTheme.colorScheme.onSurface else Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = when {
                                                isFriend || isFollowing -> "Following ✓"
                                                else -> "Follow"
                                            },
                                            color = if (isFriend || isFollowing) MaterialTheme.colorScheme.onSurface else Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        SoundManager.playClickSound()
                                        onOpenMessage(userName, userAvatarUrl)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue.copy(alpha = 0.12f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Message, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Message", color = FacebookBlue, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { Toast.makeText(context, "Profile Options", Toast.LENGTH_SHORT).show() },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.width(48.dp)
                                ) {
                                    Icon(Icons.Default.MoreHoriz, contentDescription = "More", tint = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)

                        // Profile Tabs
                        ScrollableTabRow(
                            selectedTabIndex = selectedProfileTab,
                            containerColor = MaterialTheme.colorScheme.surface,
                            edgePadding = 16.dp
                        ) {
                            profileTabs.forEachIndexed { index, tab ->
                                Tab(
                                    selected = selectedProfileTab == index,
                                    onClick = { selectedProfileTab = index },
                                    text = {
                                        Text(
                                            tab,
                                            color = if (selectedProfileTab == index) FacebookBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (selectedProfileTab == index) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 15.sp
                                        )
                                    }
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 6.dp)
                }

                // Tab 0: User Posts
                if (selectedProfileTab == 0) {
                    items(userPosts, key = { it.id }) { post ->
                        PostItemCard(
                            post = post,
                            onReactionSelect = { reaction -> onReactionSelect(post.id, reaction) },
                            onCommentClick = { onCommentClick(post.id) },
                            onShareClick = { onShareClick(post.id) },
                            onSaveToggle = { onSaveToggle(post.id) },
                            onDeletePost = { onDeletePost(post.id) },
                            currentUserName = currentUserName
                        )
                    }
                }

                // Tab 1: Reels
                if (selectedProfileTab == 1) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(
                                text = "$userName's Reels",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            if (userReels.isEmpty()) {
                                Text("No reels posted yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(380.dp)
                                ) {
                                    itemsIndexed(userReels, key = { _, item -> item.id }) { index, reel ->
                                        Card(
                                            shape = RoundedCornerShape(8.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            modifier = Modifier
                                                .aspectRatio(9f / 16f)
                                                .clickable { activePlayingReelIndex = index }
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize()) {
                                                AsyncImage(
                                                    model = reel.thumbnailUrl,
                                                    contentDescription = reel.title,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )

                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomStart)
                                                        .padding(4.dp)
                                                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.PlayArrow,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(2.dp))
                                                        Text(
                                                            text = reel.viewsCount,
                                                            color = Color.White,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
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
                }

                // Tab 2: Photos
                if (selectedProfileTab == 2) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(
                                text = "Photos",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            if (userPhotos.isEmpty()) {
                                Text("No photos available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp)
                                ) {
                                    items(userPhotos) { photoUrl ->
                                        Card(
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier
                                                .aspectRatio(1f)
                                                .clickable {
                                                    fullScreenViewerImage = photoUrl
                                                    fullScreenViewerTitle = "Photo"
                                                }
                                        ) {
                                            AsyncImage(
                                                model = photoUrl,
                                                contentDescription = "Photo",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Tab 3: About
                if (selectedProfileTab == 3) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "About $userName",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.HomeWork, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Work", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("Software Developer", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.School, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Education", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("Computer Science & Engineering", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Lives in", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("Dhaka, Bangladesh", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Tab 4: Friends
                if (selectedProfileTab == 4) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Friends",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Text(
                                text = if (isFriend) "You and $userName are friends." else "No mutual friends to show.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal: Avatar Options Sheet for Visitors when user has active story
    if (showAvatarOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAvatarOptionsSheet = false },
            sheetState = avatarOptionsSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = userName,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                if (hasStory) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                showAvatarOptionsSheet = false
                                onStoryClick(userStoryIndex)
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoStories, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("View story", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("See the active story shared by $userName", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            showAvatarOptionsSheet = false
                            fullScreenViewerImage = userAvatarUrl
                            fullScreenViewerTitle = "Profile picture"
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.RemoveRedEye, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("View profile picture", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("See $userName's profile photo in full screen", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal: Full-screen Image Viewer
    fullScreenViewerImage?.let { imgUrl ->
        FullScreenImageViewerModal(
            imageUrl = imgUrl,
            title = fullScreenViewerTitle,
            authorName = userName,
            onDismiss = { fullScreenViewerImage = null }
        )
    }

    if (showFollowersModal) {
        FollowersFollowingModal(
            userName = userName,
            followerCount = if (isFriend) 1 else 0,
            followingCount = 0,
            initialTab = followersModalInitialTab,
            onDismiss = { showFollowersModal = false }
        )
    }

    if (activePlayingReelIndex != null && userReels.isNotEmpty()) {
        ReelPlayerModal(
            reels = userReels,
            initialIndex = activePlayingReelIndex!!,
            creatorName = userName,
            creatorAvatar = userAvatarUrl,
            onDismiss = { activePlayingReelIndex = null }
        )
    }
}
