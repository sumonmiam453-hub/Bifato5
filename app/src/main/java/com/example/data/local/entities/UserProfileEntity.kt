package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Long = 1,
    val name: String = "Maruf Hossain",
    val bio: String = "Mobile Developer & Tech Enthusiast 🚀 | Android | Jetpack Compose",
    val work: String = "Software Engineer at Tech Innovations",
    val education: String = "Computer Science & Engineering",
    val livesIn: String = "Dhaka, Bangladesh",
    val followerCount: Int = 1420,
    val coverPhotoUrl: String = "",
    val avatarUrl: String = "",
    val isCreatorMode: Boolean = false,
    val privacyStatus: String = "PUBLIC"
)
