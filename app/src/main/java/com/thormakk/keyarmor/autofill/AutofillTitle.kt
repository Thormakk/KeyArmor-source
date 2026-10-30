package com.thormakk.keyarmor.autofill

object AutofillTitle {

    private val GENERIC_LEADING_LABELS: Set<String> = setOf(
        "com", "org", "net", "edu", "gov", "mil", "int",
        "io", "co", "app", "dev", "me", "tv", "cc", "xyz", "info", "biz", "mobi",
        "android",
        "ar", "br", "mx", "cl", "uy", "py", "bo", "pe", "ve", "ec", "cr", "gt",
        "hn", "ni", "pa", "cu", "pr", "es", "pt", "us", "ca", "uk", "gb", "ie",
        "fr", "de", "it", "nl", "be", "ch", "at", "se", "no", "dk", "fi", "pl",
        "cz", "sk", "hu", "ro", "bg", "gr", "tr", "ru", "ua", "il", "ae", "sa",
        "eg", "za", "ng", "ke", "ma", "in", "pk", "bd", "lk", "cn", "jp", "kr",
        "tw", "hk", "sg", "my", "th", "vn", "ph", "id", "au", "nz",
    )

    internal val PACKAGE_ALIASES: Map<String, String> = emptyMap()

    internal val DOMAIN_ALIASES: Map<String, String> = emptyMap()

    fun fromRegistrableDomain(
        registrableDomain: String,
        aliases: Map<String, String> = DOMAIN_ALIASES,
    ): String {
        val domain = registrableDomain.trim().lowercase().removeSuffix(".")
        if (domain.isEmpty()) return ""
        aliases[domain]?.let { return it }
        if (looksLikeIpAddress(domain)) return registrableDomain.trim()
        return prettify(domain.substringBefore("."))
    }

    fun fromPackageName(
        packageName: String,
        aliases: Map<String, String> = PACKAGE_ALIASES,
    ): String {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return ""
        aliases[pkg.lowercase()]?.let { return it }

        val labels = pkg.split(".").filter { it.isNotBlank() }
        if (labels.isEmpty()) return prettify(pkg)

        val meaningful = labels.firstOrNull { it.lowercase() !in GENERIC_LEADING_LABELS }
            ?: labels.last()
        return prettify(meaningful)
    }

    private fun looksLikeIpAddress(value: String): Boolean =
        value.isNotEmpty() && value.all { it.isDigit() || it == '.' }

    private fun prettify(label: String): String =
        label.replaceFirstChar { it.uppercase() }
}
