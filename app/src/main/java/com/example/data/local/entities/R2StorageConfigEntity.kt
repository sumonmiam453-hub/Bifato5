package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "r2_storage_configs")
data class R2StorageConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val bucketName: String,
    val accountId: String,
    val accessKeyId: String,
    val secretAccessKey: String,
    val publicEndpoint: String,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
