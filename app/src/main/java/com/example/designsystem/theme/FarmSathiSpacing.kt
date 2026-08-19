package com.example.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized Spacing Token definitions for FarmSathi Design System.
 */
data class FarmSathiSpacingTokens(
    val spacing0: Dp = 0.dp,
    val spacing2: Dp = 2.dp,
    val spacing4: Dp = 4.dp,
    val spacing6: Dp = 6.dp,
    val spacing8: Dp = 8.dp,
    val spacing10: Dp = 10.dp,
    val spacing12: Dp = 12.dp,
    val spacing14: Dp = 14.dp,
    val spacing16: Dp = 16.dp,
    val spacing18: Dp = 18.dp,
    val spacing20: Dp = 20.dp,
    val spacing24: Dp = 24.dp,
    val spacing28: Dp = 28.dp,
    val spacing32: Dp = 32.dp,
    val spacing40: Dp = 40.dp,
    val spacing48: Dp = 48.dp,
    val spacing56: Dp = 56.dp,
    val spacing64: Dp = 64.dp
)

/**
 * Centralized Radius and Shape Token definitions.
 */
data class FarmSathiShapeTokens(
    val none: Shape = RoundedCornerShape(0.dp),
    val extraSmall: Shape = RoundedCornerShape(6.dp),
    val small: Shape = RoundedCornerShape(10.dp),
    val medium: Shape = RoundedCornerShape(16.dp),
    val large: Shape = RoundedCornerShape(20.dp),
    val extraLarge: Shape = RoundedCornerShape(28.dp),
    val pill: Shape = RoundedCornerShape(50.dp)
)

/**
 * Centralized Elevation Tokens.
 */
data class FarmSathiElevationTokens(
    val none: Dp = 0.dp,
    val level1: Dp = 1.dp,
    val level2: Dp = 2.dp,
    val level3: Dp = 4.dp,
    val level4: Dp = 8.dp,
    val level5: Dp = 12.dp
)

val LocalFarmSpacing = compositionLocalOf { FarmSathiSpacingTokens() }
val LocalFarmShapes = compositionLocalOf { FarmSathiShapeTokens() }
val LocalFarmElevation = compositionLocalOf { FarmSathiElevationTokens() }

object FarmSathiDesign {
    val spacing: FarmSathiSpacingTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalFarmSpacing.current

    val shapes: FarmSathiShapeTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalFarmShapes.current

    val elevation: FarmSathiElevationTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalFarmElevation.current
}
