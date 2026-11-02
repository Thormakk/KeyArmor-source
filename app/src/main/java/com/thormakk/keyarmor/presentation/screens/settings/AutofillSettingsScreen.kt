@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
package com.thormakk.keyarmor.presentation.screens.settings

import com.thormakk.keyarmor.presentation.components.sigueAlTecladoObjetivo
import com.thormakk.keyarmor.presentation.components.sigueAlTecladoContenido
import com.thormakk.keyarmor.presentation.components.sigueAlTeclado
import com.thormakk.keyarmor.presentation.components.rememberSigueAlTeclado
import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spellcheck
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.BlockKind
import com.thormakk.keyarmor.data.prefs.BlockLevel
import com.thormakk.keyarmor.data.prefs.BlockTarget
import com.thormakk.keyarmor.domain.model.VaultEntry
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.cuadroConTeclado
import com.thormakk.keyarmor.presentation.components.justified
import com.thormakk.keyarmor.presentation.components.propiedadesConTeclado
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.components.warningColor
import com.thormakk.keyarmor.ui.theme.SuccessGreen
import com.thormakk.keyarmor.ui.theme.SuccessGreenBright
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AutofillSettingsScreen(
    onBack: () -> Unit,
    onAssociateNewEntry: (String) -> Unit,
    viewModel: AutofillSettingsViewModel = hiltViewModel(),
) {
    val blocked by viewModel.blocked.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val offerUnlock by viewModel.offerUnlockWhenLocked.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val associateSuccessTemplate = stringResource(R.string.autofill_associate_success)

    var editing by remember { mutableStateOf<AutofillRowItem?>(null) }
    var associatingExisting by remember { mutableStateOf<BlockTarget?>(null) }
    var removing by remember { mutableStateOf<AutofillRowItem?>(null) }
    var manualInput by remember { mutableStateOf("") }
    var manualInvalid by remember { mutableStateOf(false) }

    val addBlockIntoView = remember { BringIntoViewRequester() }
    var manualFocused by remember { mutableStateOf(false) }
    val sigueTeclado = rememberSigueAlTeclado()
    val scrollForm = rememberScrollState()
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    LaunchedEffect(manualFocused, imeVisible, manualInvalid) {
        if (manualFocused && imeVisible) {
            delay(300L)
            runCatching { addBlockIntoView.bringIntoView() }
        }
    }

    val openSystemSettings: () -> Unit = {
        try {
            context.startActivity(
                Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).apply {
                    data = "package:${context.packageName}".toUri()
                }
            )
        } catch (_: ActivityNotFoundException) {
            try {
                context.startActivity(Intent(Settings.ACTION_SETTINGS))
            } catch (_: ActivityNotFoundException) {   }
        }
    }

    var serviceEnabled by remember { mutableStateOf(viewModel.isServiceEnabled) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { serviceEnabled = viewModel.isServiceEnabled }

    editing?.let { item ->
        LevelDialog(
            item = item,
            onApply = { level, name -> viewModel.apply(item.target, level, name); editing = null },
            onDismiss = { editing = null },
        )
    }

    removing?.let { item ->
        AlertDialog(
            onDismissRequest = { removing = null },
            title = { Text(stringResource(R.string.autofill_level_none)) },
            text = {
                Text(
                    text = stringResource(R.string.autofill_remove_confirm_message, item.target.value),
                    style = MaterialTheme.typography.bodyMedium.justified(),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.apply(item.target, null, null)
                        removing = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) { Text(stringResource(R.string.autofill_level_none)) }
            },
            dismissButton = {
                TextButton(onClick = { removing = null }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }

    associatingExisting?.let { target ->
        AssociateExistingEntryBottomSheet(
            target = target,
            viewModel = viewModel,
            onAssociated = { entry ->
                associatingExisting = null
                scope.launch {
                    snackbarHostState.showSnackbar(
                        String.format(associateSuccessTemplate, target.value, entry.title)
                    )
                }
            },
            onDismiss = { associatingExisting = null },
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing.exclude(WindowInsets.ime),
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
                        Text(
                            text = stringResource(R.string.settings_autofill),
                            style = MaterialTheme.typography.titleLarge
                                .copy(fontSize = SectionTitleFontSize),
                            fontWeight = FontWeight.SemiBold,
                            color = sectionTitleColor(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    },
                    colors = topBarColors(),
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 40.dp)) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .sigueAlTeclado(scrollForm, sigueTeclado, margen = 8.dp, habilitado = { manualFocused })
                .verticalScroll(scrollForm)
                .sigueAlTecladoContenido(sigueTeclado)
                .padding(horizontal = 7.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AutofillCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openSystemSettings() }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Spellcheck,
                        contentDescription = null,
                        tint = if (serviceEnabled) statusGreen() else warningColor(),
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_autofill),
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = stringResource(
                                if (serviceEnabled) R.string.settings_status_enabled
                                else R.string.settings_status_disabled
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = serviceEnabled,
                        onCheckedChange = { openSystemSettings() },
                    )
                }
            }

            AutofillCard {
                Text(
                    text = stringResource(R.string.autofill_offer_unlock_title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setOfferUnlockWhenLocked(!offerUnlock) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (offerUnlock) Icons.Default.LockOpen
                        else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (offerUnlock) statusGreen() else warningColor(),
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = stringResource(R.string.autofill_offer_unlock_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = offerUnlock,
                        onCheckedChange = viewModel::setOfferUnlockWhenLocked,
                    )
                }
                RowDivider()
                Text(
                    text = stringResource(R.string.autofill_offer_unlock_explain),
                    style = MaterialTheme.typography.bodySmall.justified(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }

            SectionLabel(stringResource(R.string.autofill_blocked_title))
            AutofillCard {
                if (blocked.isEmpty()) {
                    Text(
                        text = stringResource(R.string.autofill_blocked_empty),
                        style = MaterialTheme.typography.bodySmall.justified(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                } else {
                    blocked.forEachIndexed { index, item ->
                        AutofillRow(
                            icon = if (item.level == BlockLevel.ALL) Icons.Default.DoNotDisturbOn
                            else Icons.Default.Block,
                            iconTint = warningColor(),
                            title = stringResource(
                                if (item.isSite) R.string.autofill_kind_site
                                else R.string.autofill_kind_app
                            ) + " - " + item.target.value,
                            subtitle = stringResource(
                                if (item.level == BlockLevel.ALL) R.string.autofill_level_all
                                else R.string.autofill_level_save
                            ),
                            extra = if (item.showsRawBelow) item.label else null,
                            onClick = { editing = item },
                            onAssociateNew = { onAssociateNewEntry(item.target.value) },
                            onAssociateExisting = { associatingExisting = item.target },
                            onRemove = { removing = item },
                            removeLabel = stringResource(R.string.autofill_level_none),
                        )
                        if (index != blocked.lastIndex) RowDivider()
                    }
                }
            }

            SectionLabel(stringResource(R.string.autofill_add_title))
            AutofillCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp, top = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.autofill_add_suggested),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (suggestions.isNotEmpty()) {
                        TextButton(
                            onClick = viewModel::clearRecent,
                            colors = ButtonDefaults.textButtonColors(contentColor = warningColor()),
                        ) { Text(stringResource(R.string.autofill_add_clear_recent)) }
                    }
                }
                if (suggestions.isEmpty()) {
                    Text(
                        text = stringResource(R.string.autofill_add_suggested_empty),
                        style = MaterialTheme.typography.bodySmall.justified(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                } else {
                    suggestions.forEach { item ->
                        AutofillRow(
                            icon = if (item.isSite) Icons.Default.Language else Icons.Default.Add,
                            title = item.label,
                            subtitle = stringResource(
                                if (item.isSite) R.string.autofill_kind_site
                                else R.string.autofill_kind_app
                            ),
                            onClick = { editing = item },
                            onAssociateNew = { onAssociateNewEntry(item.target.value) },
                            onAssociateExisting = { associatingExisting = item.target },
                            onRemove = { viewModel.removeSeen(item.target) },
                            removeLabel = stringResource(R.string.autofill_remove_suggestion),
                        )
                    }
                }

                RowDivider()

                Column(
                    modifier = Modifier
                        .sigueAlTecladoObjetivo(sigueTeclado)
                        .bringIntoViewRequester(addBlockIntoView)
                        .padding(16.dp),
                ) {
                    OutlinedTextField(
                        value = manualInput,
                        onValueChange = {
                            manualInput = it.filterNot(Char::isWhitespace)
                            manualInvalid = false
                        },
                        label = { Text(stringResource(R.string.autofill_add_manual_label)) },
                        placeholder = { Text(stringResource(R.string.autofill_add_manual_hint)) },
                        singleLine = true,
                        isError = manualInvalid,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            autoCorrectEnabled = false,
                            capitalization = KeyboardCapitalization.None,
                            imeAction = ImeAction.Done,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { manualFocused = it.isFocused },
                    )
                    if (manualInvalid) {
                        Text(
                            text = stringResource(R.string.autofill_add_manual_invalid),
                            style = MaterialTheme.typography.bodySmall,
                            color = warningColor(),
                            modifier = Modifier.padding(top = 6.dp, start = 4.dp),
                        )
                    }
                    Text(
                        text = stringResource(R.string.autofill_add_kinds_explain),
                        style = MaterialTheme.typography.bodySmall.justified(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 10.dp, start = 4.dp, end = 4.dp),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val add: (BlockKind) -> Unit = { kind ->
                            if (viewModel.addManually(manualInput, kind)) {
                                manualInput = ""
                                manualInvalid = false
                            } else {
                                manualInvalid = true
                            }
                        }
                        TextButton(
                            onClick = { add(BlockKind.SITE) },
                            enabled = manualInput.isNotBlank(),
                        ) { Text(stringResource(R.string.autofill_add_as_site)) }
                        TextButton(
                            onClick = { add(BlockKind.APP) },
                            enabled = manualInput.isNotBlank(),
                        ) { Text(stringResource(R.string.autofill_add_as_app)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelDialog(
    item: AutofillRowItem,
    onApply: (BlockLevel?, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember(item.target.key) { mutableStateOf(item.customName.orEmpty()) }
    var picked by remember(item.target.key) { mutableStateOf(item.level) }

    val textScroll = rememberScrollState()
    var nameFocused by remember { mutableStateOf(false) }
    val tecladoAbajo = WindowInsets.ime.getBottom(LocalDensity.current)
    LaunchedEffect(nameFocused, tecladoAbajo, textScroll.maxValue) {
        if (nameFocused) runCatching { textScroll.scrollTo(textScroll.maxValue) }
    }

    AlertDialog(
        conScroll = false,
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.cuadroConTeclado(),
        properties = propiedadesConTeclado(),
        title = {
            Column {
                Text(
                    text = stringResource(R.string.autofill_level_dialog_title),
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = item.target.value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(textScroll),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LevelOption(
                    text = stringResource(R.string.autofill_level_save_detail),
                    selected = picked == BlockLevel.SAVE,
                    onClick = { picked = BlockLevel.SAVE },
                )
                LevelOption(
                    text = stringResource(R.string.autofill_level_all_detail),
                    selected = picked == BlockLevel.ALL,
                    onClick = { picked = BlockLevel.ALL },
                    unselectedColor = warningColor(),
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.autofill_name_label)) },
                        placeholder = { Text(item.target.value) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false, imeAction = ImeAction.Done),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { nameFocused = it.isFocused },
                    )
                    Text(
                        text = stringResource(R.string.autofill_name_explain),
                        style = MaterialTheme.typography.bodySmall.justified(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(picked, name) },
                enabled = picked != null,
                shape   = RoundedCornerShape(8.dp),
                colors  = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor   = MaterialTheme.colorScheme.primary
                ),
                border  = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
            ) { Text(stringResource(R.string.autofill_apply), fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
private fun LevelOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    unselectedColor: Color? = null,
) {
    val green = statusGreen()
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.justified(),
        color = if (selected) Color.White
        else unselectedColor ?: MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) green else Color.Transparent)
            .then(
                if (selected) Modifier
                else Modifier.border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    RoundedCornerShape(10.dp),
                )
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    )
}

@Composable
private fun statusGreen(): Color =
    if (contrastRatio(MaterialTheme.colorScheme.background, SuccessGreen) < 3f) SuccessGreenBright
    else SuccessGreen

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
    )
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 55.dp, end = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.surface,
    )
}

@Composable
private fun AutofillCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.background),
    ) { content() }
}

@Composable
private fun AutofillRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    onAssociateNew: () -> Unit,
    onAssociateExisting: () -> Unit,
    onRemove: () -> Unit,
    removeLabel: String,
    iconTint: Color? = null,
    extra: String? = null,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint ?: MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (extra != null) {
                Text(
                    text = extra,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.action_options),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.autofill_associate_new)) },
                    onClick = { menuExpanded = false; onAssociateNew() },
                    leadingIcon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp)) },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.autofill_associate_existing)) },
                    onClick = { menuExpanded = false; onAssociateExisting() },
                    leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                )
                HorizontalDivider(thickness = 0.5.dp)
                DropdownMenuItem(
                    text = { Text(removeLabel, color = warningColor()) },
                    onClick = { menuExpanded = false; onRemove() },
                    leadingIcon = {
                        Icon(Icons.Default.Delete, null, tint = warningColor(), modifier = Modifier.size(18.dp))
                    },
                )
            }
        }
    }
}

@Composable
private fun AssociateExistingEntryBottomSheet(
    target: BlockTarget,
    viewModel: AutofillSettingsViewModel,
    onAssociated: (VaultEntry) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf<List<VaultEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        entries = viewModel.entriesForAssociation().filter { !it.isProtected }
        loading = false
    }
    val filtered = remember(query, entries) {
        if (query.isBlank()) entries
        else entries.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.username.contains(query, ignoreCase = true) ||
                it.email.contains(query, ignoreCase = true)
        }
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text(
                text = stringResource(R.string.autofill_associate_picker_title, target.value),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(stringResource(R.string.autofill_associate_picker_search)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            when {
                !loading && filtered.isEmpty() -> Text(
                    text = stringResource(R.string.autofill_associate_picker_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
                else -> LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(filtered, key = { it.id }) { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.associateWithExistingEntry(entry.id, target)
                                    onAssociated(entry)
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                                val entrySubtitle = entry.username.ifBlank { entry.email }
                                if (entrySubtitle.isNotBlank()) {
                                    Text(
                                        text = entrySubtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
