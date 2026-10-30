package com.thormakk.keyarmor.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "bank_accounts",
    indices = [Index(
        value = ["vault_id", "isDeleted", "isArchived", "updatedAt"],
        name  = "idx_bank_accounts_vault_active"
    )]
)
data class BankAccountEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),

    val bankName:      String = "",
    val swiftBic:      String = "",
    val accountType:   String = "",
    val accountNumber: String = "",
    val cbuIban:       String = "",
    val alias:         String = "",
    val notes:         String = "",

    val linkedPasswordId: String  = "",
    val category:         String  = "SIN_CATEGORIZAR",
    val tags:             String  = "",
    val isFavorite:       Boolean = false,
    val isTravelSafe:     Boolean = false,
    val isPinned:        Boolean = false,
    val isHidden:         Boolean = false,
    @androidx.room.ColumnInfo(name = "is_protected")
    val isProtected: Boolean = false,
    val createdAt:        Long    = System.currentTimeMillis(),
    val updatedAt:        Long    = System.currentTimeMillis(),
    val isDeleted:        Boolean = false,
    val deletedAt:        Long?   = null,
    val isArchived:  Boolean = false,
    val archivedAt:  Long?   = null,
    
    @ColumnInfo(name = "vault_id")
    val vaultId: String = com.thormakk.keyarmor.domain.model.VaultMetadata.PRIMARY_VAULT_ID,
    @ColumnInfo(name = "inherited_from")
    val inheritedFrom: String? = null,
)
