package com.timbre.dsp.ui.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowWidthClass {
    COMPACT, // < 600dp (standard portrait phones)
    MEDIUM,  // 600dp - 839dp (foldables, phone landscape, small tablets)
    EXPANDED // >= 840dp (large tablets, desktop)
}

@Composable
fun rememberWindowWidthClass(): WindowWidthClass {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    return when {
        screenWidth < 600.dp -> WindowWidthClass.COMPACT
        screenWidth < 840.dp -> WindowWidthClass.MEDIUM
        else -> WindowWidthClass.EXPANDED
    }
}

/**
 * Calculates responsive horizontal padding to center content comfortably on large screens.
 */
@Composable
fun wideContentPadding(
    compactPadding: Dp = 16.dp,
    mediumPadding: Dp = 32.dp,
    expandedPadding: Dp = 64.dp
): PaddingValues {
    val widthClass = rememberWindowWidthClass()
    val horizontal = when (widthClass) {
        WindowWidthClass.COMPACT -> compactPadding
        WindowWidthClass.MEDIUM -> mediumPadding
        WindowWidthClass.EXPANDED -> expandedPadding
    }
    return PaddingValues(horizontal = horizontal)
}

/**
 * Modifier to apply responsive maximum content width and centering for wide form factors.
 */
@Composable
fun Modifier.adaptiveContentPadding(): Modifier {
    val widthClass = rememberWindowWidthClass()
    val horizontalPadding = when (widthClass) {
        WindowWidthClass.COMPACT -> 0.dp
        WindowWidthClass.MEDIUM -> 24.dp
        WindowWidthClass.EXPANDED -> 48.dp
    }
    return this.padding(horizontal = horizontalPadding)
}
