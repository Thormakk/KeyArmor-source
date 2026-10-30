package com.thormakk.keyarmor.security

import android.content.Context
import com.thormakk.keyarmor.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommonPasswordsChecker @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope   private val appScope: CoroutineScope
) {
    @Volatile
    private var commonSet: HashSet<String> = HashSet()

    init {
        appScope.launch(Dispatchers.IO) {
            commonSet = try {
                context.assets.open("common_passwords.txt").bufferedReader()
                    .readLines()
                    .map { it.trim().lowercase(Locale.ROOT) }
                    .filter { it.isNotEmpty() }
                    .toHashSet()
            } catch (e: Exception) {
                HashSet()
            }
        }
    }

    fun isCommon(password: String): Boolean {
        if (password.isEmpty()) return false
        return commonSet.contains(password.lowercase(Locale.ROOT))
    }
}
