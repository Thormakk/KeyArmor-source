package com.thormakk.keyarmor.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thormakk.keyarmor.data.database.entity.VaultMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultMetadataDao {

    @Query("UPDATE `vault_entries` SET vault_id = :destino WHERE vault_id NOT IN (SELECT id FROM vault_metadata)")
    suspend fun reasignarHuerfanas0(destino: String): Int

    @Query("UPDATE `two_factor_accounts` SET vault_id = :destino WHERE vault_id NOT IN (SELECT id FROM vault_metadata)")
    suspend fun reasignarHuerfanas1(destino: String): Int

    @Query("UPDATE `credit_cards` SET vault_id = :destino WHERE vault_id NOT IN (SELECT id FROM vault_metadata)")
    suspend fun reasignarHuerfanas2(destino: String): Int

    @Query("UPDATE `bank_accounts` SET vault_id = :destino WHERE vault_id NOT IN (SELECT id FROM vault_metadata)")
    suspend fun reasignarHuerfanas3(destino: String): Int

    @Query("UPDATE `crypto_wallets` SET vault_id = :destino WHERE vault_id NOT IN (SELECT id FROM vault_metadata)")
    suspend fun reasignarHuerfanas4(destino: String): Int

    @Query("UPDATE `secure_notes` SET vault_id = :destino WHERE vault_id NOT IN (SELECT id FROM vault_metadata)")
    suspend fun reasignarHuerfanas5(destino: String): Int

    @Query("UPDATE `custom_entries` SET vault_id = :destino WHERE vault_id NOT IN (SELECT id FROM vault_metadata)")
    suspend fun reasignarHuerfanas6(destino: String): Int

    @Query("UPDATE `passkeys` SET vault_id = :destino WHERE vault_id NOT IN (SELECT id FROM vault_metadata)")
    suspend fun reasignarHuerfanas7(destino: String): Int

    @Query("UPDATE `identity_documents` SET vault_id = :destino WHERE vault_id NOT IN (SELECT id FROM vault_metadata)")
    suspend fun reasignarHuerfanas8(destino: String): Int

    @Query("UPDATE `vehicles` SET vault_id = :destino WHERE vault_id NOT IN (SELECT id FROM vault_metadata)")
    suspend fun reasignarHuerfanas9(destino: String): Int


    @Query("SELECT * FROM vault_metadata ORDER BY sortOrder ASC, createdAt ASC")
    fun observeAll(): Flow<List<VaultMetadataEntity>>

    @Query("SELECT * FROM vault_metadata ORDER BY sortOrder ASC, createdAt ASC")
    suspend fun getAll(): List<VaultMetadataEntity>

    @Query("SELECT * FROM vault_metadata WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): VaultMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: VaultMetadataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<VaultMetadataEntity>)

    @Query("DELETE FROM vault_metadata WHERE id NOT IN (:keepIds)")
    suspend fun deleteAllExcept(keepIds: List<String>)

    @Query("DELETE FROM vault_metadata WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM vault_metadata WHERE isInherited = 0 AND isPrivate = 0")
    suspend fun countOwnVaults(): Int
}
