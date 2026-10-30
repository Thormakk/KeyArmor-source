package com.thormakk.keyarmor.notifications

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNotificationController @Inject constructor() {

    private val _notifications = Channel<AppNotification>(Channel.BUFFERED)

    val notifications = _notifications.receiveAsFlow()

    fun show(message: String, duration: SnackbarDuration = SnackbarDuration.Long) {
        _notifications.trySend(AppNotification(message, duration))
    }
}

data class AppNotification(
    val message:  String,
    val duration: SnackbarDuration = SnackbarDuration.Long,
)
