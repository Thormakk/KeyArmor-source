package com.thormakk.keyarmor.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "passkeys",
    indices = [Index(
        value = ["vault_id", "is_deleted", "is_archived", "created_at"],
        name  = "idx_passkeys_vault_active"
    )]
)
data class PasskeyEntity(

    @PrimaryKey
    @ColumnInfo(name = "credential_id")
    val credentialId: String,

    @ColumnInfo(name = "rp_id")
    val rpId: String = "",

    @ColumnInfo(name = "rp_name")
    val rpName: String = "",

    @ColumnInfo(name = "user_id")
    val userId: String = "",

    @ColumnInfo(name = "user_name")
    val userName: String = "",

    @ColumnInfo(name = "user_display_name")
    val userDisplayName: String = "",

    @ColumnInfo(name = "private_key_encrypted")
    val privateKeyEncrypted: String = "",

    @ColumnInfo(name = "public_key_cose")
    val publicKeyCose: String = "",

    @ColumnInfo(name = "sign_count")
    val signCount: Long = 0L,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_used_at")
    val lastUsedAt: Long? = null,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null,

    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false,

    @ColumnInfo(name = "archived_at")
    val archivedAt: Long? = null,

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "urls")
    val urls: String = "",

    @ColumnInfo(name = "linked_password_id")
    val linkedPasswordId: String = "",

    @ColumnInfo(name = "category")
    val category: String = "SIN_CATEGORIZAR",

    @ColumnInfo(name = "tags")
    val tags: String = "",

    @ColumnInfo(name = "custom_account_label")
    val customAccountLabel: String = "",

    @ColumnInfo(name = "is_travel_safe")
    val isTravelSafe: Boolean = false,

    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean = false,
    @ColumnInfo(name = "is_hidden")
    val isHidden: Boolean = false,
    @ColumnInfo(name = "is_protected")
    val isProtected: Boolean = false,
    @ColumnInfo(name = "vault_id")
    val vaultId: String = com.thormakk.keyarmor.domain.model.VaultMetadata.PRIMARY_VAULT_ID,
    @ColumnInfo(name = "inherited_from")
    val inheritedFrom: String? = null,
)
