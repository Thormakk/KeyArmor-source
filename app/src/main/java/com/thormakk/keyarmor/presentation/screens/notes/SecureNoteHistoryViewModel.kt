package com.thormakk.keyarmor.presentation.screens.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.repository.SecureNoteHistoryRepository
import com.thormakk.keyarmor.data.repository.SecureNoteRepository
import com.thormakk.keyarmor.domain.model.SecureNoteHistoryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SecureNoteHistoryUiState(
    val items:     List<SecureNoteHistoryItem> = emptyList(),
    val isLoading: Boolean                     = true,
    val restored:  Boolean                     = false,
    val deleted:   Boolean                     = false
)

@HiltViewModel
class SecureNoteHistoryViewModel @Inject constructor(
    private val historyRepository: SecureNoteHistoryRepository,
    private val noteRepository:    SecureNoteRepository,
    savedStateHandle:              SavedStateHandle
) : ViewModel() {

    private val noteId: String = checkNotNull(savedStateHandle["noteId"])

    private val _uiState = MutableStateFlow(SecureNoteHistoryUiState())
    val uiState: StateFlow<SecureNoteHistoryUiState> = _uiState

    private val _selectedIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIds: StateFlow<Set<Int>> = _selectedIds.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val items = historyRepository.getHistory(noteId)
            _uiState.value = _uiState.value.copy(items = items, isLoading = false)
        }
    }

    fun toggleSelection(id: Int) {
        val current = _selectedIds.value
        _selectedIds.value = if (id in current) current - id else current + id
    }

    fun clearSelection() { _selectedIds.value = emptySet() }

    fun selectAll() {
        _selectedIds.value = _uiState.value.items.map { it.id }.toSet()
    }

    fun deleteSelected() {
        val ids = _selectedIds.value
        if (ids.isEmpty()) return
        viewModelScope.launch {
            historyRepository.deleteItems(ids)
            _selectedIds.value = emptySet()
            _uiState.value = _uiState.value.copy(deleted = true)
            load()
        }
    }

    fun onDeletedConsumed() { _uiState.value = _uiState.value.copy(deleted = false) }

    fun restore(item: SecureNoteHistoryItem) {
        viewModelScope.launch {
            val note = noteRepository.getNote(noteId) ?: return@launch
            if (note.isProtected) return@launch
            historyRepository.recordVersion(noteId, note.content)
            noteRepository.saveNote(
                note.copy(
                    content   = item.content,
                    updatedAt = System.currentTimeMillis()
                )
            )
            _uiState.value = _uiState.value.copy(restored = true)
            load()
        }
    }
}
