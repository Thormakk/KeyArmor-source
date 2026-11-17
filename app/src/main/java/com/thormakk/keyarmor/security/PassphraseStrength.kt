package com.thormakk.keyarmor.security

object PassphraseStrength {

    const val BITS_PER_WORD = 11
    const val MIN_WORDS = 4
    const val MAX_WORDS = 10
    const val DEFAULT_WORDS = 6

    fun bits(words: Int): Int = words * BITS_PER_WORD

    fun level(words: Int): Int = when {
        bits(words) >= 66 -> 4
        bits(words) >= 55 -> 3
        bits(words) >= 44 -> 2
        bits(words) >= 33 -> 1
        else              -> 0
    }
}
