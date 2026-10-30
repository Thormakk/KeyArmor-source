package com.thormakk.keyarmor.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

class CoercionPepperUnavailableException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)

@Singleton
class CoercionPepperManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("coercion_pepper_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ALIAS     = "KeyArmor_CoercionPepper_v1"
        private const val ANDROID_KS    = "AndroidKeyStore"
        private const val AES_GCM       = "AES/GCM/NoPadding"
        private const val GCM_TAG_BITS  = 128
        private const val IV_SIZE       = 12
        private const val PEPPER_BYTES  = 32
        private const val KEY_WRAPPED   = "wrapped_pepper"

        private const val SEPARATOR = ":"
    }

    private val keystore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KS).also { it.load(null) }
    }

    private fun getOrCreateKey(): SecretKey {
        if (!keystore.containsAlias(KEY_ALIAS)) {
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KS).apply {
                init(
                    KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build()
                )
                generateKey()
            }
        }
        return (keystore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    fun hasPepper(): Boolean = prefs.contains(KEY_WRAPPED)

    fun getOrCreatePepper(): ByteArray {
        readPepper()?.let { return it }
        return try {
            val pepper = ByteArray(PEPPER_BYTES).also { SecureRandom().nextBytes(it) }
            val cipher = Cipher.getInstance(AES_GCM).apply {
                init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            }
            val wrapped = cipher.iv + cipher.doFinal(pepper)
            prefs.edit { putString(KEY_WRAPPED, Base64.encodeToString(wrapped, Base64.NO_WRAP)) }
            pepper
        } catch (e: Exception) {
            throw CoercionPepperUnavailableException("no se pudo crear el pepper de coacción", e)
        }
    }

    fun readPepper(): ByteArray? {
        val b64 = prefs.getString(KEY_WRAPPED, null) ?: return null
        return runCatching {
            val wrapped = Base64.decode(b64, Base64.NO_WRAP)
            if (wrapped.size <= IV_SIZE) return null
            val iv = wrapped.copyOfRange(0, IV_SIZE)
            val ct = wrapped.copyOfRange(IV_SIZE, wrapped.size)
            Cipher.getInstance(AES_GCM).apply {
                init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
            }.doFinal(ct)
        }.getOrNull()
    }

    fun pepperedOrNull(pin: String): String? {
        val pepper = readPepper() ?: return null
        val encoded = Base64.encodeToString(pepper, Base64.NO_WRAP)
        pepper.fill(0)
        return pin + SEPARATOR + encoded
    }

    fun pepperedOrCreate(pin: String): String {
        val pepper = getOrCreatePepper()
        val encoded = Base64.encodeToString(pepper, Base64.NO_WRAP)
        pepper.fill(0)
        return pin + SEPARATOR + encoded
    }

    fun clear() {
        prefs.edit { remove(KEY_WRAPPED) }
        runCatching { if (keystore.containsAlias(KEY_ALIAS)) keystore.deleteEntry(KEY_ALIAS) }
    }
}
