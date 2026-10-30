package com.thormakk.keyarmor.presentation.navigation

import androidx.lifecycle.ViewModel
import com.thormakk.keyarmor.security.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AutoLockNavViewModel @Inject constructor(
    sessionManager: SessionManager
) : ViewModel() {

    val isSessionActive: StateFlow<Boolean> = sessionManager.isUnlockedFlow
}
