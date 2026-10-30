package com.thormakk.keyarmor.presentation.screens.search

import com.thormakk.keyarmor.domain.model.BankAccount
import com.thormakk.keyarmor.domain.model.CreditCard
import com.thormakk.keyarmor.domain.model.CryptoWallet
import com.thormakk.keyarmor.domain.model.CustomEntry
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.IdentityDocument
import com.thormakk.keyarmor.domain.model.PasskeyCredential
import com.thormakk.keyarmor.domain.model.Reminder
import com.thormakk.keyarmor.domain.model.SecureNote
import com.thormakk.keyarmor.domain.model.TwoFactorAccount
import com.thormakk.keyarmor.domain.model.VaultEntry
import com.thormakk.keyarmor.domain.model.Vehicle
import java.util.Locale

internal fun textContains(haystack: String, needle: String): Boolean =
    needle.isBlank() || haystack.lowercase(Locale.ROOT).contains(needle.lowercase(Locale.ROOT))

internal fun categoryMatches(entryCategory: EntryCategory, filter: EntryCategory?): Boolean =
    filter == null || entryCategory == filter

internal fun tagsMatch(entryTags: List<String>, filter: Set<String>): Boolean =
    filter.isEmpty() || filter.all { tag ->
        entryTags.any { it.equals(tag, ignoreCase = true) }
    }

internal fun VaultEntry.exactMatches(q: String, cat: EntryCategory? = null, tags: Set<String> = emptySet()): Boolean {
    if (!categoryMatches(category, cat)) return false
    if (!tagsMatch(this.tags, tags)) return false
    if (q.isBlank()) return true
    return textContains(title, q) || textContains(username, q) ||
        textContains(email, q) || textContains(url, q) || textContains(notes, q)
}

internal fun VaultEntry.fuzzyMatchesPrimary(q: String): Boolean =
    FuzzySearch.matches(title, q)

internal fun BankAccount.exactMatches(q: String, cat: EntryCategory? = null, tags: Set<String> = emptySet()): Boolean {
    if (!categoryMatches(category, cat)) return false
    if (!tagsMatch(this.tags, tags)) return false
    if (q.isBlank()) return true
    return textContains(bankName, q) || textContains(alias, q) ||
        textContains(accountNumber, q) || textContains(cbuIban, q) ||
        textContains(swiftBic, q) || textContains(accountType, q) || textContains(notes, q)
}

internal fun BankAccount.fuzzyMatchesPrimary(q: String): Boolean =
    FuzzySearch.matches(bankName, q) || FuzzySearch.matches(alias, q)

internal fun CreditCard.exactMatches(q: String, cat: EntryCategory? = null, tags: Set<String> = emptySet()): Boolean {
    if (!categoryMatches(category, cat)) return false
    if (!tagsMatch(this.tags, tags)) return false
    if (q.isBlank()) return true
    return textContains(issuer, q) || textContains(brand, q) ||
        textContains(cardholder, q) || textContains(cardNumber, q)
}

internal fun CreditCard.fuzzyMatchesPrimary(q: String): Boolean =
    FuzzySearch.matches(issuer, q) || FuzzySearch.matches(brand, q)

internal fun SecureNote.exactMatches(q: String, cat: EntryCategory? = null, tags: Set<String> = emptySet()): Boolean {
    if (!categoryMatches(category, cat)) return false
    if (!tagsMatch(this.tags, tags)) return false
    if (q.isBlank()) return true
    return textContains(title, q) || textContains(content, q)
}

internal fun SecureNote.fuzzyMatchesPrimary(q: String): Boolean =
    FuzzySearch.matches(title, q)

internal fun CustomEntry.exactMatches(q: String, cat: EntryCategory? = null, tags: Set<String> = emptySet()): Boolean {
    if (!categoryMatches(category, cat)) return false
    if (!tagsMatch(this.tags, tags)) return false
    if (q.isBlank()) return true
    if (textContains(title, q)) return true
    return fields.any { textContains(it.label, q) || textContains(it.value, q) }
}

internal fun CustomEntry.fuzzyMatchesPrimary(q: String): Boolean =
    FuzzySearch.matches(title, q)

internal fun TwoFactorAccount.exactMatches(q: String, cat: EntryCategory? = null, tags: Set<String> = emptySet()): Boolean {
    if (!categoryMatches(category, cat)) return false
    if (!tagsMatch(this.tags, tags)) return false
    if (q.isBlank()) return true
    return textContains(name, q) || textContains(issuer, q) || textContains(accountLabel, q)
}

internal fun TwoFactorAccount.fuzzyMatchesPrimary(q: String): Boolean =
    FuzzySearch.matches(name, q) || FuzzySearch.matches(issuer, q)

internal fun CryptoWallet.exactMatches(q: String, cat: EntryCategory? = null, tags: Set<String> = emptySet()): Boolean {
    if (!categoryMatches(category, cat)) return false
    if (!tagsMatch(this.tags, tags)) return false
    if (q.isBlank()) return true
    return textContains(asset, q) || textContains(network, q) ||
        textContains(address, q) || textContains(exchangeUrl, q) || textContains(notes, q)
}

internal fun CryptoWallet.fuzzyMatchesPrimary(q: String): Boolean =
    FuzzySearch.matches(asset, q) || FuzzySearch.matches(network, q)

internal fun PasskeyCredential.exactMatches(q: String, cat: EntryCategory? = null, tags: Set<String> = emptySet()): Boolean {
    if (cat != null && category != cat.name) return false
    if (!tagsMatch(tagList, tags)) return false
    if (q.isBlank()) return true
    return textContains(rpName, q) || textContains(rpId, q) ||
        textContains(userName, q) || textContains(userDisplayName, q) ||
        textContains(customAccountLabel, q) || textContains(notes, q) ||
        textContains(urls, q)
}

internal fun PasskeyCredential.fuzzyMatchesPrimary(q: String): Boolean =
    FuzzySearch.matches(rpName, q) || FuzzySearch.matches(userName, q) ||
    FuzzySearch.matches(userDisplayName, q)

internal fun IdentityDocument.exactMatches(q: String, cat: EntryCategory? = null, tags: Set<String> = emptySet()): Boolean {
    if (!categoryMatches(category, cat)) return false
    if (!tagsMatch(this.tags, tags)) return false
    if (q.isBlank()) return true
    return textContains(docType, q) || textContains(holderName, q) ||
        textContains(issuingCountry, q) || textContains(notes, q)
}

internal fun IdentityDocument.fuzzyMatchesPrimary(q: String): Boolean =
    FuzzySearch.matches(holderName, q) || FuzzySearch.matches(docType, q)

internal fun Vehicle.exactMatches(q: String, cat: EntryCategory? = null, tags: Set<String> = emptySet()): Boolean {
    if (!categoryMatches(category, cat)) return false
    if (!tagsMatch(this.tags, tags)) return false
    if (q.isBlank()) return true
    return textContains(make, q) || textContains(model, q) ||
        textContains(licensePlate, q) || textContains(vin, q) ||
        textContains(insuranceCompany, q) || textContains(notes, q)
}

internal fun Vehicle.fuzzyMatchesPrimary(q: String): Boolean =
    FuzzySearch.matches(make, q) || FuzzySearch.matches(model, q)

internal fun Reminder.exactMatches(q: String): Boolean {
    if (q.isBlank()) return true
    return textContains(name, q) || textContains(note, q)
}

internal fun Reminder.fuzzyMatchesPrimary(q: String): Boolean =
    FuzzySearch.matches(name, q)

internal fun shouldRunFuzzy(q: String): Boolean = q.trim().length >= 4

