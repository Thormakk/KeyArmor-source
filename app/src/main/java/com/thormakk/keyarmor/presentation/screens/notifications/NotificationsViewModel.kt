package com.thormakk.keyarmor.presentation.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.repository.InAppNotificationRepository
import com.thormakk.keyarmor.domain.model.InAppNotification
import com.thormakk.keyarmor.domain.model.NotificationType
import com.thormakk.keyarmor.presentation.screens.vault.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: InAppNotificationRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSortOrder(order: SortOrder) { _sortOrder.value = order }

    private val _selectedTypes = MutableStateFlow<Set<NotificationType>>(emptySet())
    val selectedTypes: StateFlow<Set<NotificationType>> = _selectedTypes.asStateFlow()

    val availableTypes: StateFlow<Set<NotificationType>> =
        repository.observeAll()
            .map { list -> list.map { it.type }.toSet() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun applyTypeFilter(types: Set<NotificationType>) { _selectedTypes.value = types }
    fun clearTypeFilter() { _selectedTypes.value = emptySet() }

    val notifications: StateFlow<List<InAppNotification>> =
        combine(
            repository.observeAll(),
            _searchQuery,
            _sortOrder,
            _selectedTypes
        ) { list, query, order, types ->
            val byType = if (types.isEmpty()) list else list.filter { it.type in types }
            val filtered = if (query.isBlank()) byType
            else byType.filter { it.title.contains(query, ignoreCase = true) || it.body.contains(query, ignoreCase = true) }
            when (order) {
                SortOrder.A_Z     -> filtered.sortedBy { it.title.lowercase() }
                SortOrder.Z_A     -> filtered.sortedByDescending { it.title.lowercase() }
                SortOrder.NEWEST  -> filtered.sortedByDescending { it.createdAt }
                SortOrder.OLDEST  -> filtered.sortedBy { it.createdAt }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    fun toggleSelected(id: Long) {
        _selectedIds.update { current ->
            if (id in current) current - id else current + id
        }
    }

    fun selectAll() {
        _selectedIds.value = notifications.value.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun deleteSelected() {
        val ids = _selectedIds.value
        _selectedIds.value = emptySet()
        viewModelScope.launch {
            ids.forEach { id -> repository.softDelete(id) }
        }
    }

    fun archiveSelected() {
        val ids = _selectedIds.value
        _selectedIds.value = emptySet()
        viewModelScope.launch {
            ids.forEach { id -> repository.archive(id) }
        }
    }

    fun markAllRead() {
        viewModelScope.launch { repository.markAllRead() }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.softDelete(id) }
    }

    fun deleteAll() {
        viewModelScope.launch { repository.softDeleteAll() }
    }

    fun archive(id: Long) {
        viewModelScope.launch { repository.archive(id) }
    }
}
