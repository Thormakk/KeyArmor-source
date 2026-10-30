package com.thormakk.keyarmor.data.export

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PdfFieldEntry(
    val label: String,
    val value: String,
    val sensitive: Boolean = false
)

data class PdfExportBlock(
    val typeLabel: String,
    val title:     String,
    val fields:    List<PdfFieldEntry>
)

object PdfExporter {

    private val PAGE_SIZE = PDRectangle.A4
    private const val MARGIN      = 50f
    private const val LINE_HEIGHT = 16f
    private const val MASK        = "********"

    fun writePdf(
        blocks:              List<PdfExportBlock>,
        showSensitiveValues: Boolean,
        password:            String,
        uri:                 Uri,
        context:             Context
    ) {
        val document = PDDocument()
        try {
            var page = PDPage(PAGE_SIZE)
            document.addPage(page)
            var cs = PDPageContentStream(document, page)
            var y = PAGE_SIZE.height - MARGIN

            fun newPageIfNeeded(linesNeeded: Int) {
                if (y - (LINE_HEIGHT * linesNeeded) < MARGIN) {
                    cs.close()
                    page = PDPage(PAGE_SIZE)
                    document.addPage(page)
                    cs = PDPageContentStream(document, page)
                    y = PAGE_SIZE.height - MARGIN
                }
            }

            fun draw(text: String, size: Float, bold: Boolean, indent: Float = 0f) {
                cs.beginText()
                cs.setFont(if (bold) PDType1Font.HELVETICA_BOLD else PDType1Font.HELVETICA, size)
                cs.newLineAtOffset(MARGIN + indent, y)
                cs.showText(text.toWinAnsiSafe())
                cs.endText()
                y -= LINE_HEIGHT
            }

            draw("KeyArmor — Exportación parcial", 16f, bold = true)
            y -= 4f
            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            draw("Generado el $dateStr — ${blocks.size} entrada(s)", 9f, bold = false)
            y -= 10f

            blocks.forEach { block ->
                runCatching {
                    newPageIfNeeded(3 + block.fields.size)
                    draw(block.typeLabel.uppercase(Locale.getDefault()), 8f, bold = false)
                    draw(block.title.ifBlank { "(sin título)" }, 12f, bold = true)
                    block.fields.forEach { field ->
                        if (field.value.isBlank()) return@forEach
                        newPageIfNeeded(1)
                        val shown = if (field.sensitive && !showSensitiveValues) MASK else field.value
                        draw("${field.label}: $shown", 10f, bold = false, indent = 10f)
                    }
                    y -= 8f
                    newPageIfNeeded(1)
                    cs.moveTo(MARGIN, y)
                    cs.lineTo(PAGE_SIZE.width - MARGIN, y)
                    cs.stroke()
                    y -= 10f
                }
            }
            cs.close()

            val permissions = AccessPermission().apply {
                setCanPrint(true)
                setCanExtractContent(false)
                setCanModify(false)
                setCanModifyAnnotations(false)
            }
            val policy = StandardProtectionPolicy(password, password, permissions)
            policy.encryptionKeyLength = 256
            document.protect(policy)

            context.contentResolver.openOutputStream(uri)?.use { out ->
                document.save(out)
            }
        } finally {
            document.close()
        }
    }
}

private val TYPOGRAPHIC_ASCII_FALLBACK = mapOf(
    '—' to "-",
    '–' to "-",
    '‘' to "'",
    '’' to "'",
    '“' to "\"",
    '”' to "\"",
    '•' to "*",
    '…' to "..."
)

private fun String.toWinAnsiSafe(): String = buildString {
    for (c in this@toWinAnsiSafe) {
        val ascii = TYPOGRAPHIC_ASCII_FALLBACK[c]
        when {
            ascii != null     -> append(ascii)
            c.code in 32..255 -> append(c)
            else              -> append('?')
        }
    }
}
