@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
package com.thormakk.keyarmor.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.database.dao.AttachmentDao
import com.thormakk.keyarmor.data.database.dao.IdentityDocumentDao
import com.thormakk.keyarmor.data.database.entity.AttachmentEntity
import com.thormakk.keyarmor.data.export.AttachmentBackupEntry
import com.thormakk.keyarmor.data.storage.AttachmentStorage
import com.thormakk.keyarmor.domain.model.Attachment
import com.thormakk.keyarmor.security.CryptoManager
import com.thormakk.keyarmor.security.SessionManager
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.io.ByteArrayInputStream
import java.io.File
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttachmentRepository @Inject constructor(
    private val dao:     AttachmentDao,
    private val identityDocumentDao: IdentityDocumentDao,
    private val storage: AttachmentStorage,
    private val crypto:  CryptoManager,
    private val session: SessionManager,
    @ApplicationContext private val appContextInyectado: Context
) {

    private val appContext: Context get() = contextoDelIdioma(appContextInyectado)
    companion object {
    }

    private fun encField(value: String, key: SecretKeySpec): String =
        if (value.isEmpty()) "" else crypto.encrypt(value, key)

    private fun decField(value: String, key: SecretKeySpec): String =
        if (value.isEmpty()) "" else runCatching { crypto.decrypt(value, key) }.getOrDefault(value)

    fun observeEntryIdsWithAttachments(): Flow<Set<String>> =
        dao.observeEntryIdsWithAttachments().map { it.toSet() }

    fun observeAttachments(entryId: String): Flow<List<Attachment>> =
        session.isUnlockedFlow.flatMapLatest { unlocked ->
            val key = if (unlocked) session.getSecretKey() else null
            if (key == null) flowOf(emptyList())
            else dao.observeForEntry(entryId).map { list -> list.map { it.toDomain(key) } }
        }

    suspend fun countForEntry(entryId: String): Int =
        dao.countForEntry(entryId)

    suspend fun getAllForBackup(): List<AttachmentEntity> = dao.getAll()

    fun readRawEncryptedBytes(storedPath: String): ByteArray = storage.readRawEncrypted(storedPath)

    suspend fun restoreFromSnapshot(entity: AttachmentEntity, encryptedBytes: ByteArray) {
        if (entity.fileSize > AttachmentStorage.MAX_FILE_SIZE_BYTES) {
            Log.w("AttachmentRepository", "restoreFromSnapshot: adjunto ${entity.id} omitido (${entity.fileSize} bytes > tope)")
            return
        }
        val newPath = storage.saveRawEncrypted(encryptedBytes)
        if (!dao.insertIfUnderLimit(entity.copy(storedPath = newPath), AttachmentStorage.MAX_FILES_PER_ENTRY)) {
            storage.delete(newPath)
            Log.w("AttachmentRepository", "restoreFromSnapshot: adjunto ${entity.id} omitido (entrada ${entity.entryId} ya en el tope)")
        }
    }

    suspend fun getAllDecryptedForBackup(): List<AttachmentBackupEntry> {
        val key = session.getSecretKey() ?: return emptyList()
        return dao.getAll().mapNotNull { entity ->
            runCatching {
                val fileName = decField(entity.fileName, key)
                val mimeType = decField(entity.mimeType, key)
                val content  = storage.decryptToBytes(entity.storedPath, key)
                AttachmentBackupEntry(
                    id        = entity.id,
                    entryId   = entity.entryId,
                    fileName  = fileName,
                    mimeType  = mimeType,
                    fileSize  = entity.fileSize,
                    createdAt = entity.createdAt,
                    content   = content
                )
            }.onFailure {
                Log.w("AttachmentRepository", "getAllDecryptedForBackup: adjunto ${entity.id} omitido (archivo ilegible)", it)
            }.getOrNull()
        }
    }

    suspend fun getAttachmentBackupSources(): List<AttachmentBackupSource> {
        val key = session.getSecretKey() ?: return emptyList()
        return dao.getAll().map { entity ->
            AttachmentBackupSource(
                id        = entity.id,
                entryId   = entity.entryId,
                fileName  = runCatching { decField(entity.fileName, key) }.getOrDefault(""),
                mimeType  = runCatching { decField(entity.mimeType, key) }.getOrDefault("application/octet-stream"),
                fileSize  = entity.fileSize,
                createdAt = entity.createdAt,
                plainContent = { runCatching { storage.decryptToBytes(entity.storedPath, key) }.getOrNull() },
            )
        }
    }

    suspend fun restoreDecryptedAttachment(
        id:             String,
        entryId:        String,
        plainFileName:  String,
        plainMimeType:  String,
        fileSize:       Long,
        createdAt:      Long,
        plainBytes:     ByteArray
    ): Boolean {
        if (plainBytes.size > AttachmentStorage.MAX_FILE_SIZE_BYTES) {
            Log.w("AttachmentRepository", "restoreDecryptedAttachment: adjunto $id omitido (${plainBytes.size} bytes > tope)")
            return false
        }
        val key = session.getSecretKey() ?: return false
        val path = ByteArrayInputStream(plainBytes).use { storage.saveEncrypted(it, key) }
        val inserted = dao.insertIfUnderLimit(
            AttachmentEntity(
                id         = id,
                entryId    = entryId,
                fileName   = encField(plainFileName, key),
                mimeType   = encField(plainMimeType, key),
                fileSize   = fileSize,
                storedPath = path,
                createdAt  = createdAt
            ),
            AttachmentStorage.MAX_FILES_PER_ENTRY
        )
        if (!inserted) {
            storage.delete(path)
            Log.w("AttachmentRepository", "restoreDecryptedAttachment: adjunto $id omitido (entrada $entryId ya en el tope)")
        }
        return inserted
    }

    suspend fun addAttachment(
        entryId:  String,
        resolver: ContentResolver,
        uri:      Uri
    ): Result<Attachment> {
        val fileName = queryFileName(resolver, uri)
        val fileSize = queryFileSize(resolver, uri)
        val mimeType = resolver.getType(uri) ?: "application/octet-stream"

        if (fileSize > AttachmentStorage.MAX_FILE_SIZE_BYTES)
            return Result.failure(AttachmentError.FileTooLarge())

        val key = session.getSecretKey()
            ?: return Result.failure(AttachmentError.NoSession())

        return runCatching {
            val inputStream = resolver.openInputStream(uri)
                ?: throw IllegalStateException("Cannot open URI")
            val path = inputStream.use { storage.saveEncrypted(it, key) }
            val entity = AttachmentEntity(
                entryId    = entryId,
                fileName   = encField(fileName, key),
                mimeType   = encField(mimeType, key),
                fileSize   = fileSize,
                storedPath = path
            )
            if (!dao.insertIfUnderLimit(entity, AttachmentStorage.MAX_FILES_PER_ENTRY)) {
                storage.delete(path)
                throw AttachmentError.MaxFilesReached()
            }
            Attachment(
                id         = entity.id,
                entryId    = entryId,
                fileName   = fileName,
                mimeType   = mimeType,
                fileSize   = fileSize,
                storedPath = path,
                createdAt  = entity.createdAt
            )
        }.mapFailure { if (it is AttachmentError) it else AttachmentError.Generic(it) }
    }

    suspend fun deleteAttachment(attachment: Attachment) {
        dao.deleteById(attachment.id)
        storage.delete(attachment.storedPath)
    }

    suspend fun deleteAllForEntry(entryId: String) {
        val paths = dao.getForEntry(entryId).map { it.storedPath }
        dao.deleteAllForEntry(entryId)
        paths.forEach { storage.delete(it) }
    }

    fun openAttachment(attachment: Attachment): Result<File> {
        val key = session.getSecretKey()
            ?: return Result.failure(AttachmentError.NoSession())
        return runCatching {
            storage.decryptToTemp(attachment.storedPath, attachment.fileName, key)
        }.mapFailure { AttachmentError.Generic(it) }
    }

    suspend fun deleteAllPermanently() {
        val paths = dao.getAll().map { it.storedPath }
        dao.deleteAll()
        paths.forEach { storage.delete(it) }
    }

    suspend fun purgarArchivosHuerfanos(minimoDeAntiguedadMs: Long = 5 * 60 * 1000L): Int {
        val referenciados = HashSet<String>()
        dao.getAll().forEach { if (it.storedPath.isNotBlank()) referenciados += it.storedPath }
        identityDocumentDao.getAllEntities().forEach { doc ->
            doc.frontPhotoPath?.takeIf { it.isNotBlank() }?.let { referenciados += it }
            doc.backPhotoPath ?.takeIf { it.isNotBlank() }?.let { referenciados += it }
        }

        val corte = System.currentTimeMillis() - minimoDeAntiguedadMs
        var borrados = 0
        storage.listarArchivosGuardados().forEach { archivo ->
            if (archivo.absolutePath in referenciados) return@forEach
            if (minimoDeAntiguedadMs > 0 && archivo.lastModified() > corte) return@forEach
            if (archivo.delete()) borrados++
        }
        return borrados
    }

    suspend fun prepareReEncrypt(
        oldKey: javax.crypto.spec.SecretKeySpec,
        newKey: javax.crypto.spec.SecretKeySpec
    ): List<Pair<String, String>> {
        val all     = dao.getAll()
        val created = mutableListOf<String>()
        val pairs   = mutableListOf<Pair<String, String>>()
        return try {
            all.forEach { entity ->
                try {
                    val plainFileName = decField(entity.fileName, oldKey)
                    val tempFile = storage.decryptToTemp(entity.storedPath, plainFileName, oldKey)
                    val newPath  = tempFile.inputStream().use { storage.saveEncrypted(it, newKey) }
                    tempFile.delete()
                    created.add(newPath)
                    pairs.add(entity.storedPath to newPath)
                } catch (e: Exception) {
                    Log.w("AttachmentRepository", "prepareReEncrypt: adjunto ${entity.id} omitido (archivo ilegible)", e)
                }
            }
            pairs
        } catch (e: Exception) {
            created.forEach { runCatching { storage.delete(it) } }
            throw e
        }
    }

    suspend fun commitPathUpdates(
        pathMap: Map<String, String>,
        oldKey: javax.crypto.spec.SecretKeySpec,
        newKey: javax.crypto.spec.SecretKeySpec,
    ) {
        val all = dao.getAll()
        for (entity in all) {
            val newPath = pathMap[entity.storedPath] ?: continue
            dao.insert(
                entity.copy(
                    storedPath = newPath,
                    fileName   = encField(decField(entity.fileName, oldKey), newKey),
                    mimeType   = encField(decField(entity.mimeType, oldKey), newKey),
                )
            )
        }
    }

    suspend fun deleteOldFiles(oldPaths: List<String>) {
        oldPaths.forEach { runCatching { storage.delete(it) } }
    }

    suspend fun rollbackNewFiles(newPaths: List<String>) {
        newPaths.forEach { runCatching { storage.delete(it) } }
    }

    private fun queryFileName(resolver: ContentResolver, uri: Uri): String {
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val col = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && col >= 0) return cursor.getString(col)
        }
        return uri.lastPathSegment ?: appContext.getString(R.string.attachment_default_name)
    }

    private fun queryFileSize(resolver: ContentResolver, uri: Uri): Long {
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val col = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst() && col >= 0) return cursor.getLong(col)
        }
        return 0L
    }

    private fun AttachmentEntity.toDomain(key: SecretKeySpec) = Attachment(
        id          = id,
        entryId     = entryId,
        fileName    = decField(fileName, key),
        mimeType    = decField(mimeType, key),
        fileSize    = fileSize,
        storedPath  = storedPath,
        createdAt   = createdAt
    )

    private inline fun <T> Result<T>.mapFailure(transform: (Throwable) -> Throwable): Result<T> =
        exceptionOrNull()?.let { Result.failure(transform(it)) } ?: this
}

data class AttachmentBackupSource(
    val id:           String,
    val entryId:      String,
    val fileName:     String,
    val mimeType:     String,
    val fileSize:     Long,
    val createdAt:    Long,
    val plainContent: () -> ByteArray?,
    val kind:         String = "FILE",
)

sealed class AttachmentError(message: String) : Exception(message) {
    class FileTooLarge   : AttachmentError("file_too_large")
    class MaxFilesReached: AttachmentError("max_files_reached")
    class NoSession      : AttachmentError("no_session")
    class Generic(rootCause: Throwable) : AttachmentError(rootCause.message ?: "generic_error")
}
