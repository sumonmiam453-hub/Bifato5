package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entities.CommentEntity
import com.example.data.local.entities.MarketplaceItemEntity
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.PostEntity
import com.example.data.local.entities.R2StorageConfigEntity
import com.example.data.local.entities.StoryEntity
import com.example.data.local.entities.UserProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FacebookRepository(private val db: AppDatabase) {

    val posts: Flow<List<PostEntity>> = db.postDao().getAllPosts()
    val savedPosts: Flow<List<PostEntity>> = db.postDao().getSavedPosts()
    val stories: Flow<List<StoryEntity>> = db.storyDao().getAllStories()
    val notifications: Flow<List<NotificationEntity>> = db.notificationDao().getAllNotifications()
    val unreadNotificationCount: Flow<Int> = db.notificationDao().getUnreadCount()
    val userProfile: Flow<UserProfileEntity?> = db.userProfileDao().getUserProfile()
    val marketplaceItems: Flow<List<MarketplaceItemEntity>> = db.marketplaceDao().getAllItems()
    val r2StorageConfigs: Flow<List<R2StorageConfigEntity>> = db.r2StorageDao().getAllConfigs()
    val activeR2StorageConfig: Flow<R2StorageConfigEntity?> = db.r2StorageDao().getActiveConfig()

    fun getCommentsForPost(postId: Long): Flow<List<CommentEntity>> {
        return db.commentDao().getCommentsForPost(postId)
    }

    suspend fun createPost(
        content: String,
        imageUrl: String? = null,
        bgStyle: String? = null,
        privacy: String = "PUBLIC",
        authorName: String = "Maruf Hossain",
        authorAvatarUrl: String = "drawable/img_user_avatar"
    ) {
        withContext(Dispatchers.IO) {
            val post = PostEntity(
                authorName = authorName,
                authorAvatarUrl = authorAvatarUrl,
                timeAgo = "Just now",
                content = content,
                imageUrl = imageUrl,
                bgStyle = bgStyle,
                privacy = privacy,
                likesCount = 0,
                commentsCount = 0,
                sharesCount = 0,
                timestamp = System.currentTimeMillis()
            )
            db.postDao().insertPost(post)

            val notifDesc = if (imageUrl != null) "Your photo/video post was published successfully! 🎉" else if (bgStyle != null) "Your colorful text post was published successfully! 🎉" else "Your post was published successfully! 🎉"
            val notif = NotificationEntity(
                title = "Post Complete",
                description = notifDesc,
                timeAgo = "Just now",
                avatarUrl = authorAvatarUrl,
                type = "POST_COMPLETED",
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            db.notificationDao().insertNotification(notif)
        }
    }

    suspend fun updatePostPrivacy(postId: Long, privacy: String) {
        withContext(Dispatchers.IO) {
            db.postDao().updatePostPrivacy(postId, privacy)
        }
    }

    suspend fun insertStory(story: StoryEntity) {
        withContext(Dispatchers.IO) {
            db.storyDao().insertStory(story)
        }
    }

    suspend fun toggleReaction(postId: Long, reaction: String, currentPost: PostEntity? = null) {
        withContext(Dispatchers.IO) {
            val post = db.postDao().getPostById(postId) ?: currentPost
            val isCurrentlyLiked = post?.let { it.isLiked || (it.userReaction != "NONE" && it.userReaction.isNotBlank()) } ?: false
            val isTogglingOff = (reaction == "LIKE" && isCurrentlyLiked) || (post?.userReaction == reaction)

            val newReaction = if (isTogglingOff) "NONE" else reaction
            val isLikedNow = newReaction != "NONE"

            val likesDelta = when {
                !isCurrentlyLiked && isLikedNow -> 1
                isCurrentlyLiked && !isLikedNow -> -1
                else -> 0
            }

            if (post != null) {
                val updatedPost = post.copy(
                    userReaction = newReaction,
                    isLiked = isLikedNow,
                    likesCount = (post.likesCount + likesDelta).coerceAtLeast(0)
                )
                db.postDao().insertPost(updatedPost)
                try {
                    com.example.data.FirebaseManager.toggleLikeOnPost(postId, reaction, updatedPost)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                try {
                    com.example.data.FirebaseManager.toggleLikeOnPost(postId, reaction, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    suspend fun toggleSavePost(postId: Long, currentPost: PostEntity? = null) {
        withContext(Dispatchers.IO) {
            val post = db.postDao().getPostById(postId) ?: currentPost ?: return@withContext
            db.postDao().insertPost(post.copy(isSaved = !post.isSaved))
        }
    }

    suspend fun deletePost(postId: Long) {
        withContext(Dispatchers.IO) {
            db.postDao().deletePost(postId)
            try {
                com.example.data.FirebaseManager.deletePost(postId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteStory(storyId: Long) {
        withContext(Dispatchers.IO) {
            db.storyDao().deleteStory(storyId)
            try {
                com.example.data.FirebaseManager.deleteStory(storyId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun addComment(postId: Long, content: String, authorName: String = "Maruf Hossain", authorAvatarUrl: String = "drawable/img_user_avatar", currentPost: PostEntity? = null) {
        withContext(Dispatchers.IO) {
            val comment = CommentEntity(
                postId = postId,
                authorName = authorName,
                authorAvatarUrl = authorAvatarUrl,
                content = content,
                timeAgo = "Just now",
                likesCount = 0
            )
            db.commentDao().insertComment(comment)

            val post = db.postDao().getPostById(postId) ?: currentPost
            if (post != null) {
                db.postDao().insertPost(post.copy(commentsCount = post.commentsCount + 1))
                
                val notif = NotificationEntity(
                    title = authorName,
                    description = "commented on your post: \"$content\"",
                    timeAgo = "Just now",
                    avatarUrl = authorAvatarUrl,
                    type = "COMMENT",
                    isRead = false,
                    timestamp = System.currentTimeMillis()
                )
                db.notificationDao().insertNotification(notif)
            }

            // Sync to Firestore in real-time
            try {
                com.example.data.FirebaseManager.addCommentToPost(
                    postId = postId,
                    content = content,
                    authorName = authorName,
                    authorAvatarUrl = authorAvatarUrl
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun sendFriendRequestNotification(targetName: String, avatarUrl: String) {
        withContext(Dispatchers.IO) {
            val notif = NotificationEntity(
                title = targetName,
                description = "sent you a friend request.",
                timeAgo = "Just now",
                avatarUrl = avatarUrl,
                type = "FRIEND_REQUEST",
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            db.notificationDao().insertNotification(notif)
        }
    }

    suspend fun createStory(imageUrl: String, authorName: String = "Your Story", authorAvatarUrl: String = "") {
        withContext(Dispatchers.IO) {
            val story = StoryEntity(
                authorName = authorName,
                authorAvatarUrl = authorAvatarUrl,
                storyImageUrl = imageUrl,
                hasUnseen = true,
                timestamp = System.currentTimeMillis()
            )
            db.storyDao().insertStory(story)
        }
    }

    suspend fun createMarketplaceItem(
        title: String,
        price: String,
        category: String,
        location: String,
        description: String,
        imageUrl: String
    ) {
        withContext(Dispatchers.IO) {
            val item = MarketplaceItemEntity(
                title = title,
                price = price,
                category = category,
                location = location,
                description = description,
                imageUrl = imageUrl
            )
            db.marketplaceDao().insertItem(item)
        }
    }

    suspend fun markAllNotificationsRead() {
        withContext(Dispatchers.IO) {
            db.notificationDao().markAllAsRead()
        }
    }

    suspend fun updateFullProfile(
        name: String,
        bio: String,
        avatarUrl: String,
        coverUrl: String,
        livesIn: String,
        work: String
    ) {
        withContext(Dispatchers.IO) {
            val current = db.userProfileDao().getUserProfileSync()
            val profile = UserProfileEntity(
                id = 1,
                name = name,
                bio = bio,
                livesIn = livesIn,
                work = work,
                education = current?.education ?: "Computer Science & Engineering",
                followerCount = current?.followerCount ?: 0,
                coverPhotoUrl = coverUrl,
                avatarUrl = avatarUrl,
                isCreatorMode = current?.isCreatorMode ?: false
            )
            db.userProfileDao().insertOrUpdateProfile(profile)
        }
    }

    suspend fun updateProfile(name: String, bio: String, livesIn: String, work: String) {
        withContext(Dispatchers.IO) {
            val current = db.userProfileDao().getUserProfileSync()
            val profile = UserProfileEntity(
                id = 1,
                name = name,
                bio = bio,
                livesIn = livesIn,
                work = work,
                education = current?.education ?: "Computer Science & Engineering",
                followerCount = current?.followerCount ?: 0,
                coverPhotoUrl = current?.coverPhotoUrl ?: "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?auto=format&fit=crop&w=1200&q=80",
                avatarUrl = current?.avatarUrl ?: "drawable/img_user_avatar",
                isCreatorMode = current?.isCreatorMode ?: false,
                privacyStatus = current?.privacyStatus ?: "PUBLIC"
            )
            db.userProfileDao().insertOrUpdateProfile(profile)
        }
    }

    suspend fun updatePrivacyStatus(privacy: String) {
        withContext(Dispatchers.IO) {
            val current = db.userProfileDao().getUserProfileSync()
            val profile = (current ?: UserProfileEntity()).copy(privacyStatus = privacy)
            db.userProfileDao().insertOrUpdateProfile(profile)
        }
    }

    suspend fun toggleCreatorMode(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            val current = db.userProfileDao().getUserProfileSync()
            val profile = (current ?: UserProfileEntity()).copy(isCreatorMode = enabled)
            db.userProfileDao().insertOrUpdateProfile(profile)
        }
    }

    suspend fun updateAvatar(avatarUrl: String) {
        withContext(Dispatchers.IO) {
            val current = db.userProfileDao().getUserProfileSync()
            val name = current?.name ?: "Maruf Hossain"
            val profile = current?.copy(avatarUrl = avatarUrl) ?: UserProfileEntity(
                id = 1,
                name = name,
                bio = "Welcome to Bika Lafa!",
                work = "Member",
                education = "General",
                livesIn = "Dhaka, Bangladesh",
                avatarUrl = avatarUrl
            )
            db.userProfileDao().insertOrUpdateProfile(profile)
            try {
                db.postDao().updateAuthorAvatar(name, avatarUrl)
                db.commentDao().updateAuthorAvatar(name, avatarUrl)
                db.storyDao().updateAuthorAvatar(name, avatarUrl)
                if (name != "Maruf Hossain") {
                    db.postDao().updateAuthorAvatar("Maruf Hossain", avatarUrl)
                    db.commentDao().updateAuthorAvatar("Maruf Hossain", avatarUrl)
                    db.storyDao().updateAuthorAvatar("Maruf Hossain", avatarUrl)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun updateCover(coverUrl: String) {
        withContext(Dispatchers.IO) {
            val current = db.userProfileDao().getUserProfileSync()
            val profile = current?.copy(coverPhotoUrl = coverUrl) ?: UserProfileEntity(
                id = 1,
                name = "Manik Hossain",
                bio = "Mobile Developer & Tech Enthusiast 🚀",
                work = "Android Developer",
                education = "CSE",
                livesIn = "Dhaka, Bangladesh",
                coverPhotoUrl = coverUrl
            )
            db.userProfileDao().insertOrUpdateProfile(profile)
        }
    }

    suspend fun getActiveR2StorageConfig(): R2StorageConfigEntity? {
        return withContext(Dispatchers.IO) {
            db.r2StorageDao().getActiveConfigSync()
        }
    }

    suspend fun insertOrUpdateR2Config(config: R2StorageConfigEntity): Long {
        return withContext(Dispatchers.IO) {
            val count = db.r2StorageDao().getConfigCount()
            val finalConfig = if (count == 0) config.copy(isActive = true) else config
            val id = db.r2StorageDao().insertConfig(finalConfig)
            if (finalConfig.isActive) {
                db.r2StorageDao().setActiveAccount(if (config.id != 0L) config.id else id)
            }
            id
        }
    }

    suspend fun setActiveR2Config(id: Long) {
        withContext(Dispatchers.IO) {
            db.r2StorageDao().setActiveAccount(id)
        }
    }

    suspend fun deleteR2Config(id: Long) {
        withContext(Dispatchers.IO) {
            db.r2StorageDao().deleteConfig(id)
        }
    }

    suspend fun seedInitialDataIfEmpty() {
        // App is initialized completely clean without default/mock data
    }
}
