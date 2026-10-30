package com.thormakk.keyarmor.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thormakk.keyarmor.data.database.entity.AppMetadataEntity

@Dao
interface AppMetadataDao {

    @Query("SELECT value FROM app_metadata WHERE `key` = :key")
    suspend fun get(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(entry: AppMetadataEntity)

    suspend fun set(key: String, value: String) = set(AppMetadataEntity(key, value))
}
