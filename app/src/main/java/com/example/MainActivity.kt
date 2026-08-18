package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CommentBottomSheet
import com.example.ui.components.CreateGroupDialog
import com.example.ui.components.CreateMarketplaceDialog
import com.example.ui.components.CreatePageDialog
import com.example.ui.components.CreatePostDialog
import com.example.ui.components.EditProfileDialog
import com.example.ui.components.FacebookHeader
import com.example.ui.components.FacebookBottomNav

import com.example.ui.components.MarketplaceDetailModal
import com.example.ui.components.MessengerDrawerModal
import com.example.ui.components.StoryViewerModal
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MarketplaceScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReelsScreen
import com.example.ui.screens.CreateStoryScreen
import com.example.ui.screens.CreatePageScreen
import com.example.ui.screens.CreateGroupScreen
import com.example.ui.screens.MessengerScreen
import com.example.ui.screens.NoInternetScreen
import com.example.ui.screens.StorageManagementScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.FacebookTheme
import com.example.ui.viewmodel.FacebookViewModel
import com.example.util.NetworkMonitor

import com.example.ui.screens.AuthScreen
import com.example.data.UserProfile
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState

class MainActivity : ComponentActivity() {

    private val viewModel: FacebookViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val isOnline by remember(context) {
                NetworkMonitor.observeConnectivity(context)
            }.collectAsStateWithLifecycle(initialValue = NetworkMonitor.isOnline(context))

            val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
            val postUploadProgress by viewModel.postUploadProgress.collectAsStateWithLifecycle()

            val posts by viewModel.filteredPosts.collectAsStateWithLifecycle()
            val stories by viewModel.stories.collectAsStateWithLifecycle()
            val notifications by viewModel.notifications.collectAsStateWithLifecycle()
            val unreadCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
            val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
            val marketplaceItems by viewModel.filteredMarketplaceItems.collectAsStateWithLifecycle()
            val activeCategory by viewModel.activeCategory.collectAsStateWithLifecycle()

            val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
            val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

            val commentPostId by viewModel.commentPostId.collectAsStateWithLifecycle()
            val activeComments by viewModel.activeComments.collectAsStateWithLifecycle()

            val activeStoryIndex by viewModel.activeStoryIndex.collectAsStateWithLifecycle()
            val showCreatePostDialog by viewModel.showCreatePostDialog.collectAsStateWithLifecycle()
            val showCreateMarketplaceDialog by viewModel.showCreateMarketplaceDialog.collectAsStateWithLifecycle()
            val showCreateGroupDialog by viewModel.showCreateGroupDialog.collectAsStateWithLifecycle()
            val showCreatePageDialog by viewModel.showCreatePageDialog.collectAsStateWithLifecycle()
            val showEntityList by viewModel.showEntityList.collectAsStateWithLifecycle()
            val showSavedScreen by viewModel.showSavedScreen.collectAsStateWithLifecycle()
            val visitedUser by viewModel.visitedUser.collectAsStateWithLifecycle()
            val savedPosts by viewModel.savedPosts.collectAsStateWithLifecycle()
            val userGroups by viewModel.userGroups.collectAsStateWithLifecycle()
            val userPages by viewModel.userPages.collectAsStateWithLifecycle()
            val showCreateStoryScreen by viewModel.showCreateStoryScreen.collectAsStateWithLifecycle()
            val showEditProfileDialog by viewModel.showEditProfileDialog.collectAsStateWithLifecycle()
            val showMessengerDrawer by viewModel.showMessengerDrawer.collectAsStateWithLifecycle()
            val activeDirectChatRecipient by viewModel.activeDirectChatRecipient.collectAsStateWithLifecycle()
            val selectedMarketplaceItem by viewModel.selectedMarketplaceItem.collectAsStateWithLifecycle()
            val sharePostUrl by viewModel.sharePostUrl.collectAsStateWithLifecycle()

            val showWatchHistoryScreen by viewModel.showWatchHistoryScreen.collectAsStateWithLifecycle()
            val showSettingsScreen by viewModel.showSettingsScreen.collectAsStateWithLifecycle()
            val showStorageManagementScreen by viewModel.showStorageManagementScreen.collectAsStateWithLifecycle()
            val showNotificationsScreen by viewModel.showNotificationsScreen.collectAsStateWithLifecycle()
            val showWalletScreen by viewModel.showWalletScreen.collectAsStateWithLifecycle()
            val showProfessionalDashboard by viewModel.showProfessionalDashboard.collectAsStateWithLifecycle()
            val r2StorageConfigs by viewModel.r2StorageConfigs.collectAsStateWithLifecycle()
            val activeR2StorageConfig by viewModel.activeR2StorageConfig.collectAsStateWithLifecycle()
            val walletBalance by viewModel.walletBalance.collectAsStateWithLifecycle()
            val walletTransactions by viewModel.walletTransactions.collectAsStateWithLifecycle()
            val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()

            var showSplash by remember { mutableStateOf(true) }

            // System Back Button Navigation Handling for all pages, modals & tabs
            val isBackHandlerEnabled = isLoggedIn && (
                commentPostId != null ||
                activeStoryIndex != null ||
                showCreatePostDialog ||
                showCreateMarketplaceDialog ||
                showCreateGroupDialog ||
                showCreatePageDialog ||
                showEditProfileDialog ||
                showMessengerDrawer ||
                selectedMarketplaceItem != null ||
                sharePostUrl != null ||
                showCreateStoryScreen ||
                showSavedScreen ||
                showEntityList != null ||
                visitedUser != null ||
                showWatchHistoryScreen ||
                showSettingsScreen ||
                showStorageManagementScreen ||
                showNotificationsScreen ||
                showWalletScreen ||
                showProfessionalDashboard ||
                isSearchActive ||
                selectedTab != 0
            )

            BackHandler(enabled = isBackHandlerEnabled) {
                when {
                    commentPostId != null -> viewModel.closeComments()
                    activeStoryIndex != null -> viewModel.closeStoryViewer()
                    sharePostUrl != null -> viewModel.closeShareSheet()
                    showCreatePostDialog -> viewModel.setCreatePostDialogVisible(false)
                    showCreateMarketplaceDialog -> viewModel.setCreateMarketplaceDialogVisible(false)
                    showCreateGroupDialog -> viewModel.setCreateGroupDialogVisible(false)
                    showCreatePageDialog -> viewModel.setCreatePageDialogVisible(false)
                    showEditProfileDialog -> viewModel.setEditProfileDialogVisible(false)
                    showMessengerDrawer -> {
                        viewModel.closeDirectChat()
                        viewModel.setMessengerDrawerVisible(false)
                    }
                    selectedMarketplaceItem != null -> viewModel.selectMarketplaceItem(null)
                    showCreateStoryScreen -> viewModel.setCreateStoryScreenVisible(false)
                    showSavedScreen -> viewModel.setShowSavedScreen(false)
                    showEntityList != null -> viewModel.setShowEntityList(null)
                    visitedUser != null -> viewModel.closeVisitedProfile()
                    showWatchHistoryScreen -> viewModel.setShowWatchHistoryScreen(false)
                    showStorageManagementScreen -> viewModel.setShowStorageManagementScreen(false)
                    showSettingsScreen -> viewModel.setShowSettingsScreen(false)
                    showNotificationsScreen -> viewModel.setShowNotificationsScreen(false)
                    showWalletScreen -> viewModel.setShowWalletScreen(false)
                    showProfessionalDashboard -> viewModel.setShowProfessionalDashboard(false)
                    isSearchActive -> viewModel.setSearchActive(false)
                    selectedTab != 0 -> viewModel.setSelectedTab(0)
                }
            }

            FacebookTheme(darkTheme = isDarkMode) {
                if (!isOnline) {
                    NoInternetScreen(
                        onRetry = {
                            viewModel.refreshData()
                        }
                    )
                } else if (showSplash) {
                    com.example.ui.screens.SplashScreen(
                        onSplashFinished = { showSplash = false }
                    )
                } else if (!isLoggedIn) {
                    AuthScreen(
                        onLoginSuccess = { profile ->
                            viewModel.loginUser(profile)
                        }
                    )
                } else {
                    val isFullScreenOverlayActive = showWalletScreen ||
                            showProfessionalDashboard ||
                            showSettingsScreen ||
                            showStorageManagementScreen ||
                            showSavedScreen ||
                            showWatchHistoryScreen ||
                            showNotificationsScreen ||
                            showEntityList != null ||
                            visitedUser != null

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            if (!isFullScreenOverlayActive) {
                                FacebookBottomNav(
                                    selectedTab = selectedTab,
                                    isMessengerActive = showMessengerDrawer,
                                    onTabSelected = { tab -> 
                                        viewModel.setMessengerDrawerVisible(false)
                                        viewModel.closeDirectChat()
                                        viewModel.setSelectedTab(tab) 
                                    },
                                    onCreatePostClick = { viewModel.setCreatePostDialogVisible(true) },
                                    onMessengerClick = { viewModel.setMessengerDrawerVisible(true) },
                                    userAvatarUrl = userProfile?.avatarUrl
                                )
                            }
                        },
                        topBar = {
                            if (!isFullScreenOverlayActive && !showMessengerDrawer) {
                                Column(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
                                    if (isSearchActive) {
                                        // Live Search Header Bar
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surface)
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(onClick = { viewModel.setSearchActive(false) }) {
                                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                            }

                                            OutlinedTextField(
                                                value = searchQuery,
                                                onValueChange = { viewModel.setSearchQuery(it) },
                                                placeholder = { Text("Search Frndom...", fontSize = 14.sp) },
                                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                                trailingIcon = {
                                                    if (searchQuery.isNotEmpty()) {
                                                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                                            Icon(Icons.Default.Close, contentDescription = "Clear")
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = TextFieldDefaults.colors(
                                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                    focusedIndicatorColor = Color.Transparent,
                                                    unfocusedIndicatorColor = Color.Transparent
                                                ),
                                                singleLine = true
                                            )
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)
                                    } else {
                                        FacebookHeader(
                                            onSearchClick = { viewModel.setSearchActive(true) },
                                            onFriendClick = { viewModel.setSelectedTab(1) },
                                            onMenuClick = { viewModel.setSelectedTab(5) },
                                            onNotificationClick = { viewModel.setShowNotificationsScreen(true) },
                                            unreadNotificationsCount = unreadCount
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(if (isFullScreenOverlayActive) androidx.compose.foundation.layout.PaddingValues(0.dp) else innerPadding)
                        ) {
                        // Main Tab Views Switch
                        if (isSearchActive) {
                            SearchScreen(
                                searchQuery = searchQuery,
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                posts = posts,
                                onReactionSelect = { id, reaction -> viewModel.toggleReaction(id, reaction) },
                                onCommentClick = { id -> viewModel.openCommentsForPost(id) },
                                onShareClick = { id -> viewModel.openShareSheet(id) },
                                onSaveToggle = { id -> viewModel.toggleSavePost(id) },
                                onDeletePost = { id -> viewModel.deletePost(id) },
                                onVisitProfile = { name, avatar -> viewModel.visitProfile(name, avatar) },
                                onSendFriendRequest = { name, avatar -> viewModel.sendFriendRequestNotification(name, avatar) },
                                currentUserName = userProfile?.name ?: "Maruf Hossain"
                            )
                        } else if (showMessengerDrawer) {
                            MessengerScreen(
                                currentUid = com.example.data.FirebaseManager.getCurrentUserId(),
                                currentUserName = userProfile?.name ?: "User",
                                currentUserAvatar = userProfile?.avatarUrl ?: "",
                                initialRecipient = activeDirectChatRecipient,
                                onBackClick = {
                                    viewModel.closeDirectChat()
                                    viewModel.setMessengerDrawerVisible(false)
                                }
                            )
                        } else {
                            when (selectedTab) {
                            0 -> HomeScreen(
                                posts = posts,
                                stories = stories,
                                userAvatarUrl = userProfile?.avatarUrl,
                                userName = userProfile?.name ?: "Maruf Hossain",
                                uploadProgress = postUploadProgress,
                                isLoading = viewModel.isLoadingFirebaseData.collectAsStateWithLifecycle().value,
                                onComposerClick = { viewModel.setCreatePostDialogVisible(true) },
                                onStoryClick = { index -> viewModel.openStoryViewer(index) },
                                onAddStoryClick = { viewModel.setCreateStoryScreenVisible(true) },
                                onReactionSelect = { id, reaction -> viewModel.toggleReaction(id, reaction) },
                                onCommentClick = { id -> viewModel.openCommentsForPost(id) },
                                onShareClick = { id -> viewModel.openShareSheet(id) },
                                onSaveToggle = { id -> viewModel.toggleSavePost(id) },
                                onDeletePost = { id -> viewModel.deletePost(id) },
                                onVisitProfile = { name, avatar -> viewModel.visitProfile(name, avatar) },
                                onRefresh = { viewModel.refreshData() }
                            )

                            1 -> com.example.ui.screens.FriendsScreen(
                                onVisitProfile = { name, avatar -> viewModel.visitProfile(name, avatar) }
                            )

                            2 -> ReelsScreen(
                                posts = posts,
                                onVisitProfile = { name, avatar -> viewModel.visitProfile(name, avatar) },
                                onVideoPlayed = { historyItem -> viewModel.addToWatchHistory(historyItem) },
                                onDeletePost = { id -> viewModel.deletePost(id) },
                                onShareClick = { id -> viewModel.openShareSheet(id) }
                            )

                            3 -> ProfileScreen(
                                profile = userProfile,
                                userPosts = posts,
                                onEditProfileClick = { viewModel.setEditProfileDialogVisible(true) },
                                onAddStoryClick = { viewModel.setCreateStoryScreenVisible(true) },
                                onReactionSelect = { id, reaction -> viewModel.toggleReaction(id, reaction) },
                                onCommentClick = { id -> viewModel.openCommentsForPost(id) },
                                onShareClick = { id -> viewModel.openShareSheet(id) },
                                onSaveToggle = { id -> viewModel.toggleSavePost(id) },
                                onDeletePost = { id -> viewModel.deletePost(id) },
                                onUpdateAvatar = { url -> viewModel.updateUserAvatar(url) },
                                onUpdateCover = { url -> viewModel.updateUserCover(url) },
                                onToggleCreatorMode = { enabled -> viewModel.toggleCreatorMode(enabled) },
                                onUpdatePrivacy = { privacy -> viewModel.updatePrivacyStatus(privacy) },
                                onOpenDashboard = { viewModel.setShowProfessionalDashboard(true) },
                                onOpenPages = { viewModel.setShowEntityList("Pages") },
                                onOpenGroups = { viewModel.setShowEntityList("Groups") },
                                onOpenWallet = { viewModel.setShowWalletScreen(true) },
                                onOpenMarketplace = { viewModel.setSelectedTab(4) },
                                onOpenSettings = { viewModel.setShowSettingsScreen(true) }
                            )

                            4 -> MarketplaceScreen(
                                items = marketplaceItems,
                                activeCategory = activeCategory,
                                onCategorySelected = { cat -> viewModel.setActiveCategory(cat) },
                                onItemClick = { item -> viewModel.selectMarketplaceItem(item) },
                                onCreateListingClick = { viewModel.setCreateMarketplaceDialogVisible(true) }
                            )

                            5 -> MenuScreen(
                                userProfile = userProfile,
                                userPages = userPages,
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = { viewModel.toggleDarkMode() },
                                onProfileClick = { viewModel.setSelectedTab(3) },
                                onSavedPostsClick = {
                                    viewModel.setShowSavedScreen(true)
                                },
                                onSwitchAccount = { email, name ->
                                    viewModel.loginUser(email, name)
                                },
                                onLogoutClick = {
                                    viewModel.logoutUser()
                                },
                                onCreateGroupClick = {
                                    viewModel.setCreateGroupDialogVisible(true)
                                },
                                onCreatePageClick = {
                                    viewModel.setCreatePageDialogVisible(true)
                                },
                                onOpenGroups = {
                                    viewModel.setShowEntityList("Groups")
                                },
                                onOpenPages = {
                                    viewModel.setShowEntityList("Pages")
                                },
                                onOpenFeeds = {
                                    viewModel.setSelectedTab(0)
                                },
                                onOpenWatch = {
                                    viewModel.setSelectedTab(2)
                                },
                                onOpenMarketplace = {
                                    viewModel.setSelectedTab(4)
                                },
                                onOpenEntityPage = { title ->
                                    viewModel.setShowEntityList(title)
                                },
                                onOpenWatchHistory = {
                                    viewModel.setShowWatchHistoryScreen(true)
                                },
                                onOpenSettings = {
                                    viewModel.setShowSettingsScreen(true)
                                },
                                onOpenWallet = {
                                    viewModel.setShowWalletScreen(true)
                                },
                                onOpenProfessionalDashboard = {
                                    viewModel.setShowProfessionalDashboard(true)
                                }
                            )
                        }
                        }

                        // MODALS & OVERLAYS

                        // 1. Comment Sheet
                        if (commentPostId != null) {
                            CommentBottomSheet(
                                comments = activeComments,
                                onAddComment = { content -> viewModel.addComment(content) },
                                onDismiss = { viewModel.closeComments() }
                            )
                        }

                        // 2. Story Viewer with Multi-Story Pipes
                        activeStoryIndex?.let { index ->
                            if (index < stories.size) {
                                StoryViewerModal(
                                    allStories = stories,
                                    initialIndex = index,
                                    currentUserName = userProfile?.name ?: "Maruf Hossain",
                                    onDeleteStory = { storyId -> viewModel.deleteStory(storyId) },
                                    onDismiss = { viewModel.closeStoryViewer() }
                                )
                            }
                        }

                        // 3. Create Post Dialog
                        if (showCreatePostDialog) {
                            CreatePostDialog(
                                userAvatarUrl = userProfile?.avatarUrl,
                                userName = userProfile?.name ?: "Maruf Hossain",
                                onPostSubmit = { text, img, bgStyle, privacy -> viewModel.createPost(text, img, bgStyle, privacy) },
                                onDismiss = { viewModel.setCreatePostDialogVisible(false) }
                            )
                        }

                        // 4. Create Marketplace Listing Dialog
                        if (showCreateMarketplaceDialog) {
                            CreateMarketplaceDialog(
                                onSubmitItem = { title, price, category, location, desc, img ->
                                    viewModel.createMarketplaceItem(title, price, category, location, desc, img)
                                },
                                onDismiss = { viewModel.setCreateMarketplaceDialogVisible(false) }
                            )
                        }

                        // 5. Create Group Screen (Bottom Sheet)
                        if (showCreateGroupDialog) {
                            ModalBottomSheet(
                                onDismissRequest = { viewModel.setCreateGroupDialogVisible(false) },
                                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                CreateGroupScreen(
                                    onDismiss = { viewModel.setCreateGroupDialogVisible(false) },
                                    onCreateGroup = { groupData ->
                                        viewModel.createGroup(groupData.name)
                                        viewModel.setCreateGroupDialogVisible(false)
                                    }
                                )
                            }
                        }

                        // 6. Create Page Screen (Bottom Sheet)
                        if (showCreatePageDialog) {
                            ModalBottomSheet(
                                onDismissRequest = { viewModel.setCreatePageDialogVisible(false) },
                                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                CreatePageScreen(
                                    onDismiss = { viewModel.setCreatePageDialogVisible(false) },
                                    onCreatePage = { pageData ->
                                        viewModel.createPage(pageData.name)
                                        viewModel.setCreatePageDialogVisible(false)
                                    }
                                )
                            }
                        }

                        // 7. Edit Profile Dialog
                        if (showEditProfileDialog) {
                            EditProfileDialog(
                                currentProfile = userProfile,
                                onSaveProfile = { name, bio, livesIn, work ->
                                    viewModel.updateProfile(name, bio, livesIn, work)
                                },
                                onDismiss = { viewModel.setEditProfileDialogVisible(false) }
                            )
                        }

                        // 8. Marketplace Detail Modal
                        selectedMarketplaceItem?.let { item ->
                            MarketplaceDetailModal(
                                item = item,
                                onDismiss = { viewModel.selectMarketplaceItem(null) }
                            )
                        }

                        // 10. Create Story Screen (Bottom Sheet)
                        if (showCreateStoryScreen) {
                            ModalBottomSheet(
                                onDismissRequest = { viewModel.setCreateStoryScreenVisible(false) },
                                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                CreateStoryScreen(
                                    onDismiss = { viewModel.setCreateStoryScreenVisible(false) },
                                    onStoryCreated = { contentOrImg, text, music -> 
                                        val name = userProfile?.name ?: "User"
                                        val avatar = userProfile?.avatarUrl ?: ""
                                        viewModel.addStory(text ?: "", contentOrImg, name, avatar)
                                        viewModel.setCreateStoryScreenVisible(false) 
                                    }
                                )
                            }
                        }

                        // 11. Saved Posts Screen
                        if (showSavedScreen) {
                            com.example.ui.screens.SavedPostsScreen(
                                savedPosts = savedPosts,
                                onBackClick = { viewModel.setShowSavedScreen(false) },
                                onReactionSelect = { id, reaction -> viewModel.toggleReaction(id, reaction) },
                                onCommentClick = { id -> viewModel.openCommentsForPost(id) },
                                onShareClick = { id -> viewModel.openShareSheet(id) },
                                onSaveToggle = { id -> viewModel.toggleSavePost(id) },
                                onDeletePost = { id -> viewModel.deletePost(id) },
                                currentUserName = userProfile?.name ?: "Maruf Hossain"
                            )
                        }

                        // 12. Groups / Pages / Memories / Events / Gaming / Feeds List Screen
                        showEntityList?.let { type ->
                            com.example.ui.screens.EntityListScreen(
                                title = type,
                                itemsList = if (type == "Groups") userGroups else userPages,
                                onCreateClick = {
                                    if (type == "Groups") viewModel.setCreateGroupDialogVisible(true)
                                    else viewModel.setCreatePageDialogVisible(true)
                                },
                                onBackClick = { viewModel.setShowEntityList(null) },
                                onItemClick = { item ->
                                    if (type == "Groups") {
                                        Toast.makeText(context, "Welcome to $item", Toast.LENGTH_SHORT).show()
                                        viewModel.setShowEntityList(null)
                                        // Allow posting directly in the selected group
                                        viewModel.setCreatePostDialogVisible(true)
                                    } else {
                                        Toast.makeText(context, "Switched to Page: $item", Toast.LENGTH_SHORT).show()
                                        viewModel.loginUser("$item@page.com", item)
                                        viewModel.setShowEntityList(null)
                                    }
                                }
                            )
                        }

                        // 13. Visited User Profile Screen
                        visitedUser?.let { user ->
                            com.example.ui.screens.VisitedUserProfileScreen(
                                userName = user.first,
                                userAvatarUrl = user.second,
                                allPosts = posts,
                                stories = stories,
                                onStoryClick = { index -> viewModel.openStoryViewer(index) },
                                onBackClick = { viewModel.closeVisitedProfile() },
                                onOpenMessage = { name, avatar ->
                                    viewModel.closeVisitedProfile()
                                    viewModel.openDirectChat(targetName = name, targetAvatar = avatar)
                                },
                                onReactionSelect = { id, reaction -> viewModel.toggleReaction(id, reaction) },
                                onCommentClick = { id -> viewModel.openCommentsForPost(id) },
                                onShareClick = { id -> viewModel.openShareSheet(id) },
                                onSaveToggle = { id -> viewModel.toggleSavePost(id) },
                                onDeletePost = { id -> viewModel.deletePost(id) },
                                currentUserName = userProfile?.name ?: "Maruf Hossain"
                            )
                        }

                        // 14. Watch & Reels History Screen
                        if (showWatchHistoryScreen) {
                            com.example.ui.screens.WatchHistoryScreen(
                                historyList = watchHistory,
                                onBackClick = { viewModel.setShowWatchHistoryScreen(false) },
                                onClearHistory = { viewModel.clearWatchHistory() },
                                onItemClick = { item ->
                                    Toast.makeText(context, "Playing history item: ${item.title}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // 15. Facebook Settings & Privacy Screen
                        if (showSettingsScreen) {
                            com.example.ui.screens.SettingsScreen(
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = { viewModel.toggleDarkMode() },
                                onBackClick = { viewModel.setShowSettingsScreen(false) },
                                onOpenWatchHistory = {
                                    viewModel.setShowSettingsScreen(false)
                                    viewModel.setShowWatchHistoryScreen(true)
                                },
                                onOpenStorageManagement = {
                                    viewModel.setShowSettingsScreen(false)
                                    viewModel.setShowStorageManagementScreen(true)
                                },
                                activeStorageLabel = activeR2StorageConfig?.label
                            )
                        }

                        // 15.1 Storage Management Screen (Cloudflare R2)
                        if (showStorageManagementScreen) {
                            StorageManagementScreen(
                                configs = r2StorageConfigs,
                                activeConfig = activeR2StorageConfig,
                                onSaveConfig = { config -> viewModel.saveR2Config(config) },
                                onSetActive = { id -> viewModel.setActiveR2Config(id) },
                                onDeleteConfig = { id -> viewModel.deleteR2Config(id) },
                                onBackClick = { viewModel.setShowStorageManagementScreen(false) }
                            )
                        }

                        // 16. Dedicated Notifications Screen
                        if (showNotificationsScreen) {
                            com.example.ui.screens.NotificationsScreen(
                                notifications = notifications,
                                onMarkAllReadClick = { viewModel.markNotificationsRead() },
                                onBackClick = { viewModel.setShowNotificationsScreen(false) }
                            )
                        }

                        // 17. Wallet Screen
                        if (showWalletScreen) {
                            com.example.ui.screens.WalletScreen(
                                balance = walletBalance,
                                transactions = walletTransactions,
                                onBackClick = { viewModel.setShowWalletScreen(false) },
                                onDeposit = { amount, method, account -> viewModel.depositMoney(amount, method, account) },
                                onWithdraw = { amount, method, account -> viewModel.withdrawMoney(amount, method, account) }
                            )
                        }

                        // 18. Professional Dashboard / Creator Studio Screen
                        if (showProfessionalDashboard) {
                            com.example.ui.screens.ProfessionalDashboardScreen(
                                userProfile = userProfile,
                                userPosts = posts,
                                walletBalance = walletBalance,
                                onBackClick = { viewModel.setShowProfessionalDashboard(false) },
                                onCreatePostClick = {
                                    viewModel.setShowProfessionalDashboard(false)
                                    viewModel.setCreatePostDialogVisible(true)
                                },
                                onClaimCreatorFund = { amount ->
                                    viewModel.depositMoney(amount, "Creator Fund", "Creator Studio")
                                },
                                onOpenWallet = {
                                    viewModel.setShowProfessionalDashboard(false)
                                    viewModel.setShowWalletScreen(true)
                                },
                                onToggleCreatorMode = { enabled ->
                                    viewModel.toggleCreatorMode(enabled)
                                }
                            )
                        }

                        // 19. Share Bottom Sheet Modal (WhatsApp, Telegram, Facebook, Copy Link, More)
                        sharePostUrl?.let { url ->
                            com.example.ui.components.ShareBottomSheet(
                                postUrl = url,
                                onDismiss = { viewModel.closeShareSheet() }
                            )
                        }
                    }
                }
            }
        }
    }
}
}
