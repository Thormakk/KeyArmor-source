package com.thormakk.keyarmor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class AutofillSettingsTrampolineActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        finish()
    }
}
