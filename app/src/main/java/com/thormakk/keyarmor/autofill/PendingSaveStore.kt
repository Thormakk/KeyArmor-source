package com.thormakk.keyarmor.autofill

import java.util.UUID

data class PendingSave(
    val baseTitle:  String,
    val url:        String,
    val username:   String,
    val email:      String,
    val password:   String,
    val fieldLabel: String,
    val target:     SaveDecision.Existing?,
    val suggestedTitle: String,
    val takenTitles: List<String>,
)

object PendingSaveStore {

    private const val TTL_MS = 3 * 60_000L

    private class Slot(val save: PendingSave, val at: Long)

    private val slots = HashMap<String, Slot>()

    @Synchronized
    fun put(save: PendingSave): String {
        purge()
        val token = UUID.randomUUID().toString()
        slots[token] = Slot(save, System.currentTimeMillis())
        return token
    }

    @Synchronized
    fun take(token: String?): PendingSave? {
        purge()
        return token?.let { slots.remove(it) }?.save
    }

    @Synchronized
    private fun purge() {
        val limite = System.currentTimeMillis() - TTL_MS
        slots.entries.removeAll { it.value.at < limite }
    }
}
