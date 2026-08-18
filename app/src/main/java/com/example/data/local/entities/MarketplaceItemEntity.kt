package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "marketplace_items")
data class MarketplaceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val price: String,
    val location: String,
    val imageUrl: String,
    val category: String, // Vehicles, Electronics, Property, Clothing, Home, Free
    val description: String,
    val sellerName: String = "Seller",
    val isSaved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
