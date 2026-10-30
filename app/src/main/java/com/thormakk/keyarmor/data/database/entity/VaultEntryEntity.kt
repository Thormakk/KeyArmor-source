package com.thormakk.keyarmor.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "vault_entries",
    indices = [Index(
        value = ["vault_id", "isDeleted", "isArchived", "updatedAt"],
        name  = "idx_vault_entries_vault_active"
    )]
)
data class VaultEntryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),

    val title: String,
    val username: String  = "",
    val email: String     = "",
    val password: String  = "",
    val url: String       = "",
    @ColumnInfo(name = "urls")
    val urls: String      = "",
    val notes: String     = "",

    val category: String  = "LOGIN",
    val isFavorite:    Boolean = false,
    val isTravelSafe:  Boolean = false,
    val isPinned:      Boolean = false,
    val isHidden:      Boolean = false,
    val createdAt: Long   = System.currentTimeMillis(),
    val updatedAt: Long   = System.currentTimeMillis(),

    val isDeleted: Boolean = false,
    val deletedAt: Long?   = null,
    val isArchived:  Boolean = false,
    val archivedAt:  Long?   = null,

    val tags: String       = "",

    @ColumnInfo(name = "expiry_date")
    val expiryDate: Long? = null,
    
    @ColumnInfo(name = "vault_id")
    val vaultId: String = com.thormakk.keyarmor.domain.model.VaultMetadata.PRIMARY_VAULT_ID,
    @ColumnInfo(name = "inherited_from")
    val inheritedFrom: String? = null,

    @ColumnInfo(name = "password_changed_at")
    val passwordChangedAt: Long? = null,

    @ColumnInfo(name = "field_label")
    val fieldLabel: String = "",

    @ColumnInfo(name = "is_protected")
    val isProtected: Boolean = false,
)
