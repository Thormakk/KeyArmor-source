package com.thormakk.keyarmor.data.remote

import javax.inject.Inject
import javax.inject.Singleton

data class CachedWalletBalance(
    val nominalBalance: String,
    val fiatValue: Double?,
    val isManual: Boolean,
    val manualBalance: String = ""
)

@Singleton
class CryptoBalanceCache @Inject constructor() {

    private val cache = mutableMapOf<String, CachedWalletBalance>()

    var lastRefreshedAt: Long? = null
        private set

    fun get(walletId: String): CachedWalletBalance? = cache[walletId]

    fun put(walletId: String, balance: CachedWalletBalance) {
        cache[walletId] = balance
    }

    fun remove(walletId: String) { cache.remove(walletId) }

    fun keys(): Set<String> = cache.keys.toSet()

    fun isEmpty(): Boolean = cache.isEmpty()

    fun clearFiatValues() {
        cache.keys.toList().forEach { id ->
            cache[id] = cache[id]!!.copy(fiatValue = null)
        }
    }

    fun clear() { cache.clear() }

    fun markRefreshed() { lastRefreshedAt = System.currentTimeMillis() }
}
