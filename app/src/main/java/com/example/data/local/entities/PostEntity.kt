package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorName: String,
    val authorAvatarUrl: String,
    val timeAgo: String,
    val content: String,
    val imageUrl: String? = null,
    val bgStyle: String? = null,
    val privacy: String = "PUBLIC", // "PUBLIC", "FRIENDS", "ONLY_ME"
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val userReaction: String = "NONE", // LIKE, LOVE, HAHA, WOW, SAD, ANGRY, NONE
    val timestamp: Long = System.currentTimeMillis()
)
