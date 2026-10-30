package com.thormakk.keyarmor.presentation.screens.reminders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.repository.TagRepository
import com.thormakk.keyarmor.data.repository.ReminderRepository
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.Reminder
import com.thormakk.keyarmor.domain.model.ReminderSchedule
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class ReminderAddEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository:    ReminderRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val reminderId: String = checkNotNull(savedStateHandle["id"])
    val isNew: Boolean = reminderId == Screen.ReminderAddEdit.NEW_ID

    private val _name  = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _dateMs = MutableStateFlow<Long?>(null)
    val dateMs: StateFlow<Long?> = _dateMs.asStateFlow()

    private val _timeHour   = MutableStateFlow<Int?>(null)
    val timeHour: StateFlow<Int?> = _timeHour.asStateFlow()

    private val _timeMinute = MutableStateFlow<Int?>(null)
    val timeMinute: StateFlow<Int?> = _timeMinute.asStateFlow()

    private val _note  = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    private val _schedules = MutableStateFlow<List<ReminderSchedule>>(emptyList())
    val schedules: StateFlow<List<ReminderSchedule>> = _schedules.asStateFlow()

    private val _isPinned   = MutableStateFlow(false)
    val isPinned: StateFlow<Boolean> = _isPinned.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _isArchived = MutableStateFlow(false)
    val isArchived: StateFlow<Boolean> = _isArchived.asStateFlow()

    private val _tags     = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    private val _category = MutableStateFlow(EntryCategory.SIN_CATEGORIZAR)
    val category: StateFlow<EntryCategory> = _category.asStateFlow()

    val availableTags: StateFlow<List<Tag>> = tagRepository.observeTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _done  = MutableStateFlow(false)
    val done: StateFlow<Boolean> = _done.asStateFlow()

    private val _lockedByProtection = MutableStateFlow(false)
    val lockedByProtection: StateFlow<Boolean> = _lockedByProtection.asStateFlow()

    private val _isLoading = MutableStateFlow(!isNew)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        if (!isNew) {
            viewModelScope.launch {
                val reminder = repository.getById(reminderId)
                if (reminder != null) {
                    if (reminder.isProtected) _lockedByProtection.value = true
                    _name.value       = reminder.name
                    _note.value       = reminder.note
                    _schedules.value  = reminder.schedules
                    _isPinned.value   = reminder.isPinned
                    _isFavorite.value = reminder.isFavorite
                    _isArchived.value = reminder.isArchived
                    _tags.value       = reminder.tags
                    _category.value   = reminder.category
                    val localCal = Calendar.getInstance().apply { timeInMillis = reminder.date }
                    _timeHour.value   = localCal.get(Calendar.HOUR_OF_DAY)
                    _timeMinute.value = localCal.get(Calendar.MINUTE)
                    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        set(localCal.get(Calendar.YEAR), localCal.get(Calendar.MONTH),
                            localCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    _dateMs.value = utcCal.timeInMillis
                }
                _isLoading.value = false
            }
        } else {
            val initialDate = savedStateHandle.get<Long>("initialDate")?.takeIf { it > 0L }
            if (initialDate != null) _dateMs.value = initialDate
        }
    }

    fun onNameChange(v: String)  { _name.value  = v }
    fun onDateChange(ms: Long)   { _dateMs.value = ms }
    fun onNoteChange(v: String)  { _note.value  = v }
    fun onTimeChange(hour: Int, minute: Int) {
        _timeHour.value   = hour
        _timeMinute.value = minute
    }

    fun addSchedule(schedule: ReminderSchedule) {
        _schedules.value = _schedules.value + schedule
        persistSchedules()
    }

    fun removeSchedule(scheduleId: String) {
        _schedules.value = _schedules.value.filter { it.id != scheduleId }
        persistSchedules()
    }

    fun updateSchedule(updated: ReminderSchedule) {
        _schedules.value = _schedules.value.map {
            if (it.id == updated.id) updated else it
        }
        persistSchedules()
    }

    private fun persistSchedules() {
        if (isNew) return
        val dateMidnightUtc = _dateMs.value ?: return
        val name = _name.value.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = dateMidnightUtc
            }
            val combinedDate = Calendar.getInstance().apply {
                set(utcCal.get(Calendar.YEAR), utcCal.get(Calendar.MONTH),
                    utcCal.get(Calendar.DAY_OF_MONTH), _timeHour.value ?: 0, _timeMinute.value ?: 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            repository.save(
                Reminder(
                    id         = reminderId,
                    name       = name,
                    date       = combinedDate,
                    note       = _note.value.trim(),
                    createdAt  = System.currentTimeMillis(),
                    schedules  = _schedules.value,
                    isPinned   = _isPinned.value,
                    isFavorite = _isFavorite.value,
                    isArchived = _isArchived.value,
                    tags       = _tags.value,
                    category   = _category.value
                )
            )
        }
    }

    fun save() {
        val dateMidnightUtc = _dateMs.value ?: return
        val name = _name.value.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = dateMidnightUtc
            }
            val combinedDate = Calendar.getInstance().apply {
                set(utcCal.get(Calendar.YEAR), utcCal.get(Calendar.MONTH),
                    utcCal.get(Calendar.DAY_OF_MONTH), _timeHour.value ?: 0, _timeMinute.value ?: 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val guardado = repository.save(
                Reminder(
                    id         = if (isNew) ReminderRepository.newId() else reminderId,
                    name       = name,
                    date       = combinedDate,
                    note       = _note.value.trim(),
                    createdAt  = System.currentTimeMillis(),
                    schedules  = _schedules.value,
                    isPinned   = _isPinned.value,
                    isFavorite = _isFavorite.value,
                    isArchived = _isArchived.value,
                    tags       = _tags.value,
                    category   = _category.value
                )
            )
            if (guardado) _done.value = true else _lockedByProtection.value = true
        }
    }

    fun delete() {
        if (isNew) return
        viewModelScope.launch {
            if (repository.softDelete(reminderId)) _done.value = true else _lockedByProtection.value = true
        }
    }

    fun togglePin() {
        if (isNew) return
        viewModelScope.launch {
            val newVal = !_isPinned.value
            repository.setPin(reminderId, newVal)
            _isPinned.value = newVal
        }
    }

    fun toggleFavorite() {
        if (isNew) return
        viewModelScope.launch {
            val newVal = !_isFavorite.value
            repository.setFavorite(reminderId, newVal)
            _isFavorite.value = newVal
        }
    }

    fun toggleArchive() {
        if (isNew) return
        viewModelScope.launch {
            val newVal = !_isArchived.value
            repository.setArchived(reminderId, newVal)
            _isArchived.value = newVal
            if (newVal) _done.value = true
        }
    }

    fun updateTagsAndCategory(newCategory: EntryCategory, newTags: List<String>) {
        _category.value = newCategory
        _tags.value     = newTags
        if (isNew) return
        viewModelScope.launch {
            repository.setCategory(reminderId, newCategory)
            repository.setTags(reminderId, newTags)
        }
    }

    fun tagConflictKey(name: String): String = tagRepository.canonicalTagKey(name)

    fun addAvailableTag(name: String) { viewModelScope.launch { tagRepository.addTag(name) } }

    fun renameAvailableTag(id: String, newName: String) = viewModelScope.launch {
        val old = tagRepository.observeTags().first().find { it.id == id }?.name
        tagRepository.renameTag(id, newName)
        if (old != null && old in _tags.value) _tags.value = _tags.value.map { if (it == old) newName.trim() else it }
    }

    fun deleteAvailableTag(id: String) = viewModelScope.launch {
        val name = tagRepository.observeTags().first().find { it.id == id }?.name
        tagRepository.deleteTag(id)
        if (name != null) _tags.value = _tags.value.filter { it != name }
    }

    fun clone() {
        val dateMidnightUtc = _dateMs.value ?: return
        val name = _name.value.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = dateMidnightUtc
            }
            val combinedDate = Calendar.getInstance().apply {
                set(utcCal.get(Calendar.YEAR), utcCal.get(Calendar.MONTH),
                    utcCal.get(Calendar.DAY_OF_MONTH), _timeHour.value ?: 0, _timeMinute.value ?: 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            repository.save(
                Reminder(
                    id         = ReminderRepository.newId(),
                    name       = name,
                    date       = combinedDate,
                    note       = _note.value.trim(),
                    createdAt  = System.currentTimeMillis(),
                    schedules  = _schedules.value,
                    isPinned   = false,
                    isFavorite = false,
                    isArchived = false,
                    tags       = _tags.value,
                    category   = _category.value
                )
            )
            _done.value = true
        }
    }
}
