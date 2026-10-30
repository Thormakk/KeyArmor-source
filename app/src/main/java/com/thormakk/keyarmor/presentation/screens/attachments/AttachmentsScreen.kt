@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UnusedAssignment")
package com.thormakk.keyarmor.presentation.screens.attachments

import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ContenidoDelFab
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.outlined.Delete
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.domain.model.Attachment
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.VioletaDelFab
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.thormakk.keyarmor.data.storage.AttachmentStorage
import com.thormakk.keyarmor.presentation.util.formatDateLocalized
import java.text.SimpleDateFormat
import java.util.Date
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea

@Composable
fun AttachmentsScreen(
    onBack: () -> Unit,
    onPlansClick: () -> Unit = {},
    viewModel: AttachmentsViewModel = hiltViewModel()
) {
    val context              = LocalContext.current
    val attachments          by viewModel.attachments.collectAsStateWithLifecycle()
    val isActiveVaultReadOnly by viewModel.isActiveVaultReadOnly.collectAsStateWithLifecycle()
    val isFreeUser           by viewModel.isFreeUser.collectAsStateWithLifecycle()
    var deleteTarget by remember { mutableStateOf<Attachment?>(null) }
    val snackbarHost = remember { SnackbarHostState() }

    val errTooLarge   = stringResource(R.string.att_error_too_large)
    val errMaxFiles   = stringResource(R.string.att_error_max_files)
    val errOpenFailed = stringResource(R.string.att_error_open)
    val errGeneric    = stringResource(R.string.att_error_generic)
    var showPremiumDialog by remember { mutableStateOf(false) }
    val limitReached = attachments.size >= AttachmentStorage.MAX_FILES_PER_ENTRY
    val fabScope     = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AttachmentEvent.OpenFile -> {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        event.file
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, event.mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    runCatching {
                        context.startActivity(Intent.createChooser(intent, null))
                    }.onFailure {
                        snackbarHost.showSnackbar(errOpenFailed)
                    }
                }
                is AttachmentEvent.Error -> {
                    val msg = when (event.kind) {
                        AttachmentUiError.FILE_TOO_LARGE    -> errTooLarge
                        AttachmentUiError.MAX_FILES_REACHED -> errMaxFiles
                        AttachmentUiError.OPEN_FAILED       -> errOpenFailed
                        AttachmentUiError.GENERIC           -> errGeneric
                        AttachmentUiError.PREMIUM_REQUIRED  -> { showPremiumDialog = true; return@collectLatest }
                    }
                    snackbarHost.showSnackbar(msg)
                }
            }
        }
    }

    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumDialog = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.premium_attachments),
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
                    onClick = { showPremiumDialog = false; onPlansClick() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border  = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) { Text(stringResource(R.string.premium_gate_cta)) }
            },
            dismissButton = {
                TextButton(onClick = { showPremiumDialog = false }) {
                    Text(stringResource(R.string.action_understood),
                        color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onFilePicked(it, context.contentResolver) }
    }

    deleteTarget?.let { att ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    text       = stringResource(R.string.att_delete_title),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text  = stringResource(R.string.att_delete_body, att.fileName),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onDelete(att); deleteTarget = null },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(
                        text  = stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                actions = {
                    CaptureAllowedBadge()
                    ThemeTrialBadge()
                },
                title = { TextoDeUnaLinea(stringResource(R.string.att_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                    color = sectionTitleColor()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = topBarColors()
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = {
                    when {
                        isActiveVaultReadOnly -> Unit
                        isFreeUser            -> showPremiumDialog = true
                        limitReached          -> fabScope.launch { snackbarHost.showSnackbar(errMaxFiles) }
                        else                  -> launcher.launch("*/*")
                    }
                },
                modifier       = Modifier.padding(bottom = 72.dp),
                containerColor = VioletaDelFab,
                contentColor   = ContenidoDelFab
            ) {
                Icon(
                    imageVector        = if (isActiveVaultReadOnly || isFreeUser || limitReached) Icons.Default.Lock else Icons.Default.Add,
                    contentDescription = stringResource(R.string.att_add)
                )
            }
        },
        snackbarHost          = { SnackbarHost(snackbarHost, modifier = Modifier.padding(bottom = 40.dp)) },
        contentWindowInsets   = WindowInsets.navigationBars
    ) { padding ->
        if (attachments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text  = stringResource(R.string.att_empty_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text  = stringResource(R.string.att_empty_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            when {
                                isActiveVaultReadOnly -> Unit
                                isFreeUser            -> showPremiumDialog = true
                                else                  -> launcher.launch("*/*")
                            }
                        },
                        colors  = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor   = MaterialTheme.colorScheme.primary
                        ),
                        border  = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                        shape   = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector        = if (isActiveVaultReadOnly || isFreeUser) Icons.Default.Lock else Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.att_add))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier             = Modifier.padding(padding),
                contentPadding       = PaddingValues(horizontal = 7.dp, vertical = 8.dp),
                verticalArrangement  = Arrangement.spacedBy(8.dp)
            ) {
                items(attachments, key = { it.id }) { att ->
                    AttachmentItem(
                        attachment  = att,
                        onOpen      = { viewModel.onOpen(att) },
                        onDelete    = { deleteTarget = att }
                    )
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }
}

@Composable
private fun AttachmentItem(
    attachment: Attachment,
    onOpen:     () -> Unit,
    onDelete:   () -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier        = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = mimeTypeIcon(attachment.mimeType),
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(36.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = attachment.fileName,
                    style    = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = formatFileSize(attachment.fileSize) + "  ·  " +
                            formatDateLocalized(attachment.createdAt, locale),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
            IconButton(onClick = onOpen) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = stringResource(R.string.att_open),
                    tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier           = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector        = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint               = dangerColor().copy(alpha = 0.8f),
                    modifier           = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun mimeTypeIcon(mimeType: String) = when {
    mimeType.startsWith("image/")       -> Icons.Default.Image
    mimeType.startsWith("video/")       -> Icons.Default.VideoFile
    mimeType.startsWith("audio/")       -> Icons.Default.AudioFile
    mimeType == "application/pdf"       -> Icons.Default.PictureAsPdf
    mimeType.startsWith("text/")        -> Icons.Default.Description
    else                                -> Icons.Default.AttachFile
}

private fun formatFileSize(bytes: Long): String = when {
    bytes < 1_024         -> "$bytes B"
    bytes < 1_048_576     -> "%.1f KB".format(bytes / 1_024.0)
    else                  -> "%.1f MB".format(bytes / 1_048_576.0)
}
