package com.example.features.agrosathi.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.models.OrbState
import com.example.ui.theme.*
import kotlin.math.*

/**
 * Futuristic, original AI Intelligence Orb for FarmSathi.
 * Features multi-layered holographic particles, organic agricultural green & bio-tech blue
 * resonant rings, and fluid state animations for Idle, Listening, Thinking, Speaking, and Offline.
 */
@Composable
fun AnimatedFarmAiOrb(
    state: OrbState,
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_infinite")

    // Core pulsing scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = when (state) {
            OrbState.IDLE -> 0.94f
            OrbState.LISTENING -> 0.88f
            OrbState.THINKING -> 0.92f
            OrbState.SPEAKING -> 0.90f
            OrbState.OFFLINE -> 0.96f
        },
        targetValue = when (state) {
            OrbState.IDLE -> 1.06f
            OrbState.LISTENING -> 1.22f
            OrbState.THINKING -> 1.08f
            OrbState.SPEAKING -> 1.18f
            OrbState.OFFLINE -> 1.02f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OrbState.IDLE -> 2400
                    OrbState.LISTENING -> 600
                    OrbState.THINKING -> 1100
                    OrbState.SPEAKING -> 750
                    OrbState.OFFLINE -> 4000
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Primary orbital rotation
    val rotationDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OrbState.IDLE -> 12000
                    OrbState.LISTENING -> 4000
                    OrbState.THINKING -> 2000
                    OrbState.SPEAKING -> 5000
                    OrbState.OFFLINE -> 30000
                },
                easing = LinearEasing
            )
        ),
        label = "rotation"
    )

    // Secondary reverse rotation
    val reverseRotationDeg by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OrbState.IDLE -> 9000
                    OrbState.LISTENING -> 3500
                    OrbState.THINKING -> 1500
                    OrbState.SPEAKING -> 4200
                    OrbState.OFFLINE -> 25000
                },
                easing = LinearEasing
            )
        ),
        label = "reverse_rotation"
    )

    // Waveform wave oscillation for Speaking / Listening
    val waveOscillation by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_osc"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2f * 0.46f

            when (state) {
                OrbState.IDLE -> {
                    drawIdleOrb(
                        center = center,
                        baseRadius = baseRadius,
                        scale = pulseScale,
                        rotation = rotationDeg,
                        reverseRotation = reverseRotationDeg
                    )
                }
                OrbState.LISTENING -> {
                    drawListeningOrb(
                        center = center,
                        baseRadius = baseRadius,
                        scale = pulseScale,
                        rotation = rotationDeg,
                        waveFactor = waveOscillation
                    )
                }
                OrbState.THINKING -> {
                    drawThinkingOrb(
                        center = center,
                        baseRadius = baseRadius,
                        scale = pulseScale,
                        rotation = rotationDeg,
                        reverseRotation = reverseRotationDeg
                    )
                }
                OrbState.SPEAKING -> {
                    drawSpeakingOrb(
                        center = center,
                        baseRadius = baseRadius,
                        scale = pulseScale,
                        rotation = rotationDeg,
                        waveFactor = waveOscillation
                    )
                }
                OrbState.OFFLINE -> {
                    drawOfflineOrb(
                        center = center,
                        baseRadius = baseRadius,
                        scale = pulseScale,
                        rotation = rotationDeg
                    )
                }
            }
        }
    }
}

// ---------------- DRAWING IMPLEMENTATIONS ----------------

private fun DrawScope.drawIdleOrb(
    center: Offset,
    baseRadius: Float,
    scale: Float,
    rotation: Float,
    reverseRotation: Float
) {
    // 1. Ambient Glow Aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                FarmPrimaryGreen.copy(alpha = 0.28f),
                FarmTechBlue.copy(alpha = 0.12f),
                Color.Transparent
            ),
            center = center,
            radius = baseRadius * 2.2f * scale
        ),
        radius = baseRadius * 2.2f * scale,
        center = center
    )

    // 2. Concentric Orbiting Rings
    rotate(rotation, center) {
        drawCircle(
            color = FarmTechBlue.copy(alpha = 0.45f),
            radius = baseRadius * 1.55f,
            center = center,
            style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(25f, 15f, 10f, 15f), 0f)
            )
        )
    }

    rotate(reverseRotation, center) {
        drawCircle(
            color = FarmPrimaryLight.copy(alpha = 0.55f),
            radius = baseRadius * 1.3f,
            center = center,
            style = Stroke(
                width = 1.8.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 12f), 0f)
            )
        )
    }

    // 3. Glowing Core Sphere
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White,
                FarmPrimaryLight,
                FarmPrimaryGreen,
                FarmTechBlueDark
            ),
            center = center.copy(y = center.y - baseRadius * 0.2f),
            radius = baseRadius * scale
        ),
        radius = baseRadius * scale,
        center = center
    )

    // 4. Subtle Inner Bio-Synapse Ring
    drawCircle(
        color = Color.White.copy(alpha = 0.7f),
        radius = baseRadius * 0.45f * scale,
        center = center,
        style = Stroke(width = 1.5.dp.toPx())
    )
}

private fun DrawScope.drawListeningOrb(
    center: Offset,
    baseRadius: Float,
    scale: Float,
    rotation: Float,
    waveFactor: Float
) {
    // 1. Expanding Responsive Soundwave Ripples
    for (i in 1..3) {
        val rippleRadius = baseRadius * (1.2f + (i * 0.35f * waveFactor))
        drawCircle(
            color = FarmTechBlue.copy(alpha = (0.45f / i) * (1f - (waveFactor * 0.3f))),
            radius = rippleRadius,
            center = center,
            style = Stroke(width = (3.dp.toPx() / i))
        )
    }

    // 2. High Energy Sound Aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                FarmTechBlue.copy(alpha = 0.4f),
                FarmPrimaryLight.copy(alpha = 0.2f),
                Color.Transparent
            ),
            center = center,
            radius = baseRadius * 2.4f
        ),
        radius = baseRadius * 2.4f,
        center = center
    )

    // 3. Listening Core Sphere with Cyan Energy Pulse
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White,
                FarmTechBlueLight,
                FarmTechBlue,
                FarmPrimaryGreen
            ),
            center = center,
            radius = baseRadius * scale
        ),
        radius = baseRadius * scale,
        center = center
    )

    // 4. Voice Input Reactive Orbit Points
    rotate(rotation, center) {
        for (angle in 0 until 360 step 45) {
            val rad = Math.toRadians(angle.toDouble())
            val orbX = center.x + (baseRadius * 1.4f * cos(rad)).toFloat()
            val orbY = center.y + (baseRadius * 1.4f * sin(rad)).toFloat()
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(orbX, orbY)
            )
        }
    }
}

private fun DrawScope.drawThinkingOrb(
    center: Offset,
    baseRadius: Float,
    scale: Float,
    rotation: Float,
    reverseRotation: Float
) {
    // 1. Quantum Computing Synaptic Particles
    rotate(rotation, center) {
        drawCircle(
            color = FarmHarvestGold.copy(alpha = 0.6f),
            radius = baseRadius * 1.65f,
            center = center,
            style = Stroke(
                width = 2.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 20f, 5f, 20f), 0f)
            )
        )
    }

    rotate(reverseRotation, center) {
        drawCircle(
            color = FarmTechBlue.copy(alpha = 0.7f),
            radius = baseRadius * 1.35f,
            center = center,
            style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
            )
        )
    }

    // 2. Synaptic Aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                FarmHarvestGold.copy(alpha = 0.3f),
                FarmTechBlue.copy(alpha = 0.25f),
                Color.Transparent
            ),
            center = center,
            radius = baseRadius * 2.2f
        ),
        radius = baseRadius * 2.2f,
        center = center
    )

    // 3. Fast Kinetic Center Core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White,
                FarmHarvestGoldContainer,
                FarmTechBlue,
                FarmPrimaryGreenDark
            ),
            center = center,
            radius = baseRadius * scale
        ),
        radius = baseRadius * scale,
        center = center
    )
}

private fun DrawScope.drawSpeakingOrb(
    center: Offset,
    baseRadius: Float,
    scale: Float,
    rotation: Float,
    waveFactor: Float
) {
    // 1. Acoustic Waveform Rings
    val waveOffset = waveFactor * 12.dp.toPx()
    drawCircle(
        color = FarmPrimaryLight.copy(alpha = 0.5f),
        radius = baseRadius * 1.5f + waveOffset,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )
    drawCircle(
        color = FarmTechBlue.copy(alpha = 0.4f),
        radius = baseRadius * 1.25f + (waveOffset * 0.5f),
        center = center,
        style = Stroke(width = 2.5.dp.toPx())
    )

    // 2. Bio-Vocal Harmonic Core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White,
                FarmPrimaryLight,
                FarmPrimaryGreen,
                FarmTechBlue
            ),
            center = center.copy(y = center.y - 8f),
            radius = baseRadius * scale
        ),
        radius = baseRadius * scale,
        center = center
    )

    // 3. Rotating Audio Harmonizer
    rotate(rotation, center) {
        for (i in 0 until 4) {
            val angle = i * 90.0
            val rad = Math.toRadians(angle)
            val px = center.x + (baseRadius * 1.15f * cos(rad)).toFloat()
            val py = center.y + (baseRadius * 1.15f * sin(rad)).toFloat()
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = 3.5.dp.toPx(),
                center = Offset(px, py)
            )
        }
    }
}

private fun DrawScope.drawOfflineOrb(
    center: Offset,
    baseRadius: Float,
    scale: Float,
    rotation: Float
) {
    // Dim Amber Orbit
    drawCircle(
        color = FarmEarthBrown.copy(alpha = 0.35f),
        radius = baseRadius * 1.3f,
        center = center,
        style = Stroke(
            width = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    )

    // Offline Muted Core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                FarmEarthBrown.copy(alpha = 0.8f),
                Color.DarkGray,
                Color.Black
            ),
            center = center,
            radius = baseRadius * scale
        ),
        radius = baseRadius * scale,
        center = center
    )
}
