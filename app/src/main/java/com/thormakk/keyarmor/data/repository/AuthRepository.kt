package com.thormakk.keyarmor.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.thormakk.keyarmor.data.database.KeyArmorDatabase
import com.thormakk.keyarmor.data.database.dao.AppMetadataDao
import com.thormakk.keyarmor.data.database.dao.BankAccountDao
import com.thormakk.keyarmor.data.database.dao.CreditCardDao
import com.thormakk.keyarmor.data.database.dao.CryptoWalletDao
import com.thormakk.keyarmor.data.database.dao.CustomEntryDao
import com.thormakk.keyarmor.data.database.dao.IdentityDocumentDao
import com.thormakk.keyarmor.data.database.dao.PasskeyDao
import com.thormakk.keyarmor.data.database.dao.PasswordHistoryDao
import com.thormakk.keyarmor.data.database.dao.ReminderDao
import com.thormakk.keyarmor.data.database.dao.SecureNoteDao
import com.thormakk.keyarmor.data.database.dao.SecureNoteHistoryDao
import com.thormakk.keyarmor.data.database.dao.TwoFactorDao
import com.thormakk.keyarmor.data.database.dao.VaultEntryDao
import com.thormakk.keyarmor.data.database.dao.VaultMetadataDao
import com.thormakk.keyarmor.data.database.dao.VehicleDao
import com.thormakk.keyarmor.data.prefs.DrivePrefs
import com.thormakk.keyarmor.domain.model.SnapshotTrigger
import com.thormakk.keyarmor.domain.model.ResultadoMaestra
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.security.CryptoManager
import com.thormakk.keyarmor.security.LoginThrottle
import com.thormakk.keyarmor.security.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val dao:           AppMetadataDao,
    private val crypto:        CryptoManager,
    private val session:       SessionManager,
    private val loginThrottle: LoginThrottle,
    private val database:      KeyArmorDatabase,
    private val vaultDao:         VaultEntryDao,
    private val bankDao:          BankAccountDao,
    private val cardDao:          CreditCardDao,
    private val noteDao:          SecureNoteDao,
    private val customDao:        CustomEntryDao,
    private val twoFactorDao:     TwoFactorDao,
    private val cryptoDao:        CryptoWalletDao,
    private val passkeyDao:       PasskeyDao,
    private val identityDao:      IdentityDocumentDao,
    private val vehicleDao:       VehicleDao,
    private val vaultMetadataDao: VaultMetadataDao,
    private val historyDao:       PasswordHistoryDao,
    private val reminderDao:      ReminderDao,
    private val secureNoteHistoryDao: SecureNoteHistoryDao,
    private val attachmentRepo:   AttachmentRepository,
    private val identityDocumentRepository: IdentityDocumentRepository,
    private val snapshotRepository: SnapshotRepository,
    private val drivePrefs:        DrivePrefs,
    private val claveDeRespaldo:   com.thormakk.keyarmor.data.backup.ClaveDeRespaldo,
    @Suppress("unused") private val trashAutoPurgeRepository: TrashAutoPurgeRepository
) {
    companion object {
        private const val TAG                = "AuthRepository"
        private const val KEY_HASH          = "master_password_hash"
        private const val KEY_SALT          = "master_password_salt"
        private const val KEY_KDF           = "master_password_kdf"
        private const val KDF_ARGON2ID      = "argon2id"
        private const val KEY_ARGON2_MEMORY_KB   = "master_password_argon2_memory_kb"
        private const val KEY_ARGON2_ITERATIONS  = "master_password_argon2_iterations"
        private const val KEY_ARGON2_PARALLELISM = "master_password_argon2_parallelism"
        private const val KEY_EMAIL         = "user_email"
        private const val KEY_FIRST_DONE    = "first_launch_done"
        private const val KEY_LANGUAGE      = "language"
        private const val KEY_BIOMETRIC_KEY  = "biometric_encrypted_key"
        private const val KEY_BIO_SETUP_SEEN = "biometric_setup_seen"
        private const val KEY_THEME           = "selected_theme"
        private const val KEY_ACCENT          = "selected_accent"
        private const val KEY_DRIVE_SYNC      = "drive_last_sync"
    }

    suspend fun isFirstLaunch(): Boolean = dao.get(KEY_FIRST_DONE) != "true"

    suspend fun getLanguage(): String?    = dao.get(KEY_LANGUAGE)
    suspend fun setLanguage(code: String) = dao.set(KEY_LANGUAGE, code)

    suspend fun isRegistered(): Boolean   = dao.get(KEY_HASH) != null

    suspend fun register(name: String, password: String) = withContext(Dispatchers.IO) {
        val salt = crypto.generateSalt()
        val masterSecret = crypto.deriveKeyArgon2id(
            password, salt,
            CryptoManager.ARGON2_DEFAULT_MEMORY_KB,
            CryptoManager.ARGON2_DEFAULT_ITERATIONS,
            CryptoManager.ARGON2_DEFAULT_PARALLELISM
        )
        val hash = crypto.deriveVerificationHash(masterSecret)
        database.withTransaction {
            dao.set(KEY_EMAIL, name)
            dao.set(KEY_HASH, hash)
            dao.set(KEY_SALT, crypto.saltToBase64(salt))
            dao.set(KEY_KDF, KDF_ARGON2ID)
            dao.set(KEY_ARGON2_MEMORY_KB, CryptoManager.ARGON2_DEFAULT_MEMORY_KB.toString())
            dao.set(KEY_ARGON2_ITERATIONS, CryptoManager.ARGON2_DEFAULT_ITERATIONS.toString())
            dao.set(KEY_ARGON2_PARALLELISM, CryptoManager.ARGON2_DEFAULT_PARALLELISM.toString())
            dao.set(KEY_FIRST_DONE, "true")
        }
        session.setSecretKey(crypto.deriveEncryptionKey(masterSecret))
    }

    private suspend fun readArgon2Params(): Triple<Int, Int, Int> {
        val memoryKb    = dao.get(KEY_ARGON2_MEMORY_KB)?.toIntOrNull()
        val iterations  = dao.get(KEY_ARGON2_ITERATIONS)?.toIntOrNull()
        val parallelism = dao.get(KEY_ARGON2_PARALLELISM)?.toIntOrNull()
        if (memoryKb == null || iterations == null || parallelism == null) {
            throw CorruptedAuthStateException()
        }
        return Triple(memoryKb, iterations, parallelism)
    }

    private suspend fun derivarSiCoincide(password: String): SecretKeySpec? = withContext(Dispatchers.IO) {
        val hash    = dao.get(KEY_HASH)  ?: return@withContext null
        val saltB64 = dao.get(KEY_SALT)  ?: return@withContext null
        val salt    = crypto.base64ToSalt(saltB64)

        val (memoryKb, iterations, parallelism) = try {
            readArgon2Params()
        } catch (e: CorruptedAuthStateException) {
            Log.e(TAG, "verifyPassword: parámetros Argon2 corruptos o incompletos, no se puede verificar", e)
            return@withContext null
        }
        val masterSecret = crypto.deriveAndVerifyArgon2idPassword(password, salt, hash, memoryKb, iterations, parallelism)
            ?: return@withContext null
        crypto.deriveEncryptionKey(masterSecret)
    }

    suspend fun verifyPassword(password: String): Boolean =
        verificarMaestra(password) is ResultadoMaestra.Correcta

    suspend fun verificarMaestra(password: String): ResultadoMaestra =
        comprobarConFreno(password, abrirSesion = false)

    suspend fun unlockWithPassword(password: String): ResultadoMaestra =
        comprobarConFreno(password, abrirSesion = true)

    private suspend fun comprobarConFreno(
        password: String,
        abrirSesion: Boolean,
    ): ResultadoMaestra {
        val espera = loginThrottle.remainingWaitMs()
        if (espera > 0L) return ResultadoMaestra.EnEspera(espera)

        val key = derivarSiCoincide(password)
        if (key == null) {
            loginThrottle.recordFailure()
            return ResultadoMaestra.Incorrecta
        }
        loginThrottle.recordSuccess()
        if (abrirSesion) session.setSecretKey(key)
        return ResultadoMaestra.Correcta
    }

    suspend fun getName(): String? = dao.get(KEY_EMAIL)
    suspend fun setName(name: String) = dao.set(KEY_EMAIL, name)

    fun lock() = session.lock()

    suspend fun getBiometricKey(): String?   = dao.get(KEY_BIOMETRIC_KEY)
    suspend fun setBiometricKey(enc: String) = dao.set(KEY_BIOMETRIC_KEY, enc)
    suspend fun clearBiometricKey()          = dao.set(KEY_BIOMETRIC_KEY, "")
    suspend fun isBiometricKeyStored(): Boolean =
        dao.get(KEY_BIOMETRIC_KEY).orEmpty().isNotEmpty()

    suspend fun isBiometricSetupSeen(): Boolean = dao.get(KEY_BIO_SETUP_SEEN) == "true"

    suspend fun markBiometricSetupSeen() = dao.set(KEY_BIO_SETUP_SEEN, "true")

    private val _themeIdFlow = MutableStateFlow("amoled")
    val themeIdFlow: StateFlow<String> = _themeIdFlow

    suspend fun getThemeId(): String {
        val id = dao.get(KEY_THEME) ?: "amoled"
        _themeIdFlow.value = id
        return id
    }

    suspend fun setThemeId(id: String) {
        dao.set(KEY_THEME, id)
        _themeIdFlow.value = id
    }

    private val _accentColorFlow = MutableStateFlow(0L)
    val accentColorFlow: StateFlow<Long> = _accentColorFlow

    suspend fun getAccentColor(): Long {
        val v = dao.get(KEY_ACCENT)?.toLongOrNull() ?: 0L
        _accentColorFlow.value = v
        return v
    }

    suspend fun setAccentColor(argb: Long) {
        dao.set(KEY_ACCENT, argb.toString())
        _accentColorFlow.value = argb
    }

    suspend fun getLastDriveSync(): Long  = dao.get(KEY_DRIVE_SYNC)?.toLongOrNull() ?: 0L
    suspend fun setLastDriveSync(ts: Long) = dao.set(KEY_DRIVE_SYNC, ts.toString())

    fun getMasterKeyBytes(): ByteArray? = session.getSecretKeyBytes()

    fun setSessionKeyFromBytes(bytes: ByteArray) =
        session.setSecretKey(SecretKeySpec(bytes, "AES"))

    suspend fun changeMasterPassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
        if (snapshotRepository.canCreateSnapshots()) {
            try {
                snapshotRepository.createSnapshot(SnapshotTrigger.PRE_MASTER_CHANGE)
            } catch (e: Exception) {
                Log.e(TAG, "changeMasterPassword: falló el snapshot previo. Se ABORTA el cambio " +
                           "para no re-cifrar la bóveda sin red de seguridad.", e)
                throw SnapshotFailedException(e)
            }
        }

        val hash    = dao.get(KEY_HASH)  ?: error("no_hash")
        val saltB64 = dao.get(KEY_SALT)  ?: error("no_salt")
        val oldSalt = crypto.base64ToSalt(saltB64)
        val (memoryKb, iterations, parallelism) = readArgon2Params()
        val masterSecret = crypto.deriveAndVerifyArgon2idPassword(currentPassword, oldSalt, hash, memoryKb, iterations, parallelism)
            ?: throw WrongPasswordException()
        val oldKey = crypto.deriveEncryptionKey(masterSecret)

        val newSalt         = crypto.generateSalt()
        val newMasterSecret = crypto.deriveKeyArgon2id(
            newPassword, newSalt,
            CryptoManager.ARGON2_DEFAULT_MEMORY_KB,
            CryptoManager.ARGON2_DEFAULT_ITERATIONS,
            CryptoManager.ARGON2_DEFAULT_PARALLELISM
        )
        val newKey  = crypto.deriveEncryptionKey(newMasterSecret)
        val newHash = crypto.deriveVerificationHash(newMasterSecret)

        reEncryptVaultAndPersistNewKey(
            oldKey                = oldKey,
            newKey                = newKey,
            newHash               = newHash,
            newSaltB64            = crypto.saltToBase64(newSalt),
            newKdf                = KDF_ARGON2ID,
            newArgon2MemoryKb     = CryptoManager.ARGON2_DEFAULT_MEMORY_KB,
            newArgon2Iterations   = CryptoManager.ARGON2_DEFAULT_ITERATIONS,
            newArgon2Parallelism  = CryptoManager.ARGON2_DEFAULT_PARALLELISM,
            invalidateBiometric   = true
        )
        }
    }

    private suspend fun reEncryptVaultAndPersistNewKey(
        oldKey: SecretKeySpec,
        newKey: SecretKeySpec,
        newHash: String,
        newSaltB64: String,
        newKdf: String,
        newArgon2MemoryKb: Int,
        newArgon2Iterations: Int,
        newArgon2Parallelism: Int,
        invalidateBiometric: Boolean
    ) {
        val reEncryptedPairs = attachmentRepo.prepareReEncrypt(oldKey, newKey)

        val reEncryptedSnapshots = try {
            snapshotRepository.prepareReEncryptSnapshots(oldKey, newKey)
        } catch (e: Exception) {
            attachmentRepo.rollbackNewFiles(reEncryptedPairs.map { it.second })
            throw e
        }

        val reEncryptedPhotos = try {
            identityDocumentRepository.prepareReEncryptPhotos(oldKey, newKey)
        } catch (e: Exception) {
            attachmentRepo.rollbackNewFiles(reEncryptedPairs.map { it.second })
            snapshotRepository.rollbackReEncryptedSnapshots(reEncryptedSnapshots)
            throw e
        }

        try {
            database.withTransaction {
                fun reEnc(value: String): String =
                    if (value.isEmpty()) value
                    else crypto.encrypt(crypto.decrypt(value, oldKey), newKey)

                fun <T> List<T>.mapReEncSafe(
                    tag: String,
                    bovedaDe: ((T) -> String)? = null,
                    block: (T) -> T,
                ): List<T> =
                    filterNot { e -> bovedaDe != null && bovedaDe(e) in VaultMetadata.COERCION_VAULT_IDS }
                        .mapNotNull { e -> runCatching { block(e) }.onFailure {
                            Log.w(TAG, "changeMasterPassword: entrada omitida en $tag (campo ilegible)", it)
                        }.getOrNull() }

                vaultDao.upsertAll(vaultDao.getAllEntities().mapReEncSafe("vaultDao", { it.vaultId }) { e ->
                    val reEncUrls = e.urls.split(",")
                        .filter { it.isNotEmpty() }
                        .joinToString(",") { reEnc(it) }
                    e.copy(
                        title    = reEnc(e.title),
                        username = reEnc(e.username),
                        email    = reEnc(e.email),
                        password = reEnc(e.password),
                        url      = reEnc(e.url),
                        urls     = reEncUrls,
                        notes    = reEnc(e.notes)
                    )
                })

                reminderDao.upsertAll(reminderDao.getAllEntities().mapReEncSafe("reminderDao") { e ->
                    e.copy(
                        name = reEnc(e.name),
                        note = reEnc(e.note)
                    )
                })

                bankDao.upsertAll(bankDao.getAllEntities().mapReEncSafe("bankDao", { it.vaultId }) { e ->
                    e.copy(
                        bankName      = reEnc(e.bankName),
                        swiftBic      = reEnc(e.swiftBic),
                        accountType   = reEnc(e.accountType),
                        accountNumber = reEnc(e.accountNumber),
                        cbuIban       = reEnc(e.cbuIban),
                        alias         = reEnc(e.alias),
                        notes         = reEnc(e.notes)
                    )
                })

                cardDao.upsertAll(cardDao.getAllEntities().mapReEncSafe("cardDao", { it.vaultId }) { e ->
                    e.copy(
                        issuer     = reEnc(e.issuer),
                        brand      = reEnc(e.brand),
                        cardNumber = reEnc(e.cardNumber),
                        cardholder = reEnc(e.cardholder),
                        expiryDate = reEnc(e.expiryDate),
                        cvv        = reEnc(e.cvv),
                        bankPhone  = reEnc(e.bankPhone)
                    )
                })

                noteDao.upsertAll(noteDao.getAllEntities().mapReEncSafe("noteDao", { it.vaultId }) { e ->
                    e.copy(
                        title   = reEnc(e.title),
                        content = reEnc(e.content)
                    )
                })

                customDao.upsertAll(customDao.getAllEntities().mapReEncSafe("customDao", { it.vaultId }) { e ->
                    e.copy(
                        title      = reEnc(e.title),
                        fieldsJson = reEnc(e.fieldsJson)
                    )
                })

                twoFactorDao.upsertAll(twoFactorDao.getAllEntities().mapReEncSafe("twoFactorDao", { it.vaultId }) { e ->
                    e.copy(
                        name              = reEnc(e.name),
                        issuer            = reEnc(e.issuer),
                        accountLabel      = reEnc(e.accountLabel),
                        secret            = reEnc(e.secret),
                        recoveryCodesJson = reEnc(e.recoveryCodesJson),
                        notes             = reEnc(e.notes)
                    )
                })

                cryptoDao.upsertAll(cryptoDao.getAllEntities().mapReEncSafe("cryptoDao", { it.vaultId }) { e ->
                    e.copy(
                        address    = reEnc(e.address),
                        seedPhrase = reEnc(e.seedPhrase),
                        privateKey = reEnc(e.privateKey),
                        notes      = reEnc(e.notes)
                    )
                })

                passkeyDao.upsertAll(passkeyDao.getAllEntities().mapReEncSafe("passkeyDao", { it.vaultId }) { e ->
                    e.copy(
                        rpName              = reEnc(e.rpName),
                        userName            = reEnc(e.userName),
                        userDisplayName     = reEnc(e.userDisplayName),
                        privateKeyEncrypted = reEnc(e.privateKeyEncrypted),
                        notes               = reEnc(e.notes),
                        customAccountLabel  = reEnc(e.customAccountLabel)
                    )
                })

                identityDao.upsertAll(identityDao.getAllEntities().mapReEncSafe("identityDao", { it.vaultId }) { e ->
                    e.copy(
                        docType        = reEnc(e.docType),
                        number         = reEnc(e.number),
                        issuingCountry = reEnc(e.issuingCountry),
                        issueDate      = reEnc(e.issueDate),
                        expiryDate     = reEnc(e.expiryDate),
                        birthDate      = reEnc(e.birthDate),
                        holderName     = reEnc(e.holderName),
                        notes          = reEnc(e.notes)
                    )
                })

                vehicleDao.upsertAll(vehicleDao.getAllEntities().mapReEncSafe("vehicleDao", { it.vaultId }) { e ->
                    e.copy(
                        make                    = reEnc(e.make),
                        model                   = reEnc(e.model),
                        year                    = reEnc(e.year),
                        licensePlate            = reEnc(e.licensePlate),
                        vin                     = reEnc(e.vin),
                        inspectionExpiryDate    = reEnc(e.inspectionExpiryDate),
                        insuranceExpiryDate     = reEnc(e.insuranceExpiryDate),
                        insuranceCompany        = reEnc(e.insuranceCompany),
                        policyNumber            = reEnc(e.policyNumber),
                        driverLicenseExpiryDate = reEnc(e.driverLicenseExpiryDate),
                        notes                   = reEnc(e.notes)
                    )
                })

                vaultMetadataDao.upsertAll(vaultMetadataDao.getAll().mapReEncSafe("vaultMetadataDao") { e ->
                    e.copy(
                        name       = reEnc(e.name),
                        ownerEmail = reEnc(e.ownerEmail),
                        ownerSub   = reEnc(e.ownerSub)
                    )
                })

                historyDao.upsertAll(historyDao.getAllEntities().map { e ->
                    e.copy(encryptedPassword = runCatching { reEnc(e.encryptedPassword) }.getOrDefault(""))
                })

                secureNoteHistoryDao.upsertAll(secureNoteHistoryDao.getAllEntities().mapNotNull { e ->
                    runCatching { e.copy(encryptedContent = reEnc(e.encryptedContent)) }.getOrNull()
                })

                attachmentRepo.commitPathUpdates(reEncryptedPairs.toMap(), oldKey, newKey)

                identityDocumentRepository.commitPhotoPathUpdates(reEncryptedPhotos.toMap())

                dao.set(KEY_HASH, newHash)
                dao.set(KEY_SALT, newSaltB64)
                dao.set(KEY_KDF, newKdf)
                dao.set(KEY_ARGON2_MEMORY_KB, newArgon2MemoryKb.toString())
                dao.set(KEY_ARGON2_ITERATIONS, newArgon2Iterations.toString())
                dao.set(KEY_ARGON2_PARALLELISM, newArgon2Parallelism.toString())

                if (invalidateBiometric) dao.set(KEY_BIOMETRIC_KEY, "")
            }
        } catch (e: Exception) {
            attachmentRepo.rollbackNewFiles(reEncryptedPairs.map { it.second })
            snapshotRepository.rollbackReEncryptedSnapshots(reEncryptedSnapshots)
            identityDocumentRepository.deleteOldPhotoFiles(reEncryptedPhotos.map { it.second })
            throw e
        }

        attachmentRepo.deleteOldFiles(reEncryptedPairs.map { it.first })

        identityDocumentRepository.deleteOldPhotoFiles(reEncryptedPhotos.map { it.first })

        snapshotRepository.commitReEncryptedSnapshots(reEncryptedSnapshots)

        session.setSecretKey(newKey)

        runCatching {
            drivePrefs.rewrapPassword(
                crypto.deriveDriveBackupWrapKey(oldKey),
                crypto.deriveDriveBackupWrapKey(newKey)
            )
        }
        runCatching {
            claveDeRespaldo.rewrap(
                crypto.deriveBackupKeyWrapKey(oldKey),
                crypto.deriveBackupKeyWrapKey(newKey)
            )
        }
    }

    class SnapshotFailedException(cause: Throwable) : Exception(
        "No se pudo crear la copia de seguridad previa al cambio de contraseña", cause
    )

    class WrongPasswordException : Exception("wrong_password")

    class CorruptedAuthStateException : Exception("corrupted_argon2_params")
}
