package com.thormakk.keyarmor.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.Collections
import java.util.LinkedHashMap
import javax.inject.Inject
import javax.inject.Singleton

sealed class HibpResult {
    object Clean : HibpResult()
    data class Pwned(val count: Int) : HibpResult()
    object RateLimited : HibpResult()
    object NetworkError : HibpResult()
}

@Singleton
class HibpPasswordChecker @Inject constructor() {

    companion object {
        private const val BASE_URL   = "https://api.pwnedpasswords.com/range/"
        private const val TIMEOUT_MS = 10_000
        private const val HTTP_OK           = 200
        private const val HTTP_RATE_LIMITED = 429
        private const val MAX_CACHE_ENTRIES = 500
    }

    private val cache: MutableMap<String, HibpResult> = Collections.synchronizedMap(
        object : LinkedHashMap<String, HibpResult>(MAX_CACHE_ENTRIES, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, HibpResult>?): Boolean =
                size > MAX_CACHE_ENTRIES
        }
    )

    fun clearCache() = cache.clear()

    fun hashFor(password: String): String = sha1(password).uppercase()

    suspend fun check(password: String): HibpResult = withContext(Dispatchers.IO) {
        runCatching {
            val sha1   = sha1(password).uppercase()
            cache[sha1]?.let { return@withContext it }

            val prefix = sha1.take(5)
            val suffix = sha1.drop(5)

            when (val fetch = fetchRange(prefix)) {
                is FetchResult.Success -> {
                    val count = fetch.body.lineSequence()
                        .firstOrNull { it.startsWith(suffix, ignoreCase = true) }
                        ?.substringAfter(':')
                        ?.trim()
                        ?.toIntOrNull()
                        ?: 0
                    val result = if (count > 0) HibpResult.Pwned(count) else HibpResult.Clean
                    cache[sha1] = result
                    result
                }
                FetchResult.RateLimited -> HibpResult.RateLimited
                FetchResult.Failed      -> HibpResult.NetworkError
            }
        }.getOrElse { HibpResult.NetworkError }
    }

    private sealed class FetchResult {
        data class Success(val body: String) : FetchResult()
        object RateLimited : FetchResult()
        object Failed      : FetchResult()
    }

    private fun fetchRange(prefix: String): FetchResult = runCatching {
        val conn = URL("$BASE_URL$prefix").openConnection() as HttpURLConnection
        conn.apply {
            requestMethod = "GET"
            connectTimeout = TIMEOUT_MS
            readTimeout    = TIMEOUT_MS
            setRequestProperty("Add-Padding", "true")
            setRequestProperty("User-Agent", "KeyArmor-Android")
        }
        when (conn.responseCode) {
            HTTP_OK           -> FetchResult.Success(
                conn.inputStream.bufferedReader().readText().also { conn.disconnect() }
            )
            HTTP_RATE_LIMITED -> { conn.disconnect(); FetchResult.RateLimited }
            else              -> { conn.disconnect(); FetchResult.Failed }
        }
    }.getOrElse { FetchResult.Failed }

    private fun sha1(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-1").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
