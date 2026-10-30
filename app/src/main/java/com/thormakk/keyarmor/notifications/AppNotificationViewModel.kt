package com.thormakk.keyarmor.notifications

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppNotificationViewModel @Inject constructor(
    controller: AppNotificationController,
) : ViewModel() {
    val notifications = controller.notifications
}
