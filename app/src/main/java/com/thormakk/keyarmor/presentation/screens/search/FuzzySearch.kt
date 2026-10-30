package com.thormakk.keyarmor.presentation.screens.search

import java.util.Locale

internal object FuzzySearch {

    fun maxDistance(queryLen: Int): Int = when {
        queryLen < 4 -> 0
        else         -> 1
    }

    fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        val prev = IntArray(b.length + 1) { it }
        val curr = IntArray(b.length + 1)

        for (i in a.indices) {
            curr[0] = i + 1
            for (j in b.indices) {
                curr[j + 1] = if (a[i] == b[j]) {
                    prev[j]
                } else {
                    1 + minOf(prev[j], prev[j + 1], curr[j])
                }
            }
            prev.indices.forEach { prev[it] = curr[it] }
        }
        return curr[b.length]
    }

    fun matches(haystack: String, query: String): Boolean {
        val q = query.trim().lowercase(Locale.ROOT)
        val threshold = maxDistance(q.length)
        if (threshold == 0) return false

        haystack.lowercase(Locale.ROOT)
            .split(Regex("[\\s\\-_./,;:]+"))
            .filter { it.length >= q.length - threshold }
            .forEach { token ->
                val cmp = token.take(q.length + threshold)
                if (levenshtein(q, cmp) <= threshold) return true
            }
        return false
    }
}
