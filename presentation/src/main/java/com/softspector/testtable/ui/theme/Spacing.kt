package com.softspector.testtable.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class Spacing(
    val extraLarge5: Dp = 72.dp,
    val extraLarge4: Dp = 64.dp,
    val extraLarge3: Dp = 56.dp,
    val extraLarge2: Dp = 48.dp,
    val extraLarge: Dp = 40.dp,
    val large: Dp = 32.dp,
    val medium: Dp = 24.dp,
    val small: Dp = 16.dp,
    val extraSmall: Dp = 12.dp,
    val extraSmall2: Dp = 8.dp,
    val extraSmall3: Dp = 4.dp,
    val extraSmall4: Dp = 2.dp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }
