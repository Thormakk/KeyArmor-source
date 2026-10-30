package com.thormakk.keyarmor.autofill

import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.domain.model.TwoFactorAccount
import java.net.URI

suspend fun matchesTotpAccount(
    account:          TwoFactorAccount,
    parsed:           ParsedAutofillRequest,
    vaultRepository:  VaultRepository,
    publicSuffixList: PublicSuffixList,
): Boolean {
    if (account.url.isNotEmpty()) {
        return AutofillMatcher.matches(account.url, parsed.webDomain, parsed.packageName, publicSuffixList)
    }

    if (account.linkedPasswordId.isNotEmpty()) {
        val linked = vaultRepository.getEntry(account.linkedPasswordId)
        if (linked != null) {
            return linked.urls.any { AutofillMatcher.matches(it, parsed.webDomain, parsed.packageName, publicSuffixList) }
        }
    }

    val candidates = listOf(account.issuer, account.name)
        .map { it.lowercase().trim() }
        .filter { it.isNotEmpty() }
    if (candidates.isEmpty()) return false

    val domainRoot = parsed.webDomain?.lowercase()?.let { totpRootDomain(it, publicSuffixList) }
    val pkgName    = parsed.packageName.lowercase()

    for (candidate in candidates) {
        val hostPart      = totpExtractHost(candidate)
        val candidateRoot = (hostPart ?: candidate)
            .substringBefore(".")
            .replace(" ", "")
        if (candidateRoot.length < 5) continue

        if (domainRoot != null) {
            if (domainRoot.substringBefore(".") == candidateRoot) return true
        }
        if (pkgName.split(".").any { it == candidateRoot }) return true
    }
    return false
}

private fun totpRootDomain(host: String, publicSuffixList: PublicSuffixList): String =
    publicSuffixList.getRegistrableDomain(host) ?: run {
        val parts = host.split(".")
        if (parts.size >= 2) parts.takeLast(2).joinToString(".") else host
    }

private fun totpExtractHost(raw: String): String? {
    val withScheme = if (raw.contains("://")) raw else "https://$raw"
    return try {
        URI(withScheme).host?.removePrefix("www.")
    } catch (_: Exception) {
        raw.removePrefix("www.").substringBefore("/")
    }
}
