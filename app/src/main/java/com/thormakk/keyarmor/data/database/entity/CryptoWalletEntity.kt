package com.thormakk.keyarmor.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "crypto_wallets",
    indices = [Index(
        value = ["vault_id", "isDeleted", "isArchived", "updatedAt"],
        name  = "idx_crypto_wallets_vault_active"
    )]
)
data class CryptoWalletEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),

    val address:     String = "",
    val seedPhrase:  String = "",
    val privateKey:  String = "",
    val notes:       String = "",

    val manualBalance:    String  = "",
    val exchangeUrl:      String = "",
    val linkedPasswordId: String = "",
    val network:    String  = "",
    val asset:      String  = "",
    val category:   String  = "SIN_CATEGORIZAR",
    val tags:       String  = "",
    val isFavorite:   Boolean = false,
    val isTravelSafe: Boolean = false,
    val isPinned:     Boolean = false,
    val isHidden:     Boolean = false,
    @androidx.room.ColumnInfo(name = "is_protected")
    val isProtected: Boolean = false,
    val createdAt:  Long    = System.currentTimeMillis(),
    val updatedAt:  Long    = System.currentTimeMillis(),
    val isDeleted:  Boolean = false,
    val deletedAt:  Long?   = null,
    val isArchived:  Boolean = false,
    val archivedAt:  Long?   = null,
    
    @ColumnInfo(name = "vault_id")
    val vaultId: String = com.thormakk.keyarmor.domain.model.VaultMetadata.PRIMARY_VAULT_ID,
    @ColumnInfo(name = "inherited_from")
    val inheritedFrom: String? = null,
)
