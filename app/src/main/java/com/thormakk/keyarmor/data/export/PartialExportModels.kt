package com.thormakk.keyarmor.data.export

import com.thormakk.keyarmor.domain.model.EntryCategory

enum class ExportEntryType {
    PASSWORD,
    TWO_FACTOR,
    CREDIT_CARD,
    BANK_ACCOUNT,
    CRYPTO_WALLET,
    SECURE_NOTE,
    CUSTOM_ENTRY,
    PASSKEY,
    IDENTITY_DOCUMENT,
    VEHICLE
}

data class ExportEntryRef(
    val vaultId: String,
    val type:    ExportEntryType,
    val id:      String
)

data class ExportEntryRow(
    val ref:            ExportEntryRef,
    val title:          String,
    val subtitle:       String        = "",
    val favorite:       Boolean       = false,
    val category:       EntryCategory = EntryCategory.SIN_CATEGORIZAR,
    val tags:           List<String>  = emptyList(),
    val hasAttachments: Boolean       = false
)

data class ExportTypeSummary(
    val type:  ExportEntryType,
    val count: Int
)

data class ExportVaultSummary(
    val vault: com.thormakk.keyarmor.domain.model.VaultMetadata,
    val types: List<ExportTypeSummary>
)

enum class PartialExportFormat { CSV, JSON, PDF }
