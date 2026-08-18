package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.CommentEntity
import com.example.data.local.entities.MarketplaceItemEntity
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.PostEntity
import com.example.data.local.entities.R2StorageConfigEntity
import com.example.data.local.entities.StoryEntity
import com.example.data.local.entities.UserProfileEntity
import com.example.data.repository.FacebookRepository
import com.example.util.MediaCompressor
import com.example.util.R2StorageManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FacebookViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FacebookRepository(AppDatabase.getDatabase(application))

    private val _isLoggedIn = MutableStateFlow(com.example.data.FirebaseManager.getCurrentUser() != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isLoadingFirebaseData = MutableStateFlow(false)
    val isLoadingFirebaseData = _isLoadingFirebaseData.asStateFlow()

    private val _deletedPostKeys = MutableStateFlow<Set<String>>(emptySet())
    val deletedPostKeys = _deletedPostKeys.asStateFlow()

    private fun getPostSignature(post: PostEntity): String {
        val cleanContent = post.content.trim()
        val cleanImage = post.imageUrl?.trim() ?: ""
        val author = post.authorName.trim()
        return "${author}_${cleanContent}_${cleanImage}"
    }

    val posts: StateFlow<List<PostEntity>> = combine(
        repository.posts,
        com.example.data.FirebaseManager.getPostsFlow(),
        _deletedPostKeys
    ) { localPosts, firebasePosts, deletedKeys ->
        val all = firebasePosts + localPosts
        all.filterNot { post ->
            val sig = getPostSignature(post)
            deletedKeys.contains(post.id.toString()) || (sig.length > 3 && deletedKeys.contains(sig))
        }.distinctBy { post ->
            if (post.id != 0L) post.id.toString() else getPostSignature(post)
        }.sortedByDescending { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val savedPosts: StateFlow<List<PostEntity>> = combine(
        repository.savedPosts,
        _deletedPostKeys
    ) { savedList, deletedKeys ->
        savedList.filterNot { post ->
            val sig = getPostSignature(post)
            deletedKeys.contains(post.id.toString()) || (sig.length > 3 && deletedKeys.contains(sig))
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val stories: StateFlow<List<StoryEntity>> = combine(
        repository.stories,
        com.example.data.FirebaseManager.getStoriesFlow()
    ) { localStories, firebaseStories ->
        val mappedFb = firebaseStories.map { fbStory ->
            StoryEntity(
                id = fbStory.id.hashCode().toLong(),
                authorName = fbStory.authorName.ifBlank { "User" },
                authorAvatarUrl = fbStory.authorAvatarUrl.ifBlank { "drawable/img_user_avatar" },
                storyImageUrl = fbStory.imageBase64OrUrl,
                hasUnseen = true,
                timestamp = fbStory.timestamp
            )
        }
        (mappedFb + localStories).distinctBy { "${it.authorName}_${it.storyImageUrl}" }.sortedByDescending { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.unreadNotificationCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val marketplaceItems: StateFlow<List<MarketplaceItemEntity>> = repository.marketplaceItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val r2StorageConfigs: StateFlow<List<R2StorageConfigEntity>> = combine(
        repository.r2StorageConfigs,
        com.example.data.FirebaseManager.getCloudR2ConfigsFlow()
    ) { localConfigs, cloudConfigs ->
        (cloudConfigs + localConfigs).distinctBy { "${it.bucketName}_${it.accountId}" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeR2StorageConfig: StateFlow<R2StorageConfigEntity?> = combine(
        repository.activeR2StorageConfig,
        com.example.data.FirebaseManager.getCloudR2ConfigsFlow()
    ) { localActive, cloudConfigs ->
        localActive ?: cloudConfigs.firstOrNull { it.isActive } ?: cloudConfigs.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // UI state states
    private val _selectedTab = MutableStateFlow(0) // 0: Home, 1: Watch, 2: Marketplace, 3: Profile, 4: Notifications, 5: Menu
    val selectedTab = _selectedTab.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode = _isDarkMode.asStateFlow()

    private val _appLanguage = MutableStateFlow("English")
    val appLanguage = _appLanguage.asStateFlow()

    fun setAppLanguage(lang: String) {
        _appLanguage.value = lang
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive = _isSearchActive.asStateFlow()

    private val _activeStoryIndex = MutableStateFlow<Int?>(null)
    val activeStoryIndex = _activeStoryIndex.asStateFlow()

    private val _commentPostId = MutableStateFlow<Long?>(null)
    val commentPostId = _commentPostId.asStateFlow()

    private val _showCreatePostDialog = MutableStateFlow(false)
    val showCreatePostDialog = _showCreatePostDialog.asStateFlow()

    private val _postUploadProgress = MutableStateFlow<Float?>(null)
    val postUploadProgress = _postUploadProgress.asStateFlow()

    private val _showCreateMarketplaceDialog = MutableStateFlow(false)
    val showCreateMarketplaceDialog = _showCreateMarketplaceDialog.asStateFlow()

    private val _showCreateGroupDialog = MutableStateFlow(false)
    val showCreateGroupDialog = _showCreateGroupDialog.asStateFlow()

    private val _showCreatePageDialog = MutableStateFlow(false)
    val showCreatePageDialog = _showCreatePageDialog.asStateFlow()

    private val _showEntityList = MutableStateFlow<String?>(null) // "Groups" or "Pages" or "Memories" or "Events" or "Gaming" or "Feeds"
    val showEntityList = _showEntityList.asStateFlow()

    private val _showSavedScreen = MutableStateFlow(false)
    val showSavedScreen = _showSavedScreen.asStateFlow()

    private val _showWatchHistoryScreen = MutableStateFlow(false)
    val showWatchHistoryScreen = _showWatchHistoryScreen.asStateFlow()

    private val _showSettingsScreen = MutableStateFlow(false)
    val showSettingsScreen = _showSettingsScreen.asStateFlow()

    private val _showStorageManagementScreen = MutableStateFlow(false)
    val showStorageManagementScreen = _showStorageManagementScreen.asStateFlow()

    private val _showNotificationsScreen = MutableStateFlow(false)
    val showNotificationsScreen = _showNotificationsScreen.asStateFlow()

    private val _showWalletScreen = MutableStateFlow(false)
    val showWalletScreen = _showWalletScreen.asStateFlow()

    private val _showProfessionalDashboard = MutableStateFlow(false)
    val showProfessionalDashboard = _showProfessionalDashboard.asStateFlow()

    private val _walletBalance = MutableStateFlow(0.00)
    val walletBalance = _walletBalance.asStateFlow()

    data class WalletTransaction(
        val id: String,
        val type: String,
        val amount: Double,
        val method: String,
        val date: String,
        val status: String
    )

    private val _walletTransactions = MutableStateFlow<List<WalletTransaction>>(emptyList())
    val walletTransactions = _walletTransactions.asStateFlow()

    private val _watchHistory = MutableStateFlow<List<com.example.ui.screens.WatchHistoryItem>>(emptyList())
    val watchHistory = _watchHistory.asStateFlow()

    private val _visitedUser = MutableStateFlow<Pair<String, String>?>(null) // Pair(name, avatarUrl)
    val visitedUser = _visitedUser.asStateFlow()

    private val _userGroups = MutableStateFlow<List<String>>(emptyList())
    val userGroups = _userGroups.asStateFlow()

    private val _userPages = MutableStateFlow<List<String>>(emptyList())
    val userPages = _userPages.asStateFlow()

    private val _showCreateStoryScreen = MutableStateFlow(false)
    val showCreateStoryScreen = _showCreateStoryScreen.asStateFlow()

    private val _showEditProfileDialog = MutableStateFlow(false)
    val showEditProfileDialog = _showEditProfileDialog.asStateFlow()

    private val _showMessengerDrawer = MutableStateFlow(false)
    val showMessengerDrawer = _showMessengerDrawer.asStateFlow()

    private val _selectedMarketplaceItem = MutableStateFlow<MarketplaceItemEntity?>(null)
    val selectedMarketplaceItem = _selectedMarketplaceItem.asStateFlow()

    private val _sharePostUrl = MutableStateFlow<String?>(null)
    val sharePostUrl = _sharePostUrl.asStateFlow()

    private val _activeCategory = MutableStateFlow("All")
    val activeCategory = _activeCategory.asStateFlow()

    private val _activeDirectChatRecipient = MutableStateFlow<Triple<String, String, String>?>(null)
    val activeDirectChatRecipient = _activeDirectChatRecipient.asStateFlow()

    fun openDirectChat(targetName: String, targetAvatar: String, targetUid: String = "") {
        clearOverlayScreens()
        _activeDirectChatRecipient.value = Triple(targetUid, targetName, targetAvatar)
        _showMessengerDrawer.value = true
    }

    fun closeDirectChat() {
        _activeDirectChatRecipient.value = null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeComments: StateFlow<List<CommentEntity>> = _commentPostId
        .flatMapLatest { id ->
            if (id != null) {
                combine(
                    repository.getCommentsForPost(id),
                    com.example.data.FirebaseManager.getCommentsFlow(id)
                ) { localComments, firebaseComments ->
                    (firebaseComments + localComments).distinctBy { "${it.authorName}_${it.content}_${it.timestamp}" }.sortedBy { it.timestamp }
                }
            } else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Filtered posts for search
    val filteredPosts: StateFlow<List<PostEntity>> = posts.flatMapLatest { postList ->
        _searchQuery.map { query ->
            if (query.isBlank()) postList
            else postList.filter {
                it.content.contains(query, ignoreCase = true) ||
                        it.authorName.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered marketplace items
    val filteredMarketplaceItems: StateFlow<List<MarketplaceItemEntity>> = marketplaceItems.flatMapLatest { items ->
        _activeCategory.map { cat ->
            if (cat == "All") items else items.filter { it.category == cat }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
        viewModelScope.launch {
            com.example.data.FirebaseManager.getCurrentUserProfileFlow().collect { profile ->
                if (profile != null) {
                    val name = profile.name.ifBlank { "${profile.firstName} ${profile.lastName}".trim() }.ifBlank { "User" }
                    val bio = profile.bio.ifBlank { "Welcome to Frndom!" }
                    val avatar = profile.avatarUrl.ifBlank { "drawable/img_user_avatar" }
                    val cover = profile.coverUrl.ifBlank { "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?auto=format&fit=crop&w=1200&q=80" }
                    val livesIn = profile.livesIn.ifBlank { "Dhaka, Bangladesh" }
                    val work = profile.work.ifBlank { "Member" }

                    repository.updateFullProfile(
                        name = name,
                        bio = bio,
                        avatarUrl = avatar,
                        coverUrl = cover,
                        livesIn = livesIn,
                        work = work
                    )
                }
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            try {
                _isLoadingFirebaseData.value = true
                kotlinx.coroutines.delay(800)
                _isLoadingFirebaseData.value = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearOverlayScreens() {
        _showMessengerDrawer.value = false
        _showSavedScreen.value = false
        _showEntityList.value = null
        _visitedUser.value = null
        _showWatchHistoryScreen.value = false
        _showSettingsScreen.value = false
        _showStorageManagementScreen.value = false
        _showNotificationsScreen.value = false
        _showWalletScreen.value = false
        _showProfessionalDashboard.value = false
        _isSearchActive.value = false
        _selectedMarketplaceItem.value = null
        _sharePostUrl.value = null
    }

    fun openShareSheet(postId: Long) {
        _sharePostUrl.value = "https://bikalaafa.app/posts/$postId"
        viewModelScope.launch {
            try {
                com.example.data.FirebaseManager.incrementShareCount(postId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun openShareSheetWithUrl(url: String) {
        _sharePostUrl.value = url.ifBlank { "https://bikalaafa.app/share" }
    }

    fun closeShareSheet() {
        _sharePostUrl.value = null
    }

    fun setSelectedTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
        clearOverlayScreens()
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchActive(active: Boolean) {
        if (active) clearOverlayScreens()
        _isSearchActive.value = active
        if (!active) _searchQuery.value = ""
    }

    fun openStoryViewer(index: Int) {
        _activeStoryIndex.value = index
    }

    fun closeStoryViewer() {
        _activeStoryIndex.value = null
    }

    fun addStory(content: String, imageUrl: String, userName: String, userAvatarUrl: String) {
        viewModelScope.launch {
            var finalStoryUrl = imageUrl
            if (imageUrl.isNotBlank()) {
                val isVideo = imageUrl.startsWith("[VIDEO]") || imageUrl.contains(".mp4") || imageUrl.contains("video")
                val cleanPath = imageUrl.replace("[VIDEO]", "").trim()
                try {
                    val compressionResult = if (isVideo) {
                        MediaCompressor.optimizeVideo(getApplication(), cleanPath)
                    } else {
                        MediaCompressor.compressImage(getApplication(), cleanPath, targetMinBytes = 100 * 1024, targetMaxBytes = 600 * 1024)
                    }
                    val activeR2 = repository.getActiveR2StorageConfig()
                    val uploadedUrl = if (activeR2 != null) {
                        val r2Res = R2StorageManager.uploadFile(activeR2, compressionResult.file, compressionResult.mimeType)
                        if (r2Res.isSuccess) {
                            r2Res.getOrNull()
                        } else {
                            com.example.data.FirebaseManager.uploadMedia(compressionResult.file.absolutePath)
                        }
                    } else {
                        com.example.data.FirebaseManager.uploadMedia(compressionResult.file.absolutePath)
                    }
                    finalStoryUrl = uploadedUrl ?: imageUrl
                } catch (e: Exception) {
                    finalStoryUrl = imageUrl
                }
            }

            // Sync to Firebase Firestore for all users
            com.example.data.FirebaseManager.addStory(
                authorName = userName,
                authorAvatarUrl = userAvatarUrl,
                imageBase64OrUrl = finalStoryUrl
            )

            // Save to local DB
            val newStory = StoryEntity(
                authorName = userName,
                authorAvatarUrl = userAvatarUrl,
                storyImageUrl = finalStoryUrl,
                hasUnseen = true,
                timestamp = System.currentTimeMillis()
            )
            repository.insertStory(newStory)
        }
    }

    fun openCommentsForPost(postId: Long) {
        _commentPostId.value = postId
    }

    fun closeComments() {
        _commentPostId.value = null
    }

    fun setCreatePostDialogVisible(visible: Boolean) {
        _showCreatePostDialog.value = visible
    }

    fun setCreateMarketplaceDialogVisible(visible: Boolean) {
        _showCreateMarketplaceDialog.value = visible
    }

    fun setCreateGroupDialogVisible(visible: Boolean) {
        _showCreateGroupDialog.value = visible
    }

    fun setCreatePageDialogVisible(visible: Boolean) {
        _showCreatePageDialog.value = visible
    }

    fun setShowEntityList(type: String?) {
        if (type != null) clearOverlayScreens()
        _showEntityList.value = type
    }

    fun setShowSavedScreen(visible: Boolean) {
        if (visible) clearOverlayScreens()
        _showSavedScreen.value = visible
    }

    fun setShowWatchHistoryScreen(visible: Boolean) {
        if (visible) clearOverlayScreens()
        _showWatchHistoryScreen.value = visible
    }

    fun setShowSettingsScreen(visible: Boolean) {
        if (visible) clearOverlayScreens()
        _showSettingsScreen.value = visible
    }

    fun setShowStorageManagementScreen(visible: Boolean) {
        if (visible) clearOverlayScreens()
        _showStorageManagementScreen.value = visible
    }

    fun saveR2Config(config: R2StorageConfigEntity) {
        viewModelScope.launch {
            repository.insertOrUpdateR2Config(config)
            com.example.data.FirebaseManager.saveCloudR2Config(config)
        }
    }

    fun setActiveR2Config(id: Long) {
        viewModelScope.launch {
            repository.setActiveR2Config(id)
            val allConfigs = r2StorageConfigs.value
            for (c in allConfigs) {
                val updated = c.copy(isActive = c.id == id)
                com.example.data.FirebaseManager.saveCloudR2Config(updated)
            }
        }
    }

    fun deleteR2Config(id: Long) {
        viewModelScope.launch {
            repository.deleteR2Config(id)
            com.example.data.FirebaseManager.deleteCloudR2Config(id)
        }
    }

    fun setShowNotificationsScreen(visible: Boolean) {
        if (visible) clearOverlayScreens()
        _showNotificationsScreen.value = visible
        if (visible) {
            markNotificationsRead()
        }
    }

    fun setShowWalletScreen(visible: Boolean) {
        if (visible) clearOverlayScreens()
        _showWalletScreen.value = visible
    }

    fun setShowProfessionalDashboard(visible: Boolean) {
        if (visible) clearOverlayScreens()
        _showProfessionalDashboard.value = visible
    }

    fun depositMoney(amount: Double, method: String, accountNo: String) {
        _walletBalance.value += amount
        val newTrx = WalletTransaction(
            id = "TRX-${(100000..999999).random()}",
            type = "Deposit",
            amount = amount,
            method = method,
            date = "Just now",
            status = "Completed"
        )
        _walletTransactions.value = listOf(newTrx) + _walletTransactions.value
    }

    fun withdrawMoney(amount: Double, method: String, accountNo: String): Boolean {
        if (_walletBalance.value >= amount) {
            _walletBalance.value -= amount
            val newTrx = WalletTransaction(
                id = "TRX-${(100000..999999).random()}",
                type = "Withdraw",
                amount = amount,
                method = method,
                date = "Just now",
                status = "Pending"
            )
            _walletTransactions.value = listOf(newTrx) + _walletTransactions.value
            return true
        }
        return false
    }

    fun addToWatchHistory(item: com.example.ui.screens.WatchHistoryItem) {
        // Prevent duplicate items at top
        val filtered = _watchHistory.value.filter { it.title != item.title }
        _watchHistory.value = listOf(item) + filtered
    }

    fun clearWatchHistory() {
        _watchHistory.value = emptyList()
    }

    fun visitProfile(name: String, avatarUrl: String) {
        clearOverlayScreens()
        val currentName = userProfile.value?.name?.trim() ?: ""
        val isSelf = name.trim().equals(currentName, ignoreCase = true) ||
                name.trim().equals("Me", ignoreCase = true) ||
                name.trim().equals("You", ignoreCase = true)
        if (isSelf) {
            _selectedTab.value = 3
        } else {
            _visitedUser.value = Pair(name, avatarUrl)
        }
    }

    fun closeVisitedProfile() {
        _visitedUser.value = null
    }

    fun setCreateStoryScreenVisible(visible: Boolean) {
        _showCreateStoryScreen.value = visible
    }

    fun setEditProfileDialogVisible(visible: Boolean) {
        _showEditProfileDialog.value = visible
    }

    fun setMessengerDrawerVisible(visible: Boolean) {
        if (visible) clearOverlayScreens()
        _showMessengerDrawer.value = visible
    }

    fun selectMarketplaceItem(item: MarketplaceItemEntity?) {
        _selectedMarketplaceItem.value = item
    }

    fun setActiveCategory(category: String) {
        _activeCategory.value = category
    }

    fun loginUser(profile: com.example.data.UserProfile) {
        viewModelScope.launch {
            val name = profile.name.ifBlank { "${profile.firstName} ${profile.lastName}".trim() }.ifBlank { "User" }
            val bio = profile.bio.ifBlank { "Welcome to Frndom!" }
            val avatar = profile.avatarUrl.ifBlank { "drawable/img_user_avatar" }
            val cover = profile.coverUrl.ifBlank { "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?auto=format&fit=crop&w=1200&q=80" }
            val livesIn = profile.livesIn.ifBlank { "Dhaka, Bangladesh" }
            val work = profile.work.ifBlank { "Member" }

            repository.updateFullProfile(
                name = name,
                bio = bio,
                avatarUrl = avatar,
                coverUrl = cover,
                livesIn = livesIn,
                work = work
            )
            _isLoggedIn.value = true
        }
    }

    fun loginUser(email: String, name: String) {
        loginUser(
            com.example.data.UserProfile(
                email = email,
                name = name
            )
        )
    }

    fun logoutUser() {
        com.example.data.FirebaseManager.signOut()
        _isLoggedIn.value = false
    }

    fun createPost(content: String, imageUrl: String? = null, bgStyle: String? = null, privacy: String = "PUBLIC") {
        viewModelScope.launch {
            _showCreatePostDialog.value = false
            _postUploadProgress.value = 0f
            
            var progress = 0f
            val progressJob = launch {
                while (progress < 0.9f) {
                    kotlinx.coroutines.delay(200)
                    progress += 0.05f
                    if (progress > 0.9f) progress = 0.9f
                    _postUploadProgress.value = progress
                }
            }

            val name = userProfile.value?.name ?: "User"
            val avatar = userProfile.value?.avatarUrl ?: "drawable/img_user_avatar"

            var finalMediaUrl: String? = null
            var mediaType = "text"

            if (!imageUrl.isNullOrBlank()) {
                val isVideo = imageUrl.startsWith("[VIDEO]") || imageUrl.contains(".mp4") || imageUrl.contains("video")
                val cleanPath = imageUrl.replace("[VIDEO]", "").trim()
                mediaType = if (isVideo) "video" else "image"

                try {
                    // 1. Perform Client-Side Compression (100 - 600 KB for images, optimization for video)
                    val compressionResult = if (isVideo) {
                        MediaCompressor.optimizeVideo(getApplication(), cleanPath)
                    } else {
                        MediaCompressor.compressImage(getApplication(), cleanPath, targetMinBytes = 100 * 1024, targetMaxBytes = 600 * 1024)
                    }

                    // 2. Upload to Active Cloudflare R2 Bucket if configured
                    val activeR2 = repository.getActiveR2StorageConfig()
                    val uploadedUrl = if (activeR2 != null) {
                        val r2Res = R2StorageManager.uploadFile(activeR2, compressionResult.file, compressionResult.mimeType)
                        if (r2Res.isSuccess) {
                            r2Res.getOrNull()
                        } else {
                            // Fallback to Firebase Storage if R2 failed
                            com.example.data.FirebaseManager.uploadMedia(compressionResult.file.absolutePath)
                        }
                    } else {
                        com.example.data.FirebaseManager.uploadMedia(compressionResult.file.absolutePath)
                    }

                    finalMediaUrl = if (isVideo && uploadedUrl != null) {
                        "[VIDEO]$uploadedUrl"
                    } else {
                        uploadedUrl ?: imageUrl
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    finalMediaUrl = imageUrl
                }
            }

            // 3. Save post to Firestore with public URL, mediaType, and background style
            com.example.data.FirebaseManager.addPost(
                authorName = name,
                authorAvatarUrl = avatar,
                content = content,
                imageBase64OrUrl = finalMediaUrl,
                mediaType = mediaType,
                bgStyle = bgStyle,
                privacy = privacy
            )

            // 4. Save to local Room repository for instant UI feed display
            repository.createPost(
                content = content,
                imageUrl = finalMediaUrl,
                bgStyle = bgStyle,
                privacy = privacy,
                authorName = name,
                authorAvatarUrl = avatar
            )
            
            progressJob.cancel()
            _postUploadProgress.value = 1f
            kotlinx.coroutines.delay(800)
            _postUploadProgress.value = null
        }
    }

    fun updatePostPrivacy(postId: Long, privacy: String) {
        viewModelScope.launch {
            repository.updatePostPrivacy(postId, privacy)
        }
    }

    fun createGroup(name: String) {
        viewModelScope.launch {
            _userGroups.value = _userGroups.value + name
            com.example.data.FirebaseManager.createGroup(
                name = name,
                description = "Group created on Beka Lafa",
                category = "General",
                coverUrl = ""
            )
        }
    }

    fun createPage(name: String) {
        viewModelScope.launch {
            _userPages.value = _userPages.value + name
            com.example.data.FirebaseManager.createPage(
                name = name,
                category = "Community",
                description = "Page created on Beka Lafa",
                avatarUrl = ""
            )
        }
    }

    fun toggleReaction(postId: Long, reaction: String, post: PostEntity? = null) {
        val targetPost = post ?: posts.value.find { it.id == postId } ?: savedPosts.value.find { it.id == postId }
        viewModelScope.launch {
            val finalPost = if (targetPost != null && targetPost.id == 0L) {
                val newId = (targetPost.content + targetPost.timestamp).hashCode().toLong().let { if (it == 0L) System.currentTimeMillis() else kotlin.math.abs(it) }
                targetPost.copy(id = newId)
            } else {
                targetPost
            }
            val finalPostId = finalPost?.id ?: postId
            repository.toggleReaction(finalPostId, reaction, finalPost)
        }
    }

    fun toggleSavePost(postId: Long, post: PostEntity? = null) {
        val targetPost = post ?: posts.value.find { it.id == postId } ?: savedPosts.value.find { it.id == postId }
        viewModelScope.launch {
            repository.toggleSavePost(postId, targetPost)
        }
    }

    fun deletePost(postId: Long, post: PostEntity? = null) {
        val targetPost = post ?: posts.value.find { it.id == postId } ?: savedPosts.value.find { it.id == postId }
        viewModelScope.launch {
            val currentName = userProfile.value?.name ?: "Maruf Hossain"
            val author = targetPost?.authorName ?: ""
            val isOwnPost = targetPost == null ||
                    author.equals(currentName, ignoreCase = true) ||
                    author.equals("Maruf Hossain", ignoreCase = true) ||
                    author.equals("Manik Hossain", ignoreCase = true) ||
                    author.equals("Me", ignoreCase = true) ||
                    author.equals("You", ignoreCase = true) ||
                    (currentName.isNotBlank() && author.contains(currentName, ignoreCase = true))

            if (isOwnPost) {
                val keySet = mutableSetOf<String>()
                keySet.add(postId.toString())
                if (targetPost != null) {
                    val sig = getPostSignature(targetPost)
                    if (sig.length > 3) keySet.add(sig)
                    if (targetPost.id != 0L) keySet.add(targetPost.id.toString())
                }
                _deletedPostKeys.value = _deletedPostKeys.value + keySet
                repository.deletePost(postId)
            }
        }
    }

    fun deleteStory(storyId: Long) {
        viewModelScope.launch {
            val targetStory = stories.value.find { it.id == storyId }
            val currentName = userProfile.value?.name ?: "Maruf Hossain"
            val author = targetStory?.authorName ?: ""
            val isOwnStory = targetStory == null ||
                    author.equals(currentName, ignoreCase = true) ||
                    author.equals("Maruf Hossain", ignoreCase = true) ||
                    author.equals("Manik Hossain", ignoreCase = true) ||
                    author.equals("Your Story", ignoreCase = true) ||
                    author.equals("Me", ignoreCase = true) ||
                    author.equals("You", ignoreCase = true) ||
                    (currentName.isNotBlank() && author.contains(currentName, ignoreCase = true))

            if (isOwnStory) {
                repository.deleteStory(storyId)
            }
        }
    }

    fun addComment(content: String) {
        val postId = _commentPostId.value ?: return
        if (content.isBlank()) return
        val targetPost = posts.value.find { it.id == postId } ?: savedPosts.value.find { it.id == postId }
        viewModelScope.launch {
            val name = userProfile.value?.name ?: "Maruf Hossain"
            val avatar = userProfile.value?.avatarUrl ?: "drawable/img_user_avatar"
            repository.addComment(postId = postId, content = content, authorName = name, authorAvatarUrl = avatar, currentPost = targetPost)
        }
    }

    fun sendFriendRequestNotification(targetName: String, avatarUrl: String) {
        viewModelScope.launch {
            repository.sendFriendRequestNotification(targetName, avatarUrl)
        }
    }

    fun createStory(imageUrl: String) {
        viewModelScope.launch {
            val name = userProfile.value?.name ?: "User"
            val avatar = userProfile.value?.avatarUrl ?: ""
            val uploadedMediaUrl = com.example.data.FirebaseManager.uploadMedia(imageUrl) ?: imageUrl
            com.example.data.FirebaseManager.addStory(name, avatar, uploadedMediaUrl)
            repository.createStory(imageUrl = uploadedMediaUrl, authorName = name, authorAvatarUrl = avatar)
        }
    }

    fun createMarketplaceItem(
        title: String,
        price: String,
        category: String,
        location: String,
        description: String,
        imageUrl: String
    ) {
        viewModelScope.launch {
            repository.createMarketplaceItem(title, price, category, location, description, imageUrl)
            _showCreateMarketplaceDialog.value = false
        }
    }

    fun markNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }

    fun updateProfile(name: String, bio: String, livesIn: String, work: String) {
        viewModelScope.launch {
            com.example.data.FirebaseManager.updateProfile(name, bio, livesIn, work)
            repository.updateProfile(name, bio, livesIn, work)
            _showEditProfileDialog.value = false
        }
    }

    fun toggleCreatorMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleCreatorMode(enabled)
        }
    }

    fun updatePrivacyStatus(privacy: String) {
        viewModelScope.launch {
            repository.updatePrivacyStatus(privacy)
        }
    }

    fun updateUserAvatar(avatarUrl: String) {
        viewModelScope.launch {
            val uploadedUrl = try {
                val cleanPath = avatarUrl.trim()
                val compressed = MediaCompressor.compressImage(getApplication(), cleanPath, targetMinBytes = 100 * 1024, targetMaxBytes = 400 * 1024)
                val activeR2 = repository.getActiveR2StorageConfig()
                if (activeR2 != null) {
                    val r2Res = R2StorageManager.uploadFile(activeR2, compressed.file, compressed.mimeType)
                    if (r2Res.isSuccess) r2Res.getOrNull() else com.example.data.FirebaseManager.uploadMedia(compressed.file.absolutePath)
                } else {
                    com.example.data.FirebaseManager.uploadMedia(compressed.file.absolutePath)
                }
            } catch (e: Exception) {
                com.example.data.FirebaseManager.uploadMedia(avatarUrl) ?: avatarUrl
            } ?: avatarUrl

            com.example.data.FirebaseManager.updateAvatar(uploadedUrl)
            repository.updateAvatar(uploadedUrl)
        }
    }

    fun updateUserCover(coverUrl: String) {
        viewModelScope.launch {
            val uploadedUrl = try {
                val cleanPath = coverUrl.trim()
                val compressed = MediaCompressor.compressImage(getApplication(), cleanPath, targetMinBytes = 100 * 1024, targetMaxBytes = 600 * 1024)
                val activeR2 = repository.getActiveR2StorageConfig()
                if (activeR2 != null) {
                    val r2Res = R2StorageManager.uploadFile(activeR2, compressed.file, compressed.mimeType)
                    if (r2Res.isSuccess) r2Res.getOrNull() else com.example.data.FirebaseManager.uploadMedia(compressed.file.absolutePath)
                } else {
                    com.example.data.FirebaseManager.uploadMedia(compressed.file.absolutePath)
                }
            } catch (e: Exception) {
                com.example.data.FirebaseManager.uploadMedia(coverUrl) ?: coverUrl
            } ?: coverUrl

            com.example.data.FirebaseManager.updateCover(uploadedUrl)
            repository.updateCover(uploadedUrl)
        }
    }
}
