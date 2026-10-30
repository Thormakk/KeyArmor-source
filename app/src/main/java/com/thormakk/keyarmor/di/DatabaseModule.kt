package com.thormakk.keyarmor.di

import android.content.Context
import androidx.room.Room
import com.thormakk.keyarmor.data.database.KeyArmorDatabase
import com.thormakk.keyarmor.data.database.dao.AppMetadataDao
import com.thormakk.keyarmor.data.database.dao.AttachmentDao
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
import com.thormakk.keyarmor.data.database.dao.VaultSnapshotDao
import com.thormakk.keyarmor.data.database.dao.VehicleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KeyArmorDatabase =
        Room.databaseBuilder(
            context,
            KeyArmorDatabase::class.java,
            "keyarmor.db"
        )
        .addMigrations(*KeyArmorDatabase.ALL_MIGRATIONS)
        .build()

    @Provides
    fun provideAppMetadataDao(db: KeyArmorDatabase): AppMetadataDao =
        db.appMetadataDao()

    @Provides
    fun provideVaultEntryDao(db: KeyArmorDatabase): VaultEntryDao =
        db.vaultEntryDao()

    @Provides
    fun provideBankAccountDao(db: KeyArmorDatabase): BankAccountDao =
        db.bankAccountDao()

    @Provides
    fun provideCustomEntryDao(db: KeyArmorDatabase): CustomEntryDao =
        db.customEntryDao()

    @Provides
    fun provideSecureNoteDao(db: KeyArmorDatabase): SecureNoteDao =
        db.secureNoteDao()

    @Provides
    fun provideCreditCardDao(db: KeyArmorDatabase): CreditCardDao =
        db.creditCardDao()

    @Provides
    fun provideTwoFactorDao(db: KeyArmorDatabase): TwoFactorDao =
        db.twoFactorDao()

    @Provides
    fun provideCryptoWalletDao(db: KeyArmorDatabase): CryptoWalletDao =
        db.cryptoWalletDao()

    @Provides
    fun provideTagDao(db: KeyArmorDatabase): TagDao =
        db.tagDao()

    @Provides
    fun providePasswordHistoryDao(db: KeyArmorDatabase): PasswordHistoryDao =
        db.passwordHistoryDao()

    @Provides
    fun provideAttachmentDao(db: KeyArmorDatabase): AttachmentDao =
        db.attachmentDao()

    @Provides
    fun provideInAppNotificationDao(db: KeyArmorDatabase): InAppNotificationDao =
        db.inAppNotificationDao()

    @Provides
    fun providePasskeyDao(db: KeyArmorDatabase): PasskeyDao =
        db.passkeyDao()

    @Provides
    fun provideIdentityDocumentDao(db: KeyArmorDatabase): IdentityDocumentDao =
        db.identityDocumentDao()

    @Provides
    fun provideVehicleDao(db: KeyArmorDatabase): VehicleDao =
        db.vehicleDao()

    @Provides
    fun provideVaultSnapshotDao(db: KeyArmorDatabase): VaultSnapshotDao =
        db.vaultSnapshotDao()

    @Provides
    fun provideReminderDao(db: KeyArmorDatabase): ReminderDao =
        db.reminderDao()

    @Provides
    fun provideVaultMetadataDao(db: KeyArmorDatabase): VaultMetadataDao =
        db.vaultMetadataDao()

    @Provides
    fun provideSecureNoteHistoryDao(db: KeyArmorDatabase): SecureNoteHistoryDao =
        db.secureNoteHistoryDao()
}
