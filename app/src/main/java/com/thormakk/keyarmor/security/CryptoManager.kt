package com.thormakk.keyarmor.security

import android.util.Base64
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.PwHash
import com.sun.jna.NativeLong
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.spec.MGF1ParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters

class CorruptedDataException(message: String, cause: Throwable? = null) : Exception(message, cause)

@Singleton
class CryptoManager @Inject constructor() {

    companion object {
        const val ARGON2_DEFAULT_MEMORY_KB   = 65_536
        const val ARGON2_DEFAULT_ITERATIONS  = 3
        const val ARGON2_DEFAULT_PARALLELISM = 1

        val ARGON2_MEMORY_KB_RANGE    = 1024..262_144
        val ARGON2_ITERATIONS_RANGE   = 1..20
        val ARGON2_PARALLELISM_RANGE  = 1..8

        fun requireValidArgon2Params(memoryKb: Int, iterations: Int, parallelism: Int) {
            require(
                memoryKb    in ARGON2_MEMORY_KB_RANGE &&
                iterations  in ARGON2_ITERATIONS_RANGE &&
                parallelism in ARGON2_PARALLELISM_RANGE
            ) {
                "Parámetros de Argon2id fuera de rango: m=$memoryKb t=$iterations p=$parallelism"
            }
        }
        private const val KEY_LENGTH_BITS   = 256
        private const val SALT_LENGTH_BYTES = 16
        private const val LIBSODIUM_SALT_BYTES = 16
        private const val TAG               = "CryptoManager"
        private const val IV_LENGTH_BYTES   = 12
        private const val GCM_TAG_BITS      = 128
        private const val AES_GCM           = "AES/GCM/NoPadding"
        private const val HMAC_SHA256       = "HmacSHA256"
        private const val RSA_OAEP_CIPHER = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"

        private const val INFO_VERIFICATION = "KeyArmor-PasswordVerification-v1"
        private const val INFO_ENCRYPTION   = "KeyArmor-VaultEncryption-v1"
        private const val INFO_HISTORY_HASH = "KeyArmor-PasswordHistoryHash-v1"
        private const val INFO_DRIVE_BACKUP_WRAP = "KeyArmor-DriveBackupWrap-v1"
        private const val INFO_BACKUP_KEY_WRAP   = "KeyArmor-BackupKeyWrap-v1"
        private const val INFO_KAB_KEY_WRAP      = "KeyArmor-KabKeyWrap-v1"
    }

    fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH_BYTES)
        SecureRandom().nextBytes(salt)
        return salt
    }

    private val lazySodium: LazySodiumAndroid? by lazy {
        runCatching { LazySodiumAndroid(SodiumAndroid()) }
            .getOrNull()
    }

    fun deriveKeyArgon2id(
        password: String,
        salt: ByteArray,
        memoryKb: Int,
        iterations: Int,
        parallelism: Int
    ): SecretKeySpec {
        val keyBytes = deriveRawKeyArgon2id(password, salt, memoryKb, iterations, parallelism)
        val result = SecretKeySpec(keyBytes, "AES")
        keyBytes.fill(0)
        return result
    }

    @VisibleForTesting
    internal fun deriveRawKeyArgon2id(
        password: String,
        salt: ByteArray,
        memoryKb: Int,
        iterations: Int,
        parallelism: Int
    ): ByteArray {
        requireValidArgon2Params(memoryKb, iterations, parallelism)
        if (salt.size == LIBSODIUM_SALT_BYTES && parallelism == 1) {
            val native = runCatching {
                deriveRawKeyArgon2idNative(password, salt, memoryKb, iterations)
            }.getOrElse {
                null
            }
            if (native != null) {
                return native
            }
        }
        return deriveRawKeyArgon2idBouncyCastle(password, salt, memoryKb, iterations, parallelism)
    }

    @VisibleForTesting
    internal fun deriveRawKeyArgon2idNative(
        password: String,
        salt: ByteArray,
        memoryKb: Int,
        iterations: Int
    ): ByteArray {
        val ls = lazySodium ?: throw IllegalStateException("Lazysodium no disponible")
        val out = ByteArray(KEY_LENGTH_BITS / 8)
        val pwd = password.toByteArray(Charsets.UTF_8)
        val ok = ls.cryptoPwHash(
            out,
            out.size,
            pwd,
            pwd.size,
            salt,
            iterations.toLong(),
            NativeLong(memoryKb.toLong() * 1024L),
            PwHash.Alg.PWHASH_ALG_ARGON2ID13
        )
        pwd.fill(0)
        if (!ok) throw IllegalStateException("cryptoPwHash devolvió false (parámetros fuera de rango)")
        return out
    }

    @VisibleForTesting
    internal fun deriveRawKeyArgon2idBouncyCastle(
        password: String,
        salt: ByteArray,
        memoryKb: Int,
        iterations: Int,
        parallelism: Int
    ): ByteArray {
        val params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withIterations(iterations)
            .withMemoryAsKB(memoryKb)
            .withParallelism(parallelism)
            .withSalt(salt)
            .build()

        val generator = Argon2BytesGenerator()
        generator.init(params)
        val keyBytes = ByteArray(KEY_LENGTH_BITS / 8)
        generator.generateBytes(password.toCharArray(), keyBytes)
        return keyBytes
    }

    private fun expandSubKey(masterSecret: SecretKeySpec, info: String): ByteArray {
        val mac = Mac.getInstance(HMAC_SHA256)
        mac.init(masterSecret)
        mac.update(info.toByteArray(Charsets.UTF_8))
        mac.update(0x01.toByte())
        return mac.doFinal()
    }

    fun deriveEncryptionKey(masterSecret: SecretKeySpec): SecretKeySpec =
        SecretKeySpec(expandSubKey(masterSecret, INFO_ENCRYPTION), "AES")

    fun deriveVerificationHash(masterSecret: SecretKeySpec): String =
        Base64.encodeToString(expandSubKey(masterSecret, INFO_VERIFICATION), Base64.NO_WRAP)

    fun deriveDriveBackupWrapKey(encryptionKey: SecretKeySpec): SecretKeySpec =
        SecretKeySpec(expandSubKey(encryptionKey, INFO_DRIVE_BACKUP_WRAP), "AES")

    fun deriveBackupKeyWrapKey(encryptionKey: SecretKeySpec): SecretKeySpec =
        SecretKeySpec(expandSubKey(encryptionKey, INFO_BACKUP_KEY_WRAP), "AES")

    fun deriveKabWrapKey(claveRespaldo: ByteArray): SecretKeySpec =
        SecretKeySpec(expandSubKey(SecretKeySpec(claveRespaldo, HMAC_SHA256), INFO_KAB_KEY_WRAP), "AES")

    fun generateKabFileKey(): SecretKeySpec {
        val raw = ByteArray(32).also { SecureRandom().nextBytes(it) }
        return SecretKeySpec(raw, "AES")
    }

    fun wrapKabFileKey(fileKey: SecretKeySpec, wrapKey: SecretKeySpec): String =
        encrypt(Base64.encodeToString(fileKey.encoded, Base64.NO_WRAP), wrapKey)

    fun unwrapKabFileKey(wrapped: String, wrapKey: SecretKeySpec): SecretKeySpec =
        SecretKeySpec(Base64.decode(decrypt(wrapped, wrapKey), Base64.NO_WRAP), "AES")

    fun historyHash(password: String, encryptionKey: SecretKeySpec): String {
        val subKey = SecretKeySpec(expandSubKey(encryptionKey, INFO_HISTORY_HASH), HMAC_SHA256)
        val mac = Mac.getInstance(HMAC_SHA256)
        mac.init(subKey)
        return mac.doFinal(password.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }

    fun deriveAndVerifyArgon2idPassword(
        password: String,
        salt: ByteArray,
        storedHash: String,
        memoryKb: Int,
        iterations: Int,
        parallelism: Int
    ): SecretKeySpec? {
        val masterSecret = deriveKeyArgon2id(password, salt, memoryKb, iterations, parallelism)
        val matches = MessageDigest.isEqual(
            deriveVerificationHash(masterSecret).toByteArray(Charsets.UTF_8),
            storedHash.toByteArray(Charsets.UTF_8)
        )
        return if (matches) masterSecret else null
    }

    fun encrypt(plaintext: String, key: SecretKeySpec): String {
        val iv = ByteArray(IV_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(AES_GCM)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        val combined = iv + cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decrypt(encryptedBase64: String, key: SecretKeySpec): String {
        val combined = try {
            Base64.decode(encryptedBase64, Base64.NO_WRAP)
        } catch (e: IllegalArgumentException) {
            throw CorruptedDataException("invalid_base64", e)
        }
        if (combined.size < IV_LENGTH_BYTES) {
            throw CorruptedDataException("data_too_short")
        }
        val iv         = combined.copyOfRange(0, IV_LENGTH_BYTES)
        val ciphertext = combined.copyOfRange(IV_LENGTH_BYTES, combined.size)
        val cipher     = Cipher.getInstance(AES_GCM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    fun encryptBytes(plaintext: ByteArray, key: SecretKeySpec): ByteArray {
        val iv = ByteArray(IV_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(AES_GCM)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        return iv + cipher.doFinal(plaintext)
    }

    fun decryptBytes(combined: ByteArray, key: SecretKeySpec): ByteArray {
        if (combined.size < IV_LENGTH_BYTES) throw CorruptedDataException("data_too_short")
        val iv         = combined.copyOfRange(0, IV_LENGTH_BYTES)
        val ciphertext = combined.copyOfRange(IV_LENGTH_BYTES, combined.size)
        val cipher     = Cipher.getInstance(AES_GCM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(ciphertext)
    }

    fun saltToBase64(salt: ByteArray): String  = Base64.encodeToString(salt, Base64.NO_WRAP)
    fun base64ToSalt(base64: String): ByteArray = Base64.decode(base64, Base64.NO_WRAP)

    fun encryptWithRsaPublicKey(plaintext: String, publicKeyBase64: String): String {
        val keyBytes  = Base64.decode(publicKeyBase64, Base64.NO_WRAP)
        val keySpec   = X509EncodedKeySpec(keyBytes)
        val publicKey = KeyFactory.getInstance("RSA").generatePublic(keySpec)

        val oaepParams = OAEPParameterSpec(
            "SHA-256",
            "MGF1",
            MGF1ParameterSpec.SHA1,
            PSource.PSpecified.DEFAULT
        )
        val cipher = Cipher.getInstance(RSA_OAEP_CIPHER)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams)
        return Base64.encodeToString(
            cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8)),
            Base64.NO_WRAP
        )
    }
}
