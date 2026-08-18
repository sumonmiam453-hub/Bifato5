package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.data.local.entities.CommentEntity
import com.example.data.local.entities.PostEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.UUID

data class UserProfile(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val name: String = "",
    val emailOrPhone: String = "",
    val email: String = "",
    val dob: String = "",
    val gender: String = "Male",
    val avatarUrl: String = "",
    val bio: String = "Welcome to my profile!",
    val coverUrl: String = "",
    val livesIn: String = "",
    val work: String = ""
)

data class FirebaseFriendRequest(
    val id: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val senderAvatar: String = "",
    val receiverUid: String = "",
    val status: String = "PENDING", // PENDING, ACCEPTED
    val timestamp: Long = System.currentTimeMillis()
)

data class FirebaseStory(
    val id: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val imageBase64OrUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class FirebaseGroup(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "General",
    val coverUrl: String = "",
    val memberCount: Int = 1,
    val members: List<String> = emptyList(),
    val adminUid: String = ""
)

data class FirebasePage(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val description: String = "",
    val avatarUrl: String = "",
    val followerCount: Int = 1,
    val followers: List<String> = emptyList(),
    val ownerUid: String = ""
)

data class FirebaseComment(
    val id: String = "",
    val postId: Long = 0L,
    val authorUid: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class FirebaseConversation(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantAvatars: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastSenderUid: String = "",
    val lastTimestamp: Long = System.currentTimeMillis()
)

data class FirebaseChatMessage(
    val id: String = "",
    val chatId: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val senderAvatar: String = "",
    val recipientUid: String = "",
    val recipientName: String = "",
    val recipientAvatar: String = "",
    val messageText: String = "",
    val imageBase64: String = "",
    val videoBase64: String = "",
    val audioBase64: String = "",
    val audioDurationSeconds: Int = 0,
    val messageType: String = "text",
    val timestamp: Long = System.currentTimeMillis()
)

object FirebaseManager {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    private val userAvatarsCache = java.util.concurrent.ConcurrentHashMap<String, String>()

    init {
        try {
            firestore.collection("users").addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    for (doc in snapshot.documents) {
                        val uid = doc.id
                        val avatar = doc.getString("avatarUrl") ?: ""
                        val name = doc.getString("name") ?: ""
                        if (avatar.isNotBlank()) {
                            userAvatarsCache[uid] = avatar
                            if (name.isNotBlank()) {
                                userAvatarsCache[name.trim().lowercase()] = avatar
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getDynamicAvatar(uid: String, fallbackAvatar: String): String {
        return getDynamicAvatar(uid, "", fallbackAvatar)
    }

    fun getDynamicAvatar(uid: String, authorName: String, fallbackAvatar: String): String {
        if (uid.isNotBlank() && userAvatarsCache.containsKey(uid)) {
            val a = userAvatarsCache[uid]
            if (!a.isNullOrBlank()) return a
        }
        val nameKey = authorName.trim().lowercase()
        if (nameKey.isNotBlank() && userAvatarsCache.containsKey(nameKey)) {
            val a = userAvatarsCache[nameKey]
            if (!a.isNullOrBlank()) return a
        }
        return fallbackAvatar
    }

    fun getCurrentUser(): FirebaseUser? {
        return try {
            auth.currentUser
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentUserId(): String {
        return try {
            auth.currentUser?.uid ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    // Helper to generate a consistent chat ID between two users
    fun getChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    // Base64 helper to convert URI to Base64 String for storing in Firestore
    fun convertUriToBase64(context: Context, uri: Uri): String {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos)
            val byteArray = baos.toByteArray()
            "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error converting image to Base64", e)
            ""
        }
    }

    suspend fun uploadMedia(localPathOrUri: String): String? {
        if (localPathOrUri.isBlank()) return null
        if (localPathOrUri.startsWith("http")) return localPathOrUri
        if (localPathOrUri.startsWith("data:image")) return localPathOrUri
        return try {
            val fileUri = if (localPathOrUri.startsWith("content://") || localPathOrUri.startsWith("file://")) {
                Uri.parse(localPathOrUri)
            } else {
                Uri.fromFile(File(localPathOrUri))
            }
            val ref = storage.reference.child("media/${UUID.randomUUID()}")
            ref.putFile(fileUri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error uploading media", e)
            null
        }
    }

    // -------------------------------------------------------------
    // AUTHENTICATION & PROFILES
    // -------------------------------------------------------------
    suspend fun updateProfile(name: String, bio: String, livesIn: String, work: String): Result<Unit> {
        val uid = getCurrentUserId()
        if (uid.isBlank()) return Result.success(Unit)
        return try {
            firestore.collection("users").document(uid)
                .set(
                    mapOf(
                        "name" to name,
                        "bio" to bio,
                        "livesIn" to livesIn,
                        "work" to work
                    ),
                    SetOptions.merge()
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAvatar(avatarUrl: String): Result<Unit> {
        val uid = getCurrentUserId()
        if (uid.isNotBlank()) {
            userAvatarsCache[uid] = avatarUrl
        }
        val currentName = auth.currentUser?.displayName ?: ""
        if (currentName.isNotBlank()) {
            userAvatarsCache[currentName.trim().lowercase()] = avatarUrl
        }
        return try {
            if (uid.isNotBlank()) {
                firestore.collection("users").document(uid)
                    .set(mapOf("avatarUrl" to avatarUrl), SetOptions.merge()).await()
            }

            // Also update avatar across user's existing posts in Firestore
            try {
                if (uid.isNotBlank()) {
                    val postsSnapshot = firestore.collection("posts")
                        .whereEqualTo("authorUid", uid)
                        .get()
                        .await()
                    for (doc in postsSnapshot.documents) {
                        doc.reference.update("authorAvatarUrl", avatarUrl)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Also update avatar across user's existing comments in Firestore
            try {
                if (uid.isNotBlank()) {
                    val commentsSnapshot = firestore.collection("comments")
                        .whereEqualTo("authorUid", uid)
                        .get()
                        .await()
                    for (doc in commentsSnapshot.documents) {
                        doc.reference.update("authorAvatarUrl", avatarUrl)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Also update avatar across user's existing stories in Firestore
            try {
                if (uid.isNotBlank()) {
                    val storiesSnapshot = firestore.collection("stories")
                        .whereEqualTo("authorUid", uid)
                        .get()
                        .await()
                    for (doc in storiesSnapshot.documents) {
                        doc.reference.update("authorAvatarUrl", avatarUrl)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCover(coverUrl: String): Result<Unit> {
        val uid = getCurrentUserId()
        if (uid.isBlank()) return Result.success(Unit)
        return try {
            firestore.collection("users").document(uid)
                .set(mapOf("coverUrl" to coverUrl), SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserProfileFlow(): Flow<UserProfile?> = callbackFlow {
        val uid = getCurrentUserId()
        if (uid.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val profile = snapshot.toObject(UserProfile::class.java)?.copy(uid = snapshot.id)
                trySend(profile)
            }
        awaitClose { listener.remove() }
    }

    // -------------------------------------------------------------
    // R2 STORAGE CONFIGURATION CLOUD SYNC
    // -------------------------------------------------------------
    fun getCloudR2ConfigsFlow(): Flow<List<com.example.data.local.entities.R2StorageConfigEntity>> = callbackFlow {
        val listener = firestore.collection("system_config")
            .document("r2_storage_accounts")
            .collection("accounts")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot.documents.mapNotNull { doc ->
                    val id = doc.getLong("id") ?: doc.id.hashCode().toLong().let { if (it == 0L) 1L else kotlin.math.abs(it) }
                    val label = doc.getString("label") ?: "Cloudflare R2"
                    val bucketName = doc.getString("bucketName") ?: ""
                    val accountId = doc.getString("accountId") ?: ""
                    val accessKeyId = doc.getString("accessKeyId") ?: ""
                    val secretAccessKey = doc.getString("secretAccessKey") ?: ""
                    val publicEndpoint = doc.getString("publicEndpoint") ?: ""
                    val isActive = doc.getBoolean("isActive") ?: true
                    val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                    if (bucketName.isNotBlank() && accountId.isNotBlank()) {
                        com.example.data.local.entities.R2StorageConfigEntity(
                            id = id,
                            label = label,
                            bucketName = bucketName,
                            accountId = accountId,
                            accessKeyId = accessKeyId,
                            secretAccessKey = secretAccessKey,
                            publicEndpoint = publicEndpoint,
                            isActive = isActive,
                            createdAt = createdAt
                        )
                    } else null
                }
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveCloudR2Config(config: com.example.data.local.entities.R2StorageConfigEntity): Result<Unit> {
        return try {
            val docId = if (config.id != 0L) config.id.toString() else "r2_${config.bucketName}_${config.accountId.take(6)}"
            val docRef = firestore.collection("system_config")
                .document("r2_storage_accounts")
                .collection("accounts")
                .document(docId)

            val data = hashMapOf(
                "id" to if (config.id != 0L) config.id else System.currentTimeMillis(),
                "label" to config.label,
                "bucketName" to config.bucketName,
                "accountId" to config.accountId,
                "accessKeyId" to config.accessKeyId,
                "secretAccessKey" to config.secretAccessKey,
                "publicEndpoint" to config.publicEndpoint,
                "isActive" to config.isActive,
                "createdAt" to config.createdAt
            )
            docRef.set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error saving Cloud R2 Config to Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun deleteCloudR2Config(id: Long): Result<Unit> {
        return try {
            val docRef = firestore.collection("system_config")
                .document("r2_storage_accounts")
                .collection("accounts")
                .document(id.toString())
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithFullDetails(
        firstName: String,
        lastName: String,
        emailOrPhone: String,
        dob: String,
        gender: String,
        pass: String,
        customAvatarUrl: String?,
        customCoverUrl: String?
    ): Result<UserProfile> {
        return try {
            val fullName = "$firstName $lastName".trim().ifBlank { "User" }
            val cleanEmailOrPhone = emailOrPhone.trim()
            val formattedEmail = if (cleanEmailOrPhone.contains("@")) {
                cleanEmailOrPhone
            } else {
                val digits = cleanEmailOrPhone.filter { it.isDigit() }
                if (digits.length >= 4) "$digits@bikalaafa.app" else "user_${System.currentTimeMillis()}@bikalaafa.app"
            }
            val safePass = if (pass.length < 6) pass.padEnd(6, '0') else pass
            val authResult = auth.createUserWithEmailAndPassword(formattedEmail, safePass).await()
            val user = authResult.user ?: throw Exception("User creation failed")

            val defaultAvatar = when (gender) {
                "Female" -> "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=400&q=80"
                "Others", "Custom" -> "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80"
                else -> "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=400&q=80"
            }
            val defaultCover = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?auto=format&fit=crop&w=1200&q=80"

            val avatar = if (!customAvatarUrl.isNullOrBlank()) customAvatarUrl else defaultAvatar
            val cover = if (!customCoverUrl.isNullOrBlank()) customCoverUrl else defaultCover

            val profile = UserProfile(
                uid = user.uid,
                firstName = firstName,
                lastName = lastName,
                name = fullName,
                emailOrPhone = cleanEmailOrPhone,
                email = formattedEmail,
                dob = dob,
                gender = gender,
                avatarUrl = avatar,
                coverUrl = cover,
                bio = "Welcome to Frndom!"
            )
            try {
                firestore.collection("users").document(user.uid).set(profile).await()
            } catch (fe: Exception) {
                Log.e("FirebaseManager", "Firestore save profile failed: ${fe.message}", fe)
            }
            Result.success(profile)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "SignUp error: ${e.message}", e)
            val friendlyMsg = when {
                e.message?.contains("email address is already in use", ignoreCase = true) == true ->
                    "An account with this email/number already exists. Please log in."
                e.message?.contains("badly formatted", ignoreCase = true) == true ->
                    "Please enter a valid email or phone number."
                e.message?.contains("at least 6 characters", ignoreCase = true) == true ->
                    "Password must be at least 6 characters long."
                else -> e.message ?: "Registration failed. Please try again."
            }
            Result.failure(Exception(friendlyMsg, e))
        }
    }

    suspend fun signUpWithEmail(name: String, email: String, pass: String): Result<UserProfile> {
        return signUpWithFullDetails(
            firstName = name.substringBefore(" "),
            lastName = name.substringAfter(" ", ""),
            emailOrPhone = email,
            dob = "2000-01-01",
            gender = "Male",
            pass = pass,
            customAvatarUrl = null,
            customCoverUrl = null
        )
    }

    suspend fun loginWithEmail(emailOrPhone: String, pass: String): Result<UserProfile> {
        return try {
            val cleanInput = emailOrPhone.trim()
            val formattedEmail = if (cleanInput.contains("@")) {
                cleanInput
            } else {
                val digits = cleanInput.filter { it.isDigit() }
                if (digits.isNotEmpty()) "$digits@bikalaafa.app" else "$cleanInput@bikalaafa.app"
            }
            val safePass = if (pass.length < 6) pass.padEnd(6, '0') else pass

            val authResult = auth.signInWithEmailAndPassword(formattedEmail, safePass).await()
            val user = authResult.user ?: throw Exception("Login failed")
            val profile = try {
                val doc = firestore.collection("users").document(user.uid).get().await()
                doc.toObject(UserProfile::class.java)
            } catch (fe: Exception) {
                Log.e("FirebaseManager", "Firestore get profile failed: ${fe.message}", fe)
                null
            } ?: UserProfile(
                uid = user.uid,
                email = formattedEmail,
                name = user.displayName ?: user.email?.substringBefore("@") ?: "User"
            )
            Result.success(profile)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Login error: ${e.message}", e)
            val friendlyMsg = when {
                e.message?.contains("credential is incorrect", ignoreCase = true) == true ||
                        e.message?.contains("user-not-found", ignoreCase = true) == true ||
                        e.message?.contains("wrong-password", ignoreCase = true) == true ->
                    "Incorrect credentials or account not found. Please check your details or create a new account."
                e.message?.contains("badly formatted", ignoreCase = true) == true ->
                    "Please enter a valid email address or phone number."
                e.message?.contains("network error", ignoreCase = true) == true ->
                    "Network error. Please check your internet connection."
                else -> e.message ?: "Login failed. Please try again."
            }
            Result.failure(Exception(friendlyMsg, e))
        }
    }

    fun getDemoUserProfile(): UserProfile {
        return UserProfile(
            uid = "demo_user_${UUID.randomUUID().toString().take(8)}",
            firstName = "Maruf",
            lastName = "Hossain",
            name = "Maruf Hossain",
            emailOrPhone = "maruf@bikalaafa.app",
            email = "maruf@bikalaafa.app",
            dob = "15 Aug 1998",
            gender = "Male",
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=400&q=80",
            coverUrl = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?auto=format&fit=crop&w=1200&q=80",
            bio = "Digital Creator & Software Enthusiast | Living life one code at a time ✨",
            livesIn = "Dhaka, Bangladesh",
            work = "Content Creator & Developer"
        )
    }

    fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.e("FirebaseManager", "SignOut error", e)
        }
    }

    suspend fun fetchUserProfile(uid: String): UserProfile? {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getAllUsersFlow(): Flow<List<UserProfile>> = callbackFlow {
        val listener = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val currentUid = getCurrentUserId()
                val users = snapshot?.documents?.mapNotNull { doc ->
                    val user = doc.toObject(UserProfile::class.java)?.copy(uid = doc.id)
                    if (user != null && user.uid != currentUid) user else null
                } ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    // -------------------------------------------------------------
    // POSTS (REALTIME MULTI-DEVICE SYNC)
    // -------------------------------------------------------------
    fun getPostsFlow(): Flow<List<PostEntity>> = callbackFlow {
        val listener = firestore.collection("posts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val currentUid = getCurrentUserId()
                val posts = snapshot?.documents?.mapNotNull { doc ->
                    val id = doc.getLong("id") ?: doc.id.hashCode().toLong().let { if (it == 0L) System.currentTimeMillis() else kotlin.math.abs(it) }
                    val authorUid = doc.getString("authorUid") ?: ""
                    val authorName = doc.getString("authorName") ?: "Anonymous"
                    val rawAvatar = doc.getString("authorAvatarUrl") ?: ""
                    val authorAvatarUrl = getDynamicAvatar(authorUid, authorName, rawAvatar)
                    val content = doc.getString("content") ?: ""
                    val rawImageUrl = doc.getString("imageUrl")
                    val mediaType = doc.getString("mediaType") ?: doc.getString("type")
                        ?: if (rawImageUrl?.contains("video") == true || rawImageUrl?.contains(".mp4") == true || rawImageUrl?.startsWith("[VIDEO]") == true) "video" else if (!rawImageUrl.isNullOrBlank()) "image" else "text"
                    val imageUrl = if (mediaType == "video" && rawImageUrl != null && !rawImageUrl.startsWith("[VIDEO]")) {
                        "[VIDEO]$rawImageUrl"
                    } else {
                        rawImageUrl
                    }
                    val bgStyle = doc.getString("bgStyle")
                    val privacy = doc.getString("privacy") ?: "PUBLIC"
                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()

                    val likedUsers = (doc.get("likedUsers") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    val userReactions = (doc.get("userReactions") as? Map<*, *>)?.entries?.associate {
                        it.key.toString() to it.value.toString()
                    } ?: emptyMap()

                    val isLiked = currentUid.isNotBlank() && (likedUsers.contains(currentUid) || (userReactions[currentUid] != null && userReactions[currentUid] != "NONE"))
                    val userReaction = if (currentUid.isNotBlank()) {
                        userReactions[currentUid] ?: if (isLiked) "LIKE" else "NONE"
                    } else "NONE"

                    val rawLikes = doc.getLong("likesCount")?.toInt() ?: likedUsers.size
                    val likesCount = maxOf(rawLikes, likedUsers.size)
                    val commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0
                    val sharesCount = doc.getLong("sharesCount")?.toInt() ?: 0

                    PostEntity(
                        id = id,
                        authorName = authorName,
                        authorAvatarUrl = authorAvatarUrl,
                        timeAgo = "Just now",
                        content = content,
                        imageUrl = imageUrl,
                        bgStyle = bgStyle,
                        privacy = privacy,
                        likesCount = likesCount,
                        commentsCount = commentsCount,
                        sharesCount = sharesCount,
                        isLiked = isLiked,
                        userReaction = userReaction,
                        timestamp = timestamp
                    )
                }?.sortedByDescending { it.timestamp } ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addPost(
        authorName: String,
        authorAvatarUrl: String,
        content: String,
        imageBase64OrUrl: String?,
        mediaType: String = if (imageBase64OrUrl?.contains("video") == true || imageBase64OrUrl?.contains(".mp4") == true || imageBase64OrUrl?.startsWith("[VIDEO]") == true) "video" else if (!imageBase64OrUrl.isNullOrBlank()) "image" else "text",
        bgStyle: String? = null,
        privacy: String = "PUBLIC"
    ): Result<Unit> {
        return try {
            val docRef = firestore.collection("posts").document()
            val id = System.currentTimeMillis()
            val postData = hashMapOf(
                "docId" to docRef.id,
                "id" to id,
                "authorUid" to getCurrentUserId(),
                "authorName" to authorName,
                "authorAvatarUrl" to authorAvatarUrl,
                "content" to content,
                "imageUrl" to imageBase64OrUrl,
                "bgStyle" to bgStyle,
                "mediaType" to mediaType,
                "type" to mediaType,
                "privacy" to privacy,
                "likesCount" to 0,
                "commentsCount" to 0,
                "sharesCount" to 0,
                "likedUsers" to emptyList<String>(),
                "userReactions" to emptyMap<String, String>(),
                "timestamp" to System.currentTimeMillis()
            )
            docRef.set(postData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleLikeOnPost(postId: Long, reaction: String, post: PostEntity? = null): Result<Unit> {
        val currentUid = getCurrentUserId().ifBlank { "anonymous_user" }
        return try {
            // Find document in Firestore
            val querySnapshot = firestore.collection("posts")
                .whereEqualTo("id", postId)
                .limit(1)
                .get()
                .await()

            val doc = querySnapshot.documents.firstOrNull()

            if (doc != null) {
                val docRef = doc.reference
                val likedUsers = (doc.get("likedUsers") as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
                val userReactions = (doc.get("userReactions") as? Map<*, *>)?.entries?.associate {
                    it.key.toString() to it.value.toString()
                }?.toMutableMap() ?: mutableMapOf()

                val isCurrentlyLiked = likedUsers.contains(currentUid) || (userReactions[currentUid] != null && userReactions[currentUid] != "NONE")
                val isTogglingOff = (reaction == "LIKE" && isCurrentlyLiked) || (userReactions[currentUid] == reaction)

                if (isTogglingOff) {
                    likedUsers.remove(currentUid)
                    userReactions[currentUid] = "NONE"
                } else {
                    if (!likedUsers.contains(currentUid)) {
                        likedUsers.add(currentUid)
                    }
                    userReactions[currentUid] = reaction
                }

                val currentLikes = doc.getLong("likesCount")?.toInt() ?: likedUsers.size
                val newLikesCount = likedUsers.size.coerceAtLeast(0)

                docRef.update(
                    mapOf(
                        "likedUsers" to likedUsers,
                        "userReactions" to userReactions,
                        "likesCount" to newLikesCount
                    )
                ).await()
            } else if (post != null) {
                // If post was not in Firestore yet (e.g. from local room template), add it to Firestore!
                val docRef = firestore.collection("posts").document()
                val isLiked = reaction != "NONE"
                val likedUsers = if (isLiked) listOf(currentUid) else emptyList()
                val userReactions = if (isLiked) mapOf(currentUid to reaction) else emptyMap()

                val newPostData = hashMapOf(
                    "docId" to docRef.id,
                    "id" to post.id,
                    "authorUid" to getCurrentUserId(),
                    "authorName" to post.authorName,
                    "authorAvatarUrl" to post.authorAvatarUrl,
                    "content" to post.content,
                    "imageUrl" to post.imageUrl,
                    "bgStyle" to post.bgStyle,
                    "privacy" to post.privacy,
                    "likesCount" to if (isLiked) (post.likesCount + 1).coerceAtLeast(1) else post.likesCount,
                    "commentsCount" to post.commentsCount,
                    "sharesCount" to post.sharesCount,
                    "likedUsers" to likedUsers,
                    "userReactions" to userReactions,
                    "timestamp" to post.timestamp
                )
                docRef.set(newPostData).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error toggling like on Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun addCommentToPost(
        postId: Long,
        content: String,
        authorName: String,
        authorAvatarUrl: String
    ): Result<Unit> {
        val currentUid = getCurrentUserId().ifBlank { "user_${System.currentTimeMillis()}" }
        return try {
            val commentDoc = firestore.collection("comments").document()
            val commentData = hashMapOf(
                "id" to commentDoc.id,
                "postId" to postId,
                "authorUid" to currentUid,
                "authorName" to authorName,
                "authorAvatarUrl" to authorAvatarUrl,
                "content" to content,
                "timestamp" to System.currentTimeMillis()
            )
            commentDoc.set(commentData).await()

            // Increment post commentsCount
            val querySnapshot = firestore.collection("posts")
                .whereEqualTo("id", postId)
                .limit(1)
                .get()
                .await()

            querySnapshot.documents.firstOrNull()?.reference?.update(
                "commentsCount", FieldValue.increment(1)
            )?.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error adding comment to Firestore", e)
            Result.failure(e)
        }
    }

    fun getCommentsFlow(postId: Long): Flow<List<CommentEntity>> = callbackFlow {
        val listener = firestore.collection("comments")
            .whereEqualTo("postId", postId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val comments = snapshot?.documents?.mapNotNull { doc ->
                    val id = doc.id.hashCode().toLong().let { if (it == 0L) System.currentTimeMillis() else kotlin.math.abs(it) }
                    val authorUid = doc.getString("authorUid") ?: ""
                    val authorName = doc.getString("authorName") ?: "User"
                    val rawAvatar = doc.getString("authorAvatarUrl") ?: ""
                    val authorAvatarUrl = getDynamicAvatar(authorUid, authorName, rawAvatar)
                    val content = doc.getString("content") ?: ""
                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()

                    CommentEntity(
                        id = id,
                        postId = postId,
                        authorName = authorName,
                        authorAvatarUrl = authorAvatarUrl,
                        content = content,
                        timeAgo = "Just now",
                        likesCount = 0,
                        timestamp = timestamp
                    )
                }?.sortedBy { it.timestamp } ?: emptyList()
                trySend(comments)
            }
        awaitClose { listener.remove() }
    }

    suspend fun incrementShareCount(postId: Long): Result<Unit> {
        return try {
            val querySnapshot = firestore.collection("posts")
                .whereEqualTo("id", postId)
                .limit(1)
                .get()
                .await()

            querySnapshot.documents.firstOrNull()?.reference?.update(
                "sharesCount", FieldValue.increment(1)
            )?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error updating shares count", e)
            Result.failure(e)
        }
    }

    suspend fun deletePost(postId: Long): Result<Unit> {
        return try {
            val querySnapshot = firestore.collection("posts")
                .whereEqualTo("id", postId)
                .limit(1)
                .get()
                .await()

            val doc = querySnapshot.documents.firstOrNull()
            doc?.reference?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------
    // STORIES
    // -------------------------------------------------------------
    fun getStoriesFlow(): Flow<List<FirebaseStory>> = callbackFlow {
        val listener = firestore.collection("stories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val stories = snapshot?.documents?.mapNotNull { doc ->
                    val story = doc.toObject(FirebaseStory::class.java)?.copy(id = doc.id)
                    if (story != null) {
                        val authorUid = doc.getString("authorUid") ?: ""
                        val dynAvatar = getDynamicAvatar(authorUid, story.authorName, story.authorAvatarUrl)
                        story.copy(authorAvatarUrl = dynAvatar)
                    } else null
                }?.sortedByDescending { it.timestamp } ?: emptyList()
                trySend(stories)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addStory(authorName: String, authorAvatarUrl: String, imageBase64OrUrl: String): Result<Unit> {
        return try {
            val currentUid = getCurrentUserId()
            val dynAvatar = getDynamicAvatar(currentUid, authorName, authorAvatarUrl)
            val story = FirebaseStory(
                authorUid = currentUid,
                authorName = authorName,
                authorAvatarUrl = dynAvatar,
                imageBase64OrUrl = imageBase64OrUrl,
                timestamp = System.currentTimeMillis()
            )
            firestore.collection("stories").add(story).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteStory(storyId: Long): Result<Unit> {
        return try {
            // Find and delete from Firestore
            val snapshot = firestore.collection("stories").get().await()
            for (doc in snapshot.documents) {
                if (doc.id.hashCode().toLong() == storyId || doc.getLong("timestamp") == storyId) {
                    doc.reference.delete().await()
                    break
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------
    // GROUPS & PAGES
    // -------------------------------------------------------------
    fun getGroupsFlow(): Flow<List<FirebaseGroup>> = callbackFlow {
        val listener = firestore.collection("groups")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val groups = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirebaseGroup::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(groups)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createGroup(name: String, description: String, category: String, coverUrl: String): Result<Unit> {
        return try {
            val group = FirebaseGroup(
                name = name,
                description = description,
                category = category,
                coverUrl = coverUrl,
                memberCount = 1,
                members = listOf(getCurrentUserId()),
                adminUid = getCurrentUserId()
            )
            firestore.collection("groups").add(group).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPagesFlow(): Flow<List<FirebasePage>> = callbackFlow {
        val listener = firestore.collection("pages")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val pages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirebasePage::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(pages)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createPage(name: String, category: String, description: String, avatarUrl: String): Result<Unit> {
        return try {
            val page = FirebasePage(
                name = name,
                category = category,
                description = description,
                avatarUrl = avatarUrl,
                followerCount = 1,
                followers = listOf(getCurrentUserId()),
                ownerUid = getCurrentUserId()
            )
            firestore.collection("pages").add(page).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------
    // 1-ON-1 MESSENGER REALTIME CHAT
    // -------------------------------------------------------------
    fun getUserConversationsFlow(currentUid: String): Flow<List<FirebaseConversation>> = callbackFlow {
        if (currentUid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("conversations")
            .whereArrayContains("participants", currentUid)
            .orderBy("lastTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseManager", "Error fetching conversations", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val conversations = snapshot?.documents?.mapNotNull { doc ->
                    val chatId = doc.getString("chatId") ?: doc.id
                    val participants = (doc.get("participants") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    val participantNames = (doc.get("participantNames") as? Map<*, *>)?.entries?.associate {
                        it.key.toString() to it.value.toString()
                    } ?: emptyMap()
                    val participantAvatars = (doc.get("participantAvatars") as? Map<*, *>)?.entries?.associate {
                        it.key.toString() to it.value.toString()
                    } ?: emptyMap()
                    val lastMessage = doc.getString("lastMessage") ?: ""
                    val lastSenderUid = doc.getString("lastSenderUid") ?: ""
                    val lastTimestamp = doc.getLong("lastTimestamp") ?: System.currentTimeMillis()

                    FirebaseConversation(
                        chatId = chatId,
                        participants = participants,
                        participantNames = participantNames,
                        participantAvatars = participantAvatars,
                        lastMessage = lastMessage,
                        lastSenderUid = lastSenderUid,
                        lastTimestamp = lastTimestamp
                    )
                } ?: emptyList()
                trySend(conversations)
            }
        awaitClose { listener.remove() }
    }

    fun getChatMessagesFlow(chatId: String): Flow<List<FirebaseChatMessage>> = callbackFlow {
        if (chatId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("messages")
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseManager", "Error fetching chat messages", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    val id = doc.id
                    val cId = doc.getString("chatId") ?: ""
                    val senderUid = doc.getString("senderUid") ?: ""
                    val senderName = doc.getString("senderName") ?: "User"
                    val senderAvatar = doc.getString("senderAvatar") ?: ""
                    val recipientUid = doc.getString("recipientUid") ?: ""
                    val recipientName = doc.getString("recipientName") ?: "User"
                    val recipientAvatar = doc.getString("recipientAvatar") ?: ""
                    val messageText = doc.getString("messageText") ?: ""
                    val imageBase64 = doc.getString("imageBase64") ?: ""
                    val videoBase64 = doc.getString("videoBase64") ?: ""
                    val audioBase64 = doc.getString("audioBase64") ?: ""
                    val audioDurationSeconds = doc.getLong("audioDurationSeconds")?.toInt() ?: 0
                    val messageType = doc.getString("messageType") ?: if (imageBase64.isNotBlank()) "image" else if (videoBase64.isNotBlank()) "video" else if (audioBase64.isNotBlank()) "audio" else "text"
                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()

                    FirebaseChatMessage(
                        id = id,
                        chatId = cId,
                        senderUid = senderUid,
                        senderName = senderName,
                        senderAvatar = senderAvatar,
                        recipientUid = recipientUid,
                        recipientName = recipientName,
                        recipientAvatar = recipientAvatar,
                        messageText = messageText,
                        imageBase64 = imageBase64,
                        videoBase64 = videoBase64,
                        audioBase64 = audioBase64,
                        audioDurationSeconds = audioDurationSeconds,
                        messageType = messageType,
                        timestamp = timestamp
                    )
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendDirectMessage(
        chatId: String,
        senderUid: String,
        senderName: String,
        senderAvatar: String,
        recipientUid: String,
        recipientName: String,
        recipientAvatar: String,
        messageText: String,
        imageBase64: String = "",
        videoBase64: String = "",
        audioBase64: String = "",
        audioDurationSeconds: Int = 0,
        messageType: String = "text"
    ): Result<Unit> {
        return try {
            val msgRef = firestore.collection("messages").document()
            val timestamp = System.currentTimeMillis()

            val msgData = hashMapOf(
                "id" to msgRef.id,
                "chatId" to chatId,
                "senderUid" to senderUid,
                "senderName" to senderName,
                "senderAvatar" to senderAvatar,
                "recipientUid" to recipientUid,
                "recipientName" to recipientName,
                "recipientAvatar" to recipientAvatar,
                "messageText" to messageText,
                "imageBase64" to imageBase64,
                "videoBase64" to videoBase64,
                "audioBase64" to audioBase64,
                "audioDurationSeconds" to audioDurationSeconds,
                "messageType" to messageType,
                "timestamp" to timestamp
            )
            msgRef.set(msgData).await()

            // Update conversation document
            val convRef = firestore.collection("conversations").document(chatId)
            val summaryText = when {
                messageText.isNotBlank() -> messageText
                imageBase64.isNotBlank() -> "📷 Photo"
                videoBase64.isNotBlank() -> "🎥 Video"
                audioBase64.isNotBlank() -> "🎤 Voice message"
                messageType == "call_audio" -> "📞 Audio call"
                messageType == "call_video" -> "📹 Video call"
                else -> "👍"
            }
            val convData = hashMapOf(
                "chatId" to chatId,
                "participants" to listOf(senderUid, recipientUid),
                "participantNames" to mapOf(senderUid to senderName, recipientUid to recipientName),
                "participantAvatars" to mapOf(senderUid to senderAvatar, recipientUid to recipientAvatar),
                "lastMessage" to summaryText,
                "lastSenderUid" to senderUid,
                "lastTimestamp" to timestamp
            )
            convRef.set(convData, SetOptions.merge()).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error sending direct message", e)
            Result.failure(e)
        }
    }
}
