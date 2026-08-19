package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.designsystem.theme.*

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = FarmPrimaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1B4D36),
    onSecondaryContainer = Color(0xFFDFF6DD),
    tertiary = FarmHarvestGold,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF5D4000),
    onTertiaryContainer = Color(0xFFFFE082),
    background = FarmCanvasBgDark,
    onBackground = FarmTextDarkThemePrimary,
    surface = FarmSurfaceCardDark,
    onSurface = FarmTextDarkThemePrimary,
    surfaceVariant = FarmSurfaceVariantDark,
    onSurfaceVariant = FarmTextDarkThemeSecondary,
    outline = FarmBorderDark,
    outlineVariant = FarmDividerDark,
    error = FarmAlertRed,
    errorContainer = Color(0xFF5C1010),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = FarmPrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = FarmContainerGreen,
    onPrimaryContainer = FarmOnContainerDark,
    secondary = FarmPrimaryLight,
    onSecondary = Color.White,
    secondaryContainer = FarmAvatarBg,
    onSecondaryContainer = FarmOnContainerGreen,
    tertiary = FarmHarvestGold,
    onTertiary = Color.White,
    tertiaryContainer = FarmHarvestGoldContainer,
    onTertiaryContainer = Color(0xFF4A3800),
    background = FarmCanvasBgLight,
    onBackground = FarmTextDark,
    surface = FarmSurfaceCardLight,
    onSurface = FarmTextDark,
    surfaceVariant = FarmSurfaceVariantLight,
    onSurfaceVariant = FarmTextSecondary,
    outline = FarmBorderLight,
    outlineVariant = FarmDividerLight,
    error = FarmAlertRed,
    errorContainer = FarmAlertRedContainer,
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun FarmSathiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val spacingTokens = FarmSathiSpacingTokens()
    val shapeTokens = FarmSathiShapeTokens()
    val elevationTokens = FarmSathiElevationTokens()

    CompositionLocalProvider(
        LocalFarmSpacing provides spacingTokens,
        LocalFarmShapes provides shapeTokens,
        LocalFarmElevation provides elevationTokens
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// Alias for backwards compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    FarmSathiTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
