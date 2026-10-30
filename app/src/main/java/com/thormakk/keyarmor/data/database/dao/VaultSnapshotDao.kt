package com.thormakk.keyarmor.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thormakk.keyarmor.data.database.entity.VaultSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultSnapshotDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: VaultSnapshotEntity)

    @Query("SELECT * FROM vault_snapshots ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<VaultSnapshotEntity>>

    @Query("SELECT COUNT(*) FROM vault_snapshots")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM vault_snapshots ORDER BY timestamp DESC")
    suspend fun getAll(): List<VaultSnapshotEntity>

    @Query("SELECT * FROM vault_snapshots WHERE `trigger` = 'WEEKLY' ORDER BY timestamp DESC")
    suspend fun getAllWeekly(): List<VaultSnapshotEntity>

    @Query("SELECT * FROM vault_snapshots WHERE `trigger` != 'WEEKLY' ORDER BY timestamp DESC")
    suspend fun getAllNonWeekly(): List<VaultSnapshotEntity>

    @Query("SELECT * FROM vault_snapshots WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): VaultSnapshotEntity?

    @Query("DELETE FROM vault_snapshots WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE vault_snapshots SET synced_to_drive = 1 WHERE id = :id")
    suspend fun markSyncedToDrive(id: String)

    @Query("SELECT COUNT(*) FROM vault_snapshots WHERE `trigger` = :trigger")
    suspend fun countByTrigger(trigger: String): Int
}
