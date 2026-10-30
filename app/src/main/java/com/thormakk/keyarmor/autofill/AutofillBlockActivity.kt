package com.thormakk.keyarmor.autofill

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.AutofillPrefs
import com.thormakk.keyarmor.data.prefs.BlockLevel
import com.thormakk.keyarmor.data.prefs.BlockTarget
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AutofillBlockActivity : ComponentActivity() {

    @Inject lateinit var autofillPrefs: AutofillPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val target = intent?.getStringExtra(EXTRA_TARGET)?.let { BlockTarget.fromKey(it) }
        if (target != null) {
            autofillPrefs.setLevel(target, BlockLevel.SAVE)
            Toast.makeText(
                this,
                getString(R.string.autofill_block_saved, target.value),
                Toast.LENGTH_LONG,
            ).show()
        }
        finish()
    }

    companion object {
        private const val EXTRA_TARGET = "target_key"

        fun createIntent(context: Context, target: BlockTarget): Intent =
            Intent(context, AutofillBlockActivity::class.java)
                .putExtra(EXTRA_TARGET, target.key)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
