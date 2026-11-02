@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@file:Suppress("UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UnusedAssignment", "DEPRECATION")
package com.thormakk.keyarmor.presentation.screens.crypto

import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.exclude
import com.thormakk.keyarmor.presentation.components.sigueAlTeclado
import com.thormakk.keyarmor.presentation.components.sigueAlTecladoContenido
import com.thormakk.keyarmor.presentation.components.rememberSigueAlTeclado
import com.thormakk.keyarmor.presentation.components.sigueAlTecladoObjetivo
import androidx.compose.ui.focus.onFocusChanged
import com.thormakk.keyarmor.presentation.components.seguirTextoMultilinea
import com.thormakk.keyarmor.presentation.components.cuadroConTeclado
import com.thormakk.keyarmor.presentation.components.justified
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.presentation.components.EntryGroupDivider
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.SimpleCardTextField
import com.thormakk.keyarmor.presentation.components.TagDeleteButton
import com.thormakk.keyarmor.presentation.components.TagSearchEmptyState
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.entryItemShape
import com.thormakk.keyarmor.presentation.components.propiedadesConTeclado
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.components.warningColor
import com.thormakk.keyarmor.presentation.util.ClipboardUtil
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.presentation.util.highlightText
import com.thormakk.keyarmor.presentation.util.LocalClipboardClearManager
import kotlinx.coroutines.launch
import java.util.Locale
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea

private val CRYPTO_NETWORKS = listOf(
    "Bitcoin (BTC)",
    "Ethereum (ETH)",
    "Solana (SOL)",
    "BNB Chain (BNB)",
    "Polygon (MATIC)",
    "Avalanche (AVAX)",
    "Cardano (ADA)",
    "Ripple (XRP)",
    "Tron (TRX)",
    "Dogecoin (DOGE)",
    "Litecoin (LTC)",
    "Arbitrum One (ETH)",
    "Optimism (ETH)",
    "Base (ETH)",
    "zkSync Era (ETH)",
    "Scroll (ETH)",
    "Fantom (FTM)",
    "Gnosis (xDAI)",
    "Celo (CELO)",
    "Moonbeam (GLMR)",
    "Ethereum Classic (ETC)",
    "Bitcoin Cash (BCH)",
    "Dash (DASH)",
    "Zcash (ZEC)",
    "Stellar (XLM)",
    "Algorand (ALGO)",
    "Hedera (HBAR)",
    "Tezos (XTZ)",
    "Cosmos (ATOM)",
    "TON (TON)",
    "Otra"
)

private val MergedCardRadius     = 14.dp
private val MergedCardStartInset = 16.dp
private val MergedCardEndInset   = 16.dp

@Composable
fun AddEditCryptoWalletScreen(
    onBack:      () -> Unit,
    onSaved:     () -> Unit,
    onViewCredential:    (String) -> Unit = {},
    onPlansClick:        () -> Unit = {},
    sharedTransitionScope:   SharedTransitionScope?   = null,
    animatedVisibilityScope: AnimatedVisibilityScope?  = null,
    viewModel: AddEditCryptoWalletViewModel = hiltViewModel()
) {
    val uiState       by viewModel.uiState.collectAsStateWithLifecycle()
    val vaultEntries  by viewModel.vaultEntries.collectAsStateWithLifecycle()

    val linkedEntryName = remember(uiState.linkedPasswordId, vaultEntries) {
        vaultEntries.firstOrNull { it.id == uiState.linkedPasswordId }?.title ?: ""
    }

    val focusManager   = LocalFocusManager.current
    val clipCtx      = LocalContext.current
    val clipClear      = LocalClipboardClearManager.current
    val autoLockManager = LocalAutoLockManager.current
    val context        = LocalContext.current
    val activity       = context as FragmentActivity

    var showNetworkPicker by remember { mutableStateOf(false) }
    var showKeysSheet      by remember { mutableStateOf(false) }
    var showPickerDialog   by remember { mutableStateOf(false) }
    var premiumFeature     by remember { mutableStateOf<String?>(null) }

    var showMasterPasswordDialog by remember { mutableStateOf(false) }

    val biometricCallback = remember {
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                showKeysSheet = true
            }
            override fun onAuthenticationFailed() {   }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_CANCELED) return
                showMasterPasswordDialog = true
            }
        }
    }
    val biometricPrompt = remember(activity) {
        BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), biometricCallback)
    }
    val cryptoBioTitle    = stringResource(R.string.crypto_keys_biometric_title)
    val cryptoBioSubtitle = stringResource(R.string.crypto_keys_biometric_subtitle)
    val usePasswordLabel  = stringResource(R.string.autofill_use_password)

    val biometricPromptInfo = remember(cryptoBioTitle, cryptoBioSubtitle, usePasswordLabel) {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(cryptoBioTitle)
            .setSubtitle(cryptoBioSubtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText(usePasswordLabel)
            .build()
    }

    val labelCryptoKeys   = stringResource(R.string.crypto_keys_section_title)

    fun launchKeysSheet() {
        val hasKeys = uiState.seedPhrase.isNotEmpty() || uiState.privateKey.isNotEmpty()
        if (!viewModel.isPremium && !hasKeys) {
            premiumFeature = labelCryptoKeys
            return
        }
        if (!hasKeys) {
            showKeysSheet = true
            return
        }
        val canAuth = BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )
        if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
            biometricPrompt.authenticate(biometricPromptInfo)
        } else {
            showMasterPasswordDialog = true
        }
    }

    premiumFeature?.let { featureName ->
        AlertDialog(
            onDismissRequest = { premiumFeature = null },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(featureName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
            },
            text = { Text(stringResource(R.string.premium_locked_dialog_message), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = { premiumFeature = null; onPlansClick() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary),
                    border  = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                ) { Text(stringResource(R.string.premium_gate_cta)) }
            },
            dismissButton = {
                TextButton(onClick = { premiumFeature = null }) {
                    Text(stringResource(R.string.action_understood), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    LaunchedEffect(uiState.isSaved)   { if (uiState.isSaved)   onSaved() }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()

    if (showMasterPasswordDialog) {
        var masterInput   by remember { mutableStateOf("") }
        var masterVisible by remember { mutableStateOf(false) }
        var masterError   by remember { mutableStateOf(false) }
        AlertDialog(
            modifier = Modifier.cuadroConTeclado(),
            properties = propiedadesConTeclado(),
            onDismissRequest = { showMasterPasswordDialog = false },
            containerColor   = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text       = stringResource(R.string.crypto_keys_biometric_title),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text  = stringResource(R.string.crypto_keys_biometric_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                        value         = masterInput,
                        onValueChange = { autoLockManager?.recordInteraction(); masterInput = it; masterError = false },
                        placeholder   = { Text(stringResource(R.string.field_password), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        singleLine    = true,
                        isError       = masterError,
                        supportingText = if (masterError) {
                            { Text(stringResource(R.string.autofill_error_wrong_password),
                                   color = dangerColor()) }
                        } else null,
                        visualTransformation = if (masterVisible) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { masterVisible = !masterVisible }) {
                                Icon(
                                    imageVector        = if (masterVisible) Icons.Default.VisibilityOff
                                                         else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier           = Modifier.size(20.dp)
                                )
                            }
                        },
                        shape  = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedLabelColor       = MaterialTheme.colorScheme.primary,
                            errorBorderColor        = dangerColor(),
                            errorSupportingTextColor = dangerColor()
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val ok = viewModel.verifyMasterPassword(masterInput)
                            if (ok) {
                                showMasterPasswordDialog = false
                                masterInput = ""
                                showKeysSheet = true
                            } else {
                                masterError = true
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text       = stringResource(R.string.lock_action_unlock),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showMasterPasswordDialog = false; masterInput = "" }) {
                    Text(
                        text  = stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }

    if (showPickerDialog) {
        CredentialPickerSheet(
            entries   = vaultEntries,
            onSelect  = { entry ->
                viewModel.onLinkedPasswordIdChange(entry.id)
                showPickerDialog = false
            },
            onDismiss = { showPickerDialog = false }
        )
    }

    if (showNetworkPicker) {
        NetworkPickerSheet(
            selected           = uiState.network,
            compatibleNetworks = uiState.compatibleNetworks,
            onSelect           = { viewModel.onNetworkChange(it); showNetworkPicker = false },
            onDismiss          = { showNetworkPicker = false }
        )
    }

    if (showKeysSheet) {
        CryptoKeysSheet(
            seedPhrase    = uiState.seedPhrase,
            privateKey    = uiState.privateKey,
            canEdit       = viewModel.isPremium,
            onSeedChange  = viewModel::onSeedPhraseChange,
            onKeyChange   = viewModel::onPrivateKeyChange,
            onSave        = { viewModel.save(); showKeysSheet = false },
            onDismiss     = { showKeysSheet = false }
        )
    }

    val sigueTeclado = rememberSigueAlTeclado()
    val scrollForm = androidx.compose.foundation.rememberScrollState()
    var notaEnfocada by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing.exclude(WindowInsets.ime),
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
                    title = {
                        TextoDeUnaLinea(
                            if (uiState.isNew) stringResource(R.string.crypto_new)
                            else stringResource(R.string.crypto_edit),
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
                    },
                    actions = {
                        CaptureAllowedBadge()
                        ThemeTrialBadge()
                        IconButton(onClick = { viewModel.save() }) {
                            Icon(
                                imageVector        = Icons.Default.Save,
                                contentDescription = stringResource(R.string.action_save),
                                tint               = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = topBarColors()
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .sigueAlTeclado(scrollForm, sigueTeclado, margen = 8.dp, habilitado = { notaEnfocada })
                .verticalScroll(scrollForm)
                .sigueAlTecladoContenido(sigueTeclado)
                .padding(horizontal = 7.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val mergedCardColor = MaterialTheme.colorScheme.surfaceVariant
            val mergedGapColor  = MaterialTheme.colorScheme.background

            Column(modifier = Modifier.fillMaxWidth()) {

            CryptoAssetTextField(
                value             = uiState.asset,
                onValueChange     = viewModel::onAssetChange,
                modifier          = Modifier.fillMaxWidth(),
                compatibleTickers = uiState.compatibleAssets,
                colors            = titleFieldColors(),
                shape             = entryItemShape(isFirst = true, isLast = false, radius = MergedCardRadius),
                leadingIcon       = {
                    val avatarModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                        with(sharedTransitionScope) {
                            Modifier.sharedElement(
                                sharedContentState      = rememberSharedContentState("crypto_avatar_${uiState.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        }
                    } else Modifier
                    CryptoAwareAvatar(asset = uiState.asset.ifEmpty { uiState.network }, size = 32.dp, modifier = avatarModifier)
                }
            )
            EntryGroupDivider(
                startInset = 0.dp,
                endInset   = 0.dp,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                    value         = uiState.network,
                    onValueChange = {},
                    readOnly      = true,
                    placeholder   = { Text(stringResource(R.string.crypto_field_network), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    trailingIcon  = {
                        Icon(
                            imageVector        = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    modifier   = Modifier.fillMaxWidth(),
                    shape      = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius),
                    colors     = fieldColors()
                )
                Box(modifier = Modifier.matchParentSize().clickable { showNetworkPicker = true })
            }
            EntryGroupDivider(
                startInset = MergedCardStartInset,
                endInset   = MergedCardEndInset,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            SimpleCardTextField(
                value           = uiState.address,
                onValueChange   = viewModel::onAddressChange,
                placeholder     = stringResource(R.string.crypto_field_address),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, keyboardType = androidx.compose.ui.text.input.KeyboardType.Uri, autoCorrectEnabled = false),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                shape           = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius),
                trailingContent = {
                    IconButton(
                        onClick  = { ClipboardUtil.copySensitive(clipCtx, uiState.address); clipClear.scheduleClear() },
                        enabled  = uiState.address.isNotEmpty()
                    ) {
                        Icon(
                            imageVector        = Icons.Default.ContentCopy,
                            contentDescription = stringResource(R.string.action_copy),
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(18.dp)
                        )
                    }
                }
            )
            EntryGroupDivider(
                startInset = MergedCardStartInset,
                endInset   = MergedCardEndInset,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            Surface(
                shape    = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius),
                color    = mergedCardColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SimpleCardTextField(
                        value           = uiState.manualBalance,
                        onValueChange   = viewModel::onManualBalanceChange,
                        placeholder     = stringResource(R.string.crypto_exchange_balance_placeholder),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                            imeAction    = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        containerColor  = Color.Transparent,
                        trailingContent = if (uiState.manualBalance.isNotEmpty()) ({
                            IconButton(onClick = { viewModel.onManualBalanceChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.crypto_exchange_balance_clear),
                                    modifier = Modifier.size(18.dp))
                            }
                        }) else null
                    )
                    Text(
                        text     = stringResource(R.string.crypto_exchange_balance_hint),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 10.dp)
                    )
                }
            }
            EntryGroupDivider(
                startInset = MergedCardStartInset,
                endInset   = MergedCardEndInset,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            val hasKeys = uiState.seedPhrase.isNotEmpty() || uiState.privateKey.isNotEmpty()
            Surface(
                onClick  = { launchKeysSheet() },
                shape    = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius),
                color    = mergedCardColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier              = Modifier.padding(start = 16.dp, end = 4.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = stringResource(R.string.crypto_keys_section_title),
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color      = if (hasKeys) MaterialTheme.colorScheme.primary
                                         else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text  = if (hasKeys) stringResource(R.string.crypto_keys_section_subtitle_set)
                                    else stringResource(R.string.crypto_keys_section_subtitle_empty),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        imageVector        = if (!viewModel.isPremium && !hasKeys) Icons.Default.Lock else Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(18.dp)
                    )
                    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector        = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier           = Modifier.size(18.dp)
                        )
                    }
                }
            }
            EntryGroupDivider(
                startInset = MergedCardStartInset,
                endInset   = MergedCardEndInset,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            CryptoCredentialLinkField(
                linkedName   = linkedEntryName,
                hasLink      = uiState.linkedPasswordId.isNotEmpty(),
                onPickClick  = { showPickerDialog = true },
                onViewClick  = { onViewCredential(uiState.linkedPasswordId) },
                onClearClick = { viewModel.onLinkedPasswordIdChange("") },
                shape        = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius)
            )
            EntryGroupDivider(
                startInset = MergedCardStartInset,
                endInset   = MergedCardEndInset,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            Box(modifier = Modifier.sigueAlTecladoObjetivo(sigueTeclado).onFocusChanged { notaEnfocada = it.hasFocus }) {
                OutlinedTextField(
                    value           = uiState.notes,
                    onValueChange   = { autoLockManager?.recordInteraction(); viewModel.onNotesChange(it) },
                    placeholder   = { Text(stringResource(R.string.crypto_field_notes), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    minLines        = 3,
                    modifier        = Modifier.fillMaxWidth().seguirTextoMultilinea(),
                    shape           = entryItemShape(isFirst = false, isLast = true, radius = MergedCardRadius),
                    colors          = fieldColors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
                Text(
                    text  = "${uiState.notes.length}/300",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 8.dp)
                )
            }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NetworkPickerSheet(
    selected:           String,
    compatibleNetworks: Set<String>,
    onSelect:           (String) -> Unit,
    onDismiss:          () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val hasFilter      = compatibleNetworks.isNotEmpty()
    val compatible     = if (hasFilter) CRYPTO_NETWORKS.filter { it == "Otra" || it in compatibleNetworks } else CRYPTO_NETWORKS
    val incompatible   = if (hasFilter) CRYPTO_NETWORKS.filter { it != "Otra" && it !in compatibleNetworks } else emptyList()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text       = stringResource(R.string.crypto_field_network),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onBackground,
                modifier   = Modifier.padding(bottom = 8.dp)
            )
            HorizontalDivider(
                thickness = 0.5.dp,
                color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                modifier  = Modifier.padding(bottom = 4.dp)
            )

            compatible.forEach { net -> NetworkPickerItem(net, net == selected, onSelect) }

            if (incompatible.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text     = stringResource(R.string.crypto_other_networks),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
                incompatible.forEach { net ->
                    NetworkPickerItem(
                        net        = net,
                        isSelected = net == selected,
                        onSelect   = onSelect,
                        dimmed     = true
                    )
                }
            }
        }
    }
}

@Composable
private fun NetworkPickerItem(
    net:        String,
    isSelected: Boolean,
    onSelect:   (String) -> Unit,
    dimmed:     Boolean = false
) {
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        dimmed     -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
        else       -> MaterialTheme.colorScheme.onBackground
    }
    DropdownMenuItem(
        text = {
            Text(
                text       = net,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color      = textColor
            )
        },
        trailingIcon = if (isSelected) ({
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(18.dp)
            )
        }) else null,
        onClick = { onSelect(net) },
        colors  = MenuDefaults.itemColors(textColor = textColor)
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedBorderColor      = Color.Transparent,
    unfocusedBorderColor    = Color.Transparent,
    focusedLabelColor       = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor     = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor             = MaterialTheme.colorScheme.primary
)

@Composable
private fun titleFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor      = Color.Transparent,
    unfocusedBorderColor    = Color.Transparent,
    focusedLabelColor       = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor     = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor             = MaterialTheme.colorScheme.primary
)

@Composable
internal fun CryptoBalanceCard(
    state:        BalanceUiState,
    fiatValue:    Double? = null,
    fiatCurrency: String  = "USD",
    onRefresh:    () -> Unit,
    shape:        Shape  = RoundedCornerShape(8.dp)
) {
    var showSupportSheet by remember { mutableStateOf(false) }

    val disclaimerPrefix = stringResource(R.string.crypto_balance_disclaimer)
    val disclaimerLink   = stringResource(R.string.crypto_balance_disclaimer_link)
    val primaryColor     = MaterialTheme.colorScheme.primary
    val bodyColor        = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val disclaimerStyle  = MaterialTheme.typography.bodySmall
    val annotatedDisclaimer = remember(disclaimerPrefix, disclaimerLink, primaryColor, bodyColor) {
        buildAnnotatedString {
            withStyle(SpanStyle(color = bodyColor)) { append("${disclaimerPrefix.trimEnd()} ") }
            pushStringAnnotation(tag = "INFO", annotation = "info")
            withStyle(SpanStyle(color = primaryColor, textDecoration = TextDecoration.Underline)) {
                append(disclaimerLink)
            }
            pop()
            withStyle(SpanStyle(color = bodyColor)) { append(".") }
        }
    }

    Surface(
        shape    = shape,
        color    = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = stringResource(R.string.crypto_balance_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                when (state) {
                    is BalanceUiState.ManualOverride -> {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Edit,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.tertiary,
                                modifier           = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text       = state.display,
                                    style      = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text  = stringResource(R.string.crypto_balance_manual_subtitle),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (fiatValue != null) {
                            Text(
                                text  = formatFiatDisplay(fiatValue, fiatCurrency),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    is BalanceUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier  = Modifier.size(18.dp),
                            color     = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    }
                    is BalanceUiState.Ready -> {
                        Text(
                            text       = state.display,
                            style      = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.primary
                        )
                        if (fiatValue != null) {
                            Text(
                                text  = formatFiatDisplay(fiatValue, fiatCurrency),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    is BalanceUiState.NotSupported -> {
                        Text(
                            text  = stringResource(R.string.crypto_balance_unsupported),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    is BalanceUiState.Failed -> {
                        Text(
                            text  = stringResource(R.string.crypto_balance_error),
                            style = MaterialTheme.typography.bodyMedium,
                            color = warningColor()
                        )
                    }
                    is BalanceUiState.Idle -> {
                        Text(
                            text  = "—",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }
            if (state !is BalanceUiState.Loading) {
                IconButton(
                    onClick  = onRefresh,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.crypto_balance_refresh),
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
        }

        Text(
            text     = annotatedDisclaimer,
            style    = disclaimerStyle,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 10.dp)
                .clickable { showSupportSheet = true }
        )
        }
    }

    if (showSupportSheet) {
        BalanceSupportSheet(onDismiss = { showSupportSheet = false })
    }
}

@Composable
private fun BalanceSupportSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(
                text       = stringResource(R.string.crypto_balance_info_title),
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = stringResource(R.string.crypto_balance_info_intro),
                style     = MaterialTheme.typography.bodyMedium.justified(),
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = warningColor().copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Text(
                        text  = stringResource(R.string.crypto_balance_cex_warning_title),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = warningColor()
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text      = stringResource(R.string.crypto_balance_cex_warning_body),
                        style     = MaterialTheme.typography.bodySmall.justified(),
                        color     = warningColor()
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            BalanceSupportSection(stringResource(R.string.crypto_balance_section_evm_native))
            BalanceSupportItem("Ethereum", "ETH")
            BalanceSupportItem("BNB Chain", "BNB")
            BalanceSupportItem("Polygon", "MATIC")
            BalanceSupportItem("Arbitrum One", "ETH")
            BalanceSupportItem("Optimism", "ETH")
            BalanceSupportItem("Base", "ETH")
            BalanceSupportItem("zkSync Era", "ETH")
            BalanceSupportItem("Scroll", "ETH")
            BalanceSupportItem("Avalanche", "AVAX")
            BalanceSupportItem("Fantom", "FTM")
            BalanceSupportItem("Gnosis", "xDAI")
            BalanceSupportItem("Celo", "CELO")
            BalanceSupportItem("Moonbeam", "GLMR")
            BalanceSupportItem("Ethereum Classic", "ETC")

            Spacer(Modifier.height(16.dp))
            BalanceSupportSection(stringResource(R.string.crypto_balance_section_evm_erc20))
            Text(
                text      = stringResource(R.string.crypto_balance_evm_erc20_tokens),
                style     = MaterialTheme.typography.bodySmall.justified(),
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            BalanceSupportSection(stringResource(R.string.crypto_balance_section_utxo_native))
            BalanceSupportItem("Bitcoin", "BTC")
            BalanceSupportItem("Bitcoin Cash", "BCH")
            BalanceSupportItem("Litecoin", "LTC")
            BalanceSupportItem("Dogecoin", "DOGE")
            BalanceSupportItem("Dash", "DASH")
            BalanceSupportItem("Zcash", "ZEC")

            Spacer(Modifier.height(16.dp))
            BalanceSupportSection(stringResource(R.string.crypto_balance_section_solana))
            BalanceSupportItem(stringResource(R.string.crypto_native_label, "SOL"), "SOL")
            Text(
                text      = stringResource(R.string.crypto_balance_solana_tokens),
                style     = MaterialTheme.typography.bodySmall.justified(),
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text      = stringResource(R.string.crypto_balance_solana_ata_note),
                style     = MaterialTheme.typography.bodySmall.justified(),
                color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(16.dp))
            BalanceSupportSection(stringResource(R.string.crypto_balance_section_tron))
            BalanceSupportItem(stringResource(R.string.crypto_native_label, "TRX"), "TRX")
            Text(
                text      = stringResource(R.string.crypto_balance_tron_tokens),
                style     = MaterialTheme.typography.bodySmall.justified(),
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            BalanceSupportSection(stringResource(R.string.crypto_balance_section_stellar))
            BalanceSupportItem(stringResource(R.string.crypto_native_label, "XLM"), "XLM")
            Text(
                text      = stringResource(R.string.crypto_balance_stellar_tokens),
                style     = MaterialTheme.typography.bodySmall.justified(),
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            BalanceSupportSection(stringResource(R.string.crypto_balance_section_algorand))
            BalanceSupportItem(stringResource(R.string.crypto_native_label, "ALGO"), "ALGO")
            Text(
                text      = stringResource(R.string.crypto_balance_algorand_tokens),
                style     = MaterialTheme.typography.bodySmall.justified(),
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            BalanceSupportSection(stringResource(R.string.crypto_balance_section_hedera))
            BalanceSupportItem(stringResource(R.string.crypto_native_label, "HBAR"), "HBAR")
            Text(
                text      = stringResource(R.string.crypto_balance_hedera_tokens),
                style     = MaterialTheme.typography.bodySmall.justified(),
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            BalanceSupportSection(stringResource(R.string.crypto_balance_section_tezos))
            BalanceSupportItem(stringResource(R.string.crypto_native_label, "XTZ"), "XTZ")
            Text(
                text      = stringResource(R.string.crypto_balance_tezos_tokens),
                style     = MaterialTheme.typography.bodySmall.justified(),
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            BalanceSupportSection(stringResource(R.string.crypto_balance_section_cosmos))
            BalanceSupportItem("Cosmos Hub", "ATOM")
            Text(
                text      = stringResource(R.string.crypto_balance_cosmos_other_chains),
                style     = MaterialTheme.typography.bodySmall.justified(),
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text      = stringResource(R.string.crypto_balance_cosmos_symbol_note),
                style     = MaterialTheme.typography.bodySmall.justified(),
                color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(16.dp))
            BalanceSupportSection(stringResource(R.string.crypto_balance_section_ton))
            BalanceSupportItem(stringResource(R.string.crypto_native_label, "TON"), "TON")
            Text(
                text      = stringResource(R.string.crypto_balance_ton_tokens),
                style     = MaterialTheme.typography.bodySmall.justified(),
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            BalanceSupportSection(stringResource(R.string.crypto_balance_section_xrp))
            BalanceSupportItem(stringResource(R.string.crypto_native_label, "XRP"), "XRP")
            Text(
                text      = stringResource(R.string.crypto_balance_xrp_note),
                style     = MaterialTheme.typography.bodySmall.justified(),
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            BalanceSupportSection(stringResource(R.string.crypto_balance_section_cardano))
            BalanceSupportItem(stringResource(R.string.crypto_native_label, "ADA"), "ADA")
            Text(
                text      = stringResource(R.string.crypto_balance_cardano_tokens),
                style     = MaterialTheme.typography.bodySmall.justified(),
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Text(
                text      = stringResource(R.string.crypto_balance_final_disclaimer),
                style     = MaterialTheme.typography.bodySmall.justified(),
                color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun BalanceSupportSection(title: String) {
    Text(
        text       = title,
        style      = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color      = MaterialTheme.colorScheme.primary,
        modifier   = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun BalanceSupportItem(network: String, symbol: String) {
    Row(
        modifier             = Modifier.padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment    = Alignment.CenterVertically
    ) {
        Text(
            text  = network,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text  = symbol,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun CryptoKeysSheet(
    seedPhrase:   String,
    privateKey:   String,
    canEdit:      Boolean,
    onSeedChange: (String) -> Unit,
    onKeyChange:  (String) -> Unit,
    onSave:       () -> Unit,
    onDismiss:    () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipClear  = LocalClipboardClearManager.current
    val autoLockManager = LocalAutoLockManager.current
    val clipContext = LocalContext.current

    var showSeed by remember { mutableStateOf(true) }
    var showKey  by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.Key,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text       = stringResource(R.string.crypto_keys_section_title),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text  = stringResource(R.string.crypto_keys_sheet_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = dangerColor().copy(alpha = 0.8f)
                    )
                }
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(4.dp))

            Surface(
                shape    = RoundedCornerShape(12.dp),
                color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier          = Modifier.padding(start = 14.dp, top = 10.dp, end = 14.dp, bottom = 2.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.Article,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text       = stringResource(R.string.crypto_field_seed_phrase),
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedTextField(
                        value                = seedPhrase,
                        readOnly             = !canEdit,
                        onValueChange        = { autoLockManager?.recordInteraction(); onSeedChange(it) },
                        placeholder          = { Text(stringResource(R.string.crypto_field_seed_phrase), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                        visualTransformation = if (showSeed) VisualTransformation.None
                                               else PasswordVisualTransformation('·'),
                        trailingIcon         = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { showSeed = !showSeed }) {
                                    Icon(
                                        imageVector        = if (showSeed) Icons.Default.VisibilityOff
                                                             else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier           = Modifier.size(20.dp)
                                    )
                                }
                                if (!canEdit && seedPhrase.isNotEmpty()) {
                                    IconButton(onClick = { onSeedChange("") }) {
                                        Icon(
                                            imageVector        = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.action_delete),
                                            tint               = warningColor(),
                                            modifier           = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick  = { ClipboardUtil.copySensitive(clipContext, seedPhrase); clipClear.scheduleClear() },
                                    enabled  = seedPhrase.isNotEmpty(),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Icon(
                                        imageVector        = Icons.Default.ContentCopy,
                                        contentDescription = stringResource(R.string.action_copy),
                                        tint               = MaterialTheme.colorScheme.primary,
                                        modifier           = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        minLines        = 5,
                        maxLines        = 8,
                        modifier        = Modifier.fillMaxWidth(),
                        shape           = RoundedCornerShape(8.dp),
                        colors          = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor   = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor          = MaterialTheme.colorScheme.primary
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false, imeAction = ImeAction.Next)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Surface(
                shape    = RoundedCornerShape(12.dp),
                color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier          = Modifier.padding(start = 14.dp, top = 10.dp, end = 14.dp, bottom = 2.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Key,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text       = stringResource(R.string.crypto_field_private_key),
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedTextField(
                        value                = privateKey,
                        readOnly             = !canEdit,
                        onValueChange        = { autoLockManager?.recordInteraction(); onKeyChange(it) },
                        placeholder          = { Text(stringResource(R.string.crypto_field_private_key), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                        visualTransformation = if (showKey) VisualTransformation.None
                                               else PasswordVisualTransformation('·'),
                        trailingIcon         = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { showKey = !showKey }) {
                                    Icon(
                                        imageVector        = if (showKey) Icons.Default.VisibilityOff
                                                             else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier           = Modifier.size(20.dp)
                                    )
                                }
                                if (!canEdit && privateKey.isNotEmpty()) {
                                    IconButton(onClick = { onKeyChange("") }) {
                                        Icon(
                                            imageVector        = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.action_delete),
                                            tint               = warningColor(),
                                            modifier           = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick  = { ClipboardUtil.copySensitive(clipContext, privateKey); clipClear.scheduleClear() },
                                    enabled  = privateKey.isNotEmpty(),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Icon(
                                        imageVector        = Icons.Default.ContentCopy,
                                        contentDescription = stringResource(R.string.action_copy),
                                        tint               = MaterialTheme.colorScheme.primary,
                                        modifier           = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        minLines        = 3,
                        maxLines        = 5,
                        modifier        = Modifier.fillMaxWidth(),
                        shape           = RoundedCornerShape(8.dp),
                        colors          = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor   = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor          = MaterialTheme.colorScheme.primary
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false, imeAction = ImeAction.Done)
                    )
                }
            }

            Button(
                onClick  = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape    = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor   = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector        = Icons.Default.Save,
                    contentDescription = null,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text       = stringResource(R.string.action_save),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
internal fun CryptoOptionsSheet(
    initialCategory: EntryCategory,
    initialTags:     List<String>,
    availableTags:   List<Tag>,
    onSave:          (EntryCategory, List<String>) -> Unit,
    onDismiss:       () -> Unit,
    onAddTag:        (String) -> Unit,
    onRenameTag:     (String, String) -> Unit,
    onDeleteTag:     (String) -> Unit,
    tagConflictKey:  (String) -> String,
    canCreateTags:   Boolean = true,
    onUpgradeClick:  () -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val autoLockManager = LocalAutoLockManager.current
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var selectedTags     by remember { mutableStateOf(initialTags.toSet()) }
    var renameTarget     by remember { mutableStateOf<Tag?>(null) }
    var renameText       by remember { mutableStateOf("") }
    var renameError      by remember { mutableStateOf<String?>(null) }
    var newTagText       by remember { mutableStateOf("") }
    var newTagError      by remember { mutableStateOf<String?>(null) }
    var showLockedDialog by remember { mutableStateOf(false) }

    val errDuplicate = stringResource(R.string.tag_error_duplicate)
    val errCategory  = stringResource(R.string.tag_error_category_conflict)

    var searchQuery  by remember { mutableStateOf("") }
    val filteredTags = remember(availableTags, searchQuery, selectedTags) {
        if (searchQuery.isBlank()) availableTags
        else availableTags.filter {
            it.name.contains(searchQuery.trim(), ignoreCase = true) || it.name in selectedTags
        }
    }
    val searchHasNoMatches = searchQuery.isNotBlank() &&
        availableTags.none { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    val tryCreateTag: (String) -> Unit = { raw ->
        val trimmed = raw.trim()
        when {
            trimmed.isBlank() -> {}
            availableTags.any { tagConflictKey(it.name) == tagConflictKey(trimmed) } -> newTagError = errDuplicate
            EntryCategory.entries.any { it.label.equals(trimmed, ignoreCase = true) } -> newTagError = errCategory
            else -> {
                onAddTag(trimmed)
                selectedTags = selectedTags + trimmed
                newTagText   = ""
                newTagError  = null
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                  .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text       = stringResource(R.string.options_sheet_category),
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EntryCategory.entries
                    .filter { it != EntryCategory.SIN_CATEGORIZAR }
                    .forEach { cat ->
                        val selected = selectedCategory == cat
                        Surface(
                            onClick      = { selectedCategory = if (selectedCategory == cat) EntryCategory.SIN_CATEGORIZAR else cat },
                            shape        = RoundedCornerShape(8.dp),
                            color        = if (selected) MaterialTheme.colorScheme.primary
                                           else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
                                           else MaterialTheme.colorScheme.onBackground
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier         = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text       = stringResource(cat.labelRes),
                                    style      = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    textAlign  = TextAlign.Center,
                                    maxLines   = 1
                                )
                            }
                        }
                    }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text  = stringResource(R.string.options_sheet_tags),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { autoLockManager?.recordInteraction(); searchQuery = it },
                placeholder   = { Text(stringResource(R.string.vault_search_tags)) },
                leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon  = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_clear))
                        }
                    }
                },
                modifier   = Modifier.fillMaxWidth(),
                shape      = CircleShape,
                singleLine = true,
                colors     = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedBorderColor    = Color.Transparent,
                    focusedBorderColor      = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(4.dp))

            Column(modifier = Modifier.animateContentSize()) {
            if (filteredTags.isEmpty()) {
                TagSearchEmptyState(
                    query       = searchQuery,
                    canCreate   = canCreateTags,
                    onCreateTag = tryCreateTag
                )
            }
            filteredTags.forEach { tag ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedTags = selectedTags.toMutableSet().apply {
                                if (contains(tag.name)) remove(tag.name) else add(tag.name)
                            }
                        }
                        .padding(start = 0.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked         = selectedTags.contains(tag.name),
                        onCheckedChange = {
                            selectedTags = selectedTags.toMutableSet().apply {
                                if (it) add(tag.name) else remove(tag.name)
                            }
                        },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text     = highlightText(tag.name, searchQuery, MaterialTheme.colorScheme.primary),
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick  = { renameTarget = tag; renameText = tag.name },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Edit,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                            modifier           = Modifier.size(18.dp)
                        )
                    }
                    TagDeleteButton(
                        tag      = tag,
                        onDelete = { selectedTags = selectedTags - tag.name; onDeleteTag(tag.id) }
                    )
                }
            }
            if (searchHasNoMatches && filteredTags.isNotEmpty()) {
                TagSearchEmptyState(
                    query       = searchQuery,
                    canCreate   = canCreateTags,
                    onCreateTag = tryCreateTag
                )
            }

            renameTarget?.let { target ->
                AlertDialog(
                    modifier = Modifier.cuadroConTeclado(),
                    properties = propiedadesConTeclado(),
                    onDismissRequest = { renameTarget = null; renameError = null },
                    title = { Text(stringResource(R.string.tag_rename_title)) },
                    text  = {
                        OutlinedTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                            value           = renameText,
                            onValueChange   = { autoLockManager?.recordInteraction(); renameText = it; renameError = null },
                            singleLine      = true,
                            label           = { Text(stringResource(R.string.tag_name_hint)) },
                            shape           = RoundedCornerShape(8.dp),
                            modifier        = Modifier.fillMaxWidth(),
                            isError         = renameError != null,
                            supportingText  = { renameError?.let { Text(it) } }
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick  = {
                                val trimmed = renameText.trim()
                                when {
                                    trimmed.isBlank() -> {}
                                    availableTags.any { it.id != target.id && tagConflictKey(it.name) == tagConflictKey(trimmed) } ->
                                        renameError = errDuplicate
                                    EntryCategory.entries.any { it.label.equals(trimmed, ignoreCase = true) } ->
                                        renameError = errCategory
                                    else -> {
                                        if (selectedTags.contains(target.name)) {
                                            selectedTags = (selectedTags - target.name) + trimmed
                                        }
                                        onRenameTag(target.id, trimmed)
                                        renameTarget = null
                                        renameError  = null
                                    }
                                }
                            },
                            enabled = renameText.trim().isNotBlank(),
                            shape   = RoundedCornerShape(8.dp),
                            colors  = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor   = MaterialTheme.colorScheme.primary
                            ),
                            border  = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                        ) { Text(stringResource(R.string.action_save), fontWeight = FontWeight.SemiBold) }
                    },
                    dismissButton = {
                        TextButton(onClick = { renameTarget = null; renameError = null }) {
                            Text(stringResource(R.string.action_cancel))
                        }
                    }
                )
            }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredTags.isNotEmpty()) {
                Button(
                    onClick  = { onSave(selectedCategory, selectedTags.toList()) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text       = stringResource(R.string.action_apply),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (showLockedDialog) {
                AlertDialog(
                    onDismissRequest = { showLockedDialog = false },
                    containerColor   = MaterialTheme.colorScheme.background,
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, null,
                                tint     = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.plans_feature_tags),
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    text = {
                        Text(stringResource(R.string.premium_locked_dialog_message),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    confirmButton = {
                        Button(
                            onClick = { showLockedDialog = false; onUpgradeClick() },
                            shape   = RoundedCornerShape(8.dp),
                            colors  = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor   = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                        ) { Text(stringResource(R.string.premium_gate_cta)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLockedDialog = false }) {
                            Text(stringResource(R.string.action_understood),
                                color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                )
            }

            if (!searchHasNoMatches) {
            if (canCreateTags) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (newTagText.isEmpty()) {
                        Text(
                            text  = stringResource(R.string.field_tag_name),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    BasicTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                        value         = newTagText,
                        onValueChange = { autoLockManager?.recordInteraction(); newTagText = it; newTagError = null },
                        singleLine    = true,
                        textStyle     = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush   = SolidColor(MaterialTheme.colorScheme.primary)
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                )
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .fillMaxHeight()
                        .clickable { tryCreateTag(newTagText) }
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier              = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Add,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(16.dp)
                        )
                        Text(
                            text       = stringResource(R.string.options_sheet_create_tag),
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.primary,
                            textAlign  = TextAlign.Center
                        )
                    }
                }
            }

            if (newTagError != null) {
                Text(
                    text     = newTagError!!,
                    color    = warningColor(),
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                        .clickable { showLockedDialog = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text  = stringResource(R.string.field_tag_name),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .fillMaxHeight()
                            .background(Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier              = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Lock,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier           = Modifier.size(16.dp)
                            )
                            Text(
                                text       = stringResource(R.string.options_sheet_create_tag),
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                textAlign  = TextAlign.Center
                            )
                        }
                    }
                }
            }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CryptoCredentialLinkField(
    linkedName:     String,
    hasLink:        Boolean,
    onPickClick:    () -> Unit,
    onViewClick:    () -> Unit,
    onClearClick:   () -> Unit,
    shape:          Shape = RoundedCornerShape(8.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerColor)
            .padding(start = 14.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = stringResource(R.string.bank_link_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text  = if (hasLink) linkedName.ifEmpty { stringResource(R.string.bank_link_linked) }
                         else stringResource(R.string.bank_link_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = if (hasLink) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
        if (hasLink) {
            IconButton(onClick = onViewClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = stringResource(R.string.action_view_credential),
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onClearClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector        = Icons.Default.LinkOff,
                    contentDescription = stringResource(R.string.action_unlink),
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(18.dp)
                )
            }
        } else {
            IconButton(onClick = onPickClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector        = Icons.Default.Link,
                    contentDescription = stringResource(R.string.action_link),
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(18.dp)
                )
            }
        }
    }
}


@Composable
private fun CredentialPickerSheet(
    entries:   List<com.thormakk.keyarmor.domain.model.VaultEntry>,
    onSelect:  (com.thormakk.keyarmor.domain.model.VaultEntry) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val autoLockManager = LocalAutoLockManager.current
    var query by remember { mutableStateOf("") }

    val filtered = remember(query, entries) {
        if (query.isBlank()) emptyList()
        else {
            val q = query.trim().lowercase(Locale.getDefault())
            entries.filter { e ->
                e.title.lowercase(Locale.getDefault()).contains(q) ||
                e.username.lowercase(Locale.getDefault()).contains(q) ||
                e.email.lowercase(Locale.getDefault()).contains(q) ||
                e.url.lowercase(Locale.getDefault()).contains(q)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text       = stringResource(R.string.bank_link_dialog_title),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onBackground,
                    modifier   = Modifier.weight(1f)
                )
                TextButton(onClick = onDismiss) {
                    Text(
                        text  = stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value         = query,
                onValueChange = { autoLockManager?.recordInteraction(); query = it },
                placeholder   = { Text(stringResource(R.string.vault_search_placeholder)) },
                leadingIcon   = {
                    Icon(
                        imageVector        = Icons.Default.Search,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon  = if (query.isNotEmpty()) ({
                    IconButton(onClick = { query = "" }) {
                        Icon(
                            imageVector        = Icons.Default.Close,
                            contentDescription = stringResource(R.string.action_clear),
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(18.dp)
                        )
                    }
                }) else null,
                singleLine = true,
                shape      = RoundedCornerShape(8.dp),
                colors     = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedLabelColor    = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when {
                query.isBlank() -> {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement  = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Search,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier           = Modifier.size(32.dp)
                            )
                            Text(
                                text  = stringResource(R.string.search_empty_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                filtered.isEmpty() -> {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = stringResource(R.string.search_no_results),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier       = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        items(filtered, key = { it.id }) { entry ->
                            Row(
                                modifier          = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(entry) }
                                    .padding(horizontal = 8.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector        = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier           = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text     = entry.title,
                                        style    = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color    = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val subtitle = entry.username
                                        .ifEmpty { entry.email }
                                        .ifEmpty { entry.url }
                                    if (subtitle.isNotEmpty()) {
                                        Text(
                                            text     = subtitle,
                                            style    = MaterialTheme.typography.bodySmall,
                                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis

                                        )
                                    }
                                }
                            }
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private fun formatFiatDisplay(amount: Double, currency: String): String {
    val nf = java.text.NumberFormat.getNumberInstance(
        androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().get(0)
            ?: Locale.getDefault()
    ).apply {
        isGroupingUsed        = true
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }
    val formatted = when {
        amount >= 1_000_000 -> "${nf.apply { maximumFractionDigits = 2 }.format(amount / 1_000_000)}M"
        else                -> nf.format(amount)
    }
    return "$formatted $currency"
}
