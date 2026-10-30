package com.thormakk.keyarmor.data.repository

import com.thormakk.keyarmor.data.prefs.TrashAutoEmptyPrefs
import com.thormakk.keyarmor.di.ApplicationScope
import com.thormakk.keyarmor.security.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrashAutoPurgeRepository @Inject constructor(
    private val session:              SessionManager,
    private val trashAutoEmptyPrefs:  TrashAutoEmptyPrefs,
    private val vaultRepo:            VaultRepository,
    private val bankRepo:             BankAccountRepository,
    private val noteRepo:             SecureNoteRepository,
    private val customRepo:           CustomEntryRepository,
    private val cardRepo:             CreditCardRepository,
    private val twoFactorRepo:        TwoFactorRepository,
    private val cryptoRepo:           CryptoWalletRepository,
    private val notifRepo:            InAppNotificationRepository,
    private val passkeyRepo:          PasskeyRepository,
    private val identityDocRepo:      IdentityDocumentRepository,
    private val vehicleRepo:          VehicleRepository,
    private val reminderRepo:         ReminderRepository,
    @ApplicationScope private val appScope: CoroutineScope
) {
    init {
        appScope.launch {
            session.isUnlockedFlow.collect { unlocked ->
                if (unlocked) {
                    runCatching { purgeExpiredTrash() }
                }
            }
        }
    }

    private suspend fun purgeExpiredTrash() {
        val days = trashAutoEmptyPrefs.getPeriodDays()
        if (days <= 0) return

        val now      = System.currentTimeMillis()
        val periodMs = days * 86_400_000L
        val cutoff   = now - periodMs
        fun isExpired(deletedAt: Long?) = deletedAt != null && now - deletedAt >= periodMs

        vaultRepo.getExpiredTrashedIds(cutoff).forEach       { vaultRepo.deletePermanently(it) }
        bankRepo.getExpiredTrashedIds(cutoff).forEach        { bankRepo.deletePermanently(it) }
        noteRepo.getExpiredTrashedIds(cutoff).forEach        { noteRepo.deletePermanently(it) }
        customRepo.getExpiredTrashedIds(cutoff).forEach      { customRepo.deletePermanently(it) }
        cardRepo.getExpiredTrashedIds(cutoff).forEach        { cardRepo.deletePermanently(it) }
        twoFactorRepo.getExpiredTrashedIds(cutoff).forEach   { twoFactorRepo.deletePermanently(it) }
        cryptoRepo.getExpiredTrashedIds(cutoff).forEach      { cryptoRepo.deletePermanently(it) }
        passkeyRepo.getExpiredTrashedIds(cutoff).forEach     { passkeyRepo.deletePermanently(it) }
        identityDocRepo.getExpiredTrashedIds(cutoff).forEach { identityDocRepo.deletePermanently(it) }
        vehicleRepo.getExpiredTrashedIds(cutoff).forEach     { vehicleRepo.deletePermanently(it) }
        notifRepo.observeTrash().first().forEach { n ->
            if (isExpired(n.trashedAt)) notifRepo.permanentlyDelete(n.id)
        }
        reminderRepo.observeTrashed().first().forEach { r ->
            if (isExpired(r.trashedAt)) reminderRepo.permanentlyDelete(r.id)
        }
    }
}
