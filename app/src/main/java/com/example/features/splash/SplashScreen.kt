package com.example.features.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FarmPreferences
import com.example.designsystem.theme.FarmSathiDesign
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    onNavigateToNext: (Boolean) -> Unit // returns true if onboarding is completed, false otherwise
) {
    val context = LocalContext.current
    val farmPreferences = remember { FarmPreferences(context) }

    // Animations
    val transition = rememberInfiniteTransition(label = "splash_infinite")
    
    // Wave / Furrow animation
    val fieldWavePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "field_wave"
    )

    // Particle orbit phase
    val particlePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_phase"
    )

    // Entry Animations
    val enterAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        enterAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
        // Splash dwell time
        delay(2200)
        val isCompleted = farmPreferences.isOnboardingCompleted()
        onNavigateToNext(isCompleted)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background Custom Canvas: Animated Farm Field Contours & AI Data Particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2f
            val centerY = canvasHeight / 2f

            // 1. Layered Farm Field Furrows / Horizon Curves
            val fieldBaseY = canvasHeight * 0.65f
            val waveRad = Math.toRadians(fieldWavePhase.toDouble())

            // Back Field Furrow
            val backFurrowPath = Path().apply {
                moveTo(0f, fieldBaseY + 40.dp.toPx())
                cubicTo(
                    canvasWidth * 0.3f, fieldBaseY + (sin(waveRad) * 15).toFloat(),
                    canvasWidth * 0.7f, fieldBaseY + 50.dp.toPx() + (cos(waveRad) * 15).toFloat(),
                    canvasWidth, fieldBaseY + 20.dp.toPx()
                )
            }
            drawPath(
                path = backFurrowPath,
                color = FarmPrimaryGreen.copy(alpha = 0.15f * enterAnim.value),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Mid Field Furrow
            val midFurrowPath = Path().apply {
                moveTo(0f, fieldBaseY + 90.dp.toPx())
                cubicTo(
                    canvasWidth * 0.35f, fieldBaseY + 60.dp.toPx() + (cos(waveRad + 1) * 20).toFloat(),
                    canvasWidth * 0.65f, fieldBaseY + 110.dp.toPx() + (sin(waveRad + 1) * 20).toFloat(),
                    canvasWidth, fieldBaseY + 70.dp.toPx()
                )
            }
            drawPath(
                path = midFurrowPath,
                color = FarmPrimaryGreen.copy(alpha = 0.25f * enterAnim.value),
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Front Field Furrow (Rich Harvest Contour)
            val frontFurrowPath = Path().apply {
                moveTo(0f, fieldBaseY + 150.dp.toPx())
                cubicTo(
                    canvasWidth * 0.4f, fieldBaseY + 130.dp.toPx() + (sin(waveRad + 2) * 18).toFloat(),
                    canvasWidth * 0.75f, fieldBaseY + 160.dp.toPx() + (cos(waveRad + 2) * 18).toFloat(),
                    canvasWidth, fieldBaseY + 135.dp.toPx()
                )
            }
            drawPath(
                path = frontFurrowPath,
                color = FarmPrimaryLight.copy(alpha = 0.35f * enterAnim.value),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            // 2. AI Data Particles (Orbiting dots connecting soil to leaf intelligence)
            val particleRadii = listOf(75.dp.toPx(), 95.dp.toPx(), 115.dp.toPx(), 60.dp.toPx())
            val particleAngles = listOf(0.0, 90.0, 180.0, 270.0)
            val particleColors = listOf(
                FarmTechBlue,
                FarmPrimaryLight,
                AiNeonGreen,
                FarmHarvestGold
            )

            particleAngles.forEachIndexed { index, baseAngle ->
                val currentAngleRad = Math.toRadians(baseAngle + particlePhase.toDouble() * (if (index % 2 == 0) 1 else -1))
                val radius = particleRadii[index]
                val px = centerX + (radius * cos(currentAngleRad)).toFloat()
                val py = (centerY - 40.dp.toPx()) + (radius * 0.55f * sin(currentAngleRad)).toFloat()

                // Particle Core
                drawCircle(
                    color = particleColors[index].copy(alpha = 0.85f * enterAnim.value),
                    radius = 3.5.dp.toPx(),
                    center = Offset(px, py)
                )

                // Particle Glow Halo
                drawCircle(
                    color = particleColors[index].copy(alpha = 0.25f * enterAnim.value),
                    radius = 8.dp.toPx(),
                    center = Offset(px, py)
                )

                // Subtle connection line to center AI node
                drawLine(
                    color = particleColors[index].copy(alpha = 0.15f * enterAnim.value),
                    start = Offset(centerX, centerY - 40.dp.toPx()),
                    end = Offset(px, py),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        // Foreground Brand Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .alpha(enterAnim.value)
                .scale(0.85f + 0.15f * enterAnim.value)
        ) {
            // Distinctive Agriculture + AI Emblem
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(FarmSathiDesign.shapes.extraLarge)
                    .background(
                        Brush.linearGradient(
                            listOf(FarmPrimaryGreen, FarmPrimaryDark)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Organic Leaf Icon
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = "Farm Leaf",
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )

                // Subtle AI Sparkle in top right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(FarmTechBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Powered",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logo Name: FarmSathi
            Text(
                text = "FarmSathi",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle: Your AI-Powered Farm Companion
            Text(
                text = "Your AI-Powered Farm Companion",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }

        // Bottom Platform Tag
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
                .alpha(enterAnim.value)
        ) {
            Text(
                text = "Smart Agriculture • IoT Telemetry • Mandi Intelligence",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
