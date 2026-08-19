package com.example.designsystem.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.FarmSathiDesign
import com.example.ui.theme.*

/**
 * Reusable Primary Button for high-priority actions across FarmSathi.
 * Enforces accessible 48dp+ touch target, clear contrast, loading spinner, and rounded design token.
 */
@Composable
fun FarmPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    testTag: String = "farm_primary_button"
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = FarmSathiDesign.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(
            horizontal = FarmSathiDesign.spacing.spacing20,
            vertical = FarmSathiDesign.spacing.spacing14
        ),
        modifier = modifier
            .testTag(testTag)
            .defaultMinSize(minHeight = 48.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.5.dp
            )
            Spacer(modifier = Modifier.width(FarmSathiDesign.spacing.spacing8))
            Text(
                text = "Processing...",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(FarmSathiDesign.spacing.spacing8))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(FarmSathiDesign.spacing.spacing8))
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Reusable Secondary / Tonal Button for secondary workflows (NPK check, Filters, View All).
 */
@Composable
fun FarmSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    testTag: String = "farm_secondary_button"
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        shape = FarmSathiDesign.shapes.large,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(
            horizontal = FarmSathiDesign.spacing.spacing16,
            vertical = FarmSathiDesign.spacing.spacing12
        ),
        modifier = modifier
            .testTag(testTag)
            .defaultMinSize(minHeight = 48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(FarmSathiDesign.spacing.spacing8))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

/**
 * Reusable Outlined Button with subtle border and crisp contrast.
 */
@Composable
fun FarmOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    testTag: String = "farm_outlined_button"
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = FarmSathiDesign.shapes.large,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = textColor
        ),
        contentPadding = PaddingValues(
            horizontal = FarmSathiDesign.spacing.spacing14,
            vertical = FarmSathiDesign.spacing.spacing10
        ),
        modifier = modifier
            .testTag(testTag)
            .defaultMinSize(minHeight = 44.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(FarmSathiDesign.spacing.spacing6))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Accessible Icon Button with customizable background container and border.
 */
@Composable
fun FarmIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    testTag: String = "farm_icon_button"
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .testTag(testTag)
            .size(44.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Dedicated FarmSathi AI Voice Assistant Microphone Button.
 * Includes gentle animation wave during listening.
 */
@Composable
fun FarmVoiceMicButton(
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "farm_voice_mic_button"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_alpha"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .scale(1.2f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = pulseAlpha))
            )
        }

        FloatingActionButton(
            onClick = onClick,
            containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .testTag(testTag)
                .size(48.dp)
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                contentDescription = "Voice Assistant",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
