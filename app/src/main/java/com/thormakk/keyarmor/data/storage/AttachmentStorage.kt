package com.thormakk.keyarmor.data.storage

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.InputStream
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttachmentStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024
        const val MAX_FILES_PER_ENTRY = 5

        private const val IV_BYTES    = 12
        private const val GCM_TAG_BITS = 128
        private const val ALGORITHM   = "AES/GCM/NoPadding"
    }

    private val attachmentsDir: File
        get() = File(context.filesDir, "attachments").also { it.mkdirs() }

    private val tempDir: File
        get() = File(context.cacheDir, "att").also { it.mkdirs() }

    fun saveEncrypted(inputStream: InputStream, key: SecretKeySpec): String {
        val plainBytes = inputStream.readBytes()
        val iv         = ByteArray(IV_BYTES).also { SecureRandom().nextBytes(it) }
        val cipher     = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        val cipherBytes = cipher.doFinal(plainBytes)

        val destFile = File(attachmentsDir, "${UUID.randomUUID()}.enc")
        destFile.outputStream().use { out ->
            out.write(iv)
            out.write(cipherBytes)
        }
        return destFile.absolutePath
    }

    fun decryptToTemp(storedPath: String, fileName: String, key: SecretKeySpec): File {
        val encBytes   = File(storedPath).readBytes()
        val iv         = encBytes.copyOfRange(0, IV_BYTES)
        val cipherText = encBytes.copyOfRange(IV_BYTES, encBytes.size)

        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        val plainBytes = cipher.doFinal(cipherText)

        val safeName = File(fileName.replace('\\', '/')).name.ifBlank { "attachment" }
        val tempFile = File(tempDir, "att_${System.currentTimeMillis()}_$safeName")
        tempFile.writeBytes(plainBytes)
        return tempFile
    }

    fun decryptToBytes(storedPath: String, key: SecretKeySpec): ByteArray {
        val encBytes   = File(storedPath).readBytes()
        val iv         = encBytes.copyOfRange(0, IV_BYTES)
        val cipherText = encBytes.copyOfRange(IV_BYTES, encBytes.size)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(cipherText)
    }

    fun delete(storedPath: String) {
        File(storedPath).delete()
    }

    fun listarArchivosGuardados(): List<File> =
        attachmentsDir.listFiles()?.filter { it.isFile && it.extension == "enc" } ?: emptyList()

    fun readRawEncrypted(storedPath: String): ByteArray = File(storedPath).readBytes()

    fun saveRawEncrypted(encryptedBytes: ByteArray): String {
        val destFile = File(attachmentsDir, "${UUID.randomUUID()}.enc")
        destFile.writeBytes(encryptedBytes)
        return destFile.absolutePath
    }

    fun clearTempFiles() {
        tempDir.listFiles()?.forEach { it.delete() }
        context.cacheDir
            .listFiles { f -> f.name.startsWith("att_") }
            ?.forEach { it.delete() }
    }
}
