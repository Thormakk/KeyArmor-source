package com.thormakk.keyarmor.autofill

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PublicSuffixList @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private data class Rules(
        val exact: Set<String>,
        val wildcardBases: Set<String>,
        val exceptions: Set<String>
    )

    private val rules: Rules by lazy { parseAsset() }

    private fun parseAsset(): Rules {
        val exact = HashSet<String>()
        val wildcardBases = HashSet<String>()
        val exceptions = HashSet<String>()
        try {
            context.assets.open(ASSET_NAME).bufferedReader(Charsets.UTF_8).useLines { lines ->
                for (raw in lines) {
                    val line = raw.trim()
                    if (line.isEmpty() || line.startsWith("//")) continue
                    when {
                        line.startsWith("!")  -> exceptions.add(line.substring(1).lowercase())
                        line.startsWith("*.") -> wildcardBases.add(line.substring(2).lowercase())
                        else                  -> exact.add(line.lowercase())
                    }
                }
            }
        } catch (e: Exception) {
        }
        return Rules(exact, wildcardBases, exceptions)
    }

    fun getRegistrableDomain(host: String): String? {
        val labels = host.lowercase().removeSuffix(".").split(".")
        if (labels.size < 2) return null

        var bestPublicSuffixLabelCount = -1

        for (i in labels.indices) {
            val candidateLabels = labels.subList(i, labels.size)
            val candidate = candidateLabels.joinToString(".")

            if (candidate in rules.exceptions) {
                val count = candidateLabels.size - 1
                if (count > bestPublicSuffixLabelCount) bestPublicSuffixLabelCount = count
                continue
            }
            if (candidate in rules.exact) {
                val count = candidateLabels.size
                if (count > bestPublicSuffixLabelCount) bestPublicSuffixLabelCount = count
            }
            if (candidateLabels.size >= 2) {
                val base = candidateLabels.subList(1, candidateLabels.size).joinToString(".")
                if (base in rules.wildcardBases) {
                    val count = candidateLabels.size
                    if (count > bestPublicSuffixLabelCount) bestPublicSuffixLabelCount = count
                }
            }
        }

        val publicSuffixLabelCount = if (bestPublicSuffixLabelCount < 0) 1 else bestPublicSuffixLabelCount
        val registrableLabelCount  = publicSuffixLabelCount + 1
        if (registrableLabelCount > labels.size) return null

        return labels.takeLast(registrableLabelCount).joinToString(".")
    }

    companion object {
        private const val ASSET_NAME = "public_suffix_list.dat"
    }
}
