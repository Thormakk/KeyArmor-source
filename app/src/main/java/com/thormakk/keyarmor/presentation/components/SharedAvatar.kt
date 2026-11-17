@file:OptIn(ExperimentalSharedTransitionApi::class)
package com.thormakk.keyarmor.presentation.components

import androidx.compose.runtime.remember
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

@Composable
fun Modifier.sharedAvatar(
    key:                     String,
    sharedTransitionScope:   SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?
): Modifier {
    if (sharedTransitionScope == null || animatedVisibilityScope == null) return this
    val base = this
    return with(sharedTransitionScope) {
        base.sharedElement(
            sharedContentState      = rememberSharedContentState(key),
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}

val LocalBottomBarShown = compositionLocalOf { true }

@Composable
fun Modifier.hideWhenLeaving(animatedVisibilityScope: AnimatedVisibilityScope?): Modifier {
    if (animatedVisibilityScope == null) return this
    val barShown = LocalBottomBarShown.current
    val hadBar = remember { booleanArrayOf(barShown) }
    if (barShown) hadBar[0] = true
    val leaving = animatedVisibilityScope.transition.targetState != EnterExitState.Visible ||
        (hadBar[0] && !barShown)
    return if (leaving) this.alpha(0f) else this
}

@Composable
fun Modifier.hideDuringTransition(animatedVisibilityScope: AnimatedVisibilityScope?): Modifier {
    if (animatedVisibilityScope == null) return this
    val tr = animatedVisibilityScope.transition
    val quieta = tr.currentState == EnterExitState.Visible && tr.targetState == EnterExitState.Visible
    return if (quieta) this else this.alpha(0f)
}
