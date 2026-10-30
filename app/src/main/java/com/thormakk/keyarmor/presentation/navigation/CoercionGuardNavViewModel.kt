package com.thormakk.keyarmor.presentation.navigation

import androidx.lifecycle.ViewModel
import com.thormakk.keyarmor.data.prefs.PanicPinPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class CoercionGuardNavViewModel @Inject constructor(
    panicPinPrefs: PanicPinPrefs
) : ViewModel() {
    val coercionActive: StateFlow<Boolean> = panicPinPrefs.coercionActiveFlow
}
