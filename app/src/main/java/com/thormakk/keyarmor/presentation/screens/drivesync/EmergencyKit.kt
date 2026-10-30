package com.thormakk.keyarmor.presentation.screens.drivesync

import com.thormakk.keyarmor.ui.theme.appButtonColors
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color as AColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.screens.donate.qrBitmap
import com.thormakk.keyarmor.presentation.util.ClipboardUtil
import com.thormakk.keyarmor.presentation.util.LocalClipboardClearManager
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

data class KitPreviousKey(val label: String, val words: List<String>)

object EmergencyKitPdf {

    private const val W = 595
    private const val H = 842
    private const val MARGIN = 50f

    fun build(
        context: Context, words: List<String>, account: String?,
        previous: List<KitPreviousKey> = emptyList()
    ): ByteArray {
        val doc = PdfDocument()
        try {
            val page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, 1).create())
            draw(context, page.canvas, words, account)
            doc.finishPage(page)
            drawPrevious(context, doc, previous)
            val out = ByteArrayOutputStream()
            doc.writeTo(out)
            return out.toByteArray()
        } finally {
            doc.close()
        }
    }

    private fun draw(context: Context, c: Canvas, words: List<String>, account: String?) {
        val ink = AColor.BLACK
        val grey = AColor.rgb(90, 90, 90)
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ink }
        var y = 70f

        text.textSize = 22f; text.typeface = Typeface.DEFAULT_BOLD
        c.drawText(context.getString(R.string.kit_title), MARGIN, y, text)
        y += 14f
        Paint().apply { color = ink; strokeWidth = 1.5f }.also { c.drawLine(MARGIN, y, W - MARGIN, y, it) }

        if (!account.isNullOrBlank()) {
            y += 28f
            text.textSize = 10f; text.typeface = Typeface.DEFAULT; text.color = grey
            c.drawText(context.getString(R.string.kit_account), MARGIN, y, text)
            y += 16f
            text.textSize = 13f; text.color = ink
            c.drawText(account, MARGIN, y, text)
        }

        y += 34f
        val colW = (W - 2 * MARGIN) / 3f
        val rowH = 40f
        val box = Paint().apply { color = AColor.rgb(150, 150, 150); style = Paint.Style.STROKE; strokeWidth = 1f }
        text.typeface = Typeface.MONOSPACE; text.textSize = 15f; text.color = ink
        words.forEachIndexed { i, w ->
            val col = i % 3
            val row = i / 3
            val left = MARGIN + col * colW
            val top = y + row * rowH
            c.drawRoundRect(left + 2, top, left + colW - 6, top + rowH - 8, 6f, 6f, box)
            c.drawText("${i + 1}. $w", left + 12, top + 22f, text)
        }
        y += 4 * rowH + 14f

        val linea = words.joinToString(" ")
        text.typeface = Typeface.MONOSPACE; text.color = ink
        var size = 13f
        text.textSize = size
        while (text.measureText(linea) > W - 2 * MARGIN && size > 6f) { size -= 0.5f; text.textSize = size }
        c.drawText(linea, MARGIN, y, text)

        y += 26f
        val qrSize = 150
        qrBitmap(linea, qrSize * 3, Color.Black, Color.White)?.let { img ->
            val bmp = img.asAndroidBitmap()
            c.drawBitmap(bmp, null, android.graphics.RectF(MARGIN, y, MARGIN + qrSize, y + qrSize), null)
        }

        val noteX = MARGIN + qrSize + 30f
        text.typeface = Typeface.DEFAULT; text.textSize = 10f; text.color = grey
        c.drawText(context.getString(R.string.kit_note), noteX, y + 14f, text)
        val line = Paint().apply { color = AColor.rgb(170, 170, 170); strokeWidth = 1f }
        for (k in 1..4) c.drawLine(noteX, y + 14f + k * 28f, W - MARGIN, y + 14f + k * 28f, line)

        y += qrSize + 40f
        text.textSize = 10f; text.color = grey
        wrap(context.getString(R.string.kit_footer), text, W - 2 * MARGIN).forEach {
            c.drawText(it, MARGIN, y, text); y += 14f
        }
    }

    private fun drawPrevious(context: Context, doc: PdfDocument, previous: List<KitPreviousKey>) {
        if (previous.isEmpty()) return
        val ink = AColor.BLACK
        val grey = AColor.rgb(90, 90, 90)
        val box = Paint().apply { color = AColor.rgb(150, 150, 150); style = Paint.Style.STROKE; strokeWidth = 1f }
        val perPage = 4
        previous.chunked(perPage).forEachIndexed { pageIdx, group ->
            val page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, pageIdx + 2).create())
            val c = page.canvas
            val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ink }
            var y = 70f
            text.textSize = 22f; text.typeface = Typeface.DEFAULT_BOLD
            c.drawText(context.getString(R.string.bk_hist_title), MARGIN, y, text)
            y += 14f
            Paint().apply { color = ink; strokeWidth = 1.5f }.also { c.drawLine(MARGIN, y, W - MARGIN, y, it) }
            y += 22f
            text.textSize = 10f; text.typeface = Typeface.DEFAULT; text.color = grey
            wrap(context.getString(R.string.bk_hist_body), text, W - 2 * MARGIN).forEach {
                c.drawText(it, MARGIN, y, text); y += 14f
            }
            y += 10f
            val colW = (W - 2 * MARGIN) / 3f
            val rowH = 24f
            group.forEach { k ->
                text.textSize = 11f; text.typeface = Typeface.DEFAULT_BOLD; text.color = ink
                c.drawText(k.label, MARGIN, y, text)
                y += 8f
                text.typeface = Typeface.MONOSPACE; text.textSize = 12f
                k.words.forEachIndexed { i, w ->
                    val left = MARGIN + (i % 3) * colW
                    val top = y + (i / 3) * rowH
                    c.drawRoundRect(left + 2, top, left + colW - 6, top + rowH - 5, 5f, 5f, box)
                    c.drawText("${i + 1}. $w", left + 10, top + 15f, text)
                }
                y += 4 * rowH + 6f
                val linea = k.words.joinToString(" ")
                var size = 11f
                text.textSize = size
                while (text.measureText(linea) > W - 2 * MARGIN && size > 6f) { size -= 0.5f; text.textSize = size }
                c.drawText(linea, MARGIN, y, text)
                y += 30f
            }
            doc.finishPage(page)
        }
    }

    private fun wrap(s: String, p: Paint, maxW: Float): List<String> {
        val out = mutableListOf<String>()
        var cur = ""
        for (w in s.split(" ")) {
            val t = if (cur.isEmpty()) w else "$cur $w"
            if (p.measureText(t) > maxW && cur.isNotEmpty()) { out += cur; cur = w } else cur = t
        }
        if (cur.isNotEmpty()) out += cur
        return out
    }

    fun share(context: Context, pdf: ByteArray) {
        val dir = java.io.File(context.cacheDir, "att").apply { mkdirs() }
        val f = java.io.File(dir, "backup-key.pdf")
        f.writeBytes(pdf)
        val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", f)
        val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(send, null).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

@Composable
fun EmergencyKitActions(
    words:    List<String>,
    account:  String?,
    snackbar: SnackbarHostState,
    previous: List<KitPreviousKey> = emptyList()
) {
    val context   = LocalContext.current
    val clipClear = LocalClipboardClearManager.current
    val scope     = rememberCoroutineScope()
    val copiedMsg = stringResource(R.string.key_kit_copied)
    val savedMsg  = stringResource(R.string.key_kit_saved)

    val saver = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            val ok = runCatching {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(EmergencyKitPdf.build(context, words, account, previous))
                }
            }.isSuccess
            if (ok) scope.launch { snackbar.showSnackbar(savedMsg) }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            colors = appButtonColors(),
            onClick = {
                ClipboardUtil.copySensitive(context, words.joinToString(" "))
                clipClear.scheduleClear()
                scope.launch { snackbar.showSnackbar(copiedMsg) }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ContentCopy, null, Modifier.size(18.dp))
            Spacer8()
            Text(stringResource(R.string.key_kit_copy))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                border = acentoBorde(),
                colors = acentoOutlinedColors(),
                onClick = { EmergencyKitPdf.share(context, EmergencyKitPdf.build(context, words, account, previous)) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Share, null, Modifier.size(18.dp))
                Spacer8()
                Text(stringResource(R.string.key_kit_share))
            }
            OutlinedButton(
                border = acentoBorde(),
                colors = acentoOutlinedColors(),
                onClick = { saver.launch("backup-key.pdf") },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Save, null, Modifier.size(18.dp))
                Spacer8()
                Text(stringResource(R.string.key_kit_save_pdf))
            }
        }
    }
}

@Composable
private fun Spacer8() = androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
