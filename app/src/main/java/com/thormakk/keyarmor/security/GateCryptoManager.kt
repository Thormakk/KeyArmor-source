package com.thormakk.keyarmor.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GateCryptoManager @Inject constructor() {

    companion object {
        private const val KEY_ALIAS  = "KeyArmor_MandatoryGate_v1"
        private const val ANDROID_KS = "AndroidKeyStore"
        private const val AES_GCM    = "AES/GCM/NoPadding"
        private val PROOF_BYTES = byteArrayOf(0)
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
            KeyProperties.PURPOSE_ENCRYPT
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

    fun getProofCipher(): Cipher =
        Cipher.getInstance(AES_GCM).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        }

    fun verifyProof(authorizedCipher: Cipher): Boolean =
        try {
            authorizedCipher.doFinal(PROOF_BYTES)
            true
        } catch (e: Exception) {
            false
        }

    fun deleteKey() {
        if (keystore.containsAlias(KEY_ALIAS)) keystore.deleteEntry(KEY_ALIAS)
    }
}
