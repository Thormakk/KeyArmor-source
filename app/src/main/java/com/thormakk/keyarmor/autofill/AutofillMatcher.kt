package com.thormakk.keyarmor.autofill

import java.net.URI

object AutofillMatcher {

    fun matchesAnyUrl(urls: List<String>, webDomain: String?, packageName: String, publicSuffixList: PublicSuffixList): Boolean {
        if (urls.isEmpty()) return false
        return urls.any { matches(it, webDomain, packageName, publicSuffixList) }
    }

    fun matches(entryUrl: String, webDomain: String?, packageName: String, publicSuffixList: PublicSuffixList): Boolean {
        if (entryUrl.isBlank()) return false
        val entryHost = extractHost(entryUrl)?.lowercase() ?: return false
        val web = webDomain?.lowercase()
        val pkg = packageName.lowercase()

        if (web != null) {
            if (web == entryHost) return true
            if (entryHost.contains(".") && publicSuffixList.getRegistrableDomain(entryHost) == null) {
                return false
            }
            return web.endsWith(".$entryHost")
        }

        return pkg == entryUrl.lowercase() || pkg == entryHost
    }

    fun extractHost(raw: String): String? {
        val withScheme = if (raw.contains("://")) raw else "https://$raw"
        return try {
            URI(withScheme).host?.removePrefix("www.")
        } catch (_: Exception) {
            raw.removePrefix("www.").substringBefore("/")
        }
    }
}
