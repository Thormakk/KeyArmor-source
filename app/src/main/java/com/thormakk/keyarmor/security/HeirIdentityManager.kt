package com.thormakk.keyarmor.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Base64
import com.thormakk.keyarmor.data.prefs.InheritancePrefs
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeirIdentityManager @Inject constructor(
    private val envelope: InheritanceEnvelope,
    private val prefs:     InheritancePrefs,
) {
    private val keystore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KS).also { it.load(null) }
    }

    fun hasIdentity(): Boolean =
        prefs.getHeirIdentityPublic() != null && getPrivateCode() != null

    fun getPublicKey(): String? = prefs.getHeirIdentityPublic()

    fun getOrCreateIdentity(): InheritanceEnvelope.HeirKeyPairCodes {
        prefs.getHeirIdentityPublic()?.let { pub ->
            getPrivateCode()?.let { priv ->
                return InheritanceEnvelope.HeirKeyPairCodes(publicKeyB64 = pub, privateKeyCode = priv)
            }
        }
        val codes = envelope.generateHeirKeyPair()
        prefs.saveHeirIdentity(
            publicKeyB64        = codes.publicKeyB64,
            encryptedPrivateB64 = encryptAtRest(codes.privateKeyCode),
        )
        return codes
    }

    fun getPrivateCode(): String? {
        val blob = prefs.getHeirIdentityPrivateEnc() ?: return null
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

    fun getIdentityForBackup(): Pair<String, String>? {
        val pub  = prefs.getHeirIdentityPublic() ?: return null
        val priv = getPrivateCode() ?: return null
        return pub to priv
    }

    fun importIdentity(publicKeyB64: String, privateKeyCode: String): Boolean {
        if (hasIdentity()) return false
        prefs.saveHeirIdentity(
            publicKeyB64        = publicKeyB64,
            encryptedPrivateB64 = encryptAtRest(privateKeyCode),
        )
        return true
    }

    fun clear() {
        prefs.clearHeirIdentity()
        if (keystore.containsAlias(KEY_ALIAS)) keystore.deleteEntry(KEY_ALIAS)
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
        private const val KEY_ALIAS    = "KeyArmor_HeirIdentity_v1"
        private const val AES_GCM      = "AES/GCM/NoPadding"
        private const val GCM_TAG_BITS = 128
        private const val IV_SIZE      = 12
    }
}
