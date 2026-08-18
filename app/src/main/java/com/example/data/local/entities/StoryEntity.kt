package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorName: String,
    val authorAvatarUrl: String,
    val storyImageUrl: String,
    val hasUnseen: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
