package com.thormakk.keyarmor.domain.model

import java.util.UUID

data class CryptoWallet(
    val id:         String        = UUID.randomUUID().toString(),
    val address:    String        = "",
    val seedPhrase: String        = "",
    val privateKey: String        = "",
    val notes:           String        = "",
    val manualBalance:   String        = "",
    val exchangeUrl:     String        = "",
    val linkedPasswordId: String       = "",
    val network:    String        = "",
    val asset:      String        = "",
    val category:   EntryCategory = EntryCategory.SIN_CATEGORIZAR,
    val tags:       List<String>  = emptyList(),
    val isFavorite:   Boolean       = false,
    val isTravelSafe: Boolean       = false,
    val isPinned:      Boolean       = false,
    val isHidden:      Boolean       = false,
    val isProtected: Boolean = false,
    val createdAt:  Long          = System.currentTimeMillis(),
    val updatedAt:  Long          = System.currentTimeMillis(),
    val isDeleted:  Boolean       = false,
    val deletedAt:  Long?         = null,
    val archivedAt: Long?         = null,
    val vaultId:       String        = VaultMetadata.PRIMARY_VAULT_ID,
    val inheritedFrom: String? = null,
)
