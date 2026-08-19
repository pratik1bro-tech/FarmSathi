package com.example.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Core Semantic Statuses required across the entire FarmSathi Platform.
 * Supports: Healthy, Moderate, Warning, Critical, Live, Offline, Cached
 */
enum class FarmSemanticStatus(val label: String) {
    Healthy("Healthy"),
    Moderate("Moderate"),
    Warning("Warning"),
    Critical("Critical"),
    Live("Live"),
    Offline("Offline"),
    Cached("Cached")
}

data class StatusVisualSpec(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val icon: ImageVector,
    val isPulsing: Boolean = false
)

object FarmStatusTheme {
    @Composable
    @ReadOnlyComposable
    fun getVisualSpec(status: FarmSemanticStatus, isDark: Boolean = isSystemInDarkTheme()): StatusVisualSpec {
        return when (status) {
            FarmSemanticStatus.Healthy -> StatusVisualSpec(
                containerColor = if (isDark) Color(0xFF133824) else Color(0xFFDFF6DD),
                contentColor = if (isDark) Color(0xFF81C784) else Color(0xFF2D6A4F),
                borderColor = if (isDark) Color(0xFF235A3B) else Color(0xFFC7EBC4),
                icon = Icons.Default.CheckCircle
            )
            FarmSemanticStatus.Moderate -> StatusVisualSpec(
                containerColor = if (isDark) Color(0xFF3D2E00) else Color(0xFFFFF0D4),
                contentColor = if (isDark) Color(0xFFFFD54F) else Color(0xFF8A6200),
                borderColor = if (isDark) Color(0xFF5E4700) else Color(0xFFFFE0A3),
                icon = Icons.Default.Info
            )
            FarmSemanticStatus.Warning -> StatusVisualSpec(
                containerColor = if (isDark) Color(0xFF45200D) else Color(0xFFFFE2D4),
                contentColor = if (isDark) Color(0xFFFF8A65) else Color(0xFFB54708),
                borderColor = if (isDark) Color(0xFF6E3314) else Color(0xFFFFC6AE),
                icon = Icons.Default.Warning
            )
            FarmSemanticStatus.Critical -> StatusVisualSpec(
                containerColor = if (isDark) Color(0xFF421010) else Color(0xFFFFDAD6),
                contentColor = if (isDark) Color(0xFFFF897D) else Color(0xFFBA1A1A),
                borderColor = if (isDark) Color(0xFF6B1B1B) else Color(0xFFFFB4AB),
                icon = Icons.Default.Error
            )
            FarmSemanticStatus.Live -> StatusVisualSpec(
                containerColor = if (isDark) Color(0xFF0F3A2C) else Color(0xFFE0F7EF),
                contentColor = if (isDark) Color(0xFF00E676) else Color(0xFF00875A),
                borderColor = if (isDark) Color(0xFF165C45) else Color(0xFFA3EBD1),
                icon = Icons.Default.Sensors,
                isPulsing = true
            )
            FarmSemanticStatus.Offline -> StatusVisualSpec(
                containerColor = if (isDark) Color(0xFF252B26) else Color(0xFFEAEFEA),
                contentColor = if (isDark) Color(0xFF9EABA1) else Color(0xFF5F6E65),
                borderColor = if (isDark) Color(0xFF38423A) else Color(0xFFD4DDD5),
                icon = Icons.Default.CloudOff
            )
            FarmSemanticStatus.Cached -> StatusVisualSpec(
                containerColor = if (isDark) Color(0xFF0C2B47) else Color(0xFFE8F3FF),
                contentColor = if (isDark) Color(0xFF64B5F6) else Color(0xFF0061A4),
                borderColor = if (isDark) Color(0xFF174570) else Color(0xFFBEDCFF),
                icon = Icons.Default.Sync
            )
        }
    }
}
