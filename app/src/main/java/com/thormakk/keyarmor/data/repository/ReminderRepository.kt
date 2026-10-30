@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
package com.thormakk.keyarmor.data.repository

import android.content.Context
import android.util.Log
import com.thormakk.keyarmor.alarms.ReminderAlarmScheduler
import com.thormakk.keyarmor.data.database.dao.ReminderDao
import com.thormakk.keyarmor.data.database.entity.ReminderEntity
import com.thormakk.keyarmor.di.DefaultDispatcher
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.Reminder
import com.thormakk.keyarmor.domain.model.ReminderSchedule
import com.thormakk.keyarmor.security.CryptoManager
import com.thormakk.keyarmor.security.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG_DECRYPT = "ReminderRepository"

@Singleton
class ReminderRepository @Inject constructor(
    private val dao:       ReminderDao,
    private val crypto:    CryptoManager,
    private val session:   SessionManager,
    private val scheduler: ReminderAlarmScheduler,
    private val attachmentRepository: AttachmentRepository,
    @ApplicationContext private val context: Context,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    fun observeActive(): Flow<List<Reminder>> =
        session.isUnlockedFlow.flatMapLatest { unlocked ->
            val key = if (unlocked) session.getSecretKey() else null
            if (key == null) flowOf(emptyList())
            else dao.observeActive().map { list -> list.mapNotNull { decrypt(it, key) } }
        }
            .flowOn(defaultDispatcher)

    fun observeTrashed(): Flow<List<Reminder>> =
        session.isUnlockedFlow.flatMapLatest { unlocked ->
            val key = if (unlocked) session.getSecretKey() else null
            if (key == null) flowOf(emptyList())
            else dao.observeTrash().map { list -> list.mapNotNull { decrypt(it, key) } }
        }
            .flowOn(defaultDispatcher)

    suspend fun save(reminder: Reminder): Boolean {
        val key = session.getSecretKey() ?: return false
        if (isProtected(reminder.id)) return false

        val existing = dao.getById(reminder.id)?.let { decrypt(it, key) }
        if (existing != null) scheduler.cancelAll(context, existing)

        dao.upsert(encrypt(reminder, key))

        if (reminder.schedules.isNotEmpty()) {
            scheduler.scheduleAll(context, reminder)
        }
        return true
    }

    suspend fun isProtected(id: String): Boolean = dao.getProtectedIds(listOf(id)).isNotEmpty()
    suspend fun setProtected(id: String, isProtected: Boolean) = dao.setProtected(id, isProtected)
    suspend fun protectedAmong(ids: List<String>): Set<String> =
        ids.chunked(500).flatMap { dao.getProtectedIds(it) }.toSet()

    suspend fun getById(id: String): Reminder? {
        val key = session.getSecretKey() ?: return null
        return dao.getById(id)?.let { decrypt(it, key) }
    }

    suspend fun restore(id: String) {
        dao.restoreFromTrash(id)
        val key = session.getSecretKey() ?: return
        val reminder = dao.getById(id)?.let { decrypt(it, key) } ?: return
        if (reminder.schedules.isNotEmpty()) {
            scheduler.scheduleAll(context, reminder)
        }
    }

    suspend fun softDelete(id: String): Boolean {
        if (isProtected(id)) return false
        val key = session.getSecretKey()
        if (key != null) {
            dao.getById(id)?.let { decrypt(it, key) }?.let { scheduler.cancelAll(context, it) }
        }
        dao.softDelete(id)
        return true
    }

    suspend fun softDeleteBatch(allIds: List<String>): Int {
        val protegidas = protectedAmong(allIds)
        val ids = allIds.filterNot { it in protegidas }
        val key = session.getSecretKey()
        if (key != null) {
            ids.forEach { id ->
                dao.getById(id)?.let { decrypt(it, key) }?.let { scheduler.cancelAll(context, it) }
            }
        }
        if (ids.isNotEmpty()) dao.softDeleteBatch(ids)
        return protegidas.size
    }

    suspend fun permanentlyDelete(id: String): Boolean {
        if (isProtected(id)) return false
        val key = session.getSecretKey()
        if (key != null) {
            dao.getById(id)?.let { decrypt(it, key) }?.let { scheduler.cancelAll(context, it) }
        }
        dao.permanentlyDelete(id)
        attachmentRepository.deleteAllForEntry(id)
        return true
    }

    suspend fun getExpiringIds(now: Long, warnMs: Long): List<String> =
        dao.getExpiringReminderInfos()
            .filter { it.date in now..(now + warnMs) }
            .map { it.id }

    suspend fun getIdsWithCustomSchedules(): Set<String> =
        dao.getActiveWithSchedules().map { it.id }.toSet()

    suspend fun getExpiringReminderPairs(now: Long, warnMs: Long): List<Pair<String, String>> {
        val key = session.getSecretKey() ?: return emptyList()
        return dao.getExpiringReminderInfos()
            .filter { it.date in now..(now + warnMs) }
            .mapNotNull { info ->
                val entity = dao.getById(info.id) ?: return@mapNotNull null
                val name = runCatching { crypto.decrypt(entity.name, key) }
                    .onFailure { Log.e(TAG_DECRYPT, "getExpiringReminderPairs: recordatorio " +
                        "id=${entity.id} ilegible, no se avisa su vencimiento", it) }
                    .getOrNull()
                    ?: return@mapNotNull null
                info.id to name
            }
    }

    suspend fun getAllDecryptedForBackup(): List<Reminder> {
        val key = session.getSecretKey() ?: return emptyList()
        val entidades = dao.getAllEntities()
        return withContext(defaultDispatcher) {
            entidades.mapNotNull { entity ->
                val r = decrypt(entity, key)
                if (r == null) {
                    Log.e(TAG_DECRYPT, "getAllDecryptedForBackup: recordatorio ${entity.id} " +
                        "ilegible, queda FUERA del backup")
                }
                r
            }
        }
    }

    suspend fun restoreFromBackup(reminders: List<Reminder>): Int {
        if (reminders.isEmpty()) return 0
        val key = session.getSecretKey() ?: return 0
        val protegidas = protectedAmong(reminders.map { it.id })
        dao.upsertAll(reminders.filterNot { it.id in protegidas }.map { encrypt(it, key) })
        return protegidas.size
    }

    private fun encrypt(reminder: Reminder, key: javax.crypto.spec.SecretKeySpec) = ReminderEntity(
        id               = reminder.id,
        name             = crypto.encrypt(reminder.name, key),
        date             = reminder.date,
        note             = if (reminder.note.isNotEmpty()) crypto.encrypt(reminder.note, key) else "",
        createdAt        = reminder.createdAt,
        isTrashed        = reminder.isTrashed,
        trashedAt        = reminder.trashedAt,
        isPinned         = reminder.isPinned,
        isFavorite       = reminder.isFavorite,
        isArchived       = reminder.isArchived,
        isProtected      = reminder.isProtected,
        tags             = if (reminder.tags.isNotEmpty()) reminder.tags.joinToString(",") else null,
        category         = if (reminder.category != EntryCategory.SIN_CATEGORIZAR) reminder.category.name else null,
        alertSchedulesJson = if (reminder.schedules.isNotEmpty())
            ReminderSchedule.listToJson(reminder.schedules) else null
    )

    private fun decrypt(entity: ReminderEntity, key: javax.crypto.spec.SecretKeySpec): Reminder? =
        try {
            Reminder(
                id        = entity.id,
                name      = crypto.decrypt(entity.name, key),
                date      = entity.date,
                note      = if (entity.note.isNotEmpty()) crypto.decrypt(entity.note, key) else "",
                createdAt = entity.createdAt,
                isTrashed = entity.isTrashed,
                trashedAt = entity.trashedAt,
                isPinned   = entity.isPinned,
                isFavorite = entity.isFavorite,
                isArchived = entity.isArchived,
                isProtected = entity.isProtected,
                tags       = entity.tags?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
                category   = EntryCategory.entries.firstOrNull { it.name == entity.category }
                             ?: EntryCategory.SIN_CATEGORIZAR,
                schedules = ReminderSchedule.listFromJson(entity.alertSchedulesJson)
            )
        } catch (e: Exception) {
            Log.e(TAG_DECRYPT, "decrypt falló para recordatorio id=${entity.id}", e)
            null
        }

    suspend fun setPin(id: String, pinned: Boolean) = dao.setPin(id, pinned)

    suspend fun setFavorite(id: String, fav: Boolean) = dao.setFavorite(id, fav)

    suspend fun setArchived(id: String, archived: Boolean) {
        if (archived) {
            val key = session.getSecretKey()
            if (key != null) dao.getById(id)?.let { decrypt(it, key) }?.let { scheduler.cancelAll(context, it) }
        }
        dao.setArchived(id, archived)
    }

    suspend fun setArchivedBatch(ids: List<String>, archived: Boolean) {
        if (archived) {
            val key = session.getSecretKey()
            if (key != null) {
                ids.forEach { id ->
                    dao.getById(id)?.let { decrypt(it, key) }?.let { scheduler.cancelAll(context, it) }
                }
            }
        }
        dao.setArchivedBatch(ids, archived)
    }

    suspend fun setTags(id: String, tags: List<String>) =
        dao.setTags(id, if (tags.isEmpty()) null else tags.joinToString(","))

    suspend fun setCategory(id: String, category: EntryCategory) =
        dao.setCategory(id, if (category == EntryCategory.SIN_CATEGORIZAR) null else category.name)

    companion object {
        fun newId(): String = UUID.randomUUID().toString()
    }
}
