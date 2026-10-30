package com.thormakk.keyarmor.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.thormakk.keyarmor.domain.model.VaultMetadata

@Entity(tableName = "vault_metadata")
data class VaultMetadataEntity(
    @PrimaryKey val id: String,
    val name:            String,
    val isInherited:     Boolean = false,
    val isPrivate:       Boolean = false,
    val ownerEmail:      String  = "",
    val ownerSub:        String  = "",
    val sortOrder:       Int     = 0,
    val createdAt:       Long    = System.currentTimeMillis()
)
