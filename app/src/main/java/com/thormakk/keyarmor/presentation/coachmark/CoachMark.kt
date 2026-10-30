package com.thormakk.keyarmor.presentation.coachmark

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.composed
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import com.thormakk.keyarmor.data.prefs.CoachMarkPrefs
import com.thormakk.keyarmor.data.prefs.OnboardingPrefs

val LocalCoachMarks = staticCompositionLocalOf<CoachMarkController?> { null }

class CoachMarkController(
    private val coachMarkPrefs:  CoachMarkPrefs,
    private val onboardingPrefs: OnboardingPrefs,
) {
    private val anchors = mutableStateMapOf<String, Rect>()

    var currentScreen by mutableStateOf<String?>(null)
        private set
    var specs by mutableStateOf<List<CoachMarkSpec>>(emptyList())
        private set
    var stepIndex by mutableIntStateOf(0)
        private set
    var active by mutableStateOf(false)
        private set

    fun reportAnchor(screenId: String, anchorId: String, rect: Rect) {
        if (screenId == currentScreen) anchors[anchorId] = rect
    }

    fun anchorRect(anchorId: String): Rect? = anchors[anchorId]

    fun onScreenChanged(screenId: String?) {
        if (screenId == currentScreen) return
        currentScreen = screenId
        anchors.clear()
        stepIndex = 0
        val unseen = if (screenId == null) emptyList()
            else CoachMarkRegistry.forScreen(screenId)
                .filter { !coachMarkPrefs.hasSeen(screenId, it.anchorId) }
        specs = unseen
        active = unseen.isNotEmpty() && onboardingPrefs.hasSeenWelcome && !onboardingPrefs.tutorialSkipped
    }

    fun setScreenSpecs(screenId: String, anchorIds: List<String>) {
        if (screenId != currentScreen) return
        val all    = CoachMarkRegistry.forScreen(screenId)
        val chosen = anchorIds.mapNotNull { id -> all.firstOrNull { it.anchorId == id } }
            .filter { !coachMarkPrefs.hasSeen(screenId, it.anchorId) }
        if (chosen == specs) return
        specs = chosen
        stepIndex = 0
        active = chosen.isNotEmpty() && onboardingPrefs.hasSeenWelcome && !onboardingPrefs.tutorialSkipped
    }

    fun next() {
        markCurrentSeen()
        if (stepIndex < specs.lastIndex) stepIndex++ else active = false
    }

    fun autoSkip() {
        if (stepIndex < specs.lastIndex) stepIndex++ else active = false
    }

    fun skip() {
        val s = currentScreen ?: run { active = false; return }
        for (i in stepIndex..specs.lastIndex) {
            specs.getOrNull(i)?.let { coachMarkPrefs.markSeen(s, it.anchorId) }
        }
        active = false
    }

    private fun markCurrentSeen() {
        val s = currentScreen ?: return
        specs.getOrNull(stepIndex)?.let { coachMarkPrefs.markSeen(s, it.anchorId) }
    }

    fun resetAllSeen() = coachMarkPrefs.resetAll()
}

fun Modifier.coachMarkAnchor(screenId: String, anchorId: String): Modifier = composed {
    val controller = LocalCoachMarks.current
    if (controller == null) this
    else this.onGloballyPositioned { coords ->
        controller.reportAnchor(screenId, anchorId, coords.boundsInWindow())
    }
}
