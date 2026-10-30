package com.thormakk.keyarmor.data.repository

import com.thormakk.keyarmor.data.database.dao.InAppNotificationDao
import com.thormakk.keyarmor.data.database.entity.InAppNotificationEntity
import com.thormakk.keyarmor.domain.model.InAppNotification
import com.thormakk.keyarmor.domain.model.NotificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppNotificationRepository @Inject constructor(
    private val dao: InAppNotificationDao,
    private val crypto:  com.thormakk.keyarmor.security.CryptoManager,
    private val session: com.thormakk.keyarmor.security.SessionManager
) {
    companion object {
        private const val RETENTION_DAYS = 30
    }

    fun observeAll(): Flow<List<InAppNotification>> =
        dao.observeAll().map { list -> list.mapNotNull { it.toDomain() } }

    fun observeUnreadCount(): Flow<Int> = dao.observeUnreadCount()

    fun observeTrash(): Flow<List<InAppNotification>> =
        dao.observeTrash().map { list -> list.mapNotNull { it.toDomain() } }

    suspend fun save(type: NotificationType, title: String, body: String, targetId: String? = null) {
        purgeOld()
        val clave = session.getSecretKey()
        val cifrado = clave != null && runCatching {
            dao.insert(
                InAppNotificationEntity(
                    type        = type.name,
                    title       = crypto.encrypt(title, clave),
                    body        = crypto.encrypt(body, clave),
                    targetId    = targetId,
                    isEncrypted = true
                )
            )
        }.isSuccess
        if (cifrado) return
        dao.insert(
            InAppNotificationEntity(
                type     = type.name,
                title    = title,
                body     = body,
                targetId = targetId
            )
        )
    }

    suspend fun softDelete(id: Long) = dao.softDeleteById(id, System.currentTimeMillis())

    suspend fun softDeleteAll() = dao.softDeleteAll(System.currentTimeMillis())

    suspend fun restoreFromTrash(id: Long) = dao.restoreFromTrash(id)

    suspend fun permanentlyDelete(id: Long) = dao.permanentlyDeleteById(id)

    suspend fun permanentlyDeleteAllTrashed() = dao.permanentlyDeleteAllTrashed()

    suspend fun markAllRead() = dao.markAllRead()

    suspend fun toggleRead(id: Long) = dao.toggleRead(id)

    suspend fun archive(id: Long) = dao.archiveById(id, System.currentTimeMillis())

    suspend fun deleteActiveExpiringWithNullTarget() = dao.deleteActiveExpiringWithNullTarget()

    private suspend fun purgeOld() {
        val cutoff = System.currentTimeMillis() - RETENTION_DAYS * 24L * 60 * 60 * 1000
        dao.deleteOlderThan(cutoff)
    }

    private fun InAppNotificationEntity.descifrar(campo: String): String? {
        if (!isEncrypted) return campo
        val clave = session.getSecretKey() ?: return null
        return runCatching { crypto.decrypt(campo, clave) }.getOrNull()
    }

    private fun InAppNotificationEntity.toDomain(): InAppNotification? {
        val tituloLegible = descifrar(title) ?: return null
        val cuerpoLegible = descifrar(body)  ?: return null
        return InAppNotification(
        id         = id,
        type       = runCatching { NotificationType.valueOf(type) }.getOrDefault(NotificationType.EXPIRING),
        title      = tituloLegible,
        body       = cuerpoLegible,
        createdAt  = createdAt,
        isRead     = isRead,
        trashedAt  = trashedAt,
        isArchived = isArchived,
        archivedAt = archivedAt,
        targetId   = targetId
        )
    }
}
