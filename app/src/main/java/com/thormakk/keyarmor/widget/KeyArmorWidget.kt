package com.thormakk.keyarmor.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import com.thormakk.keyarmor.data.prefs.CalculatorDisguisePrefs

class KeyArmorWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Responsive(
        setOf(SMALL, NORMAL, LARGE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val localizedContext = contextoDelIdioma(context)
        if (CalculatorDisguisePrefs.isDisguiseActive(context)) {
            provideContent { }
        }
        provideContent {
            CompositionLocalProvider(LocalContext provides localizedContext) {
                GlanceTheme {
                    Content()
                }
            }
        }
    }

    companion object {
        val WIDGET_PASSWORD_KEY = stringPreferencesKey("widget_password")

        val SMALL  = DpSize(110.dp,  55.dp)
        val NORMAL = DpSize(110.dp, 110.dp)
        val LARGE  = DpSize(110.dp, 190.dp)
    }
}

@Composable
private fun Content() {
    val context  = LocalContext.current
    val prefs    = currentState<Preferences>()
    val password = prefs[KeyArmorWidget.WIDGET_PASSWORD_KEY]
        ?: RegeneratePasswordCallback.generatePassword()

    val size    = LocalSize.current
    val isSmall = size.height <= 60.dp
    val isLarge = size.height >= 150.dp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = if (isSmall) 2.dp else 4.dp),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier          = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text     = context.getString(R.string.widget_generator_label),
                style    = TextStyle(
                    color      = GlanceTheme.colors.onSecondaryContainer,
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Normal
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text     = "↻",
                style    = TextStyle(
                    color    = GlanceTheme.colors.onSecondaryContainer,
                    fontSize = 24.sp
                ),
                modifier = GlanceModifier
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .clickable(actionRunCallback<RegeneratePasswordCallback>())
            )
            Text(
                text     = "⎘",
                style    = TextStyle(
                    color    = GlanceTheme.colors.onSecondaryContainer,
                    fontSize = 22.sp
                ),
                modifier = GlanceModifier
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .clickable(actionRunCallback<CopyPasswordCallback>())
            )
        }

        Spacer(modifier = GlanceModifier.height(if (isSmall) 2.dp else 4.dp))

        val passwordBoxModifier = GlanceModifier
            .fillMaxWidth()
            .background(GlanceTheme.colors.primary)
            .cornerRadius(10.dp)
            .padding(horizontal = 12.dp, vertical = if (isSmall) 6.dp else 10.dp)
            .let { base ->
                if (isSmall) base.clickable(actionRunCallback<OpenGeneratorCallback>())
                else base
            }

        Box(
            modifier         = passwordBoxModifier,
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text     = password,
                style    = TextStyle(
                    color      = GlanceTheme.colors.onPrimary,
                    fontSize   = if (isLarge) 17.sp else 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.fillMaxWidth(),
                maxLines = 1
            )
        }

        if (!isSmall) {
            Spacer(modifier = GlanceModifier.height(7.dp))
            Text(
                text     = context.getString(R.string.widget_open_generator),
                style    = TextStyle(
                    color    = GlanceTheme.colors.onSecondaryContainer,
                    fontSize = 11.sp
                ),
                modifier = GlanceModifier.clickable(
                    actionRunCallback<OpenGeneratorCallback>()
                )
            )
        }
    }
}
