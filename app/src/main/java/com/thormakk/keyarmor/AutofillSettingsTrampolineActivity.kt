package com.thormakk.keyarmor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.thormakk.keyarmor.data.prefs.CalculatorDisguisePrefs

class AutofillSettingsTrampolineActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (CalculatorDisguisePrefs.isDisguiseActive(this)) {
            finish()
            return
        }
        startActivity(
            appEntryIntent(this)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        finish()
    }
}
