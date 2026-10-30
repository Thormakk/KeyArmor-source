package com.thormakk.keyarmor.presentation.screens.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.BreachResultPrefs
import com.thormakk.keyarmor.data.prefs.ExpiryNotificationPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.SecurityPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.remote.HibpPasswordChecker
import com.thormakk.keyarmor.data.remote.HibpResult
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.domain.model.VaultEntry
import com.thormakk.keyarmor.security.CommonPasswordsChecker
import com.thormakk.keyarmor.security.PasswordPatternChecker
import com.thormakk.keyarmor.security.PasswordStrengthCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PwnedEntry(
    val id:         String,
    val title:      String,
    val username:   String,
    val pwnedCount: Int
)

sealed class HibpCheckState {
    object Idle : HibpCheckState()
    data class Checking(val current: Int, val total: Int) : HibpCheckState()
    data class Done(val pwnedEntries: List<PwnedEntry>) : HibpCheckState()
    object NetworkError : HibpCheckState()
}

data class AuditEntry(
    val id:                  String,
    val title:               String,
    val username:            String,
    val strength:            Int,
    val isDuplicate:         Boolean = false,
    val isCommon:            Boolean = false,
    val isPatternBased:      Boolean = false,
    val containsServiceName: Boolean = false,
    val containsPersonalInfo:Boolean = false,
    val isRecycled:          Boolean = false,
    val similarTo:           String?  = null,
    val passwordAgeMonths:   Int      = 0,
    val domainGroup:         String?  = null,
    val isExpired:           Boolean = false,
    val isExpiringSoon:      Boolean = false,
    val expiryDate:          Long?   = null,
    val daysUntilExpiry:     Int?    = null
)

data class AuditUiState(
    val isLocked: Boolean = false,
    val isLoading:           Boolean          = true,
    val weakEntries:         List<AuditEntry> = emptyList(),
    val duplicateEntries:    List<AuditEntry> = emptyList(),
    val mediumEntries:       List<AuditEntry> = emptyList(),
    val commonEntries:       List<AuditEntry> = emptyList(),
    val agedEntries:         List<AuditEntry> = emptyList(),
    val patternEntries:      List<AuditEntry> = emptyList(),
    val similarEntries:      List<AuditEntry> = emptyList(),
    val serviceNameEntries:  List<AuditEntry> = emptyList(),
    val personalInfoEntries: List<AuditEntry> = emptyList(),
    val recycledEntries:     List<AuditEntry> = emptyList(),
    val domainGroupEntries:  List<AuditEntry> = emptyList(),
    val expiredEntries:      List<AuditEntry> = emptyList(),
    val expiringEntries:     List<AuditEntry> = emptyList(),
    val vaultHealthScore:    Int              = 100
) {
    val hasIssues get() = listOf(
        weakEntries, duplicateEntries, mediumEntries, commonEntries,
        agedEntries, patternEntries, similarEntries, serviceNameEntries,
        personalInfoEntries, recycledEntries, domainGroupEntries,
        expiredEntries, expiringEntries
    ).any { it.isNotEmpty() }

    val totalCount get() = listOf(
        weakEntries, duplicateEntries, mediumEntries, commonEntries,
        agedEntries, patternEntries, similarEntries, serviceNameEntries,
        personalInfoEntries, recycledEntries, domainGroupEntries,
        expiredEntries, expiringEntries
    ).sumOf { it.size }
}

@HiltViewModel
class AuditViewModel @Inject constructor(
    private val repository:        VaultRepository,
    private val planPrefs:         PlanPrefs,
    securityPrefs:     SecurityPrefs,
    private val expiryPrefs:       ExpiryNotificationPrefs,
    private val commonChecker:     CommonPasswordsChecker,
    private val patternChecker:    PasswordPatternChecker,
    private val hibpChecker:       HibpPasswordChecker,
    private val breachResultPrefs: BreachResultPrefs
) : ViewModel() {

    private val _hibpState = MutableStateFlow<HibpCheckState>(HibpCheckState.Idle)
    val hibpState: StateFlow<HibpCheckState> = _hibpState.asStateFlow()

    private val _resultadoGuardado = MutableStateFlow(breachResultPrefs.load())

    val lastPwnedResult: StateFlow<List<PwnedEntry>> = combine(_resultadoGuardado, repository.observeEntries()) { r, entries ->
        entries.filter { !it.isDeleted && it.id in r.conteos && it.updatedAt <= r.checkedAt }
            .map { PwnedEntry(it.id, it.title, it.username.ifEmpty { it.email }, r.conteos[it.id] ?: 0) }
            .sortedByDescending { it.pwnedCount }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val lastPwnedPasswordCount: StateFlow<Int> = combine(lastPwnedResult, repository.observeEntries()) { pwned, entries ->
        val ids = pwned.map { it.id }.toSet()
        entries.filter { it.id in ids }.map { it.password }.distinct().size
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun recargarResultadoGuardado() { _resultadoGuardado.value = breachResultPrefs.load() }

    fun checkBreaches() {
        if (planPrefs.effectivePlan() == UserPlan.FREE) return
        if (_hibpState.value is HibpCheckState.Checking) return
        viewModelScope.launch {
            val allEntries = repository.observeEntries().first()
                .filter { it.password.isNotEmpty() }

            if (allEntries.isEmpty()) {
                _hibpState.value = HibpCheckState.Done(emptyList())
                breachResultPrefs.save(emptyMap())
                recargarResultadoGuardado()
                return@launch
            }

            val byPassword = allEntries.groupBy { it.password }
            val uniquePasswords = byPassword.keys.toList()

            _hibpState.value = HibpCheckState.Checking(0, uniquePasswords.size)

            val pwnedMap = mutableMapOf<String, Int>()

            uniquePasswords.forEachIndexed { i, password ->
                _hibpState.value = HibpCheckState.Checking(i + 1, uniquePasswords.size)
                when (val result = hibpChecker.check(password)) {
                    is HibpResult.Pwned       -> pwnedMap[password] = result.count
                    is HibpResult.RateLimited,
                    is HibpResult.NetworkError -> if (i == 0) {
                        _hibpState.value = HibpCheckState.NetworkError
                        return@launch
                    }
                    is HibpResult.Clean       -> Unit
                }
            }

            val pwned = allEntries
                .filter { it.password in pwnedMap }
                .map { entry ->
                    PwnedEntry(
                        id         = entry.id,
                        title      = entry.title,
                        username   = entry.username.ifEmpty { entry.email },
                        pwnedCount = pwnedMap[entry.password] ?: 0
                    )
                }
                .sortedByDescending { it.pwnedCount }

            breachResultPrefs.save(pwned.associate { it.id to it.pwnedCount })
            recargarResultadoGuardado()
            _hibpState.value = HibpCheckState.Done(pwned)
        }
    }

    fun resetHibpCheck() {
        _hibpState.value = HibpCheckState.Idle
    }

    val uiState: StateFlow<AuditUiState> = combine(
        planPrefs.planFlow,
        repository.observeEntries()
    ) { plan, entries ->
        if (plan == UserPlan.FREE) return@combine AuditUiState(isLoading = false, isLocked = true)
        val now          = System.currentTimeMillis()
        val withPassword = entries.filter { it.password.isNotEmpty() }

        val passwordGroups     = withPassword.groupBy { it.password }
        val duplicatePasswords = passwordGroups.filter { it.value.size > 1 }.keys.toSet()

        val weakSet = withPassword.filter { passwordStrength(it.password) <= 1 }.toSet()

        val weakEntries = weakSet.map { e ->
            val str = passwordStrength(e.password)
            AuditEntry(
                id                   = e.id,
                title                = e.title,
                username             = e.username.ifEmpty { e.email },
                strength             = str,
                isDuplicate          = e.password in duplicatePasswords,
                isCommon             = commonChecker.isCommon(e.password),
                isPatternBased       = patternChecker.isPatternBased(e.password, str),
                containsServiceName  = patternChecker.containsServiceName(e.password, e.title),
                containsPersonalInfo = patternChecker.containsPersonalInfo(e.password, e.username, e.email),
                passwordAgeMonths    = ageInMonths(e.updatedAt, now)
            )
        }

        val duplicateEntries = withPassword
            .filter { it !in weakSet && it.password in duplicatePasswords }
            .map { e ->
                val str = passwordStrength(e.password)
                AuditEntry(
                    id                   = e.id,
                    title                = e.title,
                    username             = e.username.ifEmpty { e.email },
                    strength             = str,
                    isDuplicate          = true,
                    isCommon             = commonChecker.isCommon(e.password),
                    isPatternBased       = patternChecker.isPatternBased(e.password, str),
                    containsServiceName  = patternChecker.containsServiceName(e.password, e.title),
                    containsPersonalInfo = patternChecker.containsPersonalInfo(e.password, e.username, e.email),
                    passwordAgeMonths    = ageInMonths(e.updatedAt, now)
                )
            }

        val mediumEntries = withPassword
            .filter { it !in weakSet }
            .filter { passwordStrength(it.password) == 2 }
            .map { e ->
                val str = passwordStrength(e.password)
                AuditEntry(
                    id                   = e.id,
                    title                = e.title,
                    username             = e.username.ifEmpty { e.email },
                    strength             = str,
                    isCommon             = commonChecker.isCommon(e.password),
                    isPatternBased       = patternChecker.isPatternBased(e.password, str),
                    containsServiceName  = patternChecker.containsServiceName(e.password, e.title),
                    containsPersonalInfo = patternChecker.containsPersonalInfo(e.password, e.username, e.email),
                    passwordAgeMonths    = ageInMonths(e.updatedAt, now)
                )
            }

        val alreadyReported = (weakEntries + duplicateEntries + mediumEntries).map { it.id }.toSet()

        val commonEntries = withPassword
            .filter { it.id !in alreadyReported && commonChecker.isCommon(it.password) }
            .map { e ->
                val str = passwordStrength(e.password)
                AuditEntry(
                    id                   = e.id,
                    title                = e.title,
                    username             = e.username.ifEmpty { e.email },
                    strength             = str,
                    isCommon             = true,
                    isPatternBased       = patternChecker.isPatternBased(e.password, str),
                    containsServiceName  = patternChecker.containsServiceName(e.password, e.title),
                    containsPersonalInfo = patternChecker.containsPersonalInfo(e.password, e.username, e.email),
                    passwordAgeMonths    = ageInMonths(e.updatedAt, now)
                )
            }

        val reportedWithCommon = alreadyReported + commonEntries.map { it.id }

        val agedEntries = withPassword
            .filter { it.id !in reportedWithCommon }
            .filter { ageInMonths(it.updatedAt, now) >= 12 }
            .map { e ->
                AuditEntry(
                    id                   = e.id,
                    title                = e.title,
                    username             = e.username.ifEmpty { e.email },
                    strength             = passwordStrength(e.password),
                    containsServiceName  = patternChecker.containsServiceName(e.password, e.title),
                    containsPersonalInfo = patternChecker.containsPersonalInfo(e.password, e.username, e.email),
                    passwordAgeMonths    = ageInMonths(e.updatedAt, now)
                )
            }
            .sortedByDescending { it.passwordAgeMonths }

        val reportedWithAged = reportedWithCommon + agedEntries.map { it.id }

        val patternEntries = withPassword
            .filter { it.id !in reportedWithAged }
            .filter { patternChecker.isPatternBased(it.password, passwordStrength(it.password)) }
            .map { e ->
                AuditEntry(
                    id               = e.id,
                    title            = e.title,
                    username         = e.username.ifEmpty { e.email },
                    strength         = passwordStrength(e.password),
                    isPatternBased   = true,
                    passwordAgeMonths = ageInMonths(e.updatedAt, now)
                )
            }

        val reportedWithPattern = reportedWithAged + patternEntries.map { it.id }

        val serviceNameEntries = withPassword
            .filter { it.id !in reportedWithPattern }
            .filter { patternChecker.containsServiceName(it.password, it.title) }
            .map { e ->
                AuditEntry(
                    id                  = e.id,
                    title               = e.title,
                    username            = e.username.ifEmpty { e.email },
                    strength            = passwordStrength(e.password),
                    containsServiceName = true,
                    passwordAgeMonths   = ageInMonths(e.updatedAt, now)
                )
            }

        val reportedWithService = reportedWithPattern + serviceNameEntries.map { it.id }

        val personalInfoEntries = withPassword
            .filter { it.id !in reportedWithService }
            .filter { patternChecker.containsPersonalInfo(it.password, it.username, it.email) }
            .map { e ->
                AuditEntry(
                    id                   = e.id,
                    title                = e.title,
                    username             = e.username.ifEmpty { e.email },
                    strength             = passwordStrength(e.password),
                    containsPersonalInfo = true,
                    passwordAgeMonths    = ageInMonths(e.updatedAt, now)
                )
            }

        val reportedWithPersonal = reportedWithService + personalInfoEntries.map { it.id }

        val domainGroupEntries = buildDomainGroupEntries(
            withPassword.filter { it.id !in reportedWithPersonal }
        )

        val reportedWithDomainGroup = reportedWithPersonal + domainGroupEntries.map { it.id }

        val unreported = withPassword.filter { it.id !in reportedWithDomainGroup }
        val similarEntries = buildSimilarEntries(unreported)

        val reportedWithSimilar = reportedWithDomainGroup + similarEntries.map { it.id }

        val recycledEntries = mutableListOf<AuditEntry>()
        for (e in withPassword.filter { it.id !in reportedWithSimilar }) {
            val hash   = repository.computeHistoryHash(e.password) ?: continue
            val hashes = runCatching { repository.getPasswordHashes(e.id) }.getOrDefault(emptyList())
            if (hashes.contains(hash)) {
                recycledEntries.add(AuditEntry(
                    id                = e.id,
                    title             = e.title,
                    username          = e.username.ifEmpty { e.email },
                    strength          = passwordStrength(e.password),
                    isRecycled        = true,
                    passwordAgeMonths = ageInMonths(e.updatedAt, now)
                ))
            }
        }

        val reportedWithRecycled = reportedWithSimilar + recycledEntries.map { it.id }

        val warnMs = expiryPrefs.getWarningDays() * MS_PER_DAY

        val expiredEntries = entries
            .filter { it.expiryDate != null && it.expiryDate < now }
            .map { e ->
                val daysOverdue = ((now - e.expiryDate!!) / MS_PER_DAY).toInt()
                AuditEntry(
                    id              = e.id,
                    title           = e.title,
                    username        = e.username.ifEmpty { e.email },
                    strength        = passwordStrength(e.password),
                    isExpired       = true,
                    expiryDate      = e.expiryDate,
                    daysUntilExpiry = -daysOverdue
                )
            }
            .sortedBy { it.daysUntilExpiry }

        val expiredIds = expiredEntries.map { it.id }.toSet()

        val expiringEntries = entries
            .filter { e ->
                e.expiryDate != null &&
                e.expiryDate >= now &&
                e.expiryDate <= now + warnMs &&
                e.id !in expiredIds
            }
            .map { e ->
                val daysLeft = (((e.expiryDate ?: now) - now) / MS_PER_DAY).toInt()
                AuditEntry(
                    id              = e.id,
                    title           = e.title,
                    username        = e.username.ifEmpty { e.email },
                    strength        = passwordStrength(e.password),
                    isExpiringSoon  = true,
                    expiryDate      = e.expiryDate,
                    daysUntilExpiry = daysLeft
                )
            }
            .sortedBy { it.daysUntilExpiry }

        val healthScore = computeHealthScore(
            total           = withPassword.size,
            weak            = weakEntries.size,
            duplicate       = duplicateEntries.size,
            medium          = mediumEntries.size,
            common          = commonEntries.size,
            aged            = agedEntries.size,
            pattern         = patternEntries.size,
            similar         = similarEntries.size,
            serviceName     = serviceNameEntries.size,
            personalInfo    = personalInfoEntries.size,
            recycled        = recycledEntries.size,
            domainGroup     = domainGroupEntries.size,
            expired         = expiredEntries.size,
            expiring        = expiringEntries.size
        )

        AuditUiState(
            isLoading           = false,
            weakEntries         = weakEntries,
            duplicateEntries    = duplicateEntries,
            mediumEntries       = mediumEntries,
            commonEntries       = commonEntries,
            agedEntries         = agedEntries,
            patternEntries      = patternEntries,
            similarEntries      = similarEntries,
            serviceNameEntries  = serviceNameEntries,
            personalInfoEntries = personalInfoEntries,
            recycledEntries     = recycledEntries,
            domainGroupEntries  = domainGroupEntries,
            expiredEntries      = expiredEntries,
            expiringEntries     = expiringEntries,
            vaultHealthScore    = healthScore
        )
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = AuditUiState()
        )

    init {
        securityPrefs.updateLastReview()
    }

    private fun buildSimilarEntries(
        entries: List<VaultEntry>
    ): List<AuditEntry> {
        val result    = mutableListOf<AuditEntry>()
        val seenIds   = mutableSetOf<String>()
        for (i in entries.indices) {
            if (entries[i].id in seenIds) continue
            for (j in i + 1 until entries.size) {
                if (entries[j].id in seenIds) continue
                if (patternChecker.areSimilar(entries[i].password, entries[j].password)) {
                    if (entries[i].id !in seenIds) {
                        result.add(AuditEntry(
                            id         = entries[i].id,
                            title      = entries[i].title,
                            username   = entries[i].username.ifEmpty { entries[i].email },
                            strength   = passwordStrength(entries[i].password),
                            similarTo  = entries[j].title,
                            passwordAgeMonths = ageInMonths(entries[i].updatedAt, System.currentTimeMillis())
                        ))
                        seenIds.add(entries[i].id)
                    }
                    if (entries[j].id !in seenIds) {
                        result.add(AuditEntry(
                            id         = entries[j].id,
                            title      = entries[j].title,
                            username   = entries[j].username.ifEmpty { entries[j].email },
                            strength   = passwordStrength(entries[j].password),
                            similarTo  = entries[i].title,
                            passwordAgeMonths = ageInMonths(entries[j].updatedAt, System.currentTimeMillis())
                        ))
                        seenIds.add(entries[j].id)
                    }
                }
            }
        }
        return result
    }

    private fun buildDomainGroupEntries(
        entries: List<VaultEntry>
    ): List<AuditEntry> {
        val byDomain = entries
            .filter { it.url.isNotEmpty() }
            .groupBy { patternChecker.extractDomain(it.url) }
            .filterKeys { it != null }

        val result  = mutableListOf<AuditEntry>()
        val seenIds = mutableSetOf<String>()
        val now     = System.currentTimeMillis()

        for ((domain, group) in byDomain) {
            if (group.size < 2) continue
            for (i in group.indices) {
                if (group[i].id in seenIds) continue
                for (j in i + 1 until group.size) {
                    if (group[j].id in seenIds) continue
                    if (patternChecker.areSimilar(group[i].password, group[j].password)) {
                        if (group[i].id !in seenIds) {
                            result.add(AuditEntry(
                                id                = group[i].id,
                                title             = group[i].title,
                                username          = group[i].username.ifEmpty { group[i].email },
                                strength          = passwordStrength(group[i].password),
                                similarTo         = group[j].title,
                                domainGroup       = domain,
                                passwordAgeMonths = ageInMonths(group[i].updatedAt, now)
                            ))
                            seenIds.add(group[i].id)
                        }
                        if (group[j].id !in seenIds) {
                            result.add(AuditEntry(
                                id                = group[j].id,
                                title             = group[j].title,
                                username          = group[j].username.ifEmpty { group[j].email },
                                strength          = passwordStrength(group[j].password),
                                similarTo         = group[i].title,
                                domainGroup       = domain,
                                passwordAgeMonths = ageInMonths(group[j].updatedAt, now)
                            ))
                            seenIds.add(group[j].id)
                        }
                    }
                }
            }
        }
        return result.distinctBy { it.id }
    }

    private fun computeHealthScore(
        total: Int,
        weak: Int, duplicate: Int, medium: Int, common: Int, aged: Int,
        pattern: Int, similar: Int, serviceName: Int, personalInfo: Int,
        recycled: Int, domainGroup: Int,
        expired: Int = 0, expiring: Int = 0
    ): Int {
        if (total == 0) return 100
        val t = total.toFloat()

        val totalWithIssues = minOf(
            weak + duplicate + medium + common + aged + pattern +
            similar + serviceName + personalInfo + recycled + domainGroup,
            total
        )
        val base = ((total - totalWithIssues).toFloat() / t * 100).toInt()

        var severity = 0
        severity += minOf(((weak      / t) * 20).toInt(), 20)
        severity += minOf(((common    / t) * 15).toInt(), 15)
        severity += minOf(((duplicate / t) * 10).toInt(), 10)
        severity += minOf(((expired   / t) * 20).toInt(), 20)
        severity += minOf(((expiring  / t) *  8).toInt(),  8)

        return (base - severity).coerceIn(0, 100)
    }

    private fun passwordStrength(password: String): Int =
        PasswordStrengthCalculator.calculate(password).coerceAtLeast(0)

    private fun ageInMonths(updatedAt: Long, now: Long): Int {
        if (updatedAt <= 0L) return 0
        val diffMs = now - updatedAt
        if (diffMs <= 0L) return 0
        return (diffMs / MS_PER_MONTH).toInt()
    }


    companion object {
        @Volatile var abrirBrechasAlEntrar = false

        private const val MS_PER_DAY   = 24L * 60L * 60L * 1_000L
        private const val MS_PER_MONTH = 30L * MS_PER_DAY
    }
}
