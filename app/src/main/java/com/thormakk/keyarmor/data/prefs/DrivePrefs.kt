package com.thormakk.keyarmor.data.prefs

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import com.thormakk.keyarmor.security.CryptoManager
import com.thormakk.keyarmor.security.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DrivePrefs @Inject constructor(
    @ApplicationContext private val context: Context,
    private val session: SessionManager,
    private val crypto:  CryptoManager,
) {
    companion object {
        private const val KEY_ALIAS            = "KeyArmor_DriveBackup_v1"
        private const val ANDROID_KS           = "AndroidKeyStore"
        private const val AES_GCM              = "AES/GCM/NoPadding"
        private const val GCM_TAG_BITS         = 128
        private const val IV_SIZE              = 12
        private const val PREFS_NAME           = "drive_prefs"
        private const val KEY_ENCRYPTED        = "backup_password_encrypted"
        private const val KEY_ENCRYPTED_V2     = "backup_password_encrypted_v2"
        private const val KEY_DRIVE_EMAIL       = "connected_google_email"
        private const val KEY_DUAL_LOCAL_BACKUP = "dual_local_backup_enabled"
        private const val KEY_BACKUP_PENDING    = "backup_pending"
        private const val KEY_BACKUP_WAITING_WIFI = "backup_waiting_wifi"
        private const val KEY_DRIVE_FULL = "drive_storage_full"
        private const val KEY_OWNER_GOOGLE_SUB  = "owner_google_sub"
        private const val KEY_MANUAL_BACKUP_DONE = "manual_backup_completed"
        private const val KEY_DISCONNECTED_BY_PANIC_PIN = "disconnected_by_panic_pin"
    }

    sealed class LoadResult {
        data class Success(val password: String) : LoadResult()
        object NotConfigured : LoadResult()
        object KeyInvalidated : LoadResult()
        object Locked : LoadResult()
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val keystore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KS).also { it.load(null) }
    }

    private fun getOrCreateKey(): SecretKey {
        if (!keystore.containsAlias(KEY_ALIAS)) {
            val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KS)
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGen.init(spec)
            keyGen.generateKey()
        }
        return (keystore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    private fun currentWrapKey(): SecretKey? =
        session.getSecretKey()?.let { crypto.deriveDriveBackupWrapKey(it) }

    fun savePassword(plaintext: String) {
        val wrapKey = currentWrapKey() ?: return
        savePassword(plaintext, wrapKey)
    }

    fun loadPassword(): LoadResult {
        val wrapKey = currentWrapKey() ?: return LoadResult.Locked
        return loadPassword(wrapKey)
    }

    fun savePassword(plaintext: String, wrapKey: SecretKey) {
        val cipher     = Cipher.getInstance(AES_GCM).apply { init(Cipher.ENCRYPT_MODE, wrapKey) }
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val combined   = cipher.iv + ciphertext
        prefs.edit { putString(KEY_ENCRYPTED_V2, Base64.encodeToString(combined, Base64.NO_WRAP)) }
    }

    fun loadPassword(wrapKey: SecretKey): LoadResult {
        migrateToMasterWrap(wrapKey)
        val encoded = prefs.getString(KEY_ENCRYPTED_V2, null) ?: return LoadResult.NotConfigured
        return try {
            val combined   = Base64.decode(encoded, Base64.NO_WRAP)
            val iv         = combined.copyOfRange(0, IV_SIZE)
            val ciphertext = combined.copyOfRange(IV_SIZE, combined.size)
            val cipher     = Cipher.getInstance(AES_GCM).apply {
                init(Cipher.DECRYPT_MODE, wrapKey, GCMParameterSpec(GCM_TAG_BITS, iv))
            }
            LoadResult.Success(String(cipher.doFinal(ciphertext), Charsets.UTF_8))
        } catch (_: Exception) {
            LoadResult.KeyInvalidated
        }
    }

    fun migrateToMasterWrap(wrapKey: SecretKey) {
        if (prefs.contains(KEY_ENCRYPTED_V2)) return
        val legacy = prefs.getString(KEY_ENCRYPTED, null) ?: return
        val plaintext = try {
            val combined   = Base64.decode(legacy, Base64.NO_WRAP)
            val iv         = combined.copyOfRange(0, IV_SIZE)
            val ciphertext = combined.copyOfRange(IV_SIZE, combined.size)
            val cipher     = Cipher.getInstance(AES_GCM).apply {
                init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
            }
            String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        } catch (_: Exception) {
            prefs.edit { remove(KEY_ENCRYPTED) }
            runCatching { if (keystore.containsAlias(KEY_ALIAS)) keystore.deleteEntry(KEY_ALIAS) }
            return
        }
        savePassword(plaintext, wrapKey)
        prefs.edit { remove(KEY_ENCRYPTED) }
        runCatching { if (keystore.containsAlias(KEY_ALIAS)) keystore.deleteEntry(KEY_ALIAS) }
    }

    fun rewrapPassword(oldWrapKey: SecretKey, newWrapKey: SecretKey) {
        prefs.getString(KEY_ENCRYPTED_V2, null)?.let { v2 ->
            val plaintext = runCatching { decryptWith(v2, oldWrapKey) }.getOrNull() ?: return
            savePassword(plaintext, newWrapKey)
            return
        }
        val legacy = prefs.getString(KEY_ENCRYPTED, null) ?: return
        val plaintext = runCatching { decryptWith(legacy, getOrCreateKey()) }.getOrNull() ?: return
        savePassword(plaintext, newWrapKey)
        prefs.edit { remove(KEY_ENCRYPTED) }
        runCatching { if (keystore.containsAlias(KEY_ALIAS)) keystore.deleteEntry(KEY_ALIAS) }
    }

    private fun decryptWith(encoded: String, key: SecretKey): String {
        val combined   = Base64.decode(encoded, Base64.NO_WRAP)
        val iv         = combined.copyOfRange(0, IV_SIZE)
        val ciphertext = combined.copyOfRange(IV_SIZE, combined.size)
        val cipher     = Cipher.getInstance(AES_GCM).apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        }
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    fun hasPassword(): Boolean = prefs.contains(KEY_ENCRYPTED_V2) || prefs.contains(KEY_ENCRYPTED)

    fun saveConnectedEmail(email: String) =
        prefs.edit {
            putString(KEY_DRIVE_EMAIL, email)
            remove(KEY_DISCONNECTED_BY_PANIC_PIN)
        }

    fun getConnectedEmail(): String? = prefs.getString(KEY_DRIVE_EMAIL, null)

    fun hasConnectedEmail(): Boolean = prefs.contains(KEY_DRIVE_EMAIL)

    fun saveOwnerGoogleSub(sub: String) =
        prefs.edit { putString(KEY_OWNER_GOOGLE_SUB, sub) }

    fun isDualLocalBackupEnabled(): Boolean =
        prefs.getBoolean(KEY_DUAL_LOCAL_BACKUP, false)

    fun setDualLocalBackupEnabled(enabled: Boolean) =
        prefs.edit { putBoolean(KEY_DUAL_LOCAL_BACKUP, enabled) }

    fun isBackupPending(): Boolean = prefs.getBoolean(KEY_BACKUP_PENDING, false)

    fun setBackupPending(pending: Boolean) =
        prefs.edit { putBoolean(KEY_BACKUP_PENDING, pending) }

    fun isBackupWaitingWifi(): Boolean = prefs.getBoolean(KEY_BACKUP_WAITING_WIFI, false)

    fun setBackupWaitingWifi(waiting: Boolean) =
        prefs.edit { putBoolean(KEY_BACKUP_WAITING_WIFI, waiting) }

    fun isDriveFull(): Boolean = prefs.getBoolean(KEY_DRIVE_FULL, false)

    fun setDriveFull(full: Boolean) =
        prefs.edit { putBoolean(KEY_DRIVE_FULL, full) }

    fun hasCompletedManualBackup(): Boolean = prefs.getBoolean(KEY_MANUAL_BACKUP_DONE, false)

    fun markManualBackupCompleted() =
        prefs.edit { putBoolean(KEY_MANUAL_BACKUP_DONE, true) }

    fun clear() {
        prefs.edit {
            remove(KEY_ENCRYPTED)
            remove(KEY_DRIVE_EMAIL)
            remove(KEY_OWNER_GOOGLE_SUB)
            remove(KEY_MANUAL_BACKUP_DONE)
        }
        if (keystore.containsAlias(KEY_ALIAS)) keystore.deleteEntry(KEY_ALIAS)
        cerrarIdentidadGoogle()
    }

    private fun cerrarIdentidadGoogle() {
        runCatching { com.google.firebase.auth.FirebaseAuth.getInstance().signOut() }
        runCatching { com.google.firebase.messaging.FirebaseMessaging.getInstance().deleteToken() }
    }

    fun markDisconnectedByPanicPin() =
        prefs.edit { putBoolean(KEY_DISCONNECTED_BY_PANIC_PIN, true) }

    fun consumeDisconnectedByPanicPin(): Boolean {
        val value = prefs.getBoolean(KEY_DISCONNECTED_BY_PANIC_PIN, false)
        if (value) prefs.edit { remove(KEY_DISCONNECTED_BY_PANIC_PIN) }
        return value
    }
}
