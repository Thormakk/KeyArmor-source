package com.thormakk.keyarmor.presentation.screens.search

import androidx.lifecycle.ViewModel
import com.thormakk.keyarmor.data.prefs.RecentEntriesPrefs
import com.thormakk.keyarmor.domain.model.HubEntryType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RecentEntriesViewModel @Inject constructor(
    private val recentEntriesPrefs: RecentEntriesPrefs
) : ViewModel() {

    fun recordAccess(id: String, type: HubEntryType) =
        recentEntriesPrefs.recordAccess(id, type)
}
