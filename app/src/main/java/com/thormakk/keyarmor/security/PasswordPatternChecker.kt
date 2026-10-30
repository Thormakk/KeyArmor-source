package com.thormakk.keyarmor.security

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasswordPatternChecker @Inject constructor() {

    private val leetMap = mapOf(
        '@' to 'a', '4' to 'a',
        '3' to 'e',
        '1' to 'i', '!' to 'i', '|' to 'i',
        '0' to 'o',
        '$' to 's', '5' to 's',
        '7' to 't',
        '2' to 'z'
    )

    private val keyboardWalks = setOf(
        "qwerty", "qwertyui", "qwertyuiop",
        "asdf", "asdfgh", "asdfghjkl",
        "zxcvbn", "zxcvbnm",
        "qazwsx", "qazwsxedc",
        "1234", "12345", "123456", "1234567", "12345678", "123456789",
        "abcdef", "abcdefgh", "abcdefghij",
        "password", "pass", "admin", "login", "welcome", "letmein",
        "monkey", "dragon", "master", "sunshine", "princess", "shadow",
        "superman", "batman", "trustno1", "iloveyou"
    )

    private val wordPlusDigits = Regex("""^[a-z]{3,}\d{1,6}[^a-z0-9]?$""")

    fun isPatternBased(password: String, strengthScore: Int = -1): Boolean {
        if (password.length < 4) return false
        val lower = password.lowercase()
        val isStrong = strengthScore >= 3

        if (isStrong) {
            if (keyboardWalks.any { lower == it }) return true
        } else {
            if (keyboardWalks.any { lower.contains(it) }) return true
        }

        if (wordPlusDigits.matches(lower)) return true

        if (!isStrong && hasOnlyFirstCapital(password)) return true

        if (hasRepeatedRun(password)) return true

        val normalized = lower.map { leetMap[it] ?: it }.joinToString("")
        if (hasSequentialRun(normalized)) return true

        return false
    }

    private fun hasOnlyFirstCapital(password: String): Boolean {
        if (password.length < 5) return false
        if (!password[0].isUpperCase()) return false
        return password.drop(1).none { it.isUpperCase() }
    }

    private fun hasRepeatedRun(password: String): Boolean {
        var runLen = 1
        for (i in 1 until password.length) {
            if (password[i] == password[i - 1]) {
                runLen++
                if (runLen >= 3) return true
            } else {
                runLen = 1
            }
        }
        return false
    }

    private fun hasSequentialRun(normalized: String): Boolean {
        if (normalized.length < 3) return false
        var ascRun = 1; var descRun = 1
        for (i in 1 until normalized.length) {
            val diff = normalized[i].code - normalized[i - 1].code
            if (diff == 1)  { ascRun++;  descRun = 1; if (ascRun  >= 3) return true }
            else if (diff == -1) { descRun++; ascRun  = 1; if (descRun >= 3) return true }
            else { ascRun = 1; descRun = 1 }
        }
        return false
    }

    fun containsServiceName(password: String, title: String): Boolean {
        if (password.isEmpty() || title.isEmpty()) return false
        val pwdNorm = normalizePassword(password)
        val words = title.lowercase()
            .split(Regex("[^a-zA-Z0-9]+"))
            .filter { it.length >= 4 }
        return words.any { word ->
            pwdNorm.contains(word) &&
                (word.length != 4 || word.length.toFloat() / password.length >= 0.30f)
        }
    }

    fun containsPersonalInfo(
        password: String,
        username: String,
        email:    String
    ): Boolean {
        if (password.isEmpty()) return false
        val pwdNorm    = normalizePassword(password)
        val emailLocal = email.substringBefore("@")
        return (username.length >= 5 && pwdNorm.contains(username.lowercase())) ||
               (emailLocal.length >= 5 && pwdNorm.contains(emailLocal.lowercase()))
    }

    fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
            }
        }
        return dp[a.length][b.length]
    }

    fun areSimilar(p1: String, p2: String): Boolean {
        if (p1 == p2) return false
        if (p1.length < 5 || p2.length < 5) return false
        val threshold = minOf(maxOf(2, minOf(p1.length, p2.length) / 4), 4)
        return levenshtein(p1, p2) <= threshold
    }

    fun extractDomain(url: String): String? {
        if (url.isBlank()) return null
        return try {
            val host = url.lowercase()
                .removePrefix("https://")
                .removePrefix("http://")
                .removePrefix("www.")
                .split("/")[0]
                .split("?")[0]
                .trim()
            if (!host.contains(".") || host.length <= 3) return null
            val parts = host.split(".")
            if (parts.size >= 2) "${parts[parts.size - 2]}.${parts[parts.size - 1]}" else host
        } catch (_: Exception) {
            null
        }
    }

    private fun normalizePassword(password: String): String =
        password.lowercase().map { leetMap[it] ?: it }.joinToString("")
}
