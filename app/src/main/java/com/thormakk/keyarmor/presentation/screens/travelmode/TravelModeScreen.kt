@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
package com.thormakk.keyarmor.presentation.screens.travelmode

import com.thormakk.keyarmor.ui.theme.appButtonColors
import com.thormakk.keyarmor.presentation.components.justified
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea

@Composable
fun TravelModeScreen(
    onBack:       () -> Unit,
    onPlansClick: () -> Unit = {},
    viewModel:    TravelModeViewModel = hiltViewModel()
) {
    val isEnabled         by viewModel.isEnabled.collectAsStateWithLifecycle()
    val isFreeUser        by viewModel.isFreeUser.collectAsStateWithLifecycle()
    val pendingSuggestion by viewModel.pendingSuggestion.collectAsStateWithLifecycle()

    val calendarPermission = rememberPermissionState(Manifest.permission.READ_CALENDAR)
    var calendarSuggestionEnabled by remember { mutableStateOf(viewModel.isCalendarSuggestionEnabled()) }
    var awaitingPermissionGrant   by remember { mutableStateOf(false) }

    LaunchedEffect(calendarPermission.status) {
        when {
            awaitingPermissionGrant && calendarPermission.status.isGranted -> {
                awaitingPermissionGrant   = false
                calendarSuggestionEnabled = true
                viewModel.setCalendarSuggestionEnabled(true)
            }
            !calendarPermission.status.isGranted && calendarSuggestionEnabled -> {
                calendarSuggestionEnabled = false
                viewModel.setCalendarSuggestionEnabled(false)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                actions = {
                    CaptureAllowedBadge()
                    ThemeTrialBadge()
                },
                title = { TextoDeUnaLinea(stringResource(R.string.travel_mode_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                    color = sectionTitleColor()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                colors = topBarColors()
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 7.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            pendingSuggestion?.let { suggestion ->
                TravelSuggestionBanner(
                    tripDateMillis = suggestion.tripDateMillis,
                    onAccept       = { viewModel.acceptSuggestion() },
                    onDismiss      = { viewModel.dismissSuggestion() }
                )
            }

            Card(
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isEnabled && !isFreeUser)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier          = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Flight,
                        contentDescription = null,
                        tint               = if (isEnabled && !isFreeUser)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text  = stringResource(
                            if (isEnabled && !isFreeUser) R.string.travel_mode_status_active
                            else                         R.string.travel_mode_status_inactive
                        ),
                        style      = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (isEnabled && !isFreeUser)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape          = RoundedCornerShape(12.dp),
                color          = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier       = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text       = stringResource(
                            if (isEnabled) R.string.travel_mode_toggle_disable
                            else           R.string.travel_mode_toggle_enable
                        ),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier   = Modifier.weight(1f),
                        color      = if (isFreeUser)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked         = isEnabled,
                        onCheckedChange = { viewModel.setEnabled(it) },
                        enabled         = !isFreeUser,
                        modifier        = Modifier.coachMarkAnchor("travel_mode", "switch")
                    )
                }
            }

            Surface(
                shape          = RoundedCornerShape(12.dp),
                color          = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier       = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint               = if (isFreeUser)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text       = stringResource(R.string.travel_mode_calendar_suggestion_title),
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            modifier   = Modifier.weight(1f).padding(start = 12.dp),
                            color      = if (isFreeUser)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = calendarSuggestionEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    if (calendarPermission.status.isGranted) {
                                        calendarSuggestionEnabled = true
                                        viewModel.setCalendarSuggestionEnabled(true)
                                    } else {
                                        awaitingPermissionGrant = true
                                        calendarPermission.launchPermissionRequest()
                                    }
                                } else {
                                    calendarSuggestionEnabled = false
                                    viewModel.setCalendarSuggestionEnabled(false)
                                }
                            },
                            enabled = !isFreeUser
                        )
                    }
                    Text(
                        text     = stringResource(R.string.travel_mode_calendar_suggestion_desc),
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Card(
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text       = stringResource(R.string.travel_mode_description),
                        style      = MaterialTheme.typography.bodySmall.justified(),
                        color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            Card(
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment      = Alignment.CenterVertically,
                        horizontalArrangement  = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Info,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(20.dp)
                        )
                        Text(
                            text       = stringResource(R.string.travel_mode_how_to_title),
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text      = stringResource(R.string.travel_mode_how_to_body),
                        style     = MaterialTheme.typography.bodySmall.justified(),
                        color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            if (isFreeUser) {
                PremiumGateBanner(onPlansClick = onPlansClick)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TravelSuggestionBanner(
    tripDateMillis: Long,
    onAccept:       () -> Unit,
    onDismiss:      () -> Unit
) {
    val dateText = remember(tripDateMillis) {
        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(tripDateMillis))
    }
    Card(
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.Flight,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier           = Modifier.size(20.dp)
                )
                Text(
                    text       = stringResource(R.string.travel_mode_suggestion_banner_title),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text  = stringResource(R.string.travel_mode_suggestion_banner_body, dateText),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.travel_mode_suggestion_dismiss))
                }
                Button(onClick = onAccept, modifier = Modifier.weight(1f), colors = appButtonColors()) {
                    Text(stringResource(R.string.travel_mode_suggestion_accept))
                }
            }
        }
    }
}

@Composable
private fun PremiumGateBanner(onPlansClick: () -> Unit) {
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlansClick)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.Lock,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier           = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = stringResource(R.string.premium_gate_title),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text  = stringResource(R.string.premium_gate_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Text(
                text       = stringResource(R.string.premium_gate_cta),
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}
