package com.thormakk.keyarmor.presentation.screens.notes

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.LeadingMarginSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatEditText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import android.graphics.Color as AndroidColor

private const val MAX_HISTORY = 60
private const val ZWSP = '\u200B'
private const val TYPING_BURST_GAP_MS = 700L

class NoteBulletLineSpan(val level: Int = 0) : LeadingMarginSpan {
    var owner: Spanned? = null
    private val density = Resources.getSystem().displayMetrics.density
    private val indentPerLevelPx = (18 * density).toInt()
    private val gapPx = (10 * density).toInt()
    private val radiusPx = 2.5f * density

    override fun getLeadingMargin(first: Boolean): Int {
        val o = owner
        if (o != null) {
            val st = o.getSpanStart(this)
            if (st >= 0 && st == o.getSpanEnd(this)) return 0
        }
        return indentPerLevelPx * (level + 1) + gapPx
    }

    override fun drawLeadingMargin(
        canvas: Canvas, paint: Paint, x: Int, dir: Int,
        top: Int, baseline: Int, bottom: Int,
        text: CharSequence, start: Int, end: Int,
        first: Boolean, layout: Layout?
    ) {
        if (!first) return
        if (text is Spanned && text.getSpanStart(this) != start) return
        val originalStyle = paint.style
        paint.style = Paint.Style.FILL
        val cx = x + dir * (indentPerLevelPx * level + indentPerLevelPx / 2f)
        val cy = (top + bottom) / 2f
        canvas.drawCircle(cx, cy, radiusPx, paint)
        paint.style = originalStyle
    }
}

class NoteNumberLineSpan(val level: Int = 0, var number: Int = 1) : LeadingMarginSpan {
    var owner: Spanned? = null
    private val density = Resources.getSystem().displayMetrics.density
    private val indentPerLevelPx = (22 * density).toInt()
    private val gapPx = (8 * density).toInt()

    override fun getLeadingMargin(first: Boolean): Int {
        val o = owner
        if (o != null) {
            val st = o.getSpanStart(this)
            if (st >= 0 && st == o.getSpanEnd(this)) return 0
        }
        return indentPerLevelPx * (level + 1) + gapPx
    }

    override fun drawLeadingMargin(
        canvas: Canvas, paint: Paint, x: Int, dir: Int,
        top: Int, baseline: Int, bottom: Int,
        text: CharSequence, start: Int, end: Int,
        first: Boolean, layout: Layout?
    ) {
        if (!first) return
        if (text is Spanned && text.getSpanStart(this) != start) return
        val label = "$number."
        val tx = x + dir * (indentPerLevelPx * level)
        canvas.drawText(label, tx.toFloat(), baseline.toFloat(), paint)
    }
}

class NoteEditText(context: Context) : AppCompatEditText(context) {
    var onSelectionChange: ((Int, Int) -> Unit)? = null

    var onAlturaTexto: ((Int) -> Unit)? = null
    private var ultimaAlturaTexto = -1

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val lay = layout ?: return
        val h = lay.height + compoundPaddingTop + compoundPaddingBottom
        if (h != ultimaAlturaTexto) {
            ultimaAlturaTexto = h
            val cb = onAlturaTexto ?: return
            post { cb(h) }
        }
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        val ed = text
        if (ed != null && selStart == selEnd && selStart in 0 until ed.length &&
            ed[selStart] == '\u200B' && (selStart == 0 || ed[selStart - 1] == '\n')) {
            setSelection(selStart + 1)
            return
        }
        onSelectionChange?.invoke(selStart, selEnd)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val lay = layout ?: return
        val ed = text ?: return
        val len = ed.length
        if (len == 0 || ed[len - 1] != '\n') return
        val line = lay.lineCount - 1
        if (lay.getLineStart(line) != len) return
        val pintura = Paint(paint).apply { color = currentTextColor; style = Paint.Style.FILL }
        canvas.save()
        canvas.translate(compoundPaddingLeft.toFloat(), extendedPaddingTop.toFloat())
        ed.getSpans(len, len, LeadingMarginSpan::class.java)
            .filter { ed.getSpanStart(it) == len && ed.getSpanEnd(it) == len }
            .forEach {
                it.drawLeadingMargin(
                    canvas, pintura, lay.getParagraphLeft(line), 1,
                    lay.getLineTop(line), lay.getLineBaseline(line), lay.getLineBottom(line),
                    ed, len, len, true, lay
                )
            }
        canvas.restore()
    }
}

class NativeRichTextState {

    internal var editText: NoteEditText? = null
    private var pendingHtml: String? = null

    var onUserTyped: (() -> Unit)? = null
    private var suppressWatcher = false

    var isBold          by mutableStateOf(false); private set
    var isItalic        by mutableStateOf(false); private set
    var isUnderline     by mutableStateOf(false); private set
    var isStrikethrough by mutableStateOf(false); private set
    var isBulletList    by mutableStateOf(false); private set
    var isNumberedList  by mutableStateOf(false); private set
    var canUndo         by mutableStateOf(false); private set
    var canRedo         by mutableStateOf(false); private set
    var textLength      by mutableIntStateOf(0); private set

    private var pendingBold = false
    private var pendingItalic = false
    private var pendingUnderline = false
    private var pendingStrikethrough = false

    private data class Snapshot(val text: SpannableStringBuilder, val selStart: Int, val selEnd: Int)
    private val undoStack = ArrayDeque<Snapshot>()
    private val redoStack = ArrayDeque<Snapshot>()
    private var typingBurstActive = false
    private val handler = Handler(Looper.getMainLooper())
    private val burstEndRunnable = Runnable { typingBurstActive = false }

    fun attach(et: NoteEditText) {
        editText = et
        et.addTextChangedListener(object : TextWatcher {
            var changeStart = 0
            var changeCount = 0

            var removedText: String = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                removedText = if (count > 0 && s != null) s.subSequence(start, start + count).toString() else ""
                if (!suppressWatcher) onBeforeTextChangedForUndo()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                changeStart = start
                changeCount = count
            }

            override fun afterTextChanged(s: Editable) {
                if (suppressWatcher) return
                applyPendingStylesAfterInsert(changeStart, changeCount)
                if (changeCount == 1 && changeStart < s.length && s[changeStart] == '\n') {
                    continueListOnEnter(changeStart)
                } else if (changeCount == 0 && removedText.isNotEmpty()) {
                    afterDeletionInList(changeStart, removedText)
                } else if (changeCount > 0) {
                    stripZwspNear(changeStart)
                }
                renumberOrderedLists()
                textLength = s.length
                refreshActiveStates(et.selectionStart, et.selectionEnd)
                onUserTyped?.invoke()
            }
        })

        pendingHtml?.let { html ->
            pendingHtml = null
            setHtml(html)
        }
    }

    fun setHtml(html: String) {
        val spannable = parseNoteHtml(html)
        val et = editText
        if (et == null) {
            pendingHtml = html
            return
        }
        suppressWatcher = true
        et.setText(spannable)
        et.setSelection(0)
        suppressWatcher = false
        undoStack.clear()
        redoStack.clear()
        canUndo = false
        canRedo = false
        textLength = spannable.length
        renumberOrderedLists()
        refreshActiveStates(0, 0)
    }

    fun insertText(text: String) {
        if (text.isEmpty()) return
        val et = editText ?: return
        val editable = et.editableText ?: return
        val maxLen = 5000
        val hasCursor = et.selectionStart >= 0
        val start = if (hasCursor) et.selectionStart.coerceIn(0, editable.length) else editable.length
        val end   = if (hasCursor) et.selectionEnd.coerceIn(start, editable.length) else editable.length
        val room  = maxLen - (editable.length - (end - start))
        if (room <= 0) return
        val toInsert = if (text.length > room) text.substring(0, room) else text
        editable.replace(start, end, toInsert)
        et.setSelection((start + toInsert.length).coerceAtMost(editable.length))
    }

    private var dictAnchor = -1
    private var dictLen = 0
    private var dictPrefix = ""

    fun beginDictation() {
        val et = editText ?: return
        val editable = et.editableText
        val len = editable?.length ?: 0
        val pos = if (et.selectionStart >= 0) et.selectionStart else len
        dictAnchor = pos.coerceIn(0, len)
        dictLen = 0
        val before = if (dictAnchor > 0 && editable != null) editable[dictAnchor - 1] else null
        dictPrefix = if (before != null && before != ' ' && before != '\n') " " else ""
    }

    fun updateDictation(text: String) {
        val et = editText ?: return
        val editable = et.editableText ?: return
        if (dictAnchor < 0) beginDictation()
        val full  = dictPrefix + text
        val start = dictAnchor.coerceIn(0, editable.length)
        val end   = (dictAnchor + dictLen).coerceIn(start, editable.length)
        val room  = 5000 - (editable.length - (end - start))
        if (room <= 0) return
        val toInsert = if (full.length > room) full.substring(0, room) else full
        editable.replace(start, end, toInsert)
        dictLen = toInsert.length
        et.setSelection((start + toInsert.length).coerceAtMost(editable.length))
    }

    fun endDictation() {
        dictAnchor = -1
        dictLen = 0
        dictPrefix = ""
    }

    fun toHtml(): String {
        val editable = editText?.editableText ?: return "<p></p>"
        if (editable.isEmpty()) return "<p></p>"
        val text = editable.toString()
        val lines = text.split('\n')
        val sb = StringBuilder()
        var openListType: String? = null
        var offset = 0
        for (lineStr in lines) {
            val lineStart = offset
            val lineEndExclusive = offset + lineStr.length
            val listType = when {
                lineHasSpan(editable, lineStart, NoteBulletLineSpan::class.java) -> "ul"
                lineHasSpan(editable, lineStart, NoteNumberLineSpan::class.java) -> "ol"
                else -> null
            }
            if (listType != openListType) {
                if (openListType != null) sb.append("</").append(openListType).append('>')
                if (listType != null) sb.append('<').append(listType).append('>')
            }
            if (lineStr.replace(ZWSP.toString(), "").isEmpty() && listType == null) {
                sb.append("<br>")
            } else {
                val tag = if (listType != null) "li" else "p"
                sb.append('<').append(tag).append('>')
                sb.append(serializeInlineRange(editable, lineStart, lineEndExclusive))
                sb.append("</").append(tag).append('>')
            }
            openListType = listType
            offset = lineEndExclusive + 1
        }
        if (openListType != null) sb.append("</").append(openListType).append('>')
        return sb.toString()
    }

    fun toggleBold() = toggleCharacterStyle(
        isFullyStyled = { ed, s, e -> (s until e).all { p -> isBoldAt(ed, p) } },
        apply = { ed, s, e -> applyStyleSpan(ed, s, e, Typeface.BOLD) },
        remove = { ed, s, e -> removeStyleSpan(ed, s, e, Typeface.BOLD) },
        getPending = { pendingBold },
        setPending = { pendingBold = it; isBold = it },
    )

    fun toggleItalic() = toggleCharacterStyle(
        isFullyStyled = { ed, s, e -> (s until e).all { p -> isItalicAt(ed, p) } },
        apply = { ed, s, e -> applyStyleSpan(ed, s, e, Typeface.ITALIC) },
        remove = { ed, s, e -> removeStyleSpan(ed, s, e, Typeface.ITALIC) },
        getPending = { pendingItalic },
        setPending = { pendingItalic = it; isItalic = it },
    )

    fun toggleUnderline() = toggleCharacterStyle(
        isFullyStyled = { ed, s, e -> (s until e).all { p -> ed.getSpans(p, p + 1, UnderlineSpan::class.java).isNotEmpty() } },
        apply = { ed, s, e -> applySimpleSpan(ed, s, e, UnderlineSpan::class.java) { UnderlineSpan() } },
        remove = { ed, s, e -> removeSimpleSpan(ed, s, e, UnderlineSpan::class.java) { UnderlineSpan() } },
        getPending = { pendingUnderline },
        setPending = { pendingUnderline = it; isUnderline = it },
    )

    fun toggleStrikethrough() = toggleCharacterStyle(
        isFullyStyled = { ed, s, e -> (s until e).all { p -> ed.getSpans(p, p + 1, StrikethroughSpan::class.java).isNotEmpty() } },
        apply = { ed, s, e -> applySimpleSpan(ed, s, e, StrikethroughSpan::class.java) { StrikethroughSpan() } },
        remove = { ed, s, e -> removeSimpleSpan(ed, s, e, StrikethroughSpan::class.java) { StrikethroughSpan() } },
        getPending = { pendingStrikethrough },
        setPending = { pendingStrikethrough = it; isStrikethrough = it },
    )

    private fun toggleCharacterStyle(
        isFullyStyled: (Editable, Int, Int) -> Boolean,
        apply: (Editable, Int, Int) -> Unit,
        remove: (Editable, Int, Int) -> Unit,
        getPending: () -> Boolean,
        setPending: (Boolean) -> Unit,
    ) {
        val et = editText ?: return
        val start = minOf(et.selectionStart, et.selectionEnd).coerceAtLeast(0)
        val end = maxOf(et.selectionStart, et.selectionEnd).coerceAtLeast(0)
        if (start == end) {
            setPending(!getPending())
            return
        }
        checkpoint()
        val editable = et.editableText
        if (isFullyStyled(editable, start, end)) remove(editable, start, end) else apply(editable, start, end)
        refreshActiveStates(start, end)
    }

    private fun isBoldAt(ed: Spanned, p: Int): Boolean =
        ed.getSpans(p, p + 1, StyleSpan::class.java).any { it.style == Typeface.BOLD || it.style == Typeface.BOLD_ITALIC }

    private fun isItalicAt(ed: Spanned, p: Int): Boolean =
        ed.getSpans(p, p + 1, StyleSpan::class.java).any { it.style == Typeface.ITALIC || it.style == Typeface.BOLD_ITALIC }

    private fun applyStyleSpan(editable: Editable, start: Int, end: Int, style: Int) {
        val matches: (StyleSpan) -> Boolean = {
            if (style == Typeface.BOLD) it.style == Typeface.BOLD || it.style == Typeface.BOLD_ITALIC
            else it.style == Typeface.ITALIC || it.style == Typeface.BOLD_ITALIC
        }
        editable.getSpans(start, end, StyleSpan::class.java).filter(matches).forEach { span ->
            val sStart = editable.getSpanStart(span)
            val sEnd = editable.getSpanEnd(span)
            editable.removeSpan(span)
            if (sStart < start) editable.setSpan(StyleSpan(span.style), sStart, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (sEnd > end) editable.setSpan(StyleSpan(span.style), end, sEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        editable.setSpan(StyleSpan(style), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun removeStyleSpan(editable: Editable, start: Int, end: Int, style: Int) {
        val matches: (StyleSpan) -> Boolean = {
            if (style == Typeface.BOLD) it.style == Typeface.BOLD || it.style == Typeface.BOLD_ITALIC
            else it.style == Typeface.ITALIC || it.style == Typeface.BOLD_ITALIC
        }
        editable.getSpans(start, end, StyleSpan::class.java).filter(matches).forEach { span ->
            val sStart = editable.getSpanStart(span)
            val sEnd = editable.getSpanEnd(span)
            editable.removeSpan(span)
            if (sStart < start) editable.setSpan(StyleSpan(span.style), sStart, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (sEnd > end) editable.setSpan(StyleSpan(span.style), end, sEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun <T> applySimpleSpan(editable: Editable, start: Int, end: Int, cls: Class<T>, factory: () -> T) {
        editable.getSpans(start, end, cls).forEach { span ->
            val sStart = editable.getSpanStart(span)
            val sEnd = editable.getSpanEnd(span)
            editable.removeSpan(span)
            if (sStart < start) editable.setSpan(factory(), sStart, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (sEnd > end) editable.setSpan(factory(), end, sEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        editable.setSpan(factory(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun <T> removeSimpleSpan(editable: Editable, start: Int, end: Int, cls: Class<T>, factory: () -> T) {
        editable.getSpans(start, end, cls).forEach { span ->
            val sStart = editable.getSpanStart(span)
            val sEnd = editable.getSpanEnd(span)
            editable.removeSpan(span)
            if (sStart < start) editable.setSpan(factory(), sStart, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (sEnd > end) editable.setSpan(factory(), end, sEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun applyPendingStylesAfterInsert(start: Int, count: Int) {
        if (count <= 0) return
        val editable = editText?.editableText ?: return
        val end = (start + count).coerceAtMost(editable.length)
        if (pendingBold) mergeOrAddStyleSpan(editable, start, end, Typeface.BOLD)
        if (pendingItalic) mergeOrAddStyleSpan(editable, start, end, Typeface.ITALIC)
        if (pendingUnderline) mergeOrAddSimpleSpan(editable, start, end, UnderlineSpan::class.java) { UnderlineSpan() }
        if (pendingStrikethrough) mergeOrAddSimpleSpan(editable, start, end, StrikethroughSpan::class.java) { StrikethroughSpan() }
    }

    private fun mergeOrAddStyleSpan(editable: Editable, start: Int, end: Int, style: Int) {
        val prev = if (start > 0)
            editable.getSpans(start - 1, start, StyleSpan::class.java)
                .firstOrNull { it.style == style && editable.getSpanEnd(it) == start }
        else null
        if (prev != null) {
            val s = editable.getSpanStart(prev)
            editable.removeSpan(prev)
            editable.setSpan(StyleSpan(style), s, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            editable.setSpan(StyleSpan(style), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun <T> mergeOrAddSimpleSpan(editable: Editable, start: Int, end: Int, cls: Class<T>, factory: () -> T) {
        val prev = if (start > 0)
            editable.getSpans(start - 1, start, cls).firstOrNull { editable.getSpanEnd(it) == start }
        else null
        if (prev != null) {
            val s = editable.getSpanStart(prev)
            editable.removeSpan(prev)
            editable.setSpan(factory(), s, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            editable.setSpan(factory(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun toggleBulletList() = toggleListStyle(NoteBulletLineSpan::class.java) { NoteBulletLineSpan(0) }

    fun toggleNumberedList() = toggleListStyle(NoteNumberLineSpan::class.java) { NoteNumberLineSpan(0, 1) }

    private fun <T : LeadingMarginSpan> toggleListStyle(spanClass: Class<T>, factory: () -> T) {
        val et = editText ?: return
        checkpoint()
        val editable = et.editableText
        val text0 = editable.toString()
        val selStart = minOf(et.selectionStart, et.selectionEnd).coerceAtLeast(0)
        val selEnd = maxOf(et.selectionStart, et.selectionEnd).coerceAtLeast(0)
        val firstLineStart = lineStartIndex(text0, selStart)
        val lastLineEnd = lineEndInclusiveIndex(text0, selEnd)

        val touchedLineStarts = mutableListOf<Int>()
        var cursor = firstLineStart
        do {
            touchedLineStarts.add(cursor)
            val nl = text0.indexOf('\n', cursor)
            cursor = if (nl == -1) text0.length else nl + 1
        } while (cursor < lastLineEnd)

        val allHave = touchedLineStarts.all { ls -> lineHasSpan(editable, ls, spanClass) }

        var cursorFinal = selStart
        touchedLineStarts.asReversed().forEach { ls ->
            var text = editable.toString()
            var nl = text.indexOf('\n', ls)
            var lineEndExcl = if (nl == -1) text.length else nl
            removeListSpans(editable, ls, minOf(editable.length, lineEndExcl + 1))
            if (!allHave) {
                if (lineEndExcl == ls) {
                    suppressWatcher = true
                    editable.insert(ls, ZWSP.toString())
                    suppressWatcher = false
                    if (cursorFinal >= ls) cursorFinal += 1
                    text = editable.toString()
                    nl = text.indexOf('\n', ls)
                    lineEndExcl = if (nl == -1) text.length else nl
                }
                val lineEndIncl = if (nl == -1) editable.length else nl + 1
                val nuevoSpan = factory()
                editable.setSpan(nuevoSpan, ls, lineEndIncl,
                    if (editable.substring(ls, lineEndExcl) == ZWSP.toString()) Spanned.SPAN_INCLUSIVE_INCLUSIVE else Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                (nuevoSpan as? NoteBulletLineSpan)?.owner = editable
                (nuevoSpan as? NoteNumberLineSpan)?.owner = editable
            } else if (editable.substring(ls, lineEndExcl) == ZWSP.toString()) {
                suppressWatcher = true
                editable.delete(ls, ls + 1)
                suppressWatcher = false
                if (cursorFinal > ls) cursorFinal -= 1
            }
        }
        if (et.selectionStart == et.selectionEnd) et.setSelection(cursorFinal.coerceIn(0, editable.length))
        renumberOrderedLists()
        refreshActiveStates(et.selectionStart, et.selectionEnd)
    }

    private fun <T> lineHasSpan(ed: Spanned, ls: Int, cls: Class<T>): Boolean =
        ed.getSpans(ls, (ls + 1).coerceAtMost(ed.length).coerceAtLeast(ls), cls).any {
            val st = ed.getSpanStart(it); val en = ed.getSpanEnd(it)
            st <= ls && (en > ls || (st == ls && en == ls))
        }

    private fun removeListSpans(ed: Editable, from: Int, to: Int) {
        ed.getSpans(from, to, NoteBulletLineSpan::class.java).filter { ed.getSpanStart(it) >= from }.forEach { ed.removeSpan(it) }
        ed.getSpans(from, to, NoteNumberLineSpan::class.java).filter { ed.getSpanStart(it) >= from }.forEach { ed.removeSpan(it) }
    }

    private fun continueListOnEnter(nlPos: Int) {
        val et = editText ?: return
        val ed = et.editableText
        var text = ed.toString()
        val ls = lineStartIndex(text, nlPos)
        val bullet = ed.getSpans(ls, (ls + 1).coerceAtMost(ed.length), NoteBulletLineSpan::class.java).firstOrNull { ed.getSpanStart(it) <= ls }
        val number = ed.getSpans(ls, (ls + 1).coerceAtMost(ed.length), NoteNumberLineSpan::class.java).firstOrNull { ed.getSpanStart(it) <= ls }
        if (bullet == null && number == null) return
        val level = bullet?.level ?: number!!.level
        val contenido = text.substring(ls, nlPos)
        if (contenido.isEmpty() || contenido == ZWSP.toString()) {
            removeListSpans(ed, ls, minOf(ed.length, nlPos + 2))
            suppressWatcher = true
            for (idx in minOf(ed.length, nlPos + 2) - 1 downTo ls) {
                if (idx == nlPos || ed[idx] == ZWSP) ed.delete(idx, idx + 1)
            }
            suppressWatcher = false
            et.setSelection(ls.coerceAtMost(ed.length))
            return
        }
        val newStart = nlPos + 1
        val lineaNuevaVacia = newStart >= ed.length || text[newStart] == '\n'
        removeListSpans(ed, ls, minOf(ed.length, lineEndInclusiveIndex(text, newStart) + 1))
        if (lineaNuevaVacia) {
            suppressWatcher = true
            ed.insert(newStart, ZWSP.toString())
            suppressWatcher = false
            et.setSelection(newStart + 1)
            text = ed.toString()
        }
        val stripEnd = lineEndInclusiveIndex(text, newStart).coerceAtMost(ed.length)
        fun nuevo(): Any = if (bullet != null) NoteBulletLineSpan(level) else NoteNumberLineSpan(level, 1)
        ed.setSpan(nuevo(), ls, newStart, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        val flag = if (lineaNuevaVacia) Spanned.SPAN_INCLUSIVE_INCLUSIVE else Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        val spanNuevo = nuevo()
        ed.setSpan(spanNuevo, newStart, stripEnd, flag)
        (spanNuevo as? NoteBulletLineSpan)?.owner = ed
        (spanNuevo as? NoteNumberLineSpan)?.owner = ed
    }

    private fun stripZwspNear(pos: Int) {
        val et = editText ?: return
        val ed = et.editableText
        if (ed.isEmpty()) return
        val text = ed.toString()
        val ls = lineStartIndex(text, pos.coerceIn(0, text.length))
        val le = text.indexOf('\n', ls).let { if (it == -1) text.length else it }
        val linea = text.substring(ls, le)
        if (linea.length <= 1 || linea.indexOf(ZWSP) < 0) return
        val q = minOf(le + 1, ed.length)
        val bullet = ed.getSpans(ls, q, NoteBulletLineSpan::class.java).firstOrNull { ed.getSpanEnd(it) > ls }
        val number = ed.getSpans(ls, q, NoteNumberLineSpan::class.java).firstOrNull { ed.getSpanEnd(it) > ls }
        val level = bullet?.level ?: number?.level ?: 0
        suppressWatcher = true
        var idx = linea.indexOf(ZWSP)
        while (idx >= 0) {
            ed.delete(ls + idx, ls + idx + 1)
            val nueva = ed.toString()
            val leN = nueva.indexOf('\n', ls).let { if (it == -1) nueva.length else it }
            idx = nueva.substring(ls, leN).indexOf(ZWSP)
        }
        suppressWatcher = false
        if (bullet != null || number != null) {
            val t2 = ed.toString()
            val end = lineEndInclusiveIndex(t2, ls).coerceAtMost(ed.length)
            removeListSpans(ed, ls, minOf(ed.length, end + 1))
            ed.setSpan(
                if (bullet != null) NoteBulletLineSpan(level) else NoteNumberLineSpan(level, 1),
                ls, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun afterDeletionInList(pos: Int, removed: String) {
        stripZwspNear(pos)
        val et = editText ?: return
        val ed = et.editableText
        val text = ed.toString()
        val p = pos.coerceIn(0, text.length)
        val ls = lineStartIndex(text, p)
        val le = text.indexOf('\n', ls).let { if (it == -1) text.length else it }
        if (le != ls) return
        if (!lineHasSpan(ed, ls, NoteBulletLineSpan::class.java) && !lineHasSpan(ed, ls, NoteNumberLineSpan::class.java)) return
        if (removed.contains(ZWSP)) {
            removeListSpans(ed, ls, minOf(ed.length, le + 1))
        } else if (!removed.contains('\n')) {
            val esBullet = lineHasSpan(ed, ls, NoteBulletLineSpan::class.java)
            val nivel = ed.getSpans(ls, minOf(ed.length, ls + 1), NoteBulletLineSpan::class.java).firstOrNull()?.level
                ?: ed.getSpans(ls, minOf(ed.length, ls + 1), NoteNumberLineSpan::class.java).firstOrNull()?.level ?: 0
            suppressWatcher = true
            ed.insert(ls, ZWSP.toString())
            suppressWatcher = false
            removeListSpans(ed, ls, minOf(ed.length, ls + 2))
            val sp: Any = if (esBullet) NoteBulletLineSpan(nivel) else NoteNumberLineSpan(nivel, 1)
            val fin = lineEndInclusiveIndex(ed.toString(), ls).coerceAtMost(ed.length)
            ed.setSpan(sp, ls, fin, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            (sp as? NoteBulletLineSpan)?.owner = ed
            (sp as? NoteNumberLineSpan)?.owner = ed
            et.setSelection(ls + 1)
        }
    }

    private fun renumberOrderedLists() {
        val editable = editText?.editableText ?: return
        val spans = editable.getSpans(0, editable.length, NoteNumberLineSpan::class.java)
            .sortedBy { editable.getSpanStart(it) }
        val counters = mutableMapOf<Int, Int>()
        var prevEnd = -1
        var changed = false
        for (span in spans) {
            val start = editable.getSpanStart(span)
            val end = editable.getSpanEnd(span)
            if (start != prevEnd) counters.clear()
            val n = counters.getOrDefault(span.level, 1)
            if (span.number != n) {
                span.number = n
                changed = true
            }
            counters[span.level] = n + 1
            counters.keys.filter { it > span.level }.forEach { counters.remove(it) }
            prevEnd = end
        }
        if (changed) editText?.invalidate()
    }

    fun refreshActiveStates(rawStart: Int, rawEnd: Int) {
        val et = editText ?: return
        val editable = et.editableText ?: return
        val len = editable.length
        val start = minOf(rawStart, rawEnd).coerceIn(0, len)
        val end = maxOf(rawStart, rawEnd).coerceIn(0, len)

        if (start == end) {
            syncPendingFromCursor(start)
        } else {
            isBold = (start until end).all { p -> isBoldAt(editable, p) }
            isItalic = (start until end).all { p -> isItalicAt(editable, p) }
            isUnderline = (start until end).all { p -> editable.getSpans(p, p + 1, UnderlineSpan::class.java).isNotEmpty() }
            isStrikethrough = (start until end).all { p -> editable.getSpans(p, p + 1, StrikethroughSpan::class.java).isNotEmpty() }
            pendingBold = isBold
            pendingItalic = isItalic
            pendingUnderline = isUnderline
            pendingStrikethrough = isStrikethrough
        }

        val lineStart = lineStartIndex(editable.toString(), start)
        isBulletList = lineHasSpan(editable, lineStart, NoteBulletLineSpan::class.java)
        isNumberedList = lineHasSpan(editable, lineStart, NoteNumberLineSpan::class.java)
    }

    private fun syncPendingFromCursor(pos: Int) {
        val editable = editText?.editableText ?: return
        if (editable.isEmpty()) {
            pendingBold = false; pendingItalic = false; pendingUnderline = false; pendingStrikethrough = false
        } else {
            val p = (pos - 1).coerceAtLeast(0)
            pendingBold = isBoldAt(editable, p)
            pendingItalic = isItalicAt(editable, p)
            pendingUnderline = editable.getSpans(p, p + 1, UnderlineSpan::class.java).isNotEmpty()
            pendingStrikethrough = editable.getSpans(p, p + 1, StrikethroughSpan::class.java).isNotEmpty()
        }
        isBold = pendingBold
        isItalic = pendingItalic
        isUnderline = pendingUnderline
        isStrikethrough = pendingStrikethrough
    }

    private fun currentSnapshot(): Snapshot? {
        val et = editText ?: return null
        val t = et.text ?: return null
        return Snapshot(SpannableStringBuilder(t), et.selectionStart, et.selectionEnd)
    }

    private fun checkpoint() {
        val snap = currentSnapshot() ?: return
        undoStack.addLast(snap)
        if (undoStack.size > MAX_HISTORY) undoStack.removeFirst()
        redoStack.clear()
        canUndo = true
        canRedo = false
    }

    private fun onBeforeTextChangedForUndo() {
        if (!typingBurstActive) checkpoint()
        typingBurstActive = true
        handler.removeCallbacks(burstEndRunnable)
        handler.postDelayed(burstEndRunnable, TYPING_BURST_GAP_MS)
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val current = currentSnapshot()
        val snap = undoStack.removeLast()
        if (current != null) redoStack.addLast(current)
        restoreSnapshot(snap)
        canUndo = undoStack.isNotEmpty()
        canRedo = redoStack.isNotEmpty()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val current = currentSnapshot()
        val snap = redoStack.removeLast()
        if (current != null) undoStack.addLast(current)
        restoreSnapshot(snap)
        canUndo = undoStack.isNotEmpty()
        canRedo = redoStack.isNotEmpty()
    }

    private fun restoreSnapshot(snap: Snapshot) {
        val et = editText ?: return
        suppressWatcher = true
        et.setText(snap.text)
        val len = et.text?.length ?: 0
        et.setSelection(snap.selStart.coerceIn(0, len), snap.selEnd.coerceIn(0, len))
        suppressWatcher = false
        typingBurstActive = false
        renumberOrderedLists()
        textLength = len
        refreshActiveStates(et.selectionStart, et.selectionEnd)
    }
}

@Composable
fun rememberNativeRichTextState(): NativeRichTextState = remember { NativeRichTextState() }

private fun lineStartIndex(text: String, pos: Int): Int {
    if (pos <= 0) return 0
    val idx = text.lastIndexOf('\n', pos - 1)
    return if (idx == -1) 0 else idx + 1
}

private fun lineEndInclusiveIndex(text: String, pos: Int): Int {
    val idx = text.indexOf('\n', pos)
    return if (idx == -1) text.length else idx + 1
}

private fun serializeInlineRange(editable: Spanned, start: Int, end: Int): String {
    if (start >= end) return ""
    val sb = StringBuilder()
    var pos = start
    while (pos < end) {
        val bold = editable.getSpans(pos, pos + 1, StyleSpan::class.java).any { it.style == Typeface.BOLD || it.style == Typeface.BOLD_ITALIC }
        val italic = editable.getSpans(pos, pos + 1, StyleSpan::class.java).any { it.style == Typeface.ITALIC || it.style == Typeface.BOLD_ITALIC }
        val underline = editable.getSpans(pos, pos + 1, UnderlineSpan::class.java).isNotEmpty()
        val strike = editable.getSpans(pos, pos + 1, StrikethroughSpan::class.java).isNotEmpty()

        var runEnd = pos + 1
        while (runEnd < end) {
            val b2 = editable.getSpans(runEnd, runEnd + 1, StyleSpan::class.java).any { it.style == Typeface.BOLD || it.style == Typeface.BOLD_ITALIC }
            val i2 = editable.getSpans(runEnd, runEnd + 1, StyleSpan::class.java).any { it.style == Typeface.ITALIC || it.style == Typeface.BOLD_ITALIC }
            val u2 = editable.getSpans(runEnd, runEnd + 1, UnderlineSpan::class.java).isNotEmpty()
            val s2 = editable.getSpans(runEnd, runEnd + 1, StrikethroughSpan::class.java).isNotEmpty()
            if (b2 != bold || i2 != italic || u2 != underline || s2 != strike) break
            runEnd++
        }

        val tags = buildList {
            if (bold) add("b")
            if (italic) add("i")
            if (underline) add("u")
            if (strike) add("s")
        }
        tags.forEach { sb.append('<').append(it).append('>') }
        sb.append(encodeHtmlEntities(editable.subSequence(pos, runEnd).toString().replace(ZWSP.toString(), "")))
        tags.asReversed().forEach { sb.append("</").append(it).append('>') }

        pos = runEnd
    }
    return sb.toString()
}

private fun encodeHtmlEntities(s: String): String {
    if (s.none { it == '&' || it == '<' || it == '>' }) return s
    val sb = StringBuilder(s.length + 8)
    for (c in s) {
        when (c) {
            '&' -> sb.append("&amp;")
            '<' -> sb.append("&lt;")
            '>' -> sb.append("&gt;")
            else -> sb.append(c)
        }
    }
    return sb.toString()
}

private val namedHtmlEntities: Map<String, Char> = mapOf(
    "amp" to '&', "lt" to '<', "gt" to '>', "quot" to '"', "apos" to '\'',
    "nbsp" to ' ', "iexcl" to '¡', "cent" to '¢', "pound" to '£', "curren" to '¤',
    "yen" to '¥', "brvbar" to '¦', "sect" to '§', "uml" to '¨', "copy" to '©',
    "ordf" to 'ª', "laquo" to '«', "not" to '¬', "shy" to '­', "reg" to '®',
    "macr" to '¯', "deg" to '°', "plusmn" to '±', "sup2" to '²', "sup3" to '³',
    "acute" to '´', "micro" to 'µ', "para" to '¶', "middot" to '·', "cedil" to '¸',
    "sup1" to '¹', "ordm" to 'º', "raquo" to '»', "frac14" to '¼', "frac12" to '½',
    "frac34" to '¾', "iquest" to '¿',
    "Agrave" to 'À', "Aacute" to 'Á', "Acirc" to 'Â', "Atilde" to 'Ã', "Auml" to 'Ä', "Aring" to 'Å',
    "AElig" to 'Æ', "Ccedil" to 'Ç',
    "Egrave" to 'È', "Eacute" to 'É', "Ecirc" to 'Ê', "Euml" to 'Ë',
    "Igrave" to 'Ì', "Iacute" to 'Í', "Icirc" to 'Î', "Iuml" to 'Ï',
    "ETH" to 'Ð', "Ntilde" to 'Ñ',
    "Ograve" to 'Ò', "Oacute" to 'Ó', "Ocirc" to 'Ô', "Otilde" to 'Õ', "Ouml" to 'Ö',
    "times" to '×', "Oslash" to 'Ø',
    "Ugrave" to 'Ù', "Uacute" to 'Ú', "Ucirc" to 'Û', "Uuml" to 'Ü',
    "Yacute" to 'Ý', "THORN" to 'Þ', "szlig" to 'ß',
    "agrave" to 'à', "aacute" to 'á', "acirc" to 'â', "atilde" to 'ã', "auml" to 'ä', "aring" to 'å',
    "aelig" to 'æ', "ccedil" to 'ç',
    "egrave" to 'è', "eacute" to 'é', "ecirc" to 'ê', "euml" to 'ë',
    "igrave" to 'ì', "iacute" to 'í', "icirc" to 'î', "iuml" to 'ï',
    "eth" to 'ð', "ntilde" to 'ñ',
    "ograve" to 'ò', "oacute" to 'ó', "ocirc" to 'ô', "otilde" to 'õ', "ouml" to 'ö',
    "divide" to '÷', "oslash" to 'ø',
    "ugrave" to 'ù', "uacute" to 'ú', "ucirc" to 'û', "uuml" to 'ü',
    "yacute" to 'ý', "thorn" to 'þ', "yuml" to 'ÿ',
    "hellip" to '…', "mdash" to '—', "ndash" to '–',
    "lsquo" to '‘', "rsquo" to '’', "ldquo" to '“', "rdquo" to '”',
    "trade" to '™', "euro" to '€', "bull" to '•',
    "excl" to '!', "num" to '#', "dollar" to '$', "percnt" to '%',
    "lpar" to '(', "rpar" to ')', "ast" to '*', "plus" to '+',
    "comma" to ',', "period" to '.', "sol" to '/', "colon" to ':', "semi" to ';',
    "equals" to '=', "quest" to '?', "commat" to '@',
    "lsqb" to '[', "lbrack" to '[', "bsol" to '\\', "rsqb" to ']', "rbrack" to ']',
    "Hat" to '^', "lowbar" to '_', "grave" to '`',
    "lcub" to '{', "lbrace" to '{', "verbar" to '|', "vert" to '|', "rcub" to '}', "rbrace" to '}',
)

private val entityRegex = Regex("&(#x[0-9a-fA-F]+|#[0-9]+|[a-zA-Z0-9]+);")

private fun decodeHtmlEntities(s: String): String {
    if ('&' !in s) return s
    var result = s
    var iterations = 0
    while ('&' in result && iterations < 4) {
        val next = entityRegex.replace(result) { m ->
            val body = m.groupValues[1]
            when {
                body.startsWith("#x", ignoreCase = true) ->
                    body.substring(2).toIntOrNull(16)?.let { String(Character.toChars(it)) } ?: m.value
                body.startsWith("#") ->
                    body.substring(1).toIntOrNull()?.let { String(Character.toChars(it)) } ?: m.value
                else ->
                    namedHtmlEntities[body]?.toString() ?: m.value
            }
        }
        if (next == result) break
        result = next
        iterations++
    }
    return result
}

private val inlineTagSpanFactory: Map<String, () -> Any> = mapOf(
    "b" to { StyleSpan(Typeface.BOLD) },
    "strong" to { StyleSpan(Typeface.BOLD) },
    "i" to { StyleSpan(Typeface.ITALIC) },
    "em" to { StyleSpan(Typeface.ITALIC) },
    "u" to { UnderlineSpan() },
    "ins" to { UnderlineSpan() },
    "s" to { StrikethroughSpan() },
    "strike" to { StrikethroughSpan() },
    "del" to { StrikethroughSpan() },
)

private data class ParsedLine(val text: SpannableStringBuilder, val listType: String?, val listLevel: Int)

private fun parseNoteHtml(html: String): SpannableStringBuilder {
    val lines = mutableListOf<ParsedLine>()
    var currentLineText = SpannableStringBuilder()
    var currentListType: String? = null
    var currentListLevel = 0

    val listTypeStack = ArrayDeque<String>()
    data class InlineOpen(val tag: String, val start: Int)
    val inlineStack = ArrayDeque<InlineOpen>()

    fun finishLine() {
        lines.add(ParsedLine(currentLineText, currentListType, currentListLevel))
        currentLineText = SpannableStringBuilder()
        currentListType = null
    }

    val tagRegex = Regex("<(/?)([a-zA-Z0-9]+)([^>]*)>")
    var lastIndex = 0
    for (match in tagRegex.findAll(html)) {
        if (match.range.first > lastIndex) {
            val raw = html.substring(lastIndex, match.range.first)
            val decoded = decodeHtmlEntities(raw)
            if (decoded.isNotEmpty()) currentLineText.append(decoded)
        }
        lastIndex = match.range.last + 1

        val isClosing = match.groupValues[1] == "/"
        val tag = match.groupValues[2].lowercase()

        when {
            tag == "br" -> {
                if (!isClosing) finishLine()
            }
            tag == "ul" || tag == "ol" -> {
                if (!isClosing) {
                    listTypeStack.addLast(tag)
                    currentListLevel = listTypeStack.size - 1
                } else {
                    if (listTypeStack.isNotEmpty()) listTypeStack.removeLast()
                    currentListLevel = (listTypeStack.size - 1).coerceAtLeast(0)
                }
            }
            tag == "li" -> {
                if (!isClosing) {
                    currentLineText = SpannableStringBuilder()
                    currentListType = listTypeStack.lastOrNull()
                    currentListLevel = (listTypeStack.size - 1).coerceAtLeast(0)
                } else {
                    finishLine()
                }
            }
            tag == "p" || tag == "div" -> {
                if (!isClosing) {
                    currentLineText = SpannableStringBuilder()
                    currentListType = null
                } else {
                    finishLine()
                }
            }
            inlineTagSpanFactory.containsKey(tag) -> {
                if (!isClosing) {
                    inlineStack.addLast(InlineOpen(tag, currentLineText.length))
                } else {
                    val openIdx = inlineStack.indexOfLast { it.tag == tag }
                    if (openIdx >= 0) {
                        val open = inlineStack[openIdx]
                        inlineStack.removeAt(openIdx)
                        val spanStart = open.start
                        val spanEnd = currentLineText.length
                        if (spanEnd > spanStart) {
                            currentLineText.setSpan(
                                inlineTagSpanFactory.getValue(tag).invoke(),
                                spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                }
            }
            else -> {   }
        }
    }
    if (lastIndex < html.length) {
        val decoded = decodeHtmlEntities(html.substring(lastIndex))
        if (decoded.isNotEmpty()) currentLineText.append(decoded)
    }
    if (currentLineText.isNotEmpty() || lines.isEmpty()) {
        lines.add(ParsedLine(currentLineText, currentListType, currentListLevel))
    }

    val result = SpannableStringBuilder()
    val olCounters = mutableMapOf<Int, Int>()
    var prevListType: String? = null
    var prevListLevel: Int? = null
    lines.forEachIndexed { idx, line ->
        val start = result.length
        result.append(line.text)
        val isLast = idx == lines.lastIndex
        if (!isLast) result.append('\n')
        val spanEnd = result.length

        val contiguous = prevListType == line.listType && prevListLevel == line.listLevel
        if (!contiguous) olCounters.remove(line.listLevel)

        when (line.listType) {
            "ul" -> result.setSpan(NoteBulletLineSpan(line.listLevel), start, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            "ol" -> {
                val num = olCounters.getOrDefault(line.listLevel, 1)
                olCounters[line.listLevel] = num + 1
                result.setSpan(NoteNumberLineSpan(line.listLevel, num), start, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        prevListType = line.listType
        prevListLevel = line.listLevel
    }
    return result
}

@Composable
fun NativeRichTextEditor(
    state: NativeRichTextState,
    modifier: Modifier = Modifier,
    textColor: Color,
    fontSizeSp: Float,
    selectionHighlightColor: Color,
    lineHeightSp: Float = fontSizeSp * 1.5f,
    onAlturaTexto: ((Int) -> Unit)? = null,
) {
    val textColorArgb = textColor.toArgb()
    val highlightArgb = selectionHighlightColor.toArgb()
    val lineSpacingMultiplier = if (fontSizeSp > 0f) lineHeightSp / fontSizeSp else 1.5f

    val autoLockManager = LocalAutoLockManager.current
    state.onUserTyped = { autoLockManager?.recordInteraction() }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            NoteEditText(context).apply {
                setBackgroundColor(AndroidColor.TRANSPARENT)
                setPadding(0, 0, 0, 0)
                gravity = Gravity.TOP or Gravity.START
                isSingleLine = false
                setHorizontallyScrolling(false)
                inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                    InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
                imeOptions = EditorInfo.IME_ACTION_NONE
                setTextColor(textColorArgb)
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, fontSizeSp)
                setLineSpacing(0f, lineSpacingMultiplier)
                highlightColor = highlightArgb
                onSelectionChange = { s, e -> state.refreshActiveStates(s, e) }
                this.onAlturaTexto = onAlturaTexto
                state.attach(this)
            }
        },
        update = { et ->
            if (et.currentTextColor != textColorArgb) et.setTextColor(textColorArgb)
            et.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, fontSizeSp)
            et.setLineSpacing(0f, lineSpacingMultiplier)
            if (et.highlightColor != highlightArgb) et.highlightColor = highlightArgb
            et.onAlturaTexto = onAlturaTexto
        }
    )
}