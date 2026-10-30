package com.thormakk.keyarmor.domain.model

sealed class ArchiveItem {
    abstract val id: String
    abstract val title: String
    abstract val archivedAt: Long?
    abstract val tags: List<String>
    abstract val category: EntryCategory
    abstract val isProtected: Boolean

    data class Password(
        override val id: String,
        override val title: String,
        override val archivedAt: Long?,
        override val tags: List<String> = emptyList(),
        override val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR,
        override val isProtected: Boolean = false
    ) : ArchiveItem()

    data class BankAccount(
        override val id: String,
        override val title: String,
        override val archivedAt: Long?,
        override val tags: List<String> = emptyList(),
        override val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR,
        override val isProtected: Boolean = false
    ) : ArchiveItem()

    data class Note(
        override val id: String,
        override val title: String,
        override val archivedAt: Long?,
        override val tags: List<String> = emptyList(),
        override val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR,
        override val isProtected: Boolean = false
    ) : ArchiveItem()

    data class Custom(
        override val id: String,
        override val title: String,
        override val archivedAt: Long?,
        override val tags: List<String> = emptyList(),
        override val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR,
        override val isProtected: Boolean = false
    ) : ArchiveItem()

    data class Card(
        override val id: String,
        override val title: String,
        override val archivedAt: Long?,
        override val tags: List<String> = emptyList(),
        override val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR,
        override val isProtected: Boolean = false
    ) : ArchiveItem()

    data class TwoFactor(
        override val id: String,
        override val title: String,
        override val archivedAt: Long?,
        override val tags: List<String> = emptyList(),
        override val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR,
        override val isProtected: Boolean = false
    ) : ArchiveItem()

    data class CryptoWallet(
        override val id: String,
        override val title: String,
        override val archivedAt: Long?,
        override val tags: List<String> = emptyList(),
        override val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR,
        override val isProtected: Boolean = false
    ) : ArchiveItem()
    data class IdentityDocument(
        override val id: String,
        override val title: String,
        override val archivedAt: Long?,
        override val tags: List<String> = emptyList(),
        override val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR,
        override val isProtected: Boolean = false
    ) : ArchiveItem()

    data class Vehicle(
        override val id: String,
        override val title: String,
        override val archivedAt: Long?,
        override val tags: List<String> = emptyList(),
        override val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR,
        override val isProtected: Boolean = false
    ) : ArchiveItem()

    data class Passkey(
        override val id: String,
        override val title: String,
        override val archivedAt: Long?,
        override val tags: List<String> = emptyList(),
        override val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR,
        override val isProtected: Boolean = false
    ) : ArchiveItem()
}
