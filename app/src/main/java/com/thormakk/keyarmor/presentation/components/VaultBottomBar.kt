package com.thormakk.keyarmor.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.automirrored.outlined.ManageSearch
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.ui.theme.LocalIsBlackChrome

enum class BottomNavTab { NONE, VAULT, SEARCH, GENERATOR, SETTINGS }

@Composable
fun VaultBottomBar(
    activeTab:         BottomNavTab,
    onVaultHubClick:   () -> Unit,
    onSearchClick:     () -> Unit,
    onGeneratorClick:  () -> Unit,
    onSettingsClick:   () -> Unit,
    vaultTabClickable: Boolean = false
) {
    val isBlackChrome = LocalIsBlackChrome.current
    val navItemColors = if (isBlackChrome) {
        NavigationBarItemDefaults.colors(
            selectedIconColor   = Color.White,
            selectedTextColor   = Color.White,
            unselectedIconColor = Color.White.copy(alpha = 0.6f),
            unselectedTextColor = Color.White.copy(alpha = 0.6f),
            indicatorColor      = Color.White.copy(alpha = 0.15f)
        )
    } else {
        NavigationBarItemDefaults.colors(
            selectedIconColor   = MaterialTheme.colorScheme.primary,
            selectedTextColor   = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor      = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        )
    }

    Column {
        HorizontalDivider(
            thickness = 1.dp,
            color     = if (isBlackChrome) Color(0xFF333333) else MaterialTheme.colorScheme.outlineVariant
        )

        BoxWithConstraints {
            val estiloDelRotulo = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp)
            val textoVault     = stringResource(R.string.nav_vault_hub)
            val textoSearch    = stringResource(R.string.action_search)
            val textoGenerator = stringResource(R.string.nav_generator)
            val textoSettings  = stringResource(R.string.nav_settings)

            val anchoDePestana = maxWidth / 4 - RELLENO_DEL_ROTULO
            val tamanoDelRotulo = recordarTamanoQueEntra(
                textos = listOf(textoVault, textoSearch, textoGenerator, textoSettings),
                estilo = estiloDelRotulo,
                disponible = anchoDePestana
            )

            NavigationBar(
                containerColor = if (isBlackChrome) Color.Black else MaterialTheme.colorScheme.surface,
                windowInsets   = WindowInsets.navigationBars
            ) {
                val vaultSelected = activeTab == BottomNavTab.VAULT
                NavigationBarItem(
                    selected = vaultSelected,
                    onClick  = { if (!vaultSelected || vaultTabClickable) onVaultHubClick() },
                    icon     = {
                        Icon(
                            imageVector        = if (vaultSelected) Icons.Filled.Lock else Icons.Outlined.Lock,
                            contentDescription = textoVault,
                            modifier           = Modifier.size(26.dp)
                        )
                    },
                    label  = {
                        TextoDeUnaLinea(
                            text  = textoVault,
                            style = estiloDelRotulo.copy(fontSize = tamanoDelRotulo)
                        )
                    },
                    colors = navItemColors
                )

                val searchSelected = activeTab == BottomNavTab.SEARCH
                NavigationBarItem(
                    selected = searchSelected,
                    onClick  = { if (!searchSelected) onSearchClick() },
                    icon     = {
                        Icon(
                            imageVector        = if (searchSelected) Icons.AutoMirrored.Filled.ManageSearch else Icons.AutoMirrored.Outlined.ManageSearch,
                            contentDescription = textoSearch,
                            modifier           = Modifier.size(26.dp)
                        )
                    },
                    label  = {
                        TextoDeUnaLinea(
                            text  = textoSearch,
                            style = estiloDelRotulo.copy(fontSize = tamanoDelRotulo)
                        )
                    },
                    colors = navItemColors
                )

                val generatorSelected = activeTab == BottomNavTab.GENERATOR
                NavigationBarItem(
                    selected = generatorSelected,
                    onClick  = { if (!generatorSelected) onGeneratorClick() },
                    icon     = {
                        Icon(
                            imageVector        = if (generatorSelected) Icons.Filled.Key else Icons.Outlined.Key,
                            contentDescription = textoGenerator,
                            modifier           = Modifier.size(26.dp)
                        )
                    },
                    label  = {
                        TextoDeUnaLinea(
                            text  = textoGenerator,
                            style = estiloDelRotulo.copy(fontSize = tamanoDelRotulo)
                        )
                    },
                    colors = navItemColors
                )

                NavigationBarItem(
                    selected = activeTab == BottomNavTab.SETTINGS,
                    onClick  = { if (activeTab != BottomNavTab.SETTINGS) onSettingsClick() },
                    icon     = {
                        Box(modifier = Modifier.size(32.dp)) {
                            Image(
                                painter            = painterResource(R.drawable.logo_keyarmor),
                                contentDescription = textoSettings,
                                modifier           = Modifier
                                    .size(32.dp)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                    .align(Alignment.Center)
                            )
                        }
                    },
                    label  = {
                        TextoDeUnaLinea(
                            text  = textoSettings,
                            style = estiloDelRotulo.copy(fontSize = tamanoDelRotulo)
                        )
                    },
                    colors = navItemColors
                )
            }
        }
    }
}

private val RELLENO_DEL_ROTULO = 10.dp
