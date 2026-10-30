@file:Suppress("AppBundleLocaleChanges")
@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
package com.thormakk.keyarmor.data.repository

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.database.dao.BankAccountDao
import com.thormakk.keyarmor.data.database.dao.CreditCardDao
import com.thormakk.keyarmor.data.database.dao.CryptoWalletDao
import com.thormakk.keyarmor.data.database.dao.CustomEntryDao
import com.thormakk.keyarmor.data.database.dao.IdentityDocumentDao
import com.thormakk.keyarmor.data.database.dao.ReminderDao
import com.thormakk.keyarmor.data.database.dao.SecureNoteDao
import com.thormakk.keyarmor.data.database.dao.TagDao
import com.thormakk.keyarmor.data.database.dao.TagsRow
import com.thormakk.keyarmor.data.database.dao.TwoFactorDao
import com.thormakk.keyarmor.data.database.dao.VaultEntryDao
import com.thormakk.keyarmor.data.database.dao.VehicleDao
import com.thormakk.keyarmor.data.database.entity.TagEntity
import com.thormakk.keyarmor.di.ApplicationScope
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.Tag
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope  private val applicationScope: CoroutineScope,
    private val dao:          TagDao,
    private val vaultDao:     VaultEntryDao,
    private val bankDao:      BankAccountDao,
    private val cardDao:      CreditCardDao,
    private val cryptoDao:    CryptoWalletDao,
    private val customDao:    CustomEntryDao,
    private val twoFactorDao: TwoFactorDao,
    private val noteDao:      SecureNoteDao,
    private val identityDao:  IdentityDocumentDao,
    private val vehicleDao:   VehicleDao,
    private val reminderDao:  ReminderDao
) {
    companion object {
        const val MIN_TAGS = 3

        const val KEY_IMPORTANT = "default_important"
        const val KEY_PERSONAL  = "default_personal"
        const val KEY_WORK      = "default_work"
        private val DEFAULT_KEYS = listOf(KEY_IMPORTANT, KEY_PERSONAL, KEY_WORK)

        val SUPPORTED_LANGUAGE_CODES = setOf(
            "es", "en", "pt", "id", "fr", "de", "hi", "it",
            "ja", "tr", "ko", "ru", "zh", "uk", "pl", "nl", "sv", "cs", "sk"
        )
    }

    private fun localizedVariants(@StringRes resId: Int): Set<String> =
        SUPPORTED_LANGUAGE_CODES.map { code ->
            val config = Configuration(context.resources.configuration)
            config.setLocale(Locale.forLanguageTag(code))
            context.createConfigurationContext(config).getString(resId)
        }.toSet()

    private val allImportantNames: Set<String> by lazy { localizedVariants(R.string.tag_important) }
    private val allPersonalNames:  Set<String> by lazy { localizedVariants(R.string.tag_personal) }
    private val allWorkNames:      Set<String> by lazy { localizedVariants(R.string.tag_work) }
    private val defaultTagGroups:  List<Set<String>> by lazy { listOf(allImportantNames, allPersonalNames, allWorkNames) }

    private val allImportantNamesLower: Set<String> by lazy { allImportantNames.map { it.lowercase(Locale.ROOT) }.toSet() }
    private val allPersonalNamesLower:  Set<String> by lazy { allPersonalNames.map { it.lowercase(Locale.ROOT) }.toSet() }
    private val allWorkNamesLower:      Set<String> by lazy { allWorkNames.map { it.lowercase(Locale.ROOT) }.toSet() }

    private fun defaultKeyForName(name: String): String? {
        val lower = name.trim().lowercase(Locale.ROOT)
        return when (lower) {
            in allImportantNamesLower -> KEY_IMPORTANT
            in allPersonalNamesLower  -> KEY_PERSONAL
            in allWorkNamesLower      -> KEY_WORK
            else                      -> null
        }
    }

    fun localizedDefaultTagNames(ctx: Context = context): List<String> = listOf(
        ctx.getString(R.string.tag_important),
        ctx.getString(R.string.tag_personal),
        ctx.getString(R.string.tag_work)
    )

    private val renameMutex = Mutex()

    init {
        applicationScope.launch {
            if (dao.count() == 0) {
                localizedDefaultTagNames().forEachIndexed { i, name ->
                    dao.insert(TagEntity(id = UUID.randomUUID().toString(), name = name, defaultKey = DEFAULT_KEYS[i]))
                }
            }
            stampDefaultKeys()
            renameMutex.withLock { healDefaultTagTokens() }
            reconcileOrphanedTags()
            reconcileDuplicateTagNames()
            runCatching { syncDefaultTagsToLocale() }
        }
    }

    suspend fun reconcileAfterRestore() {
        runCatching {
            stampDefaultKeys()
            renameMutex.withLock { healDefaultTagTokens() }
            reconcileOrphanedTags()
            reconcileDuplicateTagNames()
            syncDefaultTagsToLocale()
        }
    }

    fun reconcileAfterLanguageChange(localeCode: String) {
        applicationScope.launch {
            languageChangeMutex.withLock {
                runCatching {
                    syncDefaultTagsToLocale(localeCode)
                    stampDefaultKeys()
                    renameMutex.withLock { healDefaultTagTokens() }
                    reconcileOrphanedTags()
                    reconcileDuplicateTagNames(localizedDefaultTagNames(contextForLocale(localeCode)))
                    syncDefaultTagsToLocale(localeCode)
                }
            }
        }
    }

    private val languageChangeMutex = Mutex()

    private fun contextForLocale(localeCode: String): Context {
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale.forLanguageTag(localeCode))
        return context.createConfigurationContext(config)
    }

    private suspend fun stampDefaultKeys() {
        dao.getAll().forEach { tag ->
            if (tag.defaultKey.isBlank()) {
                val key = defaultKeyForName(tag.name)
                if (key != null) dao.setDefaultKey(tag.id, key)
            }
        }
    }

    suspend fun syncDefaultTagsToLocale(localeCode: String? = null) = renameMutex.withLock {
        val targetCtx: Context = if (localeCode != null) {
            val locale = Locale.forLanguageTag(localeCode)
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            context.createConfigurationContext(config)
        } else {
            context
        }
        val targetNames = localizedDefaultTagNames(targetCtx)
        val allTags = dao.getAll()

        DEFAULT_KEYS.forEachIndexed { i, key ->
            val targetName = targetNames[i]
            val entity = allTags.find { it.defaultKey == key && it.name != targetName }
                ?: return@forEachIndexed
            if (!dao.existsByName(targetName, excludeId = entity.id)) {
                dao.rename(entity.id, targetName)
                propagateTagRename(entity.name, targetName)
            }
        }
        healDefaultTagTokens()
    }

    private suspend fun healDefaultTagTokens() {
        val pool = dao.getAll()
        val poolNames = pool.map { it.name }.toSet()
        val byKey = pool.filter { it.defaultKey.isNotBlank() }.associateBy { it.defaultKey }
        (collectUsedTagNames() - poolNames).forEach { token ->
            val key = defaultKeyForName(token) ?: return@forEach
            val target = byKey[key] ?: return@forEach
            propagateTagRename(token, target.name)
        }
    }

    private suspend fun propagateTagRename(oldName: String, newName: String) {
        if (oldName == newName) return
        renameTagToken(oldName, newName, vaultDao::getEntriesWithTag, vaultDao::updateTags)
        renameTagToken(oldName, newName, bankDao::getEntriesWithTag, bankDao::updateTags)
        renameTagToken(oldName, newName, cardDao::getEntriesWithTag, cardDao::updateTags)
        renameTagToken(oldName, newName, cryptoDao::getEntriesWithTag, cryptoDao::updateTags)
        renameTagToken(oldName, newName, customDao::getEntriesWithTag, customDao::updateTags)
        renameTagToken(oldName, newName, twoFactorDao::getEntriesWithTag, twoFactorDao::updateTags)
        renameTagToken(oldName, newName, noteDao::getEntriesWithTag, noteDao::updateTags)
        renameTagToken(oldName, newName, identityDao::getEntriesWithTag, identityDao::updateTags)
        renameTagToken(oldName, newName, vehicleDao::getEntriesWithTag, vehicleDao::updateTags)
        renameTagToken(oldName, newName, reminderDao::getEntriesWithTag, reminderDao::updateTags)
    }

    private suspend fun renameTagToken(
        oldName: String,
        newName: String,
        getEntriesWithTag: suspend (String) -> List<TagsRow>,
        updateTags: suspend (String, String) -> Unit
    ) {
        getEntriesWithTag(oldName).forEach { row ->
            val merged = row.tags.split(",")
                .map { it.trim() }
                .map { if (it == oldName) newName else it }
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString(",")
            updateTags(row.id, merged)
        }
    }

    fun observeTags(): Flow<List<Tag>> = dao.observeAll()
        .flatMapLatest { entities ->
            flow {
                val total    = entities.size
                val usedTags = collectUsedTagNames()
                emit(entities.map { entity ->
                    Tag(
                        id        = entity.id,
                        name      = entity.name,
                        canDelete = total > MIN_TAGS,
                        isInUse   = entity.name in usedTags
                    )
                })
            }
        }

    private suspend fun collectUsedTagNames(): Set<String> {
        val result = mutableSetOf<String>()
        listOf(
            vaultDao.getTagStrings(),
            bankDao.getTagStrings(),
            cardDao.getTagStrings(),
            cryptoDao.getTagStrings(),
            customDao.getTagStrings(),
            twoFactorDao.getTagStrings(),
            noteDao.getTagStrings(),
            identityDao.getTagStrings(),
            vehicleDao.getTagStrings(),
            reminderDao.getTagStrings()
        ).forEach { tagStrings ->
            tagStrings.forEach { csv ->
                csv.split(",").forEach { token ->
                    val t = token.trim()
                    if (t.isNotBlank()) result.add(t)
                }
            }
        }
        return result
    }

    fun observeDefaultTagNames(): Flow<Triple<String, String, String>> = dao.observeAll()
        .map { entities ->
            val names = entities.map { it.name }
            Triple(
                names.find { it in allImportantNames } ?: localizedDefaultTagNames()[0],
                names.find { it in allPersonalNames }  ?: localizedDefaultTagNames()[1],
                names.find { it in allWorkNames }       ?: localizedDefaultTagNames()[2]
            )
        }

    suspend fun addTag(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        if (EntryCategory.entries.any { it.label.equals(trimmed, ignoreCase = true) }) return

        val defaultKey = defaultKeyForName(trimmed)
        if (defaultKey != null) {
            if (dao.existsByDefaultKey(defaultKey)) return
            dao.insert(TagEntity(id = UUID.randomUUID().toString(), name = trimmed, defaultKey = defaultKey))
            return
        }
        if (dao.existsByName(trimmed)) return
        dao.insert(TagEntity(id = UUID.randomUUID().toString(), name = trimmed))
    }

    suspend fun renameTag(id: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return
        if (EntryCategory.entries.any { it.label.equals(trimmed, ignoreCase = true) }) return

        val oldName = dao.getAll().find { it.id == id }?.name ?: return

        val defaultKey = defaultKeyForName(trimmed)
        if (defaultKey != null) {
            if (dao.existsByDefaultKey(defaultKey, excludeId = id)) return
        } else {
            if (dao.existsByName(trimmed, excludeId = id)) return
        }

        dao.rename(id, trimmed)
        dao.setDefaultKey(id, defaultKey ?: "")
        propagateTagRename(oldName, trimmed)
    }

    suspend fun deleteTag(id: String): Boolean {
        if (dao.count() <= MIN_TAGS) return false
        val tagName = dao.getAll().find { it.id == id }?.name ?: return false
        if (tagName in collectUsedTagNames()) return false
        dao.delete(id)
        return true
    }

    private suspend fun reconcileOrphanedTags() {
        val validTagNames = dao.getAll().map { it.name }.toSet()
        val usedTagNames  = collectUsedTagNames()
        val orphaned      = usedTagNames - validTagNames
        orphaned.forEach { tag ->
            vaultDao.removeTagFromEntries(tag)
            bankDao.removeTagFromEntries(tag)
            cardDao.removeTagFromEntries(tag)
            cryptoDao.removeTagFromEntries(tag)
            customDao.removeTagFromEntries(tag)
            twoFactorDao.removeTagFromEntries(tag)
            noteDao.removeTagFromEntries(tag)
            identityDao.removeTagFromEntries(tag)
            vehicleDao.removeTagFromEntries(tag)
            reminderDao.removeTagFromEntries(tag)
        }
    }

    private suspend fun reconcileDuplicateTagNames(
        currentLocaleNames: List<String> = localizedDefaultTagNames()
    ) {
        dao.getAll()
            .groupBy { it.defaultKey.ifBlank { canonicalTagKey(it.name) } }
            .values
            .filter { it.size > 1 }
            .forEach { duplicates ->
                val survivor = duplicates.find { it.name in currentLocaleNames } ?: duplicates.first()
                duplicates.filter { it.id != survivor.id }.forEach { dup ->
                    propagateTagRename(dup.name, survivor.name)
                    dao.delete(dup.id)
                }
            }
    }

    fun canonicalTagKey(name: String): String =
        defaultKeyForName(name) ?: name.trim().lowercase(Locale.ROOT)

    suspend fun getAllForBackup(): List<TagEntity> = dao.getAll()

    suspend fun upsertAll(tags: List<TagEntity>) {
        val existing    = dao.getAll()
        val existingIds = existing.map { it.id }.toSet()
        val byCanonical = existing.associateBy { canonicalTagKey(it.name) }.toMutableMap()
        val toInsert    = mutableListOf<TagEntity>()

        tags.forEach { incoming ->
            val stamped = incoming.copy(defaultKey = defaultKeyForName(incoming.name) ?: "")
            if (stamped.id in existingIds) {
                toInsert.add(stamped)
                return@forEach
            }
            val key        = canonicalTagKey(stamped.name)
            val equivalent = byCanonical[key]
            if (equivalent != null) {
                propagateTagRename(stamped.name, equivalent.name)
            } else {
                toInsert.add(stamped)
                byCanonical[key] = stamped
            }
        }
        if (toInsert.isNotEmpty()) dao.upsertAll(toInsert)
    }

    suspend fun deleteAll() = dao.deleteAll()
}
