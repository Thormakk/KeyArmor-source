package com.thormakk.keyarmor.security

object PasswordStrengthCalculator {

    fun calculate(password: String): Int {
        if (password.isEmpty()) return -1

        val hasLower   = password.any { it.isLowerCase() }
        val hasUpper   = password.any { it.isUpperCase() }
        val hasDigit   = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        val classes    = listOf(hasLower, hasUpper, hasDigit, hasSpecial).count { it }
        val len        = password.length

        return when {
            len >= 20 && classes >= 3 -> 4
            len >= 20 && classes >= 2 -> 3
            len >= 20                 -> 2
            len >= 12 && classes >= 4 -> 4
            len >= 12 && classes >= 3 -> 3
            len >= 12 && classes >= 2 -> 2
            len >= 8  && classes >= 3 -> 3
            len >= 8  && classes >= 2 -> 2
            len >= 8                  -> 1
            else                      -> 0
        }
    }
}
