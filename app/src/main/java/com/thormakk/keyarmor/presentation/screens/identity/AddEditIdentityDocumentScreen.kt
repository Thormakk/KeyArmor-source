@file:OptIn(ExperimentalMaterial3Api::class)
package com.thormakk.keyarmor.presentation.screens.identity

import com.thormakk.keyarmor.presentation.components.seguirTextoMultilinea
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.exclude
import com.thormakk.keyarmor.presentation.components.sigueAlTeclado
import com.thormakk.keyarmor.presentation.components.sigueAlTecladoContenido
import com.thormakk.keyarmor.presentation.components.rememberSigueAlTeclado
import com.thormakk.keyarmor.presentation.components.sigueAlTecladoObjetivoEnfocado
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.scale
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.ocr.OcrResult
import com.thormakk.keyarmor.presentation.components.EntryGroupDivider
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.TagDeleteButton
import com.thormakk.keyarmor.presentation.components.TagSearchEmptyState
import com.thormakk.keyarmor.presentation.components.cuadroConTeclado
import com.thormakk.keyarmor.presentation.components.entryItemShape
import com.thormakk.keyarmor.presentation.components.propiedadesConTeclado
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.presentation.util.highlightText
import com.thormakk.keyarmor.presentation.components.topBarColors
import java.io.ByteArrayOutputStream
import java.util.Date
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.justified

private val MergedCardRadius     = 14.dp
private val MergedCardStartInset = 16.dp
private val MergedCardEndInset   = 16.dp

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
private fun transparentFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedBorderColor      = Color.Transparent,
    unfocusedBorderColor    = Color.Transparent,
    focusedLabelColor       = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor     = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor             = MaterialTheme.colorScheme.primary
)

@Composable
fun AddEditIdentityDocumentScreen(
    onBack:       () -> Unit,
    onSaved:      () -> Unit,
    onDeleted:    () -> Unit,
    viewModel:    AddEditIdentityDocumentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val autoLockManager = LocalAutoLockManager.current

    var showScanner            by remember { mutableStateOf(false) }
    var scanResult             by remember { mutableStateOf<OcrResult?>(null) }
    var scanError              by remember { mutableStateOf(false) }
    var showExpiryPicker       by remember { mutableStateOf(false) }
    var showIssuePicker        by remember { mutableStateOf(false) }
    var showSaveValidationError by remember { mutableStateOf(false) }

    var pendingFrontBytes by remember { mutableStateOf<ByteArray?>(null) }
    var pendingBackBytes  by remember { mutableStateOf<ByteArray?>(null) }

    var frontBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var backBitmap  by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(uiState.frontPhotoPath) {
        frontBitmap = uiState.frontPhotoPath?.let { path ->
            viewModel.decryptPhoto(path)?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        }
    }
    LaunchedEffect(uiState.backPhotoPath) {
        backBitmap = uiState.backPhotoPath?.let { path ->
            viewModel.decryptPhoto(path)?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        }
    }
    LaunchedEffect(pendingFrontBytes) {
        pendingFrontBytes?.let { frontBitmap = BitmapFactory.decodeByteArray(it, 0, it.size) }
    }
    LaunchedEffect(pendingBackBytes) {
        pendingBackBytes?.let { backBitmap = BitmapFactory.decodeByteArray(it, 0, it.size) }
    }

    val context           = LocalContext.current
    var photoPickingFront by remember { mutableStateOf(true) }
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val stream = context.contentResolver.openInputStream(uri) ?: return@rememberLauncherForActivityResult
        val original = BitmapFactory.decodeStream(stream)
        stream.close()
        if (original == null) return@rememberLauncherForActivityResult
        val maxDim  = 1200
        val scale   = minOf(maxDim.toFloat() / original.width, maxDim.toFloat() / original.height, 1f)
        val scaled  = if (scale < 1f) original.scale((original.width * scale).toInt(), (original.height * scale).toInt()) else original
        val out     = ByteArrayOutputStream()
        scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, out)
        val bytes = out.toByteArray()
        if (photoPickingFront) pendingFrontBytes = bytes else pendingBackBytes = bytes
        viewModel.setPendingPhoto(bytes, photoPickingFront)
    }

    LaunchedEffect(uiState.isSaved)   { if (uiState.isSaved)   onSaved() }
    LaunchedEffect(uiState.isDeleted) { if (uiState.isDeleted) onDeleted() }

    BackHandler { onBack() }

    if (showScanner) {
        DocumentScannerScreen(
            onResult  = { result, frontPhoto, backPhoto ->
                showScanner = false
                if (frontPhoto != null) pendingFrontBytes = frontPhoto
                if (backPhoto  != null) pendingBackBytes  = backPhoto
                viewModel.setPendingPhotos(frontPhoto, backPhoto)
                if (result.hasAnyData) scanResult = result else scanError = true
            },
            onDismiss = { showScanner = false }
        )
        return
    }

    if (showSaveValidationError) {
        AlertDialog(
            onDismissRequest = { showSaveValidationError = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.identity_doc_validation_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
            text  = { Text(stringResource(R.string.identity_doc_validation_message), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified()) },
            confirmButton = { TextButton(onClick = { showSaveValidationError = false }) { Text(stringResource(R.string.action_ok)) } }
        )
    }

    if (scanError) {
        AlertDialog(
            onDismissRequest = { scanError = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.identity_doc_scan_error_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
            text  = { Text(stringResource(R.string.identity_doc_scan_error_message), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified()) },
            confirmButton = { TextButton(onClick = { scanError = false }) { Text(stringResource(R.string.action_ok)) } }
        )
    }

    scanResult?.let { result ->
        OcrResultDialog(
            result    = result,
            onApply   = { selected -> viewModel.applyOcrResult(selected); scanResult = null },
            onDismiss = { scanResult = null }
        )
    }

    if (showExpiryPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.expiryDate.toPickerMillis())
        DatePickerDialog(
            onDismissRequest = { showExpiryPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { viewModel.onExpiryDateChange(it.toDateString()) }
                    showExpiryPicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = { TextButton(onClick = { showExpiryPicker = false }) { Text(stringResource(R.string.action_cancel)) } }
        ) { DatePicker(state = pickerState) }
    }

    if (showIssuePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.issueDate.toPickerMillis())
        DatePickerDialog(
            onDismissRequest = { showIssuePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { viewModel.onIssueDateChange(it.toDateString()) }
                    showIssuePicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = { TextButton(onClick = { showIssuePicker = false }) { Text(stringResource(R.string.action_cancel)) } }
        ) { DatePicker(state = pickerState) }
    }

    val sigueTeclado = rememberSigueAlTeclado()
    val scrollForm = rememberScrollState()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing.exclude(WindowInsets.ime),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars).background(MaterialTheme.colorScheme.surface))
                TopAppBar(
                    windowInsets = WindowInsets(0),
                    title = {
                        TextoDeUnaLinea(
                            if (uiState.isNew) stringResource(R.string.identity_doc_new_title) else stringResource(R.string.identity_doc_edit_title),
                            style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                            fontWeight = FontWeight.SemiBold,
                            color      = sectionTitleColor()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back), tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    actions = {
                        CaptureAllowedBadge()
                        ThemeTrialBadge()
                        IconButton(onClick = { showScanner = true }) {
                            Icon(Icons.Default.DocumentScanner, stringResource(R.string.identity_doc_scan_button_desc), tint = MaterialTheme.colorScheme.onSurface)
                        }
                        IconButton(onClick = {
                            if (uiState.holderName.isNotBlank() && uiState.docType.isNotBlank() && uiState.number.isNotBlank()) {
                                viewModel.save()
                            } else {
                                showSaveValidationError = true
                            }
                        }) {
                            Icon(Icons.Default.Save, stringResource(R.string.action_save), tint = MaterialTheme.colorScheme.onSurface)
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
                .sigueAlTeclado(scrollForm, sigueTeclado, margen = 8.dp, habilitado = { sigueTeclado.campoEnfocado })
                .verticalScroll(scrollForm)
                .sigueAlTecladoContenido(sigueTeclado)
                .padding(horizontal = 7.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val mergedCardColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            val mergedGapColor  = MaterialTheme.colorScheme.background
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                    value         = uiState.holderName,
                    onValueChange = { autoLockManager?.recordInteraction(); viewModel.onHolderNameChange(it) },
                    placeholder   = { Text(stringResource(R.string.identity_doc_field_holder_name), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = entryItemShape(isFirst = true, isLast = false, radius = MergedCardRadius),
                    colors        = titleFieldColors()
                )
                EntryGroupDivider(
                    startInset = 0.dp,
                    endInset   = 0.dp,
                    cardColor  = mergedCardColor,
                    gapColor   = mergedGapColor
                )

                Surface(
                    shape    = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius),
                    color    = mergedCardColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                            value         = uiState.docType,
                            onValueChange = { autoLockManager?.recordInteraction(); viewModel.onDocTypeChange(it) },
                            placeholder   = { Text(stringResource(R.string.identity_doc_field_doctype_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            modifier      = Modifier.weight(1.6f),
                            shape         = RoundedCornerShape(8.dp),
                            colors        = transparentFieldColors()
                        )
                        Text(
                            text  = "–",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                            value         = uiState.issuingCountry,
                            onValueChange = { autoLockManager?.recordInteraction(); viewModel.onIssuingCountryChange(it) },
                            placeholder   = { Text(stringResource(R.string.identity_doc_field_issuing_country_hint), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            modifier      = Modifier.weight(1f),
                            shape         = RoundedCornerShape(8.dp),
                            colors        = transparentFieldColors()
                        )
                    }
                }
                EntryGroupDivider(
                    startInset = MergedCardStartInset,
                    endInset   = MergedCardEndInset,
                    cardColor  = mergedCardColor,
                    gapColor   = mergedGapColor
                )

                OutlinedTextField(
                    value         = uiState.number,
                    onValueChange = { autoLockManager?.recordInteraction(); viewModel.onNumberChange(it) },
                    placeholder   = { Text(stringResource(R.string.identity_doc_field_number), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
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

                OutlinedTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                    value         = uiState.birthDate,
                    onValueChange = { autoLockManager?.recordInteraction(); viewModel.onBirthDateChange(it) },
                    placeholder   = { Text(stringResource(R.string.identity_doc_field_birth_date), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
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

                OutlinedTextField(
                    value         = uiState.issueDate,
                    onValueChange = {},
                    readOnly      = true,
                    placeholder   = { Text(stringResource(R.string.identity_doc_field_issue_date), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    modifier      = Modifier.fillMaxWidth().clickable { showIssuePicker = true },
                    shape         = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius),
                    colors        = fieldColors(),
                    trailingIcon  = if (uiState.issueDate.isNotBlank()) ({
                        IconButton(onClick = { viewModel.onIssueDateChange("") }) {
                            Icon(Icons.Default.Clear, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }) else ({
                        IconButton(onClick = { showIssuePicker = true }) {
                            Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    })
                )
                EntryGroupDivider(
                    startInset = MergedCardStartInset,
                    endInset   = MergedCardEndInset,
                    cardColor  = mergedCardColor,
                    gapColor   = mergedGapColor
                )

                OutlinedTextField(
                    value         = uiState.expiryDate,
                    onValueChange = {},
                    readOnly      = true,
                    placeholder   = { Text(stringResource(R.string.identity_doc_field_expiry_date), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    modifier      = Modifier.fillMaxWidth().clickable { showExpiryPicker = true },
                    shape         = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius),
                    colors        = fieldColors(),
                    trailingIcon  = if (uiState.expiryDate.isNotBlank()) ({
                        IconButton(onClick = { viewModel.onExpiryDateChange("") }) {
                            Icon(Icons.Default.Clear, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }) else ({
                        IconButton(onClick = { showExpiryPicker = true }) {
                            Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    })
                )
                EntryGroupDivider(
                    startInset = MergedCardStartInset,
                    endInset   = MergedCardEndInset,
                    cardColor  = mergedCardColor,
                    gapColor   = mergedGapColor
                )

                Box {
                    OutlinedTextField(
                        value         = uiState.notes,
                        onValueChange = { autoLockManager?.recordInteraction(); viewModel.onNotesChange(it) },
                        placeholder   = { Text(stringResource(R.string.identity_doc_field_notes), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        modifier      = Modifier.fillMaxWidth().heightIn(min = 80.dp).sigueAlTecladoObjetivoEnfocado(sigueTeclado).seguirTextoMultilinea(),
                        shape         = entryItemShape(isFirst = false, isLast = true, radius = MergedCardRadius),
                        colors        = fieldColors(),
                        minLines      = 3
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

            Text(
                stringResource(R.string.identity_doc_photos_section_title),
                style     = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier  = Modifier.padding(top = 8.dp, bottom = 2.dp)
            )
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DocumentPhotoSlot(
                    label     = stringResource(R.string.identity_doc_photo_label_front),
                    bitmap    = frontBitmap,
                    onPick    = {
                        photoPickingFront = true
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onDelete  = {
                        frontBitmap = null
                        pendingFrontBytes = null
                        viewModel.deletePhoto(isFront = true)
                    },
                    modifier  = Modifier.weight(1f)
                )
                DocumentPhotoSlot(
                    label     = stringResource(R.string.identity_doc_photo_label_back),
                    bitmap    = backBitmap,
                    onPick    = {
                        photoPickingFront = false
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onDelete  = {
                        backBitmap = null
                        pendingBackBytes = null
                        viewModel.deletePhoto(isFront = false)
                    },
                    modifier  = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DocumentPhotoSlot(
    label:    String,
    bitmap:   android.graphics.Bitmap?,
    onPick:   () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    Box(
        modifier = modifier
            .aspectRatio(1.586f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
    ) {
        if (bitmap != null) {
            Image(
                bitmap      = bitmap.asImageBitmap(),
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier    = Modifier.fillMaxSize()
            )
            IconButton(
                onClick  = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(50))
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.identity_doc_delete_photo_desc, label),
                    tint     = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        } else {
            Column(
                modifier              = Modifier
                    .fillMaxSize()
                    .clickable { onPick() },
                verticalArrangement   = Arrangement.Center,
                horizontalAlignment   = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.AddAPhoto,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    label,
                    style  = MaterialTheme.typography.labelSmall,
                    color  = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun OcrResultDialog(
    result:   OcrResult,
    onApply:  (OcrResult) -> Unit,
    onDismiss: () -> Unit
) {
    var applyDocType   by remember { mutableStateOf(!result.docType.isNullOrBlank()) }
    var applyName      by remember { mutableStateOf(!result.holderName.isNullOrBlank()) }
    var applyNumber    by remember { mutableStateOf(!result.number.isNullOrBlank()) }
    var applyCountry   by remember { mutableStateOf(!result.issuingCountry.isNullOrBlank()) }
    var applyBirth     by remember { mutableStateOf(!result.birthDate.isNullOrBlank()) }
    var applyIssue     by remember { mutableStateOf(!result.issueDate.isNullOrBlank()) }
    var applyExpiry    by remember { mutableStateOf(!result.expiryDate.isNullOrBlank()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = MaterialTheme.colorScheme.background,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.DocumentScanner, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.identity_doc_scan_results_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }
        },
        text = {
            val docTypeLbl    = stringResource(R.string.identity_doc_field_type)
            val holderNameLbl = stringResource(R.string.identity_doc_field_holder_name)
            val docNumberLbl  = stringResource(R.string.identity_doc_field_number)
            val countryLbl    = stringResource(R.string.identity_doc_field_issuing_country)
            val birthDateLbl  = stringResource(R.string.identity_doc_field_birth_date)
            val issueDateLbl  = stringResource(R.string.identity_doc_field_issue_date)
            val expiryDateLbl = stringResource(R.string.identity_doc_field_expiry_date)
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Text(
                    stringResource(R.string.identity_doc_scan_select_fields),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                if (!result.docType.isNullOrBlank())
                    OcrFieldRow(docTypeLbl,    result.docType,        applyDocType) { applyDocType = it }
                if (!result.holderName.isNullOrBlank())
                    OcrFieldRow(holderNameLbl, result.holderName,     applyName)    { applyName = it }
                if (!result.number.isNullOrBlank())
                    OcrFieldRow(docNumberLbl,  result.number,         applyNumber)  { applyNumber = it }
                if (!result.issuingCountry.isNullOrBlank())
                    OcrFieldRow(countryLbl,    result.issuingCountry, applyCountry) { applyCountry = it }
                if (!result.birthDate.isNullOrBlank())
                    OcrFieldRow(birthDateLbl,  result.birthDate,      applyBirth)   { applyBirth = it }
                if (!result.issueDate.isNullOrBlank())
                    OcrFieldRow(issueDateLbl,  result.issueDate,      applyIssue)   { applyIssue = it }
                if (!result.expiryDate.isNullOrBlank())
                    OcrFieldRow(expiryDateLbl, result.expiryDate,     applyExpiry)  { applyExpiry = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply(OcrResult(
                        docType        = if (applyDocType)  result.docType        else null,
                        holderName     = if (applyName)     result.holderName     else null,
                        number         = if (applyNumber)   result.number         else null,
                        issuingCountry = if (applyCountry)  result.issuingCountry else null,
                        birthDate      = if (applyBirth)    result.birthDate      else null,
                        issueDate      = if (applyIssue)    result.issueDate      else null,
                        expiryDate     = if (applyExpiry)   result.expiryDate     else null,
                    ))
                },
                shape  = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor   = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
            ) { Text(stringResource(R.string.action_apply)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_discard), color = MaterialTheme.colorScheme.onBackground)
            }
        }
    )
}

@Composable
private fun OcrFieldRow(
    label:     String,
    value:     String,
    checked:   Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChecked(!checked) }
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked         = checked,
            onCheckedChange = onChecked,
            colors          = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
        )
        Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    focusedBorderColor      = Color.Transparent,
    unfocusedBorderColor    = Color.Transparent,
    focusedLabelColor       = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor     = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor             = MaterialTheme.colorScheme.primary
)

private fun dateFmt() = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).apply {
    timeZone = java.util.TimeZone.getTimeZone("UTC")
}

private fun String.toPickerMillis(): Long? =
    if (isBlank()) null
    else runCatching { dateFmt().parse(this)?.time }.getOrNull()

private fun Long.toDateString(): String = dateFmt().format(Date(this))

@Composable
internal fun IdentityOptionsSheet(
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
            Text(stringResource(R.string.options_sheet_category), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EntryCategory.entries.filter { it != EntryCategory.SIN_CATEGORIZAR }.forEach { cat ->
                    val selected = selectedCategory == cat
                    Surface(
                        onClick      = { selectedCategory = if (selectedCategory == cat) EntryCategory.SIN_CATEGORIZAR else cat },
                        shape        = RoundedCornerShape(8.dp),
                        color        = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(stringResource(cat.labelRes), style = MaterialTheme.typography.labelMedium, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, textAlign = TextAlign.Center, maxLines = 1)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.options_sheet_tags), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = searchQuery, onValueChange = { autoLockManager?.recordInteraction(); searchQuery = it },
                placeholder  = { Text(stringResource(R.string.vault_search_tags)) },
                leadingIcon  = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (searchQuery.isNotEmpty()) ({ IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_clear)) } }) else null,
                modifier = Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.CircleShape, singleLine = true,
                colors   = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent)
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
                    modifier = Modifier.fillMaxWidth().clickable { selectedTags = selectedTags.toMutableSet().apply { if (contains(tag.name)) remove(tag.name) else add(tag.name) } }.padding(start = 0.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = selectedTags.contains(tag.name), onCheckedChange = { selectedTags = selectedTags.toMutableSet().apply { if (it) add(tag.name) else remove(tag.name) } }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(highlightText(tag.name, searchQuery, MaterialTheme.colorScheme.primary), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
                    IconButton(onClick = { renameTarget = tag; renameText = tag.name }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f), modifier = Modifier.size(18.dp)) }
                    TagDeleteButton(tag = tag, onDelete = { selectedTags = selectedTags - tag.name; onDeleteTag(tag.id) })
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
                    text  = { OutlinedTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),value = renameText, onValueChange = { autoLockManager?.recordInteraction(); renameText = it; renameError = null }, singleLine = true, label = { Text(stringResource(R.string.tag_name_hint)) }, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth(), isError = renameError != null, supportingText = { renameError?.let { Text(it) } }) },
                    confirmButton = {
                        Button(onClick = {
                            val trimmed = renameText.trim()
                            when {
                                trimmed.isBlank() -> {}
                                availableTags.any { it.id != target.id && tagConflictKey(it.name) == tagConflictKey(trimmed) } -> renameError = errDuplicate
                                EntryCategory.entries.any { it.label.equals(trimmed, ignoreCase = true) } -> renameError = errCategory
                                else -> { if (selectedTags.contains(target.name)) { selectedTags = (selectedTags - target.name) + trimmed }; onRenameTag(target.id, trimmed); renameTarget = null; renameError = null }
                            }
                        }, enabled = renameText.trim().isNotBlank(), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)) { Text(stringResource(R.string.action_save), fontWeight = FontWeight.SemiBold) }
                    },
                    dismissButton = { TextButton(onClick = { renameTarget = null; renameError = null }) { Text(stringResource(R.string.action_cancel)) } }
                )
            }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (filteredTags.isNotEmpty()) {
                Button(
                    onClick  = { onSave(selectedCategory, selectedTags.toList()) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(8.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary),
                    border   = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                ) { Text(stringResource(R.string.action_apply), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            }

            if (showLockedDialog) {
                AlertDialog(
                    onDismissRequest = { showLockedDialog = false },
                    containerColor   = MaterialTheme.colorScheme.background,
                    title = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.plans_feature_tags), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) } },
                    text  = { Text(stringResource(R.string.premium_locked_dialog_message), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    confirmButton = { Button(onClick = { showLockedDialog = false; onUpgradeClick() }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)) { Text(stringResource(R.string.premium_gate_cta)) } },
                    dismissButton = { TextButton(onClick = { showLockedDialog = false }) { Text(stringResource(R.string.action_understood), color = MaterialTheme.colorScheme.onBackground) } }
                )
            }

            if (!searchHasNoMatches) {
            if (canCreateTags) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(8.dp)).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 14.dp, vertical = 14.dp), contentAlignment = Alignment.CenterStart) {
                        if (newTagText.isEmpty()) { Text(stringResource(R.string.field_tag_name), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }
                        BasicTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),value = newTagText, onValueChange = { autoLockManager?.recordInteraction(); newTagText = it; newTagError = null }, singleLine = true, textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface), cursorBrush = SolidColor(MaterialTheme.colorScheme.primary))
                    }
                    Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)))
                    Box(modifier = Modifier.width(120.dp).fillMaxHeight().clickable { tryCreateTag(newTagText) }.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(horizontal = 8.dp)) {
                            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text(stringResource(R.string.options_sheet_create_tag), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                        }
                    }
                }
                if (newTagError != null) { Text(newTagError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp)) }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)).clickable { showLockedDialog = true }, verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 14.dp, vertical = 14.dp), contentAlignment = Alignment.CenterStart) { Text(stringResource(R.string.field_tag_name), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)) }
                    Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
                    Box(modifier = Modifier.width(120.dp).fillMaxHeight().background(Color.Transparent), contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(horizontal = 8.dp)) {
                            Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                            Text(stringResource(R.string.options_sheet_create_tag), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
