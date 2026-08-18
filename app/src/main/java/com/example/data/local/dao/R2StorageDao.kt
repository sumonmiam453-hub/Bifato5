package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entities.R2StorageConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface R2StorageDao {

    @Query("SELECT * FROM r2_storage_configs ORDER BY createdAt DESC")
    fun getAllConfigs(): Flow<List<R2StorageConfigEntity>>

    @Query("SELECT * FROM r2_storage_configs WHERE isActive = 1 LIMIT 1")
    fun getActiveConfig(): Flow<R2StorageConfigEntity?>

    @Query("SELECT * FROM r2_storage_configs WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveConfigSync(): R2StorageConfigEntity?

    @Query("SELECT * FROM r2_storage_configs WHERE id = :id LIMIT 1")
    suspend fun getConfigById(id: Long): R2StorageConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: R2StorageConfigEntity): Long

    @Update
    suspend fun updateConfig(config: R2StorageConfigEntity)

    @Query("DELETE FROM r2_storage_configs WHERE id = :id")
    suspend fun deleteConfig(id: Long)

    @Query("UPDATE r2_storage_configs SET isActive = 0")
    suspend fun clearAllActive()

    @Query("UPDATE r2_storage_configs SET isActive = 1 WHERE id = :id")
    suspend fun setConfigActive(id: Long)

    @Transaction
    suspend fun setActiveAccount(activeId: Long) {
        clearAllActive()
        setConfigActive(activeId)
    }

    @Query("SELECT COUNT(*) FROM r2_storage_configs")
    suspend fun getConfigCount(): Int
}
