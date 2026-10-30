package com.thormakk.keyarmor.presentation.screens.donate

import androidx.compose.foundation.gestures.scrollBy
import com.thormakk.keyarmor.presentation.util.registrarPosicion
import com.thormakk.keyarmor.presentation.util.contenedorDe
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.components.justified
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.util.openUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonateScreen(
    onBack: () -> Unit,
) {
    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.foundation.LocalIndication provides androidx.compose.material3.ripple(),
        androidx.compose.material3.LocalRippleConfiguration provides androidx.compose.material3.RippleConfiguration(),
    ) { DonateScreenContenido(onBack) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DonateScreenContenido(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val options = remember { DonationAddresses.available() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val seguidor = com.thormakk.keyarmor.presentation.util.rememberSeguidorDeApertura { listState.scrollBy(it) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                    actions = {
                        CaptureAllowedBadge()
                        ThemeTrialBadge()
                    },
                    windowInsets = WindowInsets(0),
                    title = {
                        TextoDeUnaLinea(
                            text = stringResource(R.string.donation_title),
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                            fontWeight = FontWeight.SemiBold,
                            color = sectionTitleColor(),
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
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).contenedorDe(seguidor),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.donate_intro),
                    style = MaterialTheme.typography.bodyMedium.justified(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 12.dp),
                )
            }

            item {
                DonateSection(title = stringResource(R.string.donate_section_card)) {
                    DonateRow(
                        icon = Icons.Default.Favorite,
                        title = stringResource(R.string.donate_kofi),
                        subtitle = stringResource(R.string.donate_kofi_subtitle),
                        onClick = { context.openUrl(DonationAddresses.KOFI_URL) },
                    )
                }
            }

            if (options.isNotEmpty()) {
                item {
                    DonateSection(title = stringResource(R.string.donate_section_crypto)) {
                        options.forEachIndexed { index, option ->
                            if (index > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.background,
                                )
                            }
                            CryptoRow(option, snackbarHostState, scope, seguidor)
                        }
                    }
                }
                if (options.any { it.network.isNotBlank() }) item {
                    Text(
                        text = stringResource(R.string.donate_network_warning),
                        style = MaterialTheme.typography.bodySmall.justified(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 12.dp),
                    )
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun DonateSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 14.dp, bottom = 6.dp),
        )
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 7.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) { content() }
        }
    }
}

@Composable
private fun DonateRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 56.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(14.dp))
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
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CryptoRow(
    option: DonationAddresses.CryptoOption,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    seguidor: com.thormakk.keyarmor.presentation.util.SeguidorDeApertura,
) {
    var expanded by rememberSaveable(option.coin) { mutableStateOf(false) }
    val posicion = remember { com.thormakk.keyarmor.presentation.util.PosicionDeTitulo() }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "donate_chevron_" + option.coin,
    )
    val context = LocalContext.current
    val copied = stringResource(R.string.donate_copied)
    val copyLabel = stringResource(R.string.donate_copy)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .registrarPosicion(posicion)
                .clickable {
                    expanded = !expanded
                    if (expanded) seguidor.abrir(posicion) else seguidor.cerrar(posicion)
                }
                .heightIn(min = 56.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CoinAvatar(option)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (option.network.isBlank()) option.coin else option.coin + " · " + option.network,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = option.address.shortened(),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = {
                val manager = context.getSystemService(ClipboardManager::class.java)
                manager?.setPrimaryClip(ClipData.newPlainText(option.coin, option.address))
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    scope.launch { snackbarHostState.showSnackbar(copied) }
                }
            }) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = copyLabel,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp).rotate(chevronRotation),
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = com.thormakk.keyarmor.presentation.util.SalidaDeApertura,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val sizePx = with(LocalDensity.current) { 220.dp.roundToPx() }
                val qr = remember(option.address, sizePx) {
                    qrBitmap(
                        content = option.address,
                        sizePx = sizePx,
                        foreground = Color.Black,
                        background = Color.White,
                    )
                }
                if (qr != null) {
                    Image(
                        bitmap = qr,
                        contentDescription = null,
                        modifier = Modifier
                            .size(220.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .padding(10.dp),
                    )
                }
                Text(
                    text = option.address,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun CoinAvatar(option: DonationAddresses.CryptoOption, size: Dp = 32.dp) {
    val iconRes = option.iconRes
    if (iconRes != null) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = option.coin,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size).clip(CircleShape),
        )
        return
    }
    val brand = Color(option.brandColorArgb)
    val luminance = 0.2126f * brand.red + 0.7152f * brand.green + 0.0722f * brand.blue
    val textColor = if (luminance > 0.55f) Color(0xFF1A1A1A) else Color.White
    val fontSize = size.value * when (option.symbol.length) {
        1 -> 0.50f
        2 -> 0.39f
        3 -> 0.31f
        else -> 0.25f
    }
    Box(
        modifier = Modifier.size(size).background(brand, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = option.symbol,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize.sp,
            maxLines = 1,
        )
    }
}

private fun String.shortened(): String =
    if (length <= 20) this else take(8) + "…" + takeLast(8)
