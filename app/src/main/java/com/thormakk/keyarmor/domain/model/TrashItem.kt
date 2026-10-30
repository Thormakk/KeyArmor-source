package com.thormakk.keyarmor.domain.model

sealed class TrashItem {
    abstract val id: String
    abstract val title: String
    abstract val deletedAt: Long?
    abstract val tags: List<String>
    abstract val category: EntryCategory

    data class Password(
        override val id: String,
        override val title: String,
        override val deletedAt: Long?,
        override val tags: List<String> = emptyList(),
        override val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR
    ) : TrashItem()

    data class BankAccount(
        override val id: String,
        override val title: String,
        override val deletedAt: Long?,
        override val tags: List<String> = emptyList(),
        override val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR
    ) : TrashItem()

    data class Note(
        override val id: String,
        override val title: String,
        override val deletedAt: Long?,
        override val tags: List<String> = emptyList(),
        override val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR
    ) : TrashItem()

    data class Custom(
        override val id: String,
        override val title: String,
        override val deletedAt: Long?,
        override val tags: List<String> = emptyList(),
        override val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR
    ) : TrashItem()

    data class Card(
        override val id: String,
        override val title: String,
        override val deletedAt: Long?,
        override val tags: List<String> = emptyList(),
        override val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR
    ) : TrashItem()

    data class TwoFactor(
        override val id: String,
        override val title: String,
        override val deletedAt: Long?,
        override val tags: List<String> = emptyList(),
        override val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR
    ) : TrashItem()

    data class CryptoWallet(
        override val id: String,
        override val title: String,
        override val deletedAt: Long?,
        override val tags: List<String> = emptyList(),
        override val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR
    ) : TrashItem()

    data class Notification(
        override val id: String,
        override val title: String,
        override val deletedAt: Long?,
        val notificationType: NotificationType,
        override val tags: List<String> = emptyList(),
        override val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR
    ) : TrashItem()

    data class Passkey(
        override val id: String,
        override val title: String,
        override val deletedAt: Long?,
        override val tags: List<String> = emptyList(),
        override val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR
    ) : TrashItem()
    data class IdentityDocument(
        override val id: String,
        override val title: String,
        override val deletedAt: Long?,
        override val tags: List<String> = emptyList(),
        override val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR
    ) : TrashItem()

    data class Vehicle(
        override val id: String,
        override val title: String,
        override val deletedAt: Long?,
        override val tags: List<String> = emptyList(),
        override val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR
    ) : TrashItem()

    data class Reminder(
        override val id: String,
        override val title: String,
        override val deletedAt: Long?,
        override val tags: List<String> = emptyList(),
        override val category: EntryCategory = EntryCategory.SIN_CATEGORIZAR
    ) : TrashItem()
}
