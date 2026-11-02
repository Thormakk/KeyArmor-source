package com.thormakk.keyarmor

import android.content.Context
import android.content.Intent
import com.thormakk.keyarmor.data.prefs.CalculatorDisguisePrefs

fun appEntryIntent(context: Context): Intent =
    Intent(
        context,
        if (CalculatorDisguisePrefs.isDisguiseActive(context)) MainActivity::class.java
        else LauncherDefaultActivity::class.java
    )
