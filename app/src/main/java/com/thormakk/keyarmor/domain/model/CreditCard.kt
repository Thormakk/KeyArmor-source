package com.thormakk.keyarmor.domain.model

import java.util.UUID

data class CreditCard(
    val id:          String        = UUID.randomUUID().toString(),
    val brand:       String        = "",
    val issuer:      String        = "",
    val cardNumber:  String        = "",
    val cardholder:  String        = "",
    val expiryDate:  String        = "",
    val cvv:         String        = "",
    val bankPhone:   String        = "",
    val category:    EntryCategory = EntryCategory.SIN_CATEGORIZAR,
    val tags:        List<String>  = emptyList(),
    val isFavorite:   Boolean       = false,
    val isTravelSafe: Boolean       = false,
    val isPinned:      Boolean       = false,
    val isHidden:      Boolean       = false,
    val isProtected: Boolean = false,
    val createdAt:   Long          = System.currentTimeMillis(),
    val updatedAt:   Long          = System.currentTimeMillis(),
    val isDeleted:   Boolean       = false,
    val deletedAt:   Long?         = null,
    val archivedAt:  Long?         = null,
    val vaultId:       String        = VaultMetadata.PRIMARY_VAULT_ID,
    val inheritedFrom: String? = null,
)
