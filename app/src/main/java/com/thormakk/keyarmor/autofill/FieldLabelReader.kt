package com.thormakk.keyarmor.autofill

import android.app.assist.AssistStructure
import android.view.autofill.AutofillId

object FieldLabelReader {

    fun read(structure: AssistStructure, target: AutofillId): String {
        for (i in 0 until structure.windowNodeCount) {
            val raiz = structure.getWindowNodeAt(i).rootViewNode
            val etiqueta = buscar(raiz, target, parentHint = null)
            if (etiqueta != null) return etiqueta
        }
        return ""
    }

    private fun buscar(
        node:       AssistStructure.ViewNode,
        target:     AutofillId,
        parentHint: CharSequence?,
    ): String? {
        if (node.autofillId == target) {
            val atributos = node.htmlInfo?.attributes
                ?.associate { it.first.lowercase() to it.second }
                .orEmpty()
            return FieldLabel.resolve(
                hint               = node.hint,
                parentHint         = parentHint,
                contentDescription = node.contentDescription,
                htmlAttrs          = atributos,
                idEntry            = node.idEntry,
            )
        }
        val hintParaHijos = node.hint ?: parentHint
        for (i in 0 until node.childCount) {
            val r = buscar(node.getChildAt(i), target, hintParaHijos)
            if (r != null) return r
        }
        return null
    }
}
