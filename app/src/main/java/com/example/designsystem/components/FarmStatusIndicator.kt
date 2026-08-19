package com.example.designsystem.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.FarmSathiDesign
import com.example.designsystem.theme.FarmSemanticStatus
import com.example.designsystem.theme.FarmStatusTheme

/**
 * Reusable Status Indicator Badge across FarmSathi.
 * Supports all 7 core Semantic Statuses: Healthy, Moderate, Warning, Critical, Live, Offline, Cached.
 */
@Composable
fun FarmStatusBadge(
    status: FarmSemanticStatus,
    modifier: Modifier = Modifier,
    customLabel: String? = null,
    showIcon: Boolean = true
) {
    val spec = FarmStatusTheme.getVisualSpec(status)
    val displayLabel = customLabel ?: status.label

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_live")
    val livePulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Surface(
        color = spec.containerColor,
        shape = FarmSathiDesign.shapes.pill,
        modifier = modifier.border(1.dp, spec.borderColor, FarmSathiDesign.shapes.pill)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = FarmSathiDesign.spacing.spacing10,
                vertical = FarmSathiDesign.spacing.spacing4
            )
        ) {
            if (spec.isPulsing) {
                Box(
                    modifier = Modifier
                        .scale(livePulseScale)
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(spec.contentColor)
                )
                Spacer(modifier = Modifier.width(FarmSathiDesign.spacing.spacing6))
            } else if (showIcon) {
                Icon(
                    imageVector = spec.icon,
                    contentDescription = null,
                    tint = spec.contentColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(FarmSathiDesign.spacing.spacing6))
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(spec.contentColor)
                )
                Spacer(modifier = Modifier.width(FarmSathiDesign.spacing.spacing6))
            }

            Text(
                text = displayLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = spec.contentColor,
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Reusable Filter & Selection Chip with rural-accessible spacing and contrast.
 */
@Composable
fun FarmChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else if (selected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
        } else null,
        shape = FarmSathiDesign.shapes.large,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            iconColor = MaterialTheme.colorScheme.primary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier.defaultMinSize(minHeight = 36.dp)
    )
}
