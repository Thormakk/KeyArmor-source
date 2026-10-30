package com.thormakk.keyarmor.data.repository

import androidx.room.withTransaction
import com.thormakk.keyarmor.data.database.KeyArmorDatabase
import com.thormakk.keyarmor.data.database.dao.BankAccountDao
import com.thormakk.keyarmor.data.database.dao.CreditCardDao
import com.thormakk.keyarmor.data.database.dao.CryptoWalletDao
import com.thormakk.keyarmor.data.database.dao.CustomEntryDao
import com.thormakk.keyarmor.data.database.dao.IdentityDocumentDao
import com.thormakk.keyarmor.data.database.dao.InAppNotificationDao
import com.thormakk.keyarmor.data.database.dao.PasskeyDao
import com.thormakk.keyarmor.data.database.dao.PasswordHistoryDao
import com.thormakk.keyarmor.data.database.dao.ReminderDao
import com.thormakk.keyarmor.data.database.dao.SecureNoteDao
import com.thormakk.keyarmor.data.database.dao.SecureNoteHistoryDao
import com.thormakk.keyarmor.data.database.dao.TagDao
import com.thormakk.keyarmor.data.database.dao.TwoFactorDao
import com.thormakk.keyarmor.data.database.dao.VaultEntryDao
import com.thormakk.keyarmor.data.database.dao.VaultMetadataDao
import com.thormakk.keyarmor.data.database.dao.VehicleDao
import com.thormakk.keyarmor.data.storage.AttachmentStorage
import com.thormakk.keyarmor.domain.model.VaultMetadata
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WipeRepository @Inject constructor(
    private val database             : KeyArmorDatabase,
    private val vaultEntryDao        : VaultEntryDao,
    private val bankAccountDao       : BankAccountDao,
    private val creditCardDao        : CreditCardDao,
    private val cryptoWalletDao      : CryptoWalletDao,
    private val customEntryDao       : CustomEntryDao,
    private val secureNoteDao        : SecureNoteDao,
    private val twoFactorDao         : TwoFactorDao,
    private val passwordHistoryDao   : PasswordHistoryDao,
    private val passkeyDao           : PasskeyDao,
    private val notificationDao      : InAppNotificationDao,
    private val identityDocumentDao  : IdentityDocumentDao,
    private val vehicleDao           : VehicleDao,
    private val tagDao               : TagDao,
    private val reminderDao          : ReminderDao,
    private val secureNoteHistoryDao : SecureNoteHistoryDao,
    private val vaultMetadataDao     : VaultMetadataDao,
    private val attachmentRepository : AttachmentRepository,
    private val attachmentStorage    : AttachmentStorage
) {
    suspend fun wipeAll() {
        attachmentRepository.deleteAllPermanently()
        database.withTransaction {
            vaultEntryDao.deleteAll()
            bankAccountDao.deleteAll()
            creditCardDao.deleteAll()
            cryptoWalletDao.deleteAll()
            customEntryDao.deleteAll()
            secureNoteDao.deleteAll()
            twoFactorDao.deleteAll()
            passwordHistoryDao.deleteAll()
            passkeyDao.deleteAll()
            notificationDao.deleteAll()
            identityDocumentDao.deleteAll()
            vehicleDao.deleteAll()
            tagDao.deleteAll()
        }
    }

    suspend fun wipeForPanic() {
        wipeAll()
        attachmentStorage.clearTempFiles()
        database.withTransaction {
            secureNoteHistoryDao.deleteAll()
            reminderDao.deleteAll()
            vaultMetadataDao.deleteAllExcept(VAULTS_DE_SISTEMA)
        }
        attachmentRepository.purgarArchivosHuerfanos(minimoDeAntiguedadMs = 0)
    }

    private companion object {
        val VAULTS_DE_SISTEMA = listOf(
            VaultMetadata.PRIMARY_VAULT_ID,
            VaultMetadata.PRIVATE_VAULT_ID,
            VaultMetadata.DECOY_MAIN_VAULT_ID,
            VaultMetadata.DECOY_PRIVATE_VAULT_ID,
            VaultMetadata.HIDE_MAIN_VAULT_ID,
            VaultMetadata.HIDE_PRIVATE_VAULT_ID,
        )
    }
}
