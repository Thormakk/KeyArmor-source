package com.thormakk.keyarmor.data.repository

import android.util.Log
import com.thormakk.keyarmor.data.database.dao.SecureNoteHistoryDao
import com.thormakk.keyarmor.data.database.entity.SecureNoteHistoryEntity
import com.thormakk.keyarmor.data.repository.SecureNoteHistoryRepository.Companion.MAX_HISTORY_COUNT
import com.thormakk.keyarmor.domain.model.SecureNoteHistoryItem
import com.thormakk.keyarmor.security.CryptoManager
import com.thormakk.keyarmor.security.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG_DECRYPT = "SecureNoteHistoryRepository"

@Singleton
class SecureNoteHistoryRepository @Inject constructor(
    private val dao:     SecureNoteHistoryDao,
    private val crypto:  CryptoManager,
    private val session: SessionManager
) {
    companion object {
        const val MAX_HISTORY_COUNT = 3
    }

    suspend fun recordVersion(noteId: String, plainContent: String) {
        val key = session.getSecretKey() ?: return
        val encrypted = if (plainContent.isNotEmpty()) crypto.encrypt(plainContent, key) else ""
        dao.insert(
            SecureNoteHistoryEntity(
                noteId           = noteId,
                encryptedContent = encrypted,
                savedAt          = System.currentTimeMillis()
            )
        )
        dao.trimHistory(noteId, MAX_HISTORY_COUNT)
    }

    suspend fun getHistory(noteId: String): List<SecureNoteHistoryItem> {
        val key = session.getSecretKey() ?: return emptyList()
        return dao.getHistoryForNote(noteId).mapNotNull { entity ->
            try {
                val content = if (entity.encryptedContent.isNotEmpty())
                    crypto.decrypt(entity.encryptedContent, key)
                else ""
                SecureNoteHistoryItem(
                    id      = entity.id,
                    noteId  = entity.noteId,
                    content = content,
                    savedAt = entity.savedAt
                )
            } catch (e: Exception) {
                Log.e(TAG_DECRYPT, "decrypt falló para historial id=${entity.id}", e)
                null
            }
        }
    }

    suspend fun deleteItems(ids: Set<Int>)     = dao.deleteByIds(ids.toList())
    suspend fun deleteForNote(noteId: String) = dao.deleteForNote(noteId)
    suspend fun deleteAll()                   = dao.deleteAll()

    suspend fun getAllForBackup(): List<SecureNoteHistoryEntity> = dao.getAllEntities()
    suspend fun upsertAll(entities: List<SecureNoteHistoryEntity>) = dao.upsertAll(entities)

    suspend fun getAllDecryptedForBackup(): List<SecureNoteHistoryItem> {
        val key = session.getSecretKey() ?: return emptyList()
        return dao.getAllEntities().mapNotNull { entity ->
            runCatching {
                SecureNoteHistoryItem(
                    id      = entity.id,
                    noteId  = entity.noteId,
                    content = if (entity.encryptedContent.isNotEmpty())
                        crypto.decrypt(entity.encryptedContent, key) else "",
                    savedAt = entity.savedAt
                )
            }.onFailure {
                Log.e(TAG_DECRYPT, "getAllDecryptedForBackup: historial id=${entity.id} " +
                    "ilegible, queda FUERA del backup", it)
            }.getOrNull()
        }
    }

    suspend fun restoreFromBackup(items: List<SecureNoteHistoryItem>) {
        if (items.isEmpty()) return
        val key = session.getSecretKey() ?: return
        dao.upsertAll(
            items.map { item ->
                SecureNoteHistoryEntity(
                    noteId           = item.noteId,
                    encryptedContent = if (item.content.isNotEmpty())
                        crypto.encrypt(item.content, key) else "",
                    savedAt          = item.savedAt
                )
            }
        )
    }
}
