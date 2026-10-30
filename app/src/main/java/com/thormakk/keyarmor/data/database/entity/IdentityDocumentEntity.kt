package com.thormakk.keyarmor.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "identity_documents",
    indices = [Index(
        value = ["vault_id", "isDeleted", "isArchived", "updatedAt"],
        name  = "idx_identity_documents_vault_active"
    )]
)
data class IdentityDocumentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),

    val docType:        String = "",
    val number:         String = "",
    val issuingCountry: String = "",
    val issueDate:      String = "",
    val expiryDate:     String = "",
    val birthDate:      String = "",
    val holderName:     String = "",
    val notes:          String = "",

    val expiryTimestamp:  Long?   = null,
    val expiryAlertDays:  Int     = 90,
    val category:         String  = "SIN_CATEGORIZAR",
    val tags:             String  = "",
    val isFavorite:       Boolean = false,
    val isTravelSafe:     Boolean = false,
    val isPinned:         Boolean = false,
    val isHidden:         Boolean = false,
    @androidx.room.ColumnInfo(name = "is_protected")
    val isProtected: Boolean = false,
    val createdAt:        Long    = System.currentTimeMillis(),
    val updatedAt:        Long    = System.currentTimeMillis(),
    val isDeleted:        Boolean = false,
    val deletedAt:        Long?   = null,
    val isArchived:       Boolean = false,
    val archivedAt:       Long?   = null,
    
    @ColumnInfo(name = "vault_id")
    val vaultId: String = com.thormakk.keyarmor.domain.model.VaultMetadata.PRIMARY_VAULT_ID,
    @ColumnInfo(name = "inherited_from")
    val inheritedFrom: String? = null,

    @ColumnInfo(name = "front_photo_path")
    val frontPhotoPath: String? = null,
    @ColumnInfo(name = "back_photo_path")
    val backPhotoPath: String? = null,
)
