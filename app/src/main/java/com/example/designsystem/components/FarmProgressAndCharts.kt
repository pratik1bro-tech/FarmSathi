package com.example.designsystem.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.FarmSathiDesign
import com.example.designsystem.theme.FarmSemanticStatus
import com.example.designsystem.theme.FarmStatusTheme

/**
 * Reusable Linear Progress Bar with animated fill and semantic status color support.
 */
@Composable
fun FarmLinearProgress(
    progress: Float, // 0.0 to 1.0
    modifier: Modifier = Modifier,
    status: FarmSemanticStatus = FarmSemanticStatus.Healthy,
    height: Dp = 8.dp
) {
    val spec = FarmStatusTheme.getVisualSpec(status)
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "progress"
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(FarmSathiDesign.shapes.pill),
        color = spec.contentColor,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

/**
 * Reusable Circular Score Meter for Soil Health, Yield Forecast, or Crop Health.
 */
@Composable
fun FarmCircularScoreMeter(
    score: Int, // 0 to 100
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 90.dp,
    strokeWidth: Dp = 8.dp,
    status: FarmSemanticStatus = if (score >= 80) FarmSemanticStatus.Healthy else if (score >= 50) FarmSemanticStatus.Moderate else FarmSemanticStatus.Critical
) {
    val spec = FarmStatusTheme.getVisualSpec(status)
    val animatedProgress by animateFloatAsState(
        targetValue = (score / 100f).coerceIn(0f, 1f),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "circular_score"
    )
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val arcSize = Size(size.toPx() - stroke, size.toPx() - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)

            // Background Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Animated Foreground Arc
            drawArc(
                color = spec.contentColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$score%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp
            )
        }
    }
}

/**
 * Minimalist Sparkline / Trend Line Chart in Jetpack Compose.
 */
@Composable
fun FarmSparklineChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 2.5.dp
) {
    if (dataPoints.size < 2) return

    val minVal = dataPoints.minOrNull() ?: 0f
    val maxVal = dataPoints.maxOrNull() ?: 1f
    val range = if (maxVal - minVal == 0f) 1f else maxVal - minVal

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stepX = width / (dataPoints.size - 1)

        val path = Path()
        dataPoints.forEachIndexed { index, point ->
            val x = index * stepX
            val normalizedY = (point - minVal) / range
            val y = height - (normalizedY * (height - 12.dp.toPx())) - 6.dp.toPx()

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}

/**
 * Minimalist Bar Chart for 7-day rainfall or price trends.
 */
@Composable
fun FarmBarChart(
    values: List<Int>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    highlightIndex: Int? = null
) {
    val maxVal = (values.maxOrNull() ?: 100).coerceAtLeast(1)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEachIndexed { index, value ->
            val isHighlighted = index == highlightIndex
            val barHeightRatio = (value.toFloat() / maxVal).coerceIn(0.1f, 1f)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$value",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = if (isHighlighted) barColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(60.dp * barHeightRatio)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(if (isHighlighted) barColor else MaterialTheme.colorScheme.primaryContainer)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = labels.getOrElse(index) { "D$index" },
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
