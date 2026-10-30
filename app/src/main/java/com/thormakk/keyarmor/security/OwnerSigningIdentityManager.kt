package com.thormakk.keyarmor.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Base64
import com.thormakk.keyarmor.data.prefs.DrivePrefs
import com.thormakk.keyarmor.data.prefs.InheritancePrefs
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

class SigningKeyLostException : IllegalStateException(
    "La clave de firma del titular no está disponible en este dispositivo"
)

@Singleton
class OwnerSigningIdentityManager @Inject constructor(
    private val prefs: InheritancePrefs,
    private val drivePrefs: DrivePrefs,
) {
    private val keystore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KS).also { it.load(null) }
    }

    fun getOrCreatePublicKeyB64(): String {
        val existingPub = prefs.getOwnerSigningPublic()
        if (existingPub != null && prefs.getOwnerSigningPrivateEnc() != null) {
            if (loadPrivateKey() != null) return existingPub
            if (!prefs.isOwnerSigningProvisional()) return existingPub
        }
        return generarIdentidad(provisional = true)
    }

    private fun generarIdentidad(provisional: Boolean): String {
        val kpg = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec(CURVE)) }
        val kp  = kpg.generateKeyPair()
        val pubB64  = Base64.encodeToString(kp.public.encoded,  Base64.NO_WRAP)
        val privB64 = Base64.encodeToString(kp.private.encoded, Base64.NO_WRAP)
        prefs.saveOwnerSigningIdentity(pubB64, encryptAtRest(privB64), provisional = provisional)
        return pubB64
    }

    fun markConfirmed() = prefs.confirmOwnerSigningIdentity()

    fun getPublicKeyB64(): String? = prefs.getOwnerSigningPublic()

    fun sign(data: ByteArray): ByteArray {
        val priv = loadPrivateKey() ?: throw SigningKeyLostException()
        val sig  = Signature.getInstance("SHA256withECDSA")
        sig.initSign(priv)
        sig.update(data)
        return sig.sign()
    }

    fun getForBackup(): Pair<String, String>? {
        val pub  = prefs.getOwnerSigningPublic() ?: return null
        val priv = loadPrivatePkcs8B64() ?: return null
        return pub to priv
    }

    fun importFromBackup(publicKeyB64: String, privatePkcs8B64: String, ownerEmailDelBackup: String? = null): Boolean {
        val hayIdentidad = prefs.getOwnerSigningPublic() != null &&
                           prefs.getOwnerSigningPrivateEnc() != null
        if (hayIdentidad) {
            val sePuedePisar = prefs.isOwnerSigningProvisional() || loadPrivateKey() == null
            if (!sePuedePisar) return false
        }
        if (ownerEmailDelBackup != null &&
            !ownerEmailDelBackup.equals(drivePrefs.getConnectedEmail(), ignoreCase = true)) {
            return false
        }
        prefs.saveOwnerSigningIdentity(publicKeyB64, encryptAtRest(privatePkcs8B64), provisional = false)
        return true
    }

    fun clear() {
        prefs.clearOwnerSigningIdentity()
        if (keystore.containsAlias(KEY_ALIAS)) keystore.deleteEntry(KEY_ALIAS)
    }

    private fun loadPrivateKey(): PrivateKey? {
        val pkcs8 = loadPrivatePkcs8B64() ?: return null
        return try {
            KeyFactory.getInstance("EC")
                .generatePrivate(PKCS8EncodedKeySpec(Base64.decode(pkcs8, Base64.NO_WRAP)))
        } catch (_: Exception) { null }
    }

    private fun loadPrivatePkcs8B64(): String? {
        val blob = prefs.getOwnerSigningPrivateEnc() ?: return null
        return try {
            val combined = Base64.decode(blob, Base64.NO_WRAP)
            val iv       = combined.copyOfRange(0, IV_SIZE)
            val ct       = combined.copyOfRange(IV_SIZE, combined.size)
            val cipher   = Cipher.getInstance(AES_GCM).apply {
                init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
            }
            String(cipher.doFinal(ct), Charsets.UTF_8)
        } catch (_: KeyPermanentlyInvalidatedException) {
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun encryptAtRest(plaintext: String): String {
        val cipher = Cipher.getInstance(AES_GCM).apply { init(Cipher.ENCRYPT_MODE, getOrCreateKey()) }
        val ct     = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cipher.iv + ct, Base64.NO_WRAP)
    }

    private fun getOrCreateKey(): SecretKey {
        if (!keystore.containsAlias(KEY_ALIAS)) {
            val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KS)
            keyGen.init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
            keyGen.generateKey()
        }
        return (keystore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    companion object {
        private const val ANDROID_KS   = "AndroidKeyStore"
        private const val KEY_ALIAS    = "KeyArmor_OwnerSigning_v1"
        private const val CURVE        = "secp256r1"
        private const val AES_GCM      = "AES/GCM/NoPadding"
        private const val GCM_TAG_BITS = 128
        private const val IV_SIZE      = 12
    }
}
