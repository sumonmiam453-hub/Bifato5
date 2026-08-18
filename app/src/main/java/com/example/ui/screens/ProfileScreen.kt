package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.data.local.entities.UserProfileEntity
import com.example.ui.components.AccountStatusPrivacyDialog
import com.example.ui.components.CompanyPresetsDialog
import com.example.ui.components.CreatorDashboardModal
import com.example.ui.components.FollowersFollowingModal
import com.example.ui.components.FullScreenImageViewerModal
import com.example.ui.components.ImageCropAdjustDialog
import com.example.ui.components.PostItemCard
import com.example.ui.components.ProfileSettingsBottomSheet
import com.example.ui.components.ReelPlayerModal
import com.example.ui.theme.FacebookBlue
import com.example.util.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ProfileReelItem(
    val id: Long,
    val title: String,
    val thumbnailUrl: String,
    val viewsCount: String
)

data class FriendUserItem(
    val id: Long,
    val name: String,
    val avatarUrl: String,
    val mutualCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profile: UserProfileEntity?,
    userPosts: List<PostEntity>,
    onEditProfileClick: () -> Unit,
    onAddStoryClick: () -> Unit,
    onReactionSelect: (Long, String) -> Unit,
    onCommentClick: (Long) -> Unit,
    onShareClick: (Long) -> Unit,
    onSaveToggle: (Long) -> Unit,
    onDeletePost: (Long) -> Unit,
    onUpdateAvatar: (String) -> Unit = {},
    onUpdateCover: (String) -> Unit = {},
    onToggleCreatorMode: (Boolean) -> Unit = {},
    onUpdatePrivacy: (String) -> Unit = {},
    onOpenDashboard: () -> Unit = {},
    onOpenPages: () -> Unit = {},
    onOpenGroups: () -> Unit = {},
    onOpenWallet: () -> Unit = {},
    onOpenMarketplace: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    var selectedProfileTab by remember { mutableIntStateOf(0) }
    var showProfileSettingsSheet by remember { mutableStateOf(false) }
    var showAccountStatusDialog by remember { mutableStateOf(false) }
    var showDashboardModal by remember { mutableStateOf(false) }
    var showFollowersModal by remember { mutableStateOf(false) }
    var followersModalInitialTab by remember { mutableIntStateOf(0) }

    // Media viewer & Crop modals
    var showAvatarOptionsSheet by remember { mutableStateOf(false) }
    var showCoverOptionsSheet by remember { mutableStateOf(false) }
    var showCompanyPresetsDialog by remember { mutableStateOf(false) }
    var companyPresetsInitialTab by remember { mutableIntStateOf(0) }
    var fullScreenViewerImage by remember { mutableStateOf<String?>(null) }
    var fullScreenViewerTitle by remember { mutableStateOf("Photo") }

    var pendingCropImage by remember { mutableStateOf<String?>(null) }
    var isCropCircular by remember { mutableStateOf(true) }

    // View As (Public preview mode)
    var isViewAsMode by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()
    val avatarOptionsSheetState = rememberModalBottomSheetState()
    val coverOptionsSheetState = rememberModalBottomSheetState()

    val profileTabs = listOf("Posts", "Reels", "Photos", "About", "Friends")

    val coverLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val path = com.example.util.FileUtils.getLocalFilePathFromUri(context, it)
            pendingCropImage = path
            isCropCircular = false
        }
    }

    val avatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val path = com.example.util.FileUtils.getLocalFilePathFromUri(context, it)
            pendingCropImage = path
            isCropCircular = true
        }
    }

    val name = profile?.name ?: "Maruf Hossain"
    val bio = profile?.bio ?: "Mobile Developer & Tech Enthusiast 🚀"
    val livesIn = profile?.livesIn ?: "Dhaka, Bangladesh"
    val work = profile?.work ?: "Software Engineer"
    val education = profile?.education ?: "Computer Science & Engineering"
    val followers = profile?.followerCount ?: 1420
    val following = 340
    val coverPhotoUrl = profile?.coverPhotoUrl?.ifBlank { "drawable/img_user_cover" } ?: "drawable/img_user_cover"
    val avatarUrl = profile?.avatarUrl?.ifBlank { "drawable/img_user_avatar" } ?: "drawable/img_user_avatar"
    val isCreatorMode = profile?.isCreatorMode == true
    val privacyStatus = profile?.privacyStatus ?: "PUBLIC"

    var activePlayingReelIndex by remember { mutableStateOf<Int?>(null) }

    val userPostsFiltered = remember(userPosts, name) {
        userPosts.filter { it.authorName.equals(name, ignoreCase = true) || it.authorName == name }
    }

    val userReels = remember(userPostsFiltered) {
        val list = mutableStateListOf<ProfileReelItem>()
        userPostsFiltered.forEachIndexed { idx, post ->
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

    val userPhotos = remember(userPostsFiltered, avatarUrl, coverPhotoUrl) {
        val photosFromPosts = userPostsFiltered.mapNotNull { it.imageUrl }.filter { it.isNotBlank() }
        val all = mutableListOf<String>()
        if (avatarUrl.isNotBlank() && !avatarUrl.startsWith("drawable/")) all.add(avatarUrl)
        if (coverPhotoUrl.isNotBlank() && !coverPhotoUrl.startsWith("drawable/")) all.add(coverPhotoUrl)
        all.addAll(photosFromPosts)
        all.distinct()
    }

    val friendsList = remember {
        mutableStateListOf<FriendUserItem>()
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
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
            // View As Banner (if in View As mode)
            if (isViewAsMode) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Viewing as Public",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    )
                                    Text(
                                        text = "This is what your profile looks like to visitors.",
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { isViewAsMode = false },
                                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Exit View As", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Cover Photo & Centered Avatar Header
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
                        // Cover Photo Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clickable {
                                    if (isViewAsMode) {
                                        fullScreenViewerImage = coverPhotoUrl
                                        fullScreenViewerTitle = "Cover photo"
                                    } else {
                                        showCoverOptionsSheet = true
                                    }
                                }
                        ) {
                            if (coverPhotoUrl.startsWith("drawable/") || coverPhotoUrl.isBlank()) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_user_cover),
                                    contentDescription = "Cover Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                AsyncImage(
                                    model = coverPhotoUrl,
                                    contentDescription = "Cover Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Cover Camera Button (Only in own editable mode)
                            if (!isViewAsMode) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(bottom = 12.dp, end = 16.dp)
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.65f))
                                        .clickable { showCoverOptionsSheet = true }
                                        .testTag("edit_cover_photo_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Edit Cover",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Centered Avatar Circle
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .size(130.dp)
                                .clickable {
                                    if (isViewAsMode) {
                                        fullScreenViewerImage = avatarUrl
                                        fullScreenViewerTitle = "Profile picture"
                                    } else {
                                        showAvatarOptionsSheet = true
                                    }
                                }
                        ) {
                            if (avatarUrl.startsWith("drawable/") || avatarUrl.isBlank()) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_user_avatar),
                                    contentDescription = name,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = name,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Avatar Camera Icon (Only in own editable mode)
                            if (!isViewAsMode) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .border(2.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                        .clickable { showAvatarOptionsSheet = true }
                                        .testTag("edit_avatar_photo_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Change Profile Picture",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Profile Name & Bio (Centered)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        if (isCreatorMode) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(FacebookBlue.copy(alpha = 0.12f))
                                    .clickable { onOpenDashboard() }
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = FacebookBlue,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Digital Creator • Creator Mode Active",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FacebookBlue
                                )
                            }
                        }

                        if (bio.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = bio,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Followers / Posts / Following Stats Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    followersModalInitialTab = 0
                                    showFollowersModal = true
                                }
                            ) {
                                Text(
                                    text = "$followers",
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

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { selectedProfileTab = 0 }
                            ) {
                                Text(
                                    text = "${userPostsFiltered.size}",
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

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    followersModalInitialTab = 1
                                    showFollowersModal = true
                                }
                            ) {
                                Text(
                                    text = "$following",
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

                        // Action Buttons: If in View As mode, show Visitor buttons. Otherwise show Edit/Add Story/More.
                        if (isViewAsMode) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { Toast.makeText(context, "Cannot add yourself as friend in View As", Toast.LENGTH_SHORT).show() },
                                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Friend", fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { Toast.makeText(context, "Cannot message yourself in View As", Toast.LENGTH_SHORT).show() },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Message, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Message", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isCreatorMode) {
                                    Button(
                                        onClick = {
                                            SoundManager.playClickSound()
                                            onOpenDashboard()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("profile_dashboard_button")
                                    ) {
                                        Icon(Icons.Default.Assessment, contentDescription = "Dashboard", tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Dashboard", fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = onAddStoryClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("profile_add_story")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add to story", fontWeight = FontWeight.Bold)
                                    }
                                }

                                OutlinedButton(
                                    onClick = onEditProfileClick,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("profile_edit_button")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit profile", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { showProfileSettingsSheet = true },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .width(48.dp)
                                        .testTag("profile_three_dots_button")
                                ) {
                                    Icon(Icons.Default.MoreHoriz, contentDescription = "More", tint = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }

                    // Locked Profile Banner in View As mode if privacy is PRIVATE
                    if (isViewAsMode && privacyStatus == "PRIVATE") {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(FacebookBlue.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = FacebookBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "$name locked their profile",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Only their friends can see what they share on their profile, photos, and stories.",
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)

                    // Scrollable Profile Tabs (Posts, Reels, Photos, About, Friends)
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

            // Tab Content: If in View As mode and profile is PRIVATE, hide posts
            if (isViewAsMode && privacyStatus == "PRIVATE" && (selectedProfileTab == 0 || selectedProfileTab == 1 || selectedProfileTab == 2)) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Posts are Private",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Send a friend request to see posts and photos from this user.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Tab 0: Posts Timeline
                if (selectedProfileTab == 0) {
                    if (userPostsFiltered.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No posts on your timeline yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    items(userPostsFiltered, key = { it.id }) { post ->
                        PostItemCard(
                            post = post,
                            onReactionSelect = { reaction -> onReactionSelect(post.id, reaction) },
                            onCommentClick = { onCommentClick(post.id) },
                            onShareClick = { onShareClick(post.id) },
                            onSaveToggle = { onSaveToggle(post.id) },
                            onDeletePost = { onDeletePost(post.id) },
                            currentUserName = profile?.name ?: "Maruf Hossain"
                        )
                    }
                }

                // Tab 1: User's Reels Tab
                if (selectedProfileTab == 1) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "My Reels",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            if (userReels.isEmpty()) {
                                Text("No reels uploaded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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

                // Tab 2: Photos Tab
                if (selectedProfileTab == 2) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Photos",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            if (userPhotos.isEmpty()) {
                                Text("No photos uploaded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                                contentDescription = "User photo",
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

                // Tab 3: About Tab
                if (selectedProfileTab == 3) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "About Info",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (!isViewAsMode) {
                                    Button(
                                        onClick = onEditProfileClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Edit About", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

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
                                            Text(work, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.School, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Education", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(education, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Lives in", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(livesIn, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Tab 4: Friends Tab
                if (selectedProfileTab == 4) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Friends (${friendsList.size})",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            if (friendsList.isEmpty()) {
                                Text("No friends to show.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    friendsList.forEach { friend ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                AsyncImage(
                                                    model = friend.avatarUrl,
                                                    contentDescription = friend.name,
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(friend.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                    Text("${friend.mutualCount} mutual friends", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }

                                            if (!isViewAsMode) {
                                                OutlinedButton(
                                                    onClick = { friendsList.remove(friend) },
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("Unfriend", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Avatar Options Bottom Sheet (View vs Change)
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
                    text = "Profile Picture",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            showAvatarOptionsSheet = false
                            fullScreenViewerImage = avatarUrl
                            fullScreenViewerTitle = "Profile picture"
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.RemoveRedEye, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("View profile picture", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("See your profile photo in full screen", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            showAvatarOptionsSheet = false
                            avatarLauncher.launch("image/*")
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Select profile picture", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Choose a new photo from gallery & adjust crop", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            showAvatarOptionsSheet = false
                            companyPresetsInitialTab = 0
                            showCompanyPresetsDialog = true
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Company default avatars", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Choose from 10 stylish preset avatars & crop", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal: Cover Photo Options Bottom Sheet (View vs Change)
    if (showCoverOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCoverOptionsSheet = false },
            sheetState = coverOptionsSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Cover Photo",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            showCoverOptionsSheet = false
                            fullScreenViewerImage = coverPhotoUrl
                            fullScreenViewerTitle = "Cover photo"
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.RemoveRedEye, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("View cover photo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("See your cover banner in full screen", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            showCoverOptionsSheet = false
                            coverLauncher.launch("image/*")
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Upload cover photo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Choose a new cover image from gallery & reposition", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            showCoverOptionsSheet = false
                            companyPresetsInitialTab = 1
                            showCompanyPresetsDialog = true
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Company default banners", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Choose from 10 professional preset banners & crop", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal: Company Default Presets Dialog (10 Avatars + 10 Banners)
    if (showCompanyPresetsDialog) {
        CompanyPresetsDialog(
            initialTab = companyPresetsInitialTab,
            onSelectPreset = { selectedUrl, isAvatar ->
                showCompanyPresetsDialog = false
                pendingCropImage = selectedUrl
                isCropCircular = isAvatar
            },
            onDismiss = { showCompanyPresetsDialog = false }
        )
    }

    // Modal: Full-screen Image Viewer
    fullScreenViewerImage?.let { imgUrl ->
        FullScreenImageViewerModal(
            imageUrl = imgUrl,
            title = fullScreenViewerTitle,
            authorName = name,
            onDismiss = { fullScreenViewerImage = null }
        )
    }

    // Modal: Image Crop & Adjust Dialog
    pendingCropImage?.let { cropPath ->
        ImageCropAdjustDialog(
            imagePath = cropPath,
            isCircular = isCropCircular,
            onDismiss = { pendingCropImage = null },
            onConfirm = { confirmedPath ->
                if (isCropCircular) {
                    onUpdateAvatar(confirmedPath)
                    Toast.makeText(context, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    onUpdateCover(confirmedPath)
                    Toast.makeText(context, "Cover photo updated successfully!", Toast.LENGTH_SHORT).show()
                }
                pendingCropImage = null
            }
        )
    }

    // Modal: Profile 3-Dots Settings Bottom Sheet
    if (showProfileSettingsSheet) {
        ProfileSettingsBottomSheet(
            sheetState = sheetState,
            userName = name,
            privacyStatus = privacyStatus,
            onOpenAccountStatus = { showAccountStatusDialog = true },
            onViewAsClick = {
                isViewAsMode = true
                Toast.makeText(context, "Entered View As mode (Public)", Toast.LENGTH_SHORT).show()
            },
            onOpenPages = onOpenPages,
            onOpenGroups = onOpenGroups,
            onOpenMonetization = onOpenDashboard,
            onOpenWallet = onOpenWallet,
            onOpenMarketplace = onOpenMarketplace,
            onOpenSettings = onOpenSettings,
            onDismiss = { showProfileSettingsSheet = false }
        )
    }

    // Modal: Account Status & Privacy Dialog
    if (showAccountStatusDialog) {
        AccountStatusPrivacyDialog(
            currentPrivacy = privacyStatus,
            onSavePrivacy = { newPrivacy ->
                onUpdatePrivacy(newPrivacy)
                Toast.makeText(context, "Profile privacy updated to $newPrivacy", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showAccountStatusDialog = false }
        )
    }

    if (showDashboardModal) {
        CreatorDashboardModal(
            userName = name,
            userAvatar = avatarUrl,
            followerCount = followers,
            onDismiss = { showDashboardModal = false }
        )
    }

    if (showFollowersModal) {
        FollowersFollowingModal(
            userName = name,
            followerCount = followers,
            followingCount = following,
            initialTab = followersModalInitialTab,
            onDismiss = { showFollowersModal = false }
        )
    }

    if (activePlayingReelIndex != null && userReels.isNotEmpty()) {
        ReelPlayerModal(
            reels = userReels,
            initialIndex = activePlayingReelIndex!!,
            creatorName = name,
            creatorAvatar = avatarUrl,
            onDismiss = { activePlayingReelIndex = null }
        )
    }
}
