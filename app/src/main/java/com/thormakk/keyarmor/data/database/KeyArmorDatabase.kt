package com.thormakk.keyarmor.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
import com.thormakk.keyarmor.data.database.entity.AppMetadataEntity
import com.thormakk.keyarmor.data.database.entity.AttachmentEntity
import com.thormakk.keyarmor.data.database.entity.BankAccountEntity
import com.thormakk.keyarmor.data.database.entity.CreditCardEntity
import com.thormakk.keyarmor.data.database.entity.CryptoWalletEntity
import com.thormakk.keyarmor.data.database.entity.CustomEntryEntity
import com.thormakk.keyarmor.data.database.entity.IdentityDocumentEntity
import com.thormakk.keyarmor.data.database.entity.InAppNotificationEntity
import com.thormakk.keyarmor.data.database.entity.PasskeyEntity
import com.thormakk.keyarmor.data.database.entity.PasswordHistoryEntity
import com.thormakk.keyarmor.data.database.entity.ReminderEntity
import com.thormakk.keyarmor.data.database.entity.SecureNoteEntity
import com.thormakk.keyarmor.data.database.entity.SecureNoteHistoryEntity
import com.thormakk.keyarmor.data.database.entity.TagEntity
import com.thormakk.keyarmor.data.database.entity.TwoFactorEntity
import com.thormakk.keyarmor.data.database.entity.VaultEntryEntity
import com.thormakk.keyarmor.data.database.entity.VaultMetadataEntity
import com.thormakk.keyarmor.data.database.entity.VaultSnapshotEntity
import com.thormakk.keyarmor.data.database.entity.VehicleEntity

@Database(
    entities = [
        AppMetadataEntity::class,
        VaultEntryEntity::class,
        BankAccountEntity::class,
        CustomEntryEntity::class,
        SecureNoteEntity::class,
        CreditCardEntity::class,
        TwoFactorEntity::class,
        CryptoWalletEntity::class,
        TagEntity::class,
        PasswordHistoryEntity::class,
        AttachmentEntity::class,
        InAppNotificationEntity::class,
        PasskeyEntity::class,
        IdentityDocumentEntity::class,
        VehicleEntity::class,
        VaultSnapshotEntity::class,
        ReminderEntity::class,
        VaultMetadataEntity::class,
        SecureNoteHistoryEntity::class
    ],
    version = 31,
    exportSchema = true
)
abstract class KeyArmorDatabase : RoomDatabase() {
    abstract fun appMetadataDao(): AppMetadataDao
    abstract fun vaultEntryDao(): VaultEntryDao
    abstract fun bankAccountDao(): BankAccountDao
    abstract fun customEntryDao(): CustomEntryDao
    abstract fun secureNoteDao(): SecureNoteDao
    abstract fun creditCardDao(): CreditCardDao
    abstract fun twoFactorDao(): TwoFactorDao
    abstract fun cryptoWalletDao(): CryptoWalletDao
    abstract fun tagDao(): TagDao
    abstract fun passwordHistoryDao(): PasswordHistoryDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun inAppNotificationDao(): InAppNotificationDao
    abstract fun passkeyDao(): PasskeyDao
    abstract fun identityDocumentDao(): IdentityDocumentDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun vaultSnapshotDao(): VaultSnapshotDao
    abstract fun reminderDao(): ReminderDao
    abstract fun vaultMetadataDao(): VaultMetadataDao
    abstract fun secureNoteHistoryDao(): SecureNoteHistoryDao

    companion object {

        val ALL_MIGRATIONS: Array<Migration> get() = arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9,
            MIGRATION_9_10,
            MIGRATION_10_11,
            MIGRATION_11_12,
            MIGRATION_12_13,
            MIGRATION_13_14,
            MIGRATION_14_15,
            MIGRATION_15_16,
            MIGRATION_16_17,
            MIGRATION_17_18,
            MIGRATION_18_19,
            MIGRATION_19_20,
            MIGRATION_20_21,
            MIGRATION_21_22,
            MIGRATION_22_23,
            MIGRATION_23_24,
            MIGRATION_24_25,
            MIGRATION_25_26,
            MIGRATION_26_27,
            MIGRATION_27_28,
            MIGRATION_28_29,
            MIGRATION_29_30,
            MIGRATION_30_31
        )

        val MIGRATION_30_31 = object : Migration(30, 31) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `vault_snapshots` ADD COLUMN `includes_attachments` INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_29_30 = object : Migration(29, 30) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `bank_accounts` ADD COLUMN `is_protected` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `credit_cards` ADD COLUMN `is_protected` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `crypto_wallets` ADD COLUMN `is_protected` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `custom_entries` ADD COLUMN `is_protected` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `identity_documents` ADD COLUMN `is_protected` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `passkeys` ADD COLUMN `is_protected` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `secure_notes` ADD COLUMN `is_protected` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `two_factor_accounts` ADD COLUMN `is_protected` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `vehicles` ADD COLUMN `is_protected` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `reminders` ADD COLUMN `is_protected` INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_28_29 = object : Migration(28, 29) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `vault_entries` ADD COLUMN `is_protected` INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_27_28 = object : Migration(27, 28) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `vault_entries` ADD COLUMN `field_label` TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `tags` ADD COLUMN `defaultKey` TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        val MIGRATION_25_26 = object : Migration(25, 26) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `in_app_notifications` ADD COLUMN `is_encrypted` INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(db: SupportSQLiteDatabase) {
                listOf(
                    "vault_entries",
                    "bank_accounts",
                    "credit_cards",
                    "crypto_wallets",
                    "custom_entries",
                    "identity_documents",
                    "secure_notes",
                    "two_factor_accounts",
                    "vehicles"
                ).forEach { tabla ->
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `idx_${tabla}_vault_active` ON `$tabla` " +
                        "(`vault_id`, `isDeleted`, `isArchived`, `updatedAt`)"
                    )
                }
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `idx_passkeys_vault_active` ON `passkeys` " +
                    "(`vault_id`, `is_deleted`, `is_archived`, `created_at`)"
                )
            }
        }

        val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "DELETE FROM `vault_metadata` WHERE `isFamilyVault` = 1 OR `isFamilyMirror` = 1 OR `id` LIKE 'family_mirror_%'"
                )
                db.execSQL(
                    "CREATE TABLE `vault_metadata_new` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `isInherited` INTEGER NOT NULL, `isPrivate` INTEGER NOT NULL, `ownerEmail` TEXT NOT NULL, `ownerSub` TEXT NOT NULL, `sortOrder` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "INSERT INTO `vault_metadata_new` (`id`, `name`, `isInherited`, `isPrivate`, `ownerEmail`, `ownerSub`, `sortOrder`, `createdAt`) " +
                    "SELECT `id`, `name`, `isInherited`, `isPrivate`, `ownerEmail`, `ownerSub`, `sortOrder`, `createdAt` FROM `vault_metadata`"
                )
                db.execSQL("DROP TABLE `vault_metadata`")
                db.execSQL("ALTER TABLE `vault_metadata_new` RENAME TO `vault_metadata`")

                val destino =
                    "COALESCE(" +
                        "(SELECT `id` FROM `vault_metadata` WHERE `id` = 'main'), " +
                        "(SELECT `id` FROM `vault_metadata` ORDER BY `sortOrder` ASC, `createdAt` ASC LIMIT 1)" +
                    ")"
                listOf(
                    "vault_entries", "bank_accounts", "credit_cards", "crypto_wallets",
                    "custom_entries", "identity_documents", "secure_notes",
                    "two_factor_accounts", "vehicles", "passkeys",
                ).forEach { tabla ->
                    db.execSQL(
                        "UPDATE `$tabla` SET `vault_id` = $destino " +
                            "WHERE `vault_id` NOT IN (SELECT `id` FROM `vault_metadata`) " +
                            "AND EXISTS (SELECT 1 FROM `vault_metadata`)"
                    )
                }
            }
        }

        val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `vault_entries`        ADD COLUMN `isHidden` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `bank_accounts`        ADD COLUMN `isHidden` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `credit_cards`         ADD COLUMN `isHidden` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `identity_documents`   ADD COLUMN `isHidden` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `vehicles`             ADD COLUMN `isHidden` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `secure_notes`         ADD COLUMN `isHidden` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `two_factor_accounts`  ADD COLUMN `isHidden` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `custom_entries`       ADD COLUMN `isHidden` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `crypto_wallets`       ADD COLUMN `isHidden` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `passkeys`             ADD COLUMN `is_hidden` INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `vault_metadata` ADD COLUMN `isFamilyVault`  INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `vault_metadata` ADD COLUMN `isFamilyMirror` INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `secure_note_history` (
                        `id`                INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        `note_id`           TEXT    NOT NULL,
                        `encrypted_content` TEXT    NOT NULL,
                        `saved_at`          INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `idx_note_history_note_id` ON `secure_note_history` (`note_id`)"
                )
                db.execSQL(
                    "ALTER TABLE `secure_notes` ADD COLUMN `history_enabled` INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `identity_documents` ADD COLUMN `birthDate` TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `identity_documents` ADD COLUMN `front_photo_path` TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE `identity_documents` ADD COLUMN `back_photo_path` TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `reminders` ADD COLUMN `category` TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `in_app_notifications` ADD COLUMN `is_archived` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `in_app_notifications` ADD COLUMN `archived_at` INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `vault_entries` ADD COLUMN `password_changed_at` INTEGER"
                )
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `two_factor_accounts` ADD COLUMN `url` TEXT NOT NULL DEFAULT \'\'"
                )
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `in_app_notifications` ADD COLUMN `target_id` TEXT DEFAULT NULL"
                )
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `reminders` ADD COLUMN `is_pinned` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `reminders` ADD COLUMN `is_favorite` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `reminders` ADD COLUMN `is_archived` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `reminders` ADD COLUMN `tags` TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `reminders` ADD COLUMN `alert_schedules` TEXT DEFAULT NULL"
                )
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                listOf(
                    "vault_entries", "bank_accounts", "credit_cards", "crypto_wallets",
                    "custom_entries", "identity_documents", "passkeys", "secure_notes",
                    "two_factor_accounts", "vehicles"
                ).forEach { table ->
                    db.execSQL(
                        "ALTER TABLE `$table` ADD COLUMN `inherited_from` TEXT DEFAULT NULL"
                    )
                }
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `vault_metadata` ADD COLUMN `isPrivate` INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "INSERT OR IGNORE INTO vault_metadata (id, name, isInherited, isPrivate, ownerEmail, ownerSub, sortOrder, createdAt) " +
                    "VALUES ('private', 'Privado', 0, 1, '', '', ${Int.MAX_VALUE}, ${System.currentTimeMillis()})"
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "UPDATE vault_metadata SET name = 'Panel principal' WHERE id = 'main' AND name = 'Mi Vault'"
                )
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """UPDATE vault_metadata
                       SET name = 'Panel de ' || name
                       WHERE id != 'main'
                         AND id NOT LIKE 'inherited_%'
                         AND name NOT LIKE 'Panel de %'
                         AND name NOT LIKE 'Panel principal%'"""
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `vault_metadata` (
                        `id`          TEXT    NOT NULL,
                        `name`        TEXT    NOT NULL DEFAULT 'Panel principal',
                        `isInherited` INTEGER NOT NULL DEFAULT 0,
                        `ownerEmail`  TEXT    NOT NULL DEFAULT '',
                        `ownerSub`    TEXT    NOT NULL DEFAULT '',
                        `sortOrder`   INTEGER NOT NULL DEFAULT 0,
                        `createdAt`   INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                db.execSQL(
                    "INSERT OR IGNORE INTO vault_metadata (id, name, isInherited, ownerEmail, ownerSub, sortOrder, createdAt) VALUES ('main', 'Panel principal', 0, '', '', 0, ${System.currentTimeMillis()})"
                )

                listOf(
                    "vault_entries", "bank_accounts", "credit_cards", "crypto_wallets",
                    "custom_entries", "identity_documents", "passkeys", "secure_notes",
                    "two_factor_accounts", "vehicles"
                ).forEach { table ->
                    db.execSQL(
                        "ALTER TABLE `$table` ADD COLUMN `vault_id` TEXT NOT NULL DEFAULT 'main'"
                    )
                }
            }
        }
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `reminders` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL DEFAULT '',
                        `date` INTEGER NOT NULL DEFAULT 0,
                        `note` TEXT NOT NULL DEFAULT '',
                        `created_at` INTEGER NOT NULL DEFAULT 0,
                        `is_trashed` INTEGER NOT NULL DEFAULT 0,
                        `trashed_at` INTEGER,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `vault_snapshots` (
                        `id` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `trigger` TEXT NOT NULL,
                        `entry_count` INTEGER NOT NULL,
                        `file_path` TEXT NOT NULL,
                        `file_size_bytes` INTEGER NOT NULL,
                        `synced_to_drive` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `vault_entries` ADD COLUMN `urls` TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `passkeys` ADD COLUMN `is_archived` INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE `passkeys` ADD COLUMN `archived_at` INTEGER"
                )
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `identity_documents` (
                        `id` TEXT NOT NULL, `docType` TEXT NOT NULL DEFAULT '',
                        `number` TEXT NOT NULL DEFAULT '', `issuingCountry` TEXT NOT NULL DEFAULT '',
                        `issueDate` TEXT NOT NULL DEFAULT '', `expiryDate` TEXT NOT NULL DEFAULT '',
                        `holderName` TEXT NOT NULL DEFAULT '', `notes` TEXT NOT NULL DEFAULT '',
                        `expiryTimestamp` INTEGER, `expiryAlertDays` INTEGER NOT NULL DEFAULT 90,
                        `category` TEXT NOT NULL DEFAULT 'SIN_CATEGORIZAR', `tags` TEXT NOT NULL DEFAULT '',
                        `isFavorite` INTEGER NOT NULL DEFAULT 0, `isTravelSafe` INTEGER NOT NULL DEFAULT 0,
                        `isPinned` INTEGER NOT NULL DEFAULT 0, `createdAt` INTEGER NOT NULL DEFAULT 0,
                        `updatedAt` INTEGER NOT NULL DEFAULT 0, `isDeleted` INTEGER NOT NULL DEFAULT 0,
                        `deletedAt` INTEGER, `isArchived` INTEGER NOT NULL DEFAULT 0, `archivedAt` INTEGER,
                        PRIMARY KEY(`id`)
                    )
                """)
                db.execSQL(
                    "ALTER TABLE `crypto_wallets` ADD COLUMN `manualBalance` TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `vehicles` (
                        `id` TEXT NOT NULL, `make` TEXT NOT NULL DEFAULT '',
                        `model` TEXT NOT NULL DEFAULT '', `year` TEXT NOT NULL DEFAULT '',
                        `licensePlate` TEXT NOT NULL DEFAULT '', `vin` TEXT NOT NULL DEFAULT '',
                        `inspectionExpiryDate` TEXT NOT NULL DEFAULT '', `insuranceExpiryDate` TEXT NOT NULL DEFAULT '',
                      `insuranceCompany` TEXT NOT NULL DEFAULT '',
                        `policyNumber` TEXT NOT NULL DEFAULT '',
                        `driverLicenseExpiryDate` TEXT NOT NULL DEFAULT '',
                        `notes` TEXT NOT NULL DEFAULT '',
                        `inspectionExpiryTimestamp` INTEGER,
                        `inspectionAlertDays` INTEGER NOT NULL DEFAULT 30,
                        `insuranceExpiryTimestamp` INTEGER,
                        `insuranceAlertDays` INTEGER NOT NULL DEFAULT 30,
                        `driverLicenseExpiryTimestamp` INTEGER,
                        `driverLicenseAlertDays` INTEGER NOT NULL DEFAULT 30,
                        `category` TEXT NOT NULL DEFAULT 'SIN_CATEGORIZAR',
                        `tags` TEXT NOT NULL DEFAULT '',
                        `isFavorite` INTEGER NOT NULL DEFAULT 0,
                        `isTravelSafe` INTEGER NOT NULL DEFAULT 0,
                        `isPinned` INTEGER NOT NULL DEFAULT 0,
                        `createdAt` INTEGER NOT NULL DEFAULT 0,
                        `updatedAt` INTEGER NOT NULL DEFAULT 0,
                        `isDeleted` INTEGER NOT NULL DEFAULT 0,
                        `deletedAt` INTEGER,
                        `isArchived` INTEGER NOT NULL DEFAULT 0,
                        `archivedAt` INTEGER,
                        PRIMARY KEY(`id`)
                    )
                """)
            }
        }
    }
}
