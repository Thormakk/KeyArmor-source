package com.thormakk.keyarmor.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricCryptoManager @Inject constructor() {

    companion object {
        private const val KEY_ALIAS    = "KeyArmor_BiometricUnlock_v3"
        private const val KEY_ALIAS_V1 = "KeyArmor_BiometricUnlock"
        private const val KEY_ALIAS_V2 = "KeyArmor_BiometricUnlock_v2"
        private const val ANDROID_KS   = "AndroidKeyStore"
        private const val AES_GCM      = "AES/GCM/NoPadding"
        private const val GCM_TAG_BITS = 128
        private const val IV_SIZE      = 12
    }

    private val keystore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KS).also { it.load(null) }
    }

    private fun getOrCreateKey(): SecretKey {
        if (!keystore.containsAlias(KEY_ALIAS)) {
            generateKey()
        }
        return (keystore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    private fun generateKey() {
        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KS)
        keyGen.init(buildKeySpec())
        keyGen.generateKey()
    }

    private fun buildKeySpec(): KeyGenParameterSpec =
        KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
            .also { builder ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    builder.setUserAuthenticationParameters(
                        0,
                        KeyProperties.AUTH_BIOMETRIC_STRONG
                    )
                } else {
                    @Suppress("DEPRECATION")
                    builder.setUserAuthenticationValidityDurationSeconds(-1)
                }
            }
            .build()

    fun getEncryptionCipher(): Cipher =
        Cipher.getInstance(AES_GCM).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        }

    fun getDecryptionCipher(encryptedB64: String): Cipher {
        val decoded = try {
            Base64.decode(encryptedB64, Base64.NO_WRAP)
        } catch (e: IllegalArgumentException) {
            throw CorruptedDataException("invalid_base64", e)
        }
        if (decoded.size < IV_SIZE) {
            throw CorruptedDataException("data_too_short")
        }
        val iv  = decoded.copyOfRange(0, IV_SIZE)
        val key = getOrCreateKey()
        return Cipher.getInstance(AES_GCM).apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        }
    }

    fun encryptMasterKey(authorizedCipher: Cipher, masterKeyBytes: ByteArray): String {
        val ciphertext = authorizedCipher.doFinal(masterKeyBytes)
        val combined   = authorizedCipher.iv + ciphertext
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decryptMasterKey(authorizedCipher: Cipher, encryptedB64: String): ByteArray {
        val combined = try {
            Base64.decode(encryptedB64, Base64.NO_WRAP)
        } catch (e: IllegalArgumentException) {
            throw CorruptedDataException("invalid_base64", e)
        }
        if (combined.size < IV_SIZE) {
            throw CorruptedDataException("data_too_short")
        }
        val ciphertext = combined.copyOfRange(IV_SIZE, combined.size)
        return authorizedCipher.doFinal(ciphertext)
    }

    fun deleteKey() {
        listOf(KEY_ALIAS, KEY_ALIAS_V1, KEY_ALIAS_V2).forEach { alias ->
            if (keystore.containsAlias(alias)) keystore.deleteEntry(alias)
        }
    }
}
