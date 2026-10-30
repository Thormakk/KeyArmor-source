package com.thormakk.keyarmor.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import android.util.Base64
import androidx.annotation.RequiresApi
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.spec.MGF1ParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InheritanceKeyManager @Inject constructor() {

    companion object {
        private const val KEYSTORE_PROVIDER  = "AndroidKeyStore"
        private const val KEY_ALIAS          = "KeyArmor_Inheritance_RSA"
        private const val CIPHER_ALGORITHM   = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
    }

    private val keyStore: KeyStore =
        KeyStore.getInstance(KEYSTORE_PROVIDER).also { it.load(null) }

    fun getOrCreatePublicKeyBase64(): String {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            generateKeyPair()
        }
        val publicKey = keyStore.getCertificate(KEY_ALIAS).publicKey
        return Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
    }

    fun decryptHeirSub(encryptedBase64: String): String {
        val privateKey = keyStore.getKey(KEY_ALIAS, null) as? PrivateKey
            ?: error("No existe la clave privada RSA de Herencia Digital en este dispositivo (Keystore vacío o alias distinto) — probablemente por una reinstalación o borrado de datos desde que se generó. Hay que eliminar y volver a invitar al heredero.")
        val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
        val oaepParams = OAEPParameterSpec(
            "SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT
        )
        cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams)
        val decrypted = cipher.doFinal(Base64.decode(encryptedBase64, Base64.NO_WRAP))
        return String(decrypted, Charsets.UTF_8)
    }

    private fun generateKeyPair() {
        val generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEYSTORE_PROVIDER)
        val strongBoxOk = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && tryGenerateStrongBoxKeyPair(generator)
        if (!strongBoxOk) {
            generator.initialize(buildKeySpec(strongBox = false))
            generator.generateKeyPair()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun tryGenerateStrongBoxKeyPair(generator: KeyPairGenerator): Boolean = try {
        generator.initialize(buildKeySpec(strongBox = true))
        generator.generateKeyPair()
        true
    } catch (e: StrongBoxUnavailableException) {
        false
    }

    private fun buildKeySpec(strongBox: Boolean): KeyGenParameterSpec =
        KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_DECRYPT
        )
            .setKeySize(2048)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA1)
            .also {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (strongBox) it.setIsStrongBoxBacked(true)
                }
            }
            .build()
}
