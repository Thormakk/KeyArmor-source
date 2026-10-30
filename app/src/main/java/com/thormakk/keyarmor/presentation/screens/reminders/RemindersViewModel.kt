package com.thormakk.keyarmor.presentation.screens.reminders

import com.thormakk.keyarmor.security.OrdenEnSesion
import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.repository.TagRepository
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.AttachmentRepository
import com.thormakk.keyarmor.data.repository.ReminderRepository
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.Reminder
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.presentation.screens.vault.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val repository:         ReminderRepository,
    attachmentRepository: AttachmentRepository,
    planPrefs:          PlanPrefs,
    private val tagRepository:      TagRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _exactAlarmsGranted = MutableStateFlow(computeExactAlarmsGranted())
    val exactAlarmsGranted: StateFlow<Boolean> = _exactAlarmsGranted.asStateFlow()

    fun refreshExactAlarmsState() {
        _exactAlarmsGranted.value = computeExactAlarmsGranted()
    }

    private fun computeExactAlarmsGranted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val am = context.getSystemService(AlarmManager::class.java)
        return am.canScheduleExactAlarms()
    }

    private val _allReminders: StateFlow<List<Reminder>> =
        repository.observeActive()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val entryIdsWithAttachments: StateFlow<Set<String>> =
        attachmentRepository.observeEntryIdsWithAttachments()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val isFreeUser: StateFlow<Boolean> = planPrefs.planFlow
        .map { it == UserPlan.FREE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _searchQuery  = MutableStateFlow("")
    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    private val _sortOrder    = MutableStateFlow(OrdenEnSesion.leer("reminders", SortOrder.NEWEST))

    val searchQuery:  StateFlow<String>      = _searchQuery.asStateFlow()
    val selectedTags: StateFlow<Set<String>> = _selectedTags.asStateFlow()
    val sortOrder:    StateFlow<SortOrder>   = _sortOrder.asStateFlow()

    val availableTags: StateFlow<List<Tag>> = tagRepository.observeTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val topFilterTags: StateFlow<List<String>> = combine(
        _allReminders, tagRepository.observeDefaultTagNames()
    ) { list, defaults ->
        val used = list.flatMap { it.tags.map { t -> t.trim() } }
            .filter { it.isNotBlank() }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }.map { it.key }
        (used + listOf(defaults.first, defaults.second, defaults.third)).distinct().take(3)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tagCountMap: StateFlow<Map<String, Int>> = _allReminders
        .map { list ->
            list.flatMap { it.tags.map { t -> t.trim() } }
                .filter { it.isNotBlank() }
                .groupingBy { it }.eachCount()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val _onlyFavorites   = MutableStateFlow(false)
    val onlyFavorites: StateFlow<Boolean> = _onlyFavorites.asStateFlow()

    private val _onlyAttachments = MutableStateFlow(false)
    val onlyAttachments: StateFlow<Boolean> = _onlyAttachments.asStateFlow()

    private val _onlyProtected = MutableStateFlow(false)
    val onlyProtected: StateFlow<Boolean> = _onlyProtected.asStateFlow()

    private val _categoryFilter = MutableStateFlow<EntryCategory?>(null)
    val categoryFilter: StateFlow<EntryCategory?> = _categoryFilter.asStateFlow()

    private val _calendarStart = MutableStateFlow<Long?>(null)
    private val _calendarEnd   = MutableStateFlow<Long?>(null)
    val calendarStart: StateFlow<Long?> = _calendarStart.asStateFlow()
    val calendarEnd:   StateFlow<Long?> = _calendarEnd.asStateFlow()

    fun setCalendarRange(start: Long?, end: Long?) {
        _calendarStart.value = start
        _calendarEnd.value   = if (end == start) null else end
    }

    val reminderCountByDay: StateFlow<Map<Long, Int>> = _allReminders
        .map { list ->
            list.filter { !it.isArchived }
                .groupBy { it.date.toUtcMidnight() }
                .mapValues { it.value.size }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val filteredReminders: StateFlow<List<Reminder>> = combine(
        combine(_allReminders, _searchQuery, _selectedTags) { a, b, c -> Triple(a, b, c) },
        combine(_sortOrder, _onlyFavorites, _onlyAttachments) { a, b, c -> Triple(a, b, c) },
        entryIdsWithAttachments,
        _onlyProtected
    ) { (all, query, tags), (sort, favs, attachments), attIds, onlyProt ->
        val base = all.filter { !it.isArchived }
        val filtered = base.filter { r ->
            val matchesQuery       = query.isBlank() || r.name.contains(query, ignoreCase = true)
            val matchesTags        = tags.isEmpty() || tags.any { it in r.tags }
            val matchesFavs        = !favs || r.isFavorite
            val matchesAttachments = !attachments || r.id in attIds
            val matchesProtected   = !onlyProt || r.isProtected
            matchesQuery && matchesTags && matchesFavs && matchesAttachments && matchesProtected
        }
        when (sort) {
            SortOrder.A_Z    -> filtered.sortedBy   { it.name.lowercase() }
            SortOrder.Z_A    -> filtered.sortedByDescending { it.name.lowercase() }
            SortOrder.NEWEST -> filtered.sortedByDescending { it.createdAt }
            SortOrder.OLDEST -> filtered.sortedBy   { it.createdAt }
        }
    }.combine(_categoryFilter) { list, cat ->
        if (cat == null) list else list.filter { it.category == cat }
    }.combine(combine(_calendarStart, _calendarEnd) { s, e -> s to e }) { list, (start, end) ->
        when {
            start == null -> list
            end   == null -> list.filter { it.date.toUtcMidnight() == start }
            else          -> {
                val lo = minOf(start, end)
                val hi = maxOf(start, end)
                list.filter { it.date.toUtcMidnight() in lo..hi }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()
    val isSelectionMode: Boolean get() = _selectedIds.value.isNotEmpty()

    fun toggleSelected(id: String) {
        _selectedIds.update { if (id in it) it - id else it + id }
    }
    fun clearSelection() { _selectedIds.value = emptySet() }
    fun selectAll() { _selectedIds.value = filteredReminders.value.map { it.id }.toSet() }

    fun setSearchQuery(q: String)      { _searchQuery.value = q }
    fun toggleTag(tag: String)         { _selectedTags.update { if (tag in it) it - tag else it + tag } }
    fun setTags(tags: Set<String>)     { _selectedTags.value = tags }
    fun setSortOrder(o: SortOrder)     { OrdenEnSesion.guardar("reminders", o); _sortOrder.value = o }
    fun setFavorites(v: Boolean)       { _onlyFavorites.value = v }
    fun setAttachments(v: Boolean)           { _onlyAttachments.value = v }
    fun setOnlyProtected(v: Boolean)         { _onlyProtected.value = v }
    fun setCategoryFilter(c: EntryCategory?) { _categoryFilter.value  = c }
    fun clearAllFilters() {
        _selectedTags.value    = emptySet()
        _onlyFavorites.value   = false
        _onlyAttachments.value = false
        _onlyProtected.value   = false
        _categoryFilter.value  = null
        _calendarStart.value   = null
        _calendarEnd.value     = null
    }

    fun tagConflictKey(name: String): String = tagRepository.canonicalTagKey(name)
    fun addTag(name: String)                   { viewModelScope.launch { tagRepository.addTag(name) } }
    fun renameTag(id: String, newName: String) {
        viewModelScope.launch {
            val old = tagRepository.observeTags().first().find { it.id == id }?.name
            tagRepository.renameTag(id, newName)
            if (old != null) _selectedTags.update { if (old in it) (it - old) + newName.trim() else it }
        }
    }
    fun deleteTag(id: String) {
        viewModelScope.launch {
            val name = tagRepository.observeTags().first().find { it.id == id }?.name
            tagRepository.deleteTag(id)
            if (name != null) _selectedTags.update { it - name }
        }
    }

    val canProtectNew: Boolean get() = !isFreeUser.value

    fun setProtected(id: String, value: Boolean) {
        if (value && !canProtectNew) return
        viewModelScope.launch { repository.setProtected(id, value) }
    }

    fun bulkSetProtected(value: Boolean) {
        if (value && isFreeUser.value) return
        val ids = _selectedIds.value.toList()
        viewModelScope.launch {
            ids.forEach { repository.setProtected(it, value) }
            _selectedIds.value = emptySet()
        }
    }

    private val _protectedSkipped = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val protectedSkipped: SharedFlow<Int> = _protectedSkipped.asSharedFlow()

    fun delete(id: String) = viewModelScope.launch { repository.softDelete(id) }

    fun togglePin(reminder: Reminder) = viewModelScope.launch {
        repository.setPin(reminder.id, !reminder.isPinned)
    }

    fun toggleFavorite(reminder: Reminder) = viewModelScope.launch {
        repository.setFavorite(reminder.id, !reminder.isFavorite)
    }

    fun toggleArchive(reminder: Reminder) = viewModelScope.launch {
        repository.setArchived(reminder.id, !reminder.isArchived)
    }

    fun bulkDelete() {
        val ids = _selectedIds.value.toList()
        viewModelScope.launch {
            val protegidos = repository.protectedAmong(ids)
            repository.softDeleteBatch(ids)
            if (protegidos.isNotEmpty()) _protectedSkipped.tryEmit(protegidos.size)
            _selectedIds.value = emptySet()
        }
    }

    fun bulkArchive() {
        val ids = _selectedIds.value.toList()
        viewModelScope.launch {
            repository.setArchivedBatch(ids, true)
            _selectedIds.value = emptySet()
        }
    }

}

private fun Long.toUtcMidnight(): Long {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = this@toUtcMidnight
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE,      0)
        set(Calendar.SECOND,      0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}
