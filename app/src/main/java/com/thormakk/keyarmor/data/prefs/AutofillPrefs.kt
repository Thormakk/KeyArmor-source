package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class BlockLevel {
    SAVE,

    ALL,
}

enum class BlockKind { APP, SITE }

data class BlockTarget(val kind: BlockKind, val value: String) {
    val key: String get() = (if (kind == BlockKind.APP) APP_PREFIX else SITE_PREFIX) + value

    companion object {
        const val APP_PREFIX = "app:"
        const val SITE_PREFIX = "site:"

        fun app(packageName: String) = BlockTarget(BlockKind.APP, packageName)
        fun site(domain: String) = BlockTarget(BlockKind.SITE, domain.lowercase())

        fun fromKey(key: String): BlockTarget? = when {
            key.startsWith(APP_PREFIX) -> app(key.removePrefix(APP_PREFIX))
            key.startsWith(SITE_PREFIX) -> site(key.removePrefix(SITE_PREFIX))
            else -> null
        }
    }
}

@Singleton
class AutofillPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "autofill_prefs"
        private const val KEY_SAVE = "blocked_save"
        private const val KEY_ALL = "blocked_all"
        private const val KEY_OFFER_UNLOCK = "offer_unlock_when_locked"
        private const val KEY_RECENT = "recent_targets"
        private const val KEY_NAME_PREFIX = "name_"

        private const val KEY_LEGACY_PACKAGES = "blocked_packages"
        private const val KEY_LEGACY_RECENT = "recent_packages"

        const val MAX_RECENT = 20

        private const val RECENT_SEPARATOR = "\n"
    }

    init { migrateIfNeeded() }

    private fun migrateIfNeeded() {
        val legacyBlocked = prefs.getStringSet(KEY_LEGACY_PACKAGES, null)
        if (legacyBlocked != null) {
            prefs.edit {
                putStringSet(KEY_SAVE, raw(KEY_SAVE) + legacyBlocked)
                remove(KEY_LEGACY_PACKAGES)
            }
        }
        for (key in listOf(KEY_SAVE, KEY_ALL)) {
            val current = raw(key)
            if (current.none { !it.startsWith(BlockTarget.APP_PREFIX) && !it.startsWith(BlockTarget.SITE_PREFIX) }) continue
            val prefixed = current.map {
                if (it.startsWith(BlockTarget.APP_PREFIX) || it.startsWith(BlockTarget.SITE_PREFIX)) it
                else BlockTarget.APP_PREFIX + it
            }.toSet()
            prefs.edit { putStringSet(key, prefixed) }
        }
        val legacyRecent = prefs.getString(KEY_LEGACY_RECENT, null)
        if (legacyRecent != null) {
            val prefixed = legacyRecent.split(RECENT_SEPARATOR)
                .filter { it.isNotBlank() }
                .map { BlockTarget.APP_PREFIX + it }
            prefs.edit {
                putString(KEY_RECENT, prefixed.joinToString(RECENT_SEPARATOR))
                remove(KEY_LEGACY_RECENT)
            }
        }
    }

    private fun raw(key: String): Set<String> =
        prefs.getStringSet(key, emptySet())?.toSet() ?: emptySet()

    fun level(target: BlockTarget): BlockLevel? = when (target.key) {
        in raw(KEY_ALL) -> BlockLevel.ALL
        in raw(KEY_SAVE) -> BlockLevel.SAVE
        else -> null
    }

    fun blocked(): Map<BlockTarget, BlockLevel> =
        (raw(KEY_ALL).mapNotNull { BlockTarget.fromKey(it) }.associateWith { BlockLevel.ALL }) +
            (raw(KEY_SAVE).mapNotNull { BlockTarget.fromKey(it) }.associateWith { BlockLevel.SAVE })

    fun effectiveLevel(targets: List<BlockTarget>): BlockLevel? {
        val levels = targets.mapNotNull { level(it) }
        return when {
            BlockLevel.ALL in levels -> BlockLevel.ALL
            levels.isNotEmpty() -> BlockLevel.SAVE
            else -> null
        }
    }

    fun isSaveBlocked(targets: List<BlockTarget>): Boolean = effectiveLevel(targets) != null

    fun isFullyBlocked(targets: List<BlockTarget>): Boolean =
        effectiveLevel(targets) == BlockLevel.ALL

    fun setLevel(target: BlockTarget, level: BlockLevel?) {
        if (target.value.isBlank()) return
        val save = raw(KEY_SAVE) - target.key
        val all = raw(KEY_ALL) - target.key
        prefs.edit {
            putStringSet(KEY_SAVE, if (level == BlockLevel.SAVE) save + target.key else save)
            putStringSet(KEY_ALL, if (level == BlockLevel.ALL) all + target.key else all)
            if (level == null) remove(KEY_NAME_PREFIX + target.key)
        }
    }

    fun name(target: BlockTarget): String? =
        prefs.getString(KEY_NAME_PREFIX + target.key, null)?.takeIf { it.isNotBlank() }

    fun setName(target: BlockTarget, name: String?) {
        val clean = name?.trim()
        prefs.edit {
            if (clean.isNullOrBlank()) remove(KEY_NAME_PREFIX + target.key)
            else putString(KEY_NAME_PREFIX + target.key, clean)
        }
    }

    fun recentTargets(): List<BlockTarget> =
        prefs.getString(KEY_RECENT, "")
            ?.split(RECENT_SEPARATOR)
            ?.filter { it.isNotBlank() }
            ?.mapNotNull { BlockTarget.fromKey(it) }
            ?: emptyList()

    fun noteSeen(target: BlockTarget) {
        if (target.value.isBlank()) return
        if (target.kind == BlockKind.APP && target.value == context.packageName) return
        val current = recentTargets()
        if (current.firstOrNull() == target) return
        val updated = (listOf(target) + current.filterNot { it == target }).take(MAX_RECENT)
        prefs.edit { putString(KEY_RECENT, updated.joinToString(RECENT_SEPARATOR) { it.key }) }
    }

    fun clearRecent() = prefs.edit { remove(KEY_RECENT) }

    fun removeSeen(target: BlockTarget) {
        val updated = recentTargets().filterNot { it == target }
        prefs.edit { putString(KEY_RECENT, updated.joinToString(RECENT_SEPARATOR) { it.key }) }
    }

    fun offerUnlockWhenLocked(): Boolean = prefs.getBoolean(KEY_OFFER_UNLOCK, false)

    fun setOfferUnlockWhenLocked(enabled: Boolean) =
        prefs.edit { putBoolean(KEY_OFFER_UNLOCK, enabled) }
}
