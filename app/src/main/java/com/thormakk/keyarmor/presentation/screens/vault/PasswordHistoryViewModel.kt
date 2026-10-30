package com.thormakk.keyarmor.presentation.screens.vault

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.domain.model.PasswordHistoryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordHistoryViewModel @Inject constructor(
    private val planPrefs:       PlanPrefs,
    private val vaultRepository: VaultRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val entryId: String = checkNotNull(savedStateHandle["entryId"])

    private val _history = MutableStateFlow<List<PasswordHistoryItem>>(emptyList())
    val history: StateFlow<List<PasswordHistoryItem>> = _history

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _history.value = vaultRepository.getPasswordHistory(entryId)
            _isLocked.value = planPrefs.effectivePlan() == UserPlan.FREE && _history.value.isEmpty()
            _isLoading.value = false
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            vaultRepository.deletePasswordHistory(entryId)
            _history.value = emptyList()
            _isLocked.value = planPrefs.effectivePlan() == UserPlan.FREE
        }
    }
}
