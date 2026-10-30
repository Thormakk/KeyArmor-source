@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@file:Suppress("UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UnusedAssignment")
package com.thormakk.keyarmor.presentation.screens.creditcard

import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.BiometricPrefs
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.Tag
import androidx.compose.ui.graphics.Shape
import com.thormakk.keyarmor.presentation.components.CountryCodePickerDialog
import com.thormakk.keyarmor.presentation.components.EntryGroupDivider
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.TagDeleteButton
import com.thormakk.keyarmor.presentation.components.TagSearchEmptyState
import com.thormakk.keyarmor.presentation.components.cuadroConTeclado
import com.thormakk.keyarmor.presentation.components.defaultDialCode
import com.thormakk.keyarmor.presentation.components.entryItemShape
import com.thormakk.keyarmor.presentation.components.propiedadesConTeclado
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.util.BiometricGate
import com.thormakk.keyarmor.presentation.util.ClipboardUtil
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.presentation.util.LocalClipboardClearManager
import com.thormakk.keyarmor.presentation.util.highlightText
import com.thormakk.keyarmor.presentation.util.rememberBiometricGate
import java.util.Calendar
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea

private val MergedCardRadius     = 14.dp
private val MergedCardStartInset = 16.dp
private val MergedCardEndInset   = 16.dp

private val CARD_BRANDS = listOf(
    "Visa", "Mastercard", "American Express", "Discover", "Maestro",
    "UnionPay", "JCB", "Diners Club",
    "Cabal", "Naranja", "Elo", "Hipercard",
    "RuPay", "Troy"
)

@Composable
fun AddEditCreditCardScreen(
    onBack:       () -> Unit,
    onSaved:      () -> Unit,
    sharedTransitionScope:   SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    viewModel: AddEditCreditCardViewModel = hiltViewModel()
) {
    val uiState       by viewModel.uiState.collectAsStateWithLifecycle()
    val clipClear     = LocalClipboardClearManager.current
    val autoLockManager = LocalAutoLockManager.current

    LaunchedEffect(uiState.isSaved)   { if (uiState.isSaved)   onSaved() }

    val context = LocalContext.current
    LaunchedEffect(uiState.isNew) {
        if (uiState.isNew && uiState.bankPhonePrefix == "+") {
            val code = defaultDialCode(context)
            if (code.isNotEmpty()) viewModel.onBankPhonePrefixChange(code)
        }
    }

    BackHandler { onBack() }

    var showCardNumber    by remember { mutableStateOf(false) }
    var showCvv           by remember { mutableStateOf(false) }
    var showBrandPicker   by remember { mutableStateOf(false) }
    var showCountryPicker by remember { mutableStateOf(false) }
    var showExpiryPicker  by remember { mutableStateOf(false) }

    val cvvGate: BiometricGate = rememberBiometricGate(
        section              = BiometricPrefs.SECTION_CARDS,
        prefs                = viewModel.biometricPrefs,
        verifyMasterPassword = viewModel::verifyMasterPassword
    )
    val cvvSessionExpiry by cvvGate.sessionExpiresAt

    LaunchedEffect(cvvSessionExpiry) {
        if (cvvSessionExpiry > 0L) {
            val remaining = cvvSessionExpiry - System.currentTimeMillis()
            if (remaining > 0) kotlinx.coroutines.delay(remaining)
            showCvv = false
        }
    }

    if (showBrandPicker) {
        var showCustomInput by remember { mutableStateOf(false) }
        var customBrandText by remember { mutableStateOf("") }
        val otherLabel = stringResource(R.string.card_brand_other)

        AlertDialog(
        conScroll = false,
            modifier = Modifier.cuadroConTeclado(),
            properties = propiedadesConTeclado(),
            onDismissRequest = { showBrandPicker = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    if (showCustomInput) otherLabel
                    else stringResource(R.string.card_select_brand)
                )
            },
            text = {
                if (showCustomInput) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(
                            value         = customBrandText,
                            onValueChange = { autoLockManager?.recordInteraction(); customBrandText = it },
                            placeholder   = { Text(stringResource(R.string.card_brand_custom_label), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            singleLine    = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RoundedCornerShape(8.dp),
                            colors        = fieldColors()
                        )
                    }
                } else {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        (CARD_BRANDS + otherLabel).forEach { brand ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (brand == otherLabel) {
                                            customBrandText = ""
                                            showCustomInput = true
                                        } else {
                                            viewModel.onBrandChange(brand)
                                            showBrandPicker = false
                                        }
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text  = brand,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (uiState.brand == brand) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onBackground
                                )
                                if (uiState.brand == brand) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint     = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (showCustomInput) {
                    Button(
                        onClick  = {
                            val trimmed = customBrandText.trim()
                            if (trimmed.isNotEmpty()) {
                                viewModel.onBrandChange(trimmed)
                                showBrandPicker = false
                            }
                        },
                        enabled  = customBrandText.isNotBlank(),
                        shape    = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor   = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            stringResource(R.string.action_save)
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (showCustomInput) showCustomInput = false
                        else showBrandPicker = false
                    }
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
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
                    windowInsets = WindowInsets(0),
                    title = {
                        TextoDeUnaLinea(
                            text       = if (uiState.isNew) stringResource(R.string.card_new)
                                         else stringResource(R.string.card_edit),
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 7.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val mergedCardColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            val mergedGapColor  = MaterialTheme.colorScheme.background

            Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value         = uiState.brand,
                    onValueChange = {},
                    readOnly      = true,
                    placeholder   = { Text(stringResource(R.string.card_field_brand), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    leadingIcon   = {
                        val avatarModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                            with(sharedTransitionScope) {
                                Modifier.sharedElement(
                                    sharedContentState      = rememberSharedContentState("card_avatar_${uiState.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                            }
                        } else Modifier
                        CreditCardAwareAvatar(brand = uiState.brand, size = 32.dp, modifier = avatarModifier)
                    },
                    trailingIcon  = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = entryItemShape(isFirst = true, isLast = false, radius = MergedCardRadius),
                    colors        = titleFieldColors(),
                    enabled       = false
                )
                Box(modifier = Modifier.matchParentSize().clickable { showBrandPicker = true })
            }
            EntryGroupDivider(
                startInset = 0.dp,
                endInset   = 0.dp,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            CardTextField(
                value         = uiState.issuer,
                onValueChange = viewModel::onIssuerChange,
                label         = stringResource(R.string.card_field_issuer),
                leadingIcon   = Icons.Default.AccountBalance,
                shape         = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius)
            )
            EntryGroupDivider(
                startInset = MergedCardStartInset,
                endInset   = MergedCardEndInset,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            CardTextField(
                value         = uiState.cardholder,
                onValueChange = viewModel::onCardholderChange,
                label         = stringResource(R.string.card_field_cardholder),
                leadingIcon   = Icons.Default.Person,
                shape         = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius)
            )
            EntryGroupDivider(
                startInset = MergedCardStartInset,
                endInset   = MergedCardEndInset,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            OutlinedTextField(
                value         = uiState.cardNumber,
                onValueChange = { raw ->
                    autoLockManager?.recordInteraction()
                    viewModel.onCardNumberChange(raw.filter { it.isDigit() }.take(16))
                },
                placeholder   = {
                    Text(
                        stringResource(R.string.card_number_placeholder),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                leadingIcon   = {
                    Icon(Icons.Default.CreditCard, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon  = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showCardNumber = !showCardNumber }) {
                            Icon(
                                imageVector        = if (showCardNumber) Icons.Default.VisibilityOff
                                                     else Icons.Default.Visibility,
                                contentDescription = if (showCardNumber) stringResource(R.string.card_hide_cvv)
                                                     else stringResource(R.string.card_show_cvv),
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick  = { if (uiState.cardNumber.isNotEmpty()) { ClipboardUtil.copySensitive(context, uiState.cardNumber); clipClear.scheduleClear() } },
                            enabled  = uiState.cardNumber.isNotEmpty()
                        ) {
                            Icon(
                                imageVector        = Icons.Default.ContentCopy,
                                contentDescription = stringResource(R.string.action_copy),
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                visualTransformation = if (showCardNumber) CardNumberGroupTransformation()
                                       else CardNumberVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                shape         = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius),
                colors        = fieldColors()
            )
            EntryGroupDivider(
                startInset = MergedCardStartInset,
                endInset   = MergedCardEndInset,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            if (showExpiryPicker) {
                val initialMillis = remember(uiState.expiryDate) {
                    if (uiState.expiryDate.length >= 4) {
                        val mm   = uiState.expiryDate.take(2).toIntOrNull() ?: 1
                        val yy   = uiState.expiryDate.drop(2).toIntOrNull() ?: 0
                        Calendar.getInstance().apply {
                            set(Calendar.YEAR,         2000 + yy)
                            set(Calendar.MONTH,        mm - 1)
                            set(Calendar.DAY_OF_MONTH, 1)
                        }.timeInMillis
                    } else null
                }
                val pickerState = rememberDatePickerState(
                    initialSelectedDateMillis = initialMillis
                )
                DatePickerDialog(
                    onDismissRequest = { showExpiryPicker = false },
                    confirmButton    = {
                        TextButton(onClick = {
                            pickerState.selectedDateMillis?.let { millis ->
                                val cal = Calendar.getInstance().apply { timeInMillis = millis }
                                val mm  = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
                                val yy  = (cal.get(Calendar.YEAR) % 100).toString().padStart(2, '0')
                                viewModel.onExpiryDateChange(mm + yy)
                            }
                            showExpiryPicker = false
                        }) { Text(stringResource(android.R.string.ok)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showExpiryPicker = false }) {
                            Text(stringResource(android.R.string.cancel))
                        }
                    }
                ) { DatePicker(state = pickerState) }
            }
            Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value         = uiState.expiryDate,
                onValueChange = {},
                readOnly      = true,
                placeholder   = {
                    Text(
                        stringResource(R.string.card_expiry_placeholder),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                leadingIcon   = {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                visualTransformation = ExpiryDateVisualTransformation(),
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                shape         = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius),
                colors        = fieldColors()
            )
            Box(modifier = Modifier.matchParentSize().clickable { showExpiryPicker = true })
            }
            EntryGroupDivider(
                startInset = MergedCardStartInset,
                endInset   = MergedCardEndInset,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            OutlinedTextField(
                value         = uiState.cvv,
                onValueChange = { v -> autoLockManager?.recordInteraction(); viewModel.onCvvChange(v.filter { it.isDigit() }.take(4)) },
                placeholder   = { Text(stringResource(R.string.card_field_cvv), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                leadingIcon   = {
                    Icon(Icons.Default.Lock, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon  = {
                    IconButton(onClick = {
                        if (showCvv) showCvv = false
                        else cvvGate.launch { showCvv = true }
                    }) {
                        Icon(
                            imageVector        = if (showCvv) Icons.Default.VisibilityOff
                                                 else Icons.Default.Visibility,
                            contentDescription = if (showCvv) stringResource(R.string.card_hide_cvv)
                                                 else stringResource(R.string.card_show_cvv),
                            tint               = if (viewModel.biometricPrefs.isSectionEnabled(BiometricPrefs.SECTION_CARDS))
                                                     MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                                 else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                },
                visualTransformation = if (showCvv) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                shape         = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius),
                colors        = fieldColors()
            )
            EntryGroupDivider(
                startInset = MergedCardStartInset,
                endInset   = MergedCardEndInset,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            Row(
                modifier              = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.Top
            ) {
                if (showCountryPicker) {
                    CountryCodePickerDialog(
                        onSelect  = { code ->
                            viewModel.onBankPhonePrefixChange(code.dialCode)
                            showCountryPicker = false
                        },
                        onDismiss = { showCountryPicker = false }
                    )
                }
                OutlinedTextField(
                    value         = uiState.bankPhonePrefix.removePrefix("+"),
                    onValueChange = { v ->
                        autoLockManager?.recordInteraction()
                        val digits = v.filter { it.isDigit() }.take(4)
                        viewModel.onBankPhonePrefixChange(if (digits.isEmpty()) "" else "+$digits")
                    },
                    placeholder   = { Text(stringResource(R.string.card_field_bank_phone_prefix), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    prefix        = {
                        Text(
                            text  = "+",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon   = {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { showCountryPicker = true }
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine    = true,
                    modifier      = Modifier.weight(1f),
                    shape         = entryItemShape(isFirst = false, isLast = true, radius = MergedCardRadius),
                    colors        = fieldColors()
                )
                val context = LocalContext.current
                OutlinedTextField(
                    value           = uiState.bankPhoneNumber,
                    onValueChange   = { v ->
                        autoLockManager?.recordInteraction()
                        val filtered = v.filter { it.isDigit() || it == ' ' || it == '-' || it == '(' || it == ')' }
                        viewModel.onBankPhoneNumberChange(truncateToMaxDigits(filtered, 13))
                    },
                    placeholder     = { Text(stringResource(R.string.card_field_bank_phone), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    trailingIcon    = {
                        val fullNumber = buildString {
                            val prefix = uiState.bankPhonePrefix.trim()
                            if (prefix.isNotEmpty()) append(prefix)
                            append(uiState.bankPhoneNumber.trim())
                        }
                        IconButton(
                            onClick  = {
                                val uri = "tel:${fullNumber.filter { it.isDigit() || it == '+' }}".toUri()
                                context.startActivity(Intent(Intent.ACTION_DIAL, uri))
                            },
                            enabled  = uiState.bankPhoneNumber.isNotEmpty()
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Call,
                                contentDescription = stringResource(R.string.action_call),
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine      = true,
                    modifier        = Modifier.weight(2f),
                    shape           = entryItemShape(isFirst = false, isLast = true, radius = MergedCardRadius),
                    colors          = fieldColors()
                )
            }
            }

        }
    }
}

@Composable
private fun CardTextField(
    value:         String,
    onValueChange: (String) -> Unit,
    label:         String,
    leadingIcon:   ImageVector,
    keyboardType:  KeyboardType = KeyboardType.Uri,
    shape:         Shape = RoundedCornerShape(8.dp)
) {
    val autoLockManager = LocalAutoLockManager.current
    OutlinedTextField(
        value           = value,
        onValueChange   = { autoLockManager?.recordInteraction(); onValueChange(it) },
        placeholder     = { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
        leadingIcon     = {
            Icon(leadingIcon, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, autoCorrectEnabled = false),
        singleLine      = true,
        modifier        = Modifier.fillMaxWidth(),
        shape           = shape,
        colors          = fieldColors()
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    unfocusedContainerColor  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    disabledContainerColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    focusedBorderColor       = Color.Transparent,
    unfocusedBorderColor     = Color.Transparent,
    disabledBorderColor      = Color.Transparent,
    disabledTextColor        = MaterialTheme.colorScheme.onSurface,
    disabledLabelColor       = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedLabelColor        = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor      = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor              = MaterialTheme.colorScheme.primary
)

@Composable
private fun titleFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor  = MaterialTheme.colorScheme.surface,
    focusedBorderColor      = Color.Transparent,
    unfocusedBorderColor    = Color.Transparent,
    disabledBorderColor     = Color.Transparent,
    disabledTextColor       = MaterialTheme.colorScheme.onSurface,
    disabledLabelColor      = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedLabelColor       = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor     = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor             = MaterialTheme.colorScheme.primary
)

@Suppress("SameParameterValue")
private fun truncateToMaxDigits(value: String, maxDigits: Int): String {
    var count = 0
    return buildString {
        for (ch in value) {
            if (ch.isDigit()) {
                if (count < maxDigits) { append(ch); count++ } else return@buildString
            } else {
                append(ch)
            }
        }
    }
}

private class ExpiryDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val digits = text.text
        val out = buildString {
            digits.forEachIndexed { i, ch ->
                if (i == 2) append('/')
                append(ch)
            }
        }
        val offsetMapping = object : androidx.compose.ui.text.input.OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                (if (offset <= 1) offset else offset + 1).coerceAtMost(out.length)
            override fun transformedToOriginal(offset: Int): Int =
                (if (offset <= 2) offset else offset - 1).coerceIn(0, digits.length)
        }
        return androidx.compose.ui.text.input.TransformedText(AnnotatedString(out), offsetMapping)
    }
}

private class CardNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val raw    = text.text
        val masked = buildString {
            raw.forEachIndexed { index, ch ->
                if (index > 0 && index % 4 == 0) append('-')
                append(if (index < raw.length - 4) '×' else ch)
            }
        }
        val offsetMapping = object : androidx.compose.ui.text.input.OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val dashes = minOf(offset / 4, 3)
                return minOf(offset + dashes, masked.length)
            }
            override fun transformedToOriginal(offset: Int): Int {
                val dashes = minOf(offset / 5, 3)
                return minOf(offset - dashes, raw.length)
            }
        }
        return androidx.compose.ui.text.input.TransformedText(AnnotatedString(masked), offsetMapping)
    }
}

private class CardNumberGroupTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val raw     = text.text
        val grouped = buildString {
            raw.forEachIndexed { index, ch ->
                if (index > 0 && index % 4 == 0) append('-')
                append(ch)
            }
        }
        val offsetMapping = object : androidx.compose.ui.text.input.OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val dashes = minOf(offset / 4, 3)
                return minOf(offset + dashes, grouped.length)
            }
            override fun transformedToOriginal(offset: Int): Int {
                val dashes = minOf(offset / 5, 3)
                return minOf(offset - dashes, raw.length)
            }
        }
        return androidx.compose.ui.text.input.TransformedText(AnnotatedString(grouped), offsetMapping)
    }
}

@Composable
internal fun CreditCardOptionsSheet(
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
    var newTagText       by remember { mutableStateOf("") }
    var newTagError      by remember { mutableStateOf<String?>(null) }
    var renameTarget     by remember { mutableStateOf<Tag?>(null) }
    var renameText       by remember { mutableStateOf("") }
    var renameError      by remember { mutableStateOf<String?>(null) }
    var showLockedDialog by remember { mutableStateOf(false) }

    val errDuplicate = stringResource(R.string.tag_error_duplicate)
    val errCategory  = stringResource(R.string.tag_error_category_conflict)

    var searchQuery by remember { mutableStateOf("") }
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
                text       = stringResource(R.string.options_sheet_tags),
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { autoLockManager?.recordInteraction(); searchQuery = it },
                placeholder   = { Text(stringResource(R.string.vault_search_tags)) },
                leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon  = if (searchQuery.isNotEmpty()) ({
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_clear))
                    }
                }) else null,
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
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
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
                    containerColor   = MaterialTheme.colorScheme.background,
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
                            onClick = {
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
                            shape    = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor   = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        ) { Text(stringResource(R.string.action_save), fontWeight = FontWeight.SemiBold) }
                    },
                    dismissButton = {
                        TextButton(onClick = { renameTarget = null; renameError = null }) {
                            Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
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
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
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
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
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
                    color    = MaterialTheme.colorScheme.error,
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
        }
    }
}
