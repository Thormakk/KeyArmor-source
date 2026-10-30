package com.thormakk.keyarmor.notifications

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConsentController @Inject constructor() {
    private val _events = Channel<Unit>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun requestConsent() {
        _events.trySend(Unit)
    }
}
