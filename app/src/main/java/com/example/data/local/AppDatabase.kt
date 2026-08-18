package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CommentDao
import com.example.data.local.dao.MarketplaceDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.PostDao
import com.example.data.local.dao.R2StorageDao
import com.example.data.local.dao.StoryDao
import com.example.data.local.dao.UserProfileDao
import com.example.data.local.entities.CommentEntity
import com.example.data.local.entities.MarketplaceItemEntity
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.PostEntity
import com.example.data.local.entities.R2StorageConfigEntity
import com.example.data.local.entities.StoryEntity
import com.example.data.local.entities.UserProfileEntity

@Database(
    entities = [
        PostEntity::class,
        CommentEntity::class,
        StoryEntity::class,
        NotificationEntity::class,
        UserProfileEntity::class,
        MarketplaceItemEntity::class,
        R2StorageConfigEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
    abstract fun storyDao(): StoryDao
    abstract fun notificationDao(): NotificationDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun marketplaceDao(): MarketplaceDao
    abstract fun r2StorageDao(): R2StorageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bika_lafa_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
