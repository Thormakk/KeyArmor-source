@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
package com.thormakk.keyarmor.presentation.screens.audit

import com.thormakk.keyarmor.presentation.components.warningColor
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.withFrameNanos
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.unit.IntSize
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea

private val LightWeak      = Color(0xFFE53935)
private val LightDuplicate = Color(0xFFFF6F00)
private val LightMedium    = Color(0xFFFFB300)
private val LightCommon    = Color(0xFF8E24AA)
private val LightAged      = Color(0xFF00897B)
private val LightPattern   = Color(0xFFEF6C00)
private val LightSimilar   = Color(0xFF6D4C41)
private val LightService   = Color(0xFF1565C0)
private val LightPersonal  = Color(0xFFAD1457)
private val LightRecycled  = Color(0xFF37474F)
private val LightDomain    = Color(0xFF00695C)
private val LightPriority  = Color(0xFFB71C1C)
private val LightExpired   = Color(0xFFC62828)
private val LightExpiring  = Color(0xFFF57F17)
private val LightStrong    = Color(0xFF43A047)
private val LightBreach    = Color(0xFF6A1B9A)

private val DarkWeak      = Color(0xFFEF9A9A)
private val DarkDuplicate = Color(0xFFFFCC80)
private val DarkMedium    = Color(0xFFFFB300)
private val DarkCommon    = Color(0xFFCE93D8)
private val DarkAged      = Color(0xFF80DEEA)
private val DarkPattern   = Color(0xFFFFAB40)
private val DarkSimilar   = Color(0xFFBCAAA4)
private val DarkService   = Color(0xFF90CAF9)
private val DarkPersonal  = Color(0xFFF48FB1)
private val DarkRecycled  = Color(0xFFB0BEC5)
private val DarkDomain    = Color(0xFF4DB6AC)
private val DarkPriority  = Color(0xFFEF9A9A)
private val DarkExpired   = Color(0xFFFF8A80)
private val DarkExpiring  = Color(0xFFFFD54F)
private val DarkStrong    = Color(0xFFA5D6A7)
private val DarkBreach    = Color(0xFFE1BEE7)

private data class AuditColors(
    val weak: Color, val duplicate: Color, val medium: Color,
    val common: Color, val aged: Color, val pattern: Color,
    val similar: Color, val service: Color, val personal: Color,
    val recycled: Color, val domain: Color, val priority: Color,
    val expired: Color, val expiring: Color,
    val strong: Color,
    val breach: Color
)

@Composable
private fun auditColors(): AuditColors {
    val bg  = MaterialTheme.colorScheme.background
    val lum = 0.2126f * bg.red + 0.7152f * bg.green + 0.0722f * bg.blue
    return if (lum > 0.5f) AuditColors(
        weak=LightWeak, duplicate=LightDuplicate, medium=LightMedium,
        common=LightCommon, aged=LightAged, pattern=LightPattern,
        similar=LightSimilar, service=LightService, personal=LightPersonal,
        recycled=LightRecycled, domain=LightDomain, priority=LightPriority,
        expired=LightExpired, expiring=LightExpiring,
        strong=LightStrong, breach=LightBreach
    ) else AuditColors(
        weak=DarkWeak, duplicate=DarkDuplicate, medium=DarkMedium,
        common=DarkCommon, aged=DarkAged, pattern=DarkPattern,
        similar=DarkSimilar, service=DarkService, personal=DarkPersonal,
        recycled=DarkRecycled, domain=DarkDomain, priority=DarkPriority,
        expired=DarkExpired, expiring=DarkExpiring,
        strong=DarkStrong, breach=DarkBreach
    )
}

private enum class AuditCategory {
    WEAK, EXPIRED, BREACH, DUPLICATE, PATTERN, SIMILAR, SERVICE, PERSONAL,
    DOMAIN, RECYCLED, AGED, EXPIRING, COMMON, MEDIUM
}

private data class AuditCategorySection(
    val category:      AuditCategory,
    val entries:        List<AuditEntry>,
    val color:          Color,
    val titleRes:       Int,
    val descRes:        Int,
    val onViewInVault:  (() -> Unit)?              = null,
    val extraLabel:     @Composable (AuditEntry) -> String? = { null }
)

@Composable
fun AuditScreen(
    onBack:                      () -> Unit,
    onPlansClick:                () -> Unit = {},
    onEntryClick:                (String) -> Unit,
    onNavigateToWeakPasswords:   () -> Unit = {},
    onNavigateToMediumPasswords: () -> Unit = {},
    viewModel:                   AuditViewModel = hiltViewModel()
) {
    val uiState        by viewModel.uiState.collectAsStateWithLifecycle()
    val hibpState      by viewModel.hibpState.collectAsStateWithLifecycle()
    val lastPwnedResult by viewModel.lastPwnedResult.collectAsStateWithLifecycle()
    val lastPwnedPasswordCount by viewModel.lastPwnedPasswordCount.collectAsStateWithLifecycle()

    if (uiState.isLocked) {
        PremiumLockedScreen(
            featureName  = stringResource(R.string.hub_audit_title),
            onBack       = onBack,
            onPlansClick = onPlansClick
        )
        return
    }
    val c = auditColors()

    val sections = buildList {
        if (uiState.weakEntries.isNotEmpty()) add(
            AuditCategorySection(
                category      = AuditCategory.WEAK,
                entries       = uiState.weakEntries,
                color         = c.weak,
                titleRes      = R.string.audit_section_weak,
                descRes       = R.string.audit_section_weak_desc,
                onViewInVault = onNavigateToWeakPasswords
            )
        )
        if (uiState.expiredEntries.isNotEmpty()) add(
            AuditCategorySection(
                category   = AuditCategory.EXPIRED,
                entries    = uiState.expiredEntries,
                color      = c.expired,
                titleRes   = R.string.audit_section_expired,
                descRes    = R.string.audit_section_expired_desc,
                extraLabel = { entry -> expiryLabel(entry) }
            )
        )
        if (lastPwnedResult.isNotEmpty()) add(
            AuditCategorySection(
                category   = AuditCategory.BREACH,
                entries    = lastPwnedResult.map { pwned ->
                    AuditEntry(id = pwned.id, title = pwned.title, username = pwned.username, strength = -1)
                },
                color      = c.breach,
                titleRes   = R.string.audit_section_breach,
                descRes    = R.string.hibp_found_subtitle,
                extraLabel = { entry ->
                    lastPwnedResult.firstOrNull { it.id == entry.id }?.let { pwned ->
                        if (pwned.pwnedCount == 1) stringResource(R.string.hibp_count_once)
                        else stringResource(R.string.hibp_count, pwned.pwnedCount)
                    }
                }
            )
        )
        if (uiState.duplicateEntries.isNotEmpty()) add(
            AuditCategorySection(
                category = AuditCategory.DUPLICATE,
                entries  = uiState.duplicateEntries,
                color    = c.duplicate,
                titleRes = R.string.audit_section_duplicate,
                descRes  = R.string.audit_section_duplicate_desc
            )
        )
        if (uiState.patternEntries.isNotEmpty()) add(
            AuditCategorySection(
                category = AuditCategory.PATTERN,
                entries  = uiState.patternEntries,
                color    = c.pattern,
                titleRes = R.string.audit_section_pattern,
                descRes  = R.string.audit_section_pattern_desc
            )
        )
        if (uiState.similarEntries.isNotEmpty()) add(
            AuditCategorySection(
                category   = AuditCategory.SIMILAR,
                entries    = uiState.similarEntries,
                color      = c.similar,
                titleRes   = R.string.audit_section_similar,
                descRes    = R.string.audit_section_similar_desc,
                extraLabel = { entry -> entry.similarTo?.let { stringResource(R.string.audit_similar_to, it) } }
            )
        )
        if (uiState.serviceNameEntries.isNotEmpty()) add(
            AuditCategorySection(
                category = AuditCategory.SERVICE,
                entries  = uiState.serviceNameEntries,
                color    = c.service,
                titleRes = R.string.audit_section_service_name,
                descRes  = R.string.audit_section_service_name_desc
            )
        )
        if (uiState.personalInfoEntries.isNotEmpty()) add(
            AuditCategorySection(
                category = AuditCategory.PERSONAL,
                entries  = uiState.personalInfoEntries,
                color    = c.personal,
                titleRes = R.string.audit_section_personal_info,
                descRes  = R.string.audit_section_personal_info_desc
            )
        )
        if (uiState.domainGroupEntries.isNotEmpty()) add(
            AuditCategorySection(
                category   = AuditCategory.DOMAIN,
                entries    = uiState.domainGroupEntries,
                color      = c.domain,
                titleRes   = R.string.audit_section_domain_group,
                descRes    = R.string.audit_section_domain_group_desc,
                extraLabel = { entry -> entry.domainGroup }
            )
        )
        if (uiState.recycledEntries.isNotEmpty()) add(
            AuditCategorySection(
                category = AuditCategory.RECYCLED,
                entries  = uiState.recycledEntries,
                color    = c.recycled,
                titleRes = R.string.audit_section_recycled,
                descRes  = R.string.audit_section_recycled_desc
            )
        )
        if (uiState.agedEntries.isNotEmpty()) add(
            AuditCategorySection(
                category   = AuditCategory.AGED,
                entries    = uiState.agedEntries,
                color      = c.aged,
                titleRes   = R.string.audit_section_aged,
                descRes    = R.string.audit_section_aged_desc,
                extraLabel = { entry -> ageLabel(entry.passwordAgeMonths) }
            )
        )
        if (uiState.expiringEntries.isNotEmpty()) add(
            AuditCategorySection(
                category   = AuditCategory.EXPIRING,
                entries    = uiState.expiringEntries,
                color      = c.expiring,
                titleRes   = R.string.audit_section_expiring,
                descRes    = R.string.audit_section_expiring_desc,
                extraLabel = { entry -> expiryLabel(entry) }
            )
        )
        if (uiState.commonEntries.isNotEmpty()) add(
            AuditCategorySection(
                category = AuditCategory.COMMON,
                entries  = uiState.commonEntries,
                color    = c.common,
                titleRes = R.string.audit_section_common,
                descRes  = R.string.audit_section_common_desc
            )
        )
        if (uiState.mediumEntries.isNotEmpty()) add(
            AuditCategorySection(
                category      = AuditCategory.MEDIUM,
                entries       = uiState.mediumEntries,
                color         = c.medium,
                titleRes      = R.string.audit_section_medium,
                descRes       = R.string.audit_section_medium_desc,
                onViewInVault = onNavigateToMediumPasswords
            )
        )
    }

    val hasIssues  = sections.isNotEmpty()
    val totalCount = sections.sumOf { it.entries.size }

    var expandedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    val scope     = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var blinkRequest by remember { mutableStateOf<Pair<AuditCategory, Long>?>(null) }

    fun expandAndScrollTo(category: AuditCategory) {
        expandedCategory = category.name
        val catIndex = sections.indexOfFirst { it.category == category }
        if (catIndex < 0) return
        scope.launch {
            listState.animateScrollToItem(2 + catIndex * 2, scrollOffset = 0)
            delay(150)
            blinkRequest = category to System.nanoTime()
        }
    }

    val hayBrechas = sections.any { it.category == AuditCategory.BREACH }
    LaunchedEffect(hayBrechas) {
        if (hayBrechas && AuditViewModel.abrirBrechasAlEntrar) {
            AuditViewModel.abrirBrechasAlEntrar = false
            expandAndScrollTo(AuditCategory.BREACH)
        }
    }

    fun alternarSiguiendo(category: AuditCategory) {
        val estabaAbierta = expandedCategory == category.name
        expandedCategory = if (estabaAbierta) null else category.name
        if (estabaAbierta) return
        val clave = "cat_${category.name}"
        scope.launch {
            val y0 = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == clave }?.offset
                ?: return@launch
            val objetivo = listState.layoutInfo.viewportSize.height * 0.2f
            if (y0 <= objetivo) return@launch
            val inicio = withFrameNanos { it }
            while (true) {
                val p = ((withFrameNanos { it } - inicio) / 700_000_000f).coerceIn(0f, 1f)
                val deseado = y0 + (objetivo - y0) * FastOutSlowInEasing.transform(p)
                val actual = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == clave }?.offset
                    ?: break
                val delta = actual - deseado
                if (kotlin.math.abs(delta) >= 1f) listState.scrollBy(delta)
                if (p >= 1f) break
            }
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .background(MaterialTheme.colorScheme.surface)
                )
                TopAppBar(
                    actions = {
                        CaptureAllowedBadge()
                        ThemeTrialBadge()
                    },
                    windowInsets = WindowInsets(0),
                    title = {
                        TextoDeUnaLinea(
                            stringResource(R.string.hub_audit_title),
                            style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                            fontWeight = FontWeight.SemiBold,
                            color      = sectionTitleColor()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = topBarColors()
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        LazyColumn(
            state          = listState,
            modifier       = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 7.dp, vertical = 8.dp)
        ) {

            item(key = "audit_top") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.coachMarkAnchor("audit", "score")) {
                        HealthScoreCard(score = uiState.vaultHealthScore)
                    }

                    Box(Modifier.coachMarkAnchor("audit", "hibp")) {
                        HibpCheckCard(
                            state       = hibpState,
                            breachColor = c.breach,
                            passwordCount = lastPwnedPasswordCount,
                            onCheck     = { viewModel.checkBreaches() },
                            onReset     = { viewModel.resetHibpCheck() }
                        )
                    }

                    if (!hasIssues) {
                        AllClearCard()
                    } else {
                        AuditSummaryCard(
                            totalCount  = totalCount,
                            sections    = sections,
                            onChipClick = ::expandAndScrollTo
                        )
                    }
                }
            }

            if (hasIssues) {
                item(key = "audit_top_gap") { Spacer(modifier = Modifier.height(8.dp)) }

                sections.forEachIndexed { index, section ->
                    val shape = when {
                        sections.size == 1           -> RoundedCornerShape(10.dp)
                        index == 0                   -> RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                        index == sections.lastIndex  -> RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)
                        else                          -> RoundedCornerShape(0.dp)
                    }
                    item(key = "cat_${section.category.name}") {
                        AuditCategoryCard(
                            section      = section,
                            shape        = shape,
                            expanded     = expandedCategory == section.category.name,
                            blinkSignal  = blinkRequest?.takeIf { it.first == section.category }?.second,
                            onToggle     = { alternarSiguiendo(section.category) },
                            onEntryClick = onEntryClick
                        )
                    }
                    if (index != sections.lastIndex) {
                        item(key = "cat_gap_${section.category.name}") {
                            Spacer(modifier = Modifier.height(0.5.dp))
                        }
                    }
                }
            }

            item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun HealthScoreCard(score: Int) {
    val c = auditColors()
    val (color, label) = when {
        score >= 80 -> c.strong    to stringResource(R.string.audit_health_excellent)
        score >= 60 -> c.medium    to stringResource(R.string.audit_health_fair)
        score >= 40 -> c.duplicate to stringResource(R.string.audit_health_poor)
        else        -> c.weak      to stringResource(R.string.audit_health_critical)
    }
    Surface(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        color     = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier             = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
                CircularProgressIndicator(
                    progress     = { score / 100f },
                    modifier     = Modifier.fillMaxSize(),
                    color        = color,
                    trackColor   = color.copy(alpha = 0.15f),
                    strokeWidth  = 5.dp,
                    strokeCap    = StrokeCap.Round
                )
                Text(
                    text       = "$score",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color      = color
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = stringResource(R.string.audit_health_title),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text  = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = color
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress      = { score / 100f },
                    modifier      = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color         = color,
                    trackColor    = color.copy(alpha = 0.15f),
                    strokeCap     = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun AllClearCard() {
    val green = auditColors().strong
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier            = Modifier.padding(horizontal = 32.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(72.dp)
                    .background(green.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint               = green,
                    modifier           = Modifier.size(40.dp)
                )
            }
            Text(
                text       = stringResource(R.string.audit_all_clear_title),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text      = stringResource(R.string.audit_all_clear_subtitle),
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AuditSummaryCard(
    totalCount:  Int,
    sections:    List<AuditCategorySection>,
    onChipClick: (AuditCategory) -> Unit
) {
    Surface(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        color     = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Text(
                    text       = stringResource(R.string.audit_summary_title, totalCount),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement   = Arrangement.spacedBy(8.dp)
            ) {
                sections.forEach { section ->
                    val count = section.entries.size
                    AuditCountChip(
                        count   = count,
                        color   = section.color,
                        label   = pluralStringResource(pluralsResFor(section.category), count, count),
                        onClick = { onChipClick(section.category) }
                    )
                }
            }
        }
    }
}

private fun pluralsResFor(category: AuditCategory): Int = when (category) {
    AuditCategory.WEAK      -> R.plurals.audit_badge_weak
    AuditCategory.BREACH    -> R.plurals.audit_badge_breach
    AuditCategory.DUPLICATE -> R.plurals.audit_badge_duplicate
    AuditCategory.PATTERN   -> R.plurals.audit_badge_pattern
    AuditCategory.SIMILAR   -> R.plurals.audit_badge_similar
    AuditCategory.SERVICE   -> R.plurals.audit_badge_service_name
    AuditCategory.PERSONAL  -> R.plurals.audit_badge_personal_info
    AuditCategory.DOMAIN    -> R.plurals.audit_badge_domain_group
    AuditCategory.RECYCLED  -> R.plurals.audit_badge_recycled
    AuditCategory.AGED      -> R.plurals.audit_badge_aged
    AuditCategory.EXPIRED   -> R.plurals.audit_badge_expired
    AuditCategory.EXPIRING  -> R.plurals.audit_badge_expiring
    AuditCategory.COMMON    -> R.plurals.audit_badge_common
    AuditCategory.MEDIUM    -> R.plurals.audit_badge_medium
}

@Composable
private fun AuditCategoryCard(
    section:      AuditCategorySection,
    shape:        Shape,
    expanded:     Boolean,
    blinkSignal:  Long?,
    onToggle:     () -> Unit,
    onEntryClick: (String) -> Unit
) {
    val titleAlpha = remember { Animatable(1f) }
    LaunchedEffect(blinkSignal) {
        if (blinkSignal != null) {
            repeat(2) {
                titleAlpha.animateTo(0.25f, animationSpec = tween(380))
                titleAlpha.animateTo(1f,    animationSpec = tween(380))
            }
        }
    }

    Surface(
        modifier       = Modifier.fillMaxWidth(),
        shape          = shape,
        color          = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onToggle
                    )
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text       = "${stringResource(section.titleRes)} (${section.entries.size})",
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = section.color,
                    modifier   = Modifier.weight(1f).alpha(titleAlpha.value)
                )
                Icon(
                    imageVector        = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val filas = section.entries.size
            val alargue = (1f + kotlin.math.sqrt((filas - 4).coerceAtLeast(0).toFloat()) * 0.25f)
                .coerceAtMost(2.5f)
            val rigidez = Spring.StiffnessMediumLow / (alargue * alargue)
            AnimatedVisibility(
                visible = expanded,
                enter   = expandVertically(
                    animationSpec = spring(stiffness = rigidez, visibilityThreshold = IntSize. VisibilityThreshold)
                ) + fadeIn(),
                exit    = shrinkVertically(
                    animationSpec = spring(stiffness = rigidez, visibilityThreshold = IntSize.VisibilityThreshold)
                ) + fadeOut()
            ) {
                Column(
                    modifier            = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text     = stringResource(section.descRes),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    section.entries.forEach { entry ->
                        AuditEntryRow(
                            entry        = entry,
                            accentColor  = section.color,
                            extraLabel   = section.extraLabel(entry),
                            onEntryClick = onEntryClick
                        )
                    }
                    if (section.onViewInVault != null) {
                        TextButton(
                            onClick        = section.onViewInVault,
                            modifier       = Modifier.align(Alignment.End),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text  = stringResource(R.string.audit_view_in_vault),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditEntryRow(
    entry:        AuditEntry,
    accentColor:  Color,
    extraLabel:   String? = null,
    onEntryClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onEntryClick(entry.id) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = entry.title,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onBackground,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            if (entry.username.isNotEmpty()) {
                Text(
                    text     = entry.username,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (extraLabel != null) {
                Text(
                    text     = extraLabel,
                    style    = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color    = accentColor.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (entry.strength >= 0) {
                Spacer(modifier = Modifier.height(6.dp))
                AuditStrengthBar(strength = entry.strength)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Icon(
            imageVector        = Icons.Default.ChevronRight,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(18.dp)
        )
    }
}


@Composable
private fun AuditCountChip(count: Int, color: Color, label: String, onClick: () -> Unit) {
    Surface(
        onClick         = onClick,
        shape           = RoundedCornerShape(8.dp),
        color           = color.copy(alpha = 0.12f),
        contentColor    = color,
        tonalElevation  = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier              = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Text(
                text       = "$count $label",
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AuditStrengthBar(strength: Int) {
    val c = auditColors()
    val filled = when (strength) {
        0, 1 -> c.weak
        2    -> c.medium
        3, 4 -> c.strong
        else -> Color.Transparent
    }
    val empty = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val segments = if (strength == 0) 1 else strength
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (index < segments) filled else empty)
            )
        }
    }
}

@Composable
private fun ageLabel(months: Int): String = when {
    months < 12 -> stringResource(R.string.audit_password_age_months, months)
    months < 24 -> stringResource(R.string.audit_password_age_year)
    else        -> stringResource(R.string.audit_password_age_years, months / 12)
}

@Composable
private fun HibpCheckCard(
    state:        HibpCheckState,
    breachColor:  Color,
    passwordCount: Int,
    onCheck:      () -> Unit,
    onReset:      () -> Unit
) {
    Surface(
        modifier       = Modifier.fillMaxWidth(),
        shape          = RoundedCornerShape(12.dp),
        color          = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier         = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Default.Policy,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = stringResource(R.string.hibp_card_title),
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text  = stringResource(R.string.hibp_card_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (state) {
                is HibpCheckState.Idle -> {
                    OutlinedButton(
                        onClick  = onCheck,
                        modifier = Modifier.fillMaxWidth(),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        border   = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Shield, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.hibp_check_button))
                    }
                }

                is HibpCheckState.Checking -> {
                    val progress = if (state.total > 0) state.current.toFloat() / state.total else 0f
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text  = stringResource(R.string.hibp_checking, state.current, state.total),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LinearProgressIndicator(
                            progress      = { progress },
                            modifier      = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color         = MaterialTheme.colorScheme.primary,
                            trackColor    = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            strokeCap     = StrokeCap.Round
                        )
                    }
                }

                is HibpCheckState.Done -> {
                    if (state.pwnedEntries.isEmpty()) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier              = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector        = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint               = auditColors().strong,
                                modifier           = Modifier.size(28.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text       = stringResource(R.string.hibp_all_clear_title),
                                    style      = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = auditColors().strong
                                )
                                Text(
                                    text  = stringResource(R.string.hibp_all_clear_subtitle),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text       = if (state.pwnedEntries.size > passwordCount && passwordCount > 0)
                                    stringResource(
                                        R.string.breach_two_figures,
                                        pluralStringResource(R.plurals.breach_passwords_count, passwordCount, passwordCount),
                                        pluralStringResource(R.plurals.breach_entries_count, state.pwnedEntries.size, state.pwnedEntries.size)
                                    )
                                else
                                    pluralStringResource(R.plurals.hibp_found_title, state.pwnedEntries.size, state.pwnedEntries.size),
                                style      = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = breachColor
                            )
                            Text(
                                text  = stringResource(R.string.hibp_found_subtitle),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick        = onReset,
                        modifier       = Modifier.align(Alignment.End),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text  = stringResource(R.string.hibp_recheck),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is HibpCheckState.NetworkError -> {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector        = Icons.Default.WifiOff,
                            contentDescription = null,
                            tint               = warningColor(),
                            modifier           = Modifier.size(20.dp)
                        )
                        Text(
                            text     = stringResource(R.string.hibp_network_error),
                            style    = MaterialTheme.typography.bodySmall,
                            color    = warningColor(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick  = onCheck,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.hibp_retry))
                    }
                }
            }

            if (state !is HibpCheckState.Checking) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Lock,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier           = Modifier.size(11.dp)
                    )
                    Text(
                        text  = stringResource(R.string.hibp_privacy_note),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun expiryLabel(entry: AuditEntry): String {
    val days = entry.daysUntilExpiry ?: return ""
    return when {
        entry.isExpired -> when {
            days == 0   -> stringResource(R.string.audit_expiry_expired_today)
            else        -> stringResource(R.string.audit_expiry_overdue, -days)
        }
        else -> when {
            days == 0   -> stringResource(R.string.audit_expiry_today)
            days == 1   -> stringResource(R.string.audit_expiry_tomorrow)
            else        -> stringResource(R.string.audit_expiry_days_left, days)
        }
    }
}

@Suppress("SameParameterValue")
@Composable
private fun PremiumLockedScreen(
    featureName:  String,
    onBack:       () -> Unit,
    onPlansClick: () -> Unit
) {
    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars))
                TopAppBar(
                    windowInsets = WindowInsets(0),
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector        = Icons.Default.Lock,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text       = featureName,
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                color      = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text      = stringResource(R.string.premium_locked_dialog_message),
                style     = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick  = onPlansClick,
                shape    = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor   = MaterialTheme.colorScheme.primary
                ),
                border   = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text       = stringResource(R.string.premium_gate_cta),
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(12.dp))
            TextButton(
                onClick  = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text  = stringResource(R.string.action_understood),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
