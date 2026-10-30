package com.thormakk.keyarmor.presentation.components

import android.content.Context
import android.telephony.TelephonyManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.components.cuadroConTeclado
import com.thormakk.keyarmor.presentation.components.propiedadesConTeclado
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager

data class CountryCode(val name: String, val dialCode: String)

val COUNTRY_CODES: List<CountryCode> = listOf(
    CountryCode("Argentina",            "+54"),
    CountryCode("Bolivia",              "+591"),
    CountryCode("Brasil",               "+55"),
    CountryCode("Canadá",               "+1"),
    CountryCode("Chile",                "+56"),
    CountryCode("Colombia",             "+57"),
    CountryCode("Costa Rica",           "+506"),
    CountryCode("Cuba",                 "+53"),
    CountryCode("Ecuador",              "+593"),
    CountryCode("El Salvador",          "+503"),
    CountryCode("Estados Unidos",       "+1"),
    CountryCode("Guatemala",            "+502"),
    CountryCode("Honduras",             "+504"),
    CountryCode("Jamaica",              "+1876"),
    CountryCode("México",               "+52"),
    CountryCode("Nicaragua",            "+505"),
    CountryCode("Panamá",               "+507"),
    CountryCode("Paraguay",             "+595"),
    CountryCode("Perú",                 "+51"),
    CountryCode("Puerto Rico",          "+1787"),
    CountryCode("República Dominicana", "+1809"),
    CountryCode("Uruguay",              "+598"),
    CountryCode("Venezuela",            "+58"),
    CountryCode("Alemania",             "+49"),
    CountryCode("Austria",              "+43"),
    CountryCode("Bélgica",              "+32"),
    CountryCode("Bulgaria",             "+359"),
    CountryCode("Chequia",              "+420"),
    CountryCode("Croacia",              "+385"),
    CountryCode("Dinamarca",            "+45"),
    CountryCode("Eslovaquia",           "+421"),
    CountryCode("Eslovenia",            "+386"),
    CountryCode("España",               "+34"),
    CountryCode("Estonia",              "+372"),
    CountryCode("Finlandia",            "+358"),
    CountryCode("Francia",              "+33"),
    CountryCode("Grecia",               "+30"),
    CountryCode("Hungría",              "+36"),
    CountryCode("Irlanda",              "+353"),
    CountryCode("Italia",               "+39"),
    CountryCode("Letonia",              "+371"),
    CountryCode("Lituania",             "+370"),
    CountryCode("Luxemburgo",           "+352"),
    CountryCode("Malta",                "+356"),
    CountryCode("Noruega",              "+47"),
    CountryCode("Países Bajos",         "+31"),
    CountryCode("Polonia",              "+48"),
    CountryCode("Portugal",             "+351"),
    CountryCode("Reino Unido",          "+44"),
    CountryCode("Rumanía",              "+40"),
    CountryCode("Rusia",                "+7"),
    CountryCode("Suecia",               "+46"),
    CountryCode("Suiza",                "+41"),
    CountryCode("Turquía",              "+90"),
    CountryCode("Ucrania",              "+380"),
    CountryCode("Arabia Saudita",       "+966"),
    CountryCode("Australia",            "+61"),
    CountryCode("China",                "+86"),
    CountryCode("Corea del Sur",        "+82"),
    CountryCode("Emiratos Árabes",      "+971"),
    CountryCode("Filipinas",            "+63"),
    CountryCode("Hong Kong",            "+852"),
    CountryCode("India",                "+91"),
    CountryCode("Indonesia",            "+62"),
    CountryCode("Israel",               "+972"),
    CountryCode("Japón",                "+81"),
    CountryCode("Malasia",              "+60"),
    CountryCode("Nueva Zelanda",        "+64"),
    CountryCode("Pakistán",             "+92"),
    CountryCode("Singapur",             "+65"),
    CountryCode("Taiwán",               "+886"),
    CountryCode("Tailandia",            "+66"),
    CountryCode("Vietnam",              "+84"),
    CountryCode("Egipto",               "+20"),
    CountryCode("Etiopía",              "+251"),
    CountryCode("Ghana",                "+233"),
    CountryCode("Kenia",                "+254"),
    CountryCode("Marruecos",            "+212"),
    CountryCode("Nigeria",              "+234"),
    CountryCode("Sudáfrica",            "+27"),
    CountryCode("Tanzania",             "+255")
).sortedBy { it.name }

@Composable
fun CountryCodePickerDialog(
    onSelect:  (CountryCode) -> Unit,
    onDismiss: () -> Unit
) {
    val autoLockManager = LocalAutoLockManager.current
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        if (query.isBlank()) COUNTRY_CODES
        else COUNTRY_CODES.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.dialCode.contains(query, ignoreCase = true)
        }
    }

    AlertDialog(
        conScroll = false,
        modifier = Modifier.cuadroConTeclado(),
        properties = propiedadesConTeclado(),
        onDismissRequest = onDismiss,
        containerColor   = MaterialTheme.colorScheme.background,
        title = { Text(stringResource(R.string.card_field_bank_phone_prefix)) },
        text  = {
            Column {
                OutlinedTextField(
                    value         = query,
                    onValueChange = { autoLockManager?.recordInteraction(); query = it },
                    placeholder   = {
                        Text(
                            stringResource(R.string.action_search),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    leadingIcon  = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = if (query.isNotEmpty()) {{
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    }} else null,
                    singleLine = true,
                    shape      = RoundedCornerShape(8.dp),
                    colors     = pickerFieldColors(),
                    modifier   = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    items(filtered, key = { "${it.name}_${it.dialCode}" }) { code ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onSelect(code) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text     = code.name,
                                style    = MaterialTheme.typography.bodyMedium,
                                color    = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text  = code.dialCode,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color     = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    if (filtered.isEmpty()) {
                        item {
                            Text(
                                text     = "—",
                                style    = MaterialTheme.typography.bodySmall,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(R.string.action_cancel),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    )
}

fun combinePhone(prefix: String, localNumber: String): String = when {
    prefix.isEmpty() && localNumber.isEmpty() -> ""
    prefix.isEmpty()                          -> localNumber
    localNumber.isEmpty()                     -> "+$prefix"
    else                                      -> "+$prefix $localNumber"
}

fun splitPhone(stored: String): Pair<String, String> {
    if (!stored.startsWith("+")) return "" to stored
    val space = stored.indexOf(' ')
    return if (space >= 0) stored.substring(1, space) to stored.substring(space + 1)
           else            stored.removePrefix("+")   to ""
}

private val ISO_TO_DIAL_CODE: Map<String, String> = mapOf(
    "AR" to "+54",  "BO" to "+591", "BR" to "+55",  "CA" to "+1",
    "CL" to "+56",  "CO" to "+57",  "CR" to "+506", "CU" to "+53",
    "DO" to "+1809","EC" to "+593", "GT" to "+502", "HN" to "+504",
    "JM" to "+1876","MX" to "+52",  "NI" to "+505", "PA" to "+507",
    "PE" to "+51",  "PR" to "+1787","PY" to "+595", "SV" to "+503",
    "US" to "+1",   "UY" to "+598", "VE" to "+58",
    "AT" to "+43",  "BE" to "+32",  "BG" to "+359", "CH" to "+41",
    "CZ" to "+420", "DE" to "+49",  "DK" to "+45",  "EE" to "+372",
    "ES" to "+34",  "FI" to "+358", "FR" to "+33",  "GB" to "+44",
    "GR" to "+30",  "HR" to "+385", "HU" to "+36",  "IE" to "+353",
    "IT" to "+39",  "LT" to "+370", "LU" to "+352", "LV" to "+371",
    "MT" to "+356", "NL" to "+31",  "NO" to "+47",  "PL" to "+48",
    "PT" to "+351", "RO" to "+40",  "RU" to "+7",   "SE" to "+46",
    "SI" to "+386", "SK" to "+421", "TR" to "+90",  "UA" to "+380",
    "AE" to "+971", "AU" to "+61",  "CN" to "+86",  "HK" to "+852",
    "ID" to "+62",  "IL" to "+972", "IN" to "+91",  "JP" to "+81",
    "KR" to "+82",  "MY" to "+60",  "NZ" to "+64",  "PH" to "+63",
    "PK" to "+92",  "SA" to "+966", "SG" to "+65",  "TH" to "+66",
    "TW" to "+886", "VN" to "+84",
    "EG" to "+20",  "ET" to "+251", "GH" to "+233", "KE" to "+254",
    "MA" to "+212", "NG" to "+234", "TZ" to "+255", "ZA" to "+27"
)

fun defaultDialCode(context: Context): String {
    val tm  = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    val iso = tm?.networkCountryIso?.uppercase()?.takeIf { it.length == 2 }
           ?: tm?.simCountryIso?.uppercase()?.takeIf { it.length == 2 }
           ?: java.util.Locale.getDefault().country.uppercase().takeIf { it.length == 2 }
           ?: return ""
    return ISO_TO_DIAL_CODE[iso] ?: ""
}

@Composable
private fun pickerFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedLabelColor    = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor  = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor          = MaterialTheme.colorScheme.primary
)
