package com.thormakk.keyarmor.autofill

object SaveDecision {

    data class Existing(
        val id:         String,
        val title:      String,
        val username:   String,
        val email:      String,
        val password:   String,
        val fieldLabel: String,
        val isProtected: Boolean = false,
    )

    sealed interface Result {
        data object AlreadySaved : Result

        data class Create(val title: String) : Result

        data class AskUpdateOrAdd(val target: Existing, val suggestedTitle: String) : Result

        data class AskName(val suggestedTitle: String) : Result
    }

    fun decide(
        existing:   List<Existing>,
        baseTitle:  String,
        username:   String,
        email:      String,
        password:   String,
        fieldLabel: String,
    ): Result {
        val mismaIdentidad = existing.filter { sameIdentity(it, username, email) }

        if (mismaIdentidad.any { it.password == password }) return Result.AlreadySaved

        val objetivo = mismaIdentidad.firstOrNull { FieldLabel.sameField(it.fieldLabel, fieldLabel) }
            ?: mismaIdentidad.singleOrNull { it.fieldLabel.isBlank() || fieldLabel.isBlank() }

        val actualizable = objetivo?.takeUnless { it.isProtected }

        val sugerido = suggestTitle(baseTitle, fieldLabel, existing.map { it.title })

        if (actualizable != null) return Result.AskUpdateOrAdd(actualizable, sugerido)

        val choca = mismaIdentidad.any { it.title.equals(baseTitle, ignoreCase = true) }
        return if (choca) Result.AskName(sugerido) else Result.Create(baseTitle)
    }

    fun suggestTitle(baseTitle: String, fieldLabel: String, taken: List<String>): String {
        fun libre(t: String) = taken.none { it.equals(t, ignoreCase = true) }
        if (!FieldLabel.isGeneric(fieldLabel)) {
            val conEtiqueta = "$baseTitle – $fieldLabel"
            if (libre(conEtiqueta)) return conEtiqueta
        }
        var n = 2
        while (!libre("$baseTitle ($n)")) n++
        return "$baseTitle ($n)"
    }

    fun isTitleTaken(candidate: String, taken: List<String>): Boolean =
        taken.any { it.trim().equals(candidate.trim(), ignoreCase = true) }

    private fun sameIdentity(e: Existing, username: String, email: String): Boolean =
        norm(e.username) == norm(username) && norm(e.email) == norm(email)

    private fun norm(s: String) = s.trim().lowercase()
}
