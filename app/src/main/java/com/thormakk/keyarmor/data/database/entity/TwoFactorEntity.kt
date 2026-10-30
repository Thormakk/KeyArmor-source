package com.thormakk.keyarmor.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "two_factor_accounts",
    indices = [Index(
        value = ["vault_id", "isDeleted", "isArchived", "updatedAt"],
        name  = "idx_two_factor_accounts_vault_active"
    )]
)
data class TwoFactorEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),

    val name:              String = "",
    val issuer:            String = "",
    val accountLabel:      String = "",
    val secret:            String = "",
    val recoveryCodesJson: String = "",
    val notes:             String = "",
    val url:               String = "",
    val linkedPasswordId:  String = "",

    val digits:    Int     = 6,
    val period:    Int     = 30,
    val algorithm: String  = "SHA1",
    val category:  String  = "SIN_CATEGORIZAR",
    val tags:      String  = "",
    val isFavorite:  Boolean = false,
    val isTravelSafe: Boolean = false,
    val isPinned:     Boolean = false,
    val isHidden:     Boolean = false,
    @androidx.room.ColumnInfo(name = "is_protected")
    val isProtected: Boolean = false,
    val createdAt: Long    = System.currentTimeMillis(),
    val updatedAt: Long    = System.currentTimeMillis(),
    val isDeleted:  Boolean = false,
    val deletedAt:  Long?   = null,
    val isArchived: Boolean = false,
    val archivedAt: Long?   = null,
    
    @ColumnInfo(name = "vault_id")
    val vaultId: String = com.thormakk.keyarmor.domain.model.VaultMetadata.PRIMARY_VAULT_ID,
    @ColumnInfo(name = "inherited_from")
    val inheritedFrom: String? = null,
)
