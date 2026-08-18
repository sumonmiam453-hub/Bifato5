package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Long,
    val authorName: String,
    val authorAvatarUrl: String,
    val content: String,
    val timeAgo: String,
    val likesCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
