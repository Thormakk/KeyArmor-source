package com.thormakk.keyarmor.notifications

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppConsentViewModel @Inject constructor(
    controller: AppConsentController,
) : ViewModel() {
    val events = controller.events
}
