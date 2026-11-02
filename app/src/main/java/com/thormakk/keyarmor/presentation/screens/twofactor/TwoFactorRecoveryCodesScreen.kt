package com.thormakk.keyarmor.presentation.screens.twofactor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.background
import com.thormakk.keyarmor.presentation.screens.vault.ServiceAwareAvatar
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.Key
import com.thormakk.keyarmor.presentation.components.EntryGroupDivider
import com.thormakk.keyarmor.presentation.components.entryItemShape
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.data.repository.TwoFactorRepository
import com.thormakk.keyarmor.domain.model.TwoFactorAccount
import com.thormakk.keyarmor.presentation.util.ClipboardUtil
import com.thormakk.keyarmor.presentation.util.LocalClipboardClearManager
import com.thormakk.keyarmor.ui.theme.MonoFontFamily
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TwoFactorRecoveryCodesViewModel @Inject constructor(
    private val repository: TwoFactorRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val id: String = savedStateHandle["id"] ?: ""

    val account: StateFlow<TwoFactorAccount?> = repository.observeAccountById(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun toggleUsed(index: Int) {
        val acc = account.value ?: return
        if (acc.isProtected) return
        val codes = acc.recoveryCodes.toMutableList()
        if (index !in codes.indices) return
        codes[index] = codes[index].copy(used = !codes[index].used)
        viewModelScope.launch { repository.saveAccount(acc.copy(recoveryCodes = codes)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoFactorRecoveryCodesScreen(
    onBack: () -> Unit,
    viewModel: TwoFactorRecoveryCodesViewModel = hiltViewModel()
) {
    val account by viewModel.account.collectAsStateWithLifecycle()
    val clipClear = LocalClipboardClearManager.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val copiedMsg = stringResource(R.string.action_copied)
    val codes = account?.recoveryCodes.orEmpty()
    val protectedAcc = account?.isProtected == true

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 40.dp)) },
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .background(MaterialTheme.colorScheme.surface)
                )
                TopAppBar(
                    windowInsets = WindowInsets(0),
                    colors = topBarColors(),
                    title = {
                        TextoDeUnaLinea(
                            text       = stringResource(R.string.twofa_section_recovery_codes),
                            style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                            fontWeight = FontWeight.SemiBold,
                            color      = sectionTitleColor()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                                tint               = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            }
        }
    ) { padding ->
        val mergedCardColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        val mergedGapColor  = MaterialTheme.colorScheme.background
        val cardRadius      = 14.dp
        val title = account?.let { it.name.ifBlank { it.issuer } }.orEmpty()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 7.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape          = entryItemShape(isFirst = true, isLast = codes.isEmpty(), radius = cardRadius),
                    color          = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier       = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier          = Modifier.fillMaxWidth().padding(20.dp)
                    ) {
                        ServiceAwareAvatar(title = title, url = account?.url.orEmpty(), size = 48.dp)
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                text       = title.ifBlank { stringResource(R.string.twofa_screen_title) },
                                style      = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onSurface,
                                maxLines   = 1
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text  = stringResource(R.string.twofa_recovery_unused_count, codes.count { !it.used }, codes.size),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (codes.isNotEmpty()) {
                    EntryGroupDivider(
                        startInset = 0.dp,
                        endInset   = 0.dp,
                        cardColor  = mergedCardColor,
                        gapColor   = mergedGapColor
                    )
                }
                codes.forEachIndexed { index, rc ->
                    if (index > 0) {
                        EntryGroupDivider(
                            startInset = 48.dp,
                            endInset   = 13.dp,
                            cardColor  = mergedCardColor,
                            gapColor   = mergedGapColor
                        )
                    }
                    Surface(
                        shape    = entryItemShape(isFirst = false, isLast = index == codes.lastIndex, radius = cardRadius),
                        color    = mergedCardColor,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier          = Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Key,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier           = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = rc.code,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = MonoFontFamily,
                                    textDecoration = if (rc.used) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (rc.used) MaterialTheme.colorScheme.onSurfaceVariant
                                        else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    ClipboardUtil.copySensitive(context, rc.code)
                                    clipClear.scheduleClear()
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = stringResource(R.string.action_copy),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            IconButton(
                                onClick = { viewModel.toggleUsed(index) },
                                enabled = !protectedAcc,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    if (rc.used) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = stringResource(R.string.twofa_recovery_code_used),
                                    tint = if (rc.used) MaterialTheme.colorScheme.primary
                                           else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
