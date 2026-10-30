package com.thormakk.keyarmor.data.repository

import com.thormakk.keyarmor.data.database.dao.PasswordHistoryDao
import com.thormakk.keyarmor.data.database.entity.PasswordHistoryEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasswordHistoryRepository @Inject constructor(
    private val dao: PasswordHistoryDao
) {

    suspend fun getAllForBackup(): List<PasswordHistoryEntity> = dao.getAllEntities()

    suspend fun upsertAll(entities: List<PasswordHistoryEntity>) = dao.upsertAll(entities)
}
