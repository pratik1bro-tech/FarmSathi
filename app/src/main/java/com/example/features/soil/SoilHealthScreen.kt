package com.example.features.soil

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.models.*
import com.example.data.repository.FarmRepository
import com.example.designsystem.theme.FarmSathiDesign
import com.example.shared.components.FarmSathiTopBar
import com.example.ui.theme.*

@Composable
fun SoilHealthScreen(
    repository: FarmRepository,
    onBack: () -> Unit,
    onNavigateToAi: (() -> Unit)? = null,
    viewModel: SoilHealthViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val currentReport = state.currentReport ?: return

    Scaffold(
        topBar = {
            FarmSathiTopBar(
                title = "Soil & NPK Health Card",
                subtitle = currentReport.fieldName,
                showBackButton = true,
                onBackClick = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("soil_health_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Multiple Field Selector Horizontal Tabs
            item {
                FieldSelectorSection(
                    fields = state.fields,
                    selectedFieldId = state.selectedFieldId,
                    onSelectField = { viewModel.selectField(it) }
                )
            }

            // 2. Soil Health Score Hero Card
            item {
                SoilHealthScoreHeroCard(report = currentReport)
            }

            // 3. AI Soil Recommendation (What is wrong? Why? What should farmer do? Priority)
            item {
                AiSoilRecommendationCard(
                    recommendation = currentReport.recommendation,
                    onAskAi = { onNavigateToAi?.invoke() }
                )
            }

            // 4. Primary Nutrients Matrix (N, P, K, pH, Moisture, OC) with LOW / OPTIMAL / HIGH indicators
            item {
                Text(
                    text = "SOIL NUTRIENTS & ATTRIBUTES / पोषक तत्व",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    NutrientMetricRow(
                        metric = currentReport.nitrogen,
                        isSelected = state.selectedNutrientId == "nitrogen",
                        onClick = { viewModel.selectNutrient("nitrogen") }
                    )
                    NutrientMetricRow(
                        metric = currentReport.phosphorus,
                        isSelected = state.selectedNutrientId == "phosphorus",
                        onClick = { viewModel.selectNutrient("phosphorus") }
                    )
                    NutrientMetricRow(
                        metric = currentReport.potassium,
                        isSelected = state.selectedNutrientId == "potassium",
                        onClick = { viewModel.selectNutrient("potassium") }
                    )
                    NutrientMetricRow(
                        metric = currentReport.soilPh,
                        isSelected = state.selectedNutrientId == "ph",
                        onClick = { viewModel.selectNutrient("ph") }
                    )
                    NutrientMetricRow(
                        metric = currentReport.moisture,
                        isSelected = state.selectedNutrientId == "moisture",
                        onClick = { viewModel.selectNutrient("moisture") }
                    )
                    NutrientMetricRow(
                        metric = currentReport.organicCarbon,
                        isSelected = state.selectedNutrientId == "organic_carbon",
                        onClick = { viewModel.selectNutrient("organic_carbon") }
                    )
                }
            }

            // 5. Historical Trends Chart
            item {
                HistoricalSoilChartCard(
                    selectedNutrientId = state.selectedNutrientId,
                    historicalData = state.historicalData,
                    onSelectNutrient = { viewModel.selectNutrient(it) }
                )
            }
        }
    }
}

// ================= 1. FIELD SELECTOR SECTION =================

@Composable
private fun FieldSelectorSection(
    fields: List<FieldSoilReport>,
    selectedFieldId: String,
    onSelectField: (String) -> Unit
) {
    Column {
        Text(
            text = "SELECT FARM FIELD / खेत चुनें",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(fields) { field ->
                val isSelected = field.fieldId == selectedFieldId
                Surface(
                    onClick = { onSelectField(field.fieldId) },
                    shape = FarmSathiDesign.shapes.medium,
                    color = if (isSelected) FarmPrimaryGreen else MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .border(
                            1.dp,
                            if (isSelected) FarmPrimaryLight else MaterialTheme.colorScheme.outline,
                            FarmSathiDesign.shapes.medium
                        )
                        .testTag("field_tab_${field.fieldId}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSelected) "🌱" else "🌾",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = field.fieldName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${field.crop} • Score: ${field.healthScore}/100",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ================= 2. SOIL HEALTH SCORE HERO CARD =================

@Composable
private fun SoilHealthScoreHeroCard(report: FieldSoilReport) {
    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.extraLarge)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SOIL HEALTH INDEX",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = report.healthGrade,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FarmPrimaryGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Soil Type: ${report.soilType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Sampled: ${report.sampleDate} (${report.areaAcres} Acres)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }

                // Circular Health Score Gauge
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(FarmPrimaryContainer)
                        .border(3.dp, FarmPrimaryGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${report.healthScore}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = FarmPrimaryGreen
                        )
                        Text(
                            text = "/ 100",
                            style = MaterialTheme.typography.labelSmall,
                            color = FarmOnContainerGreen,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

// ================= 3. AI SOIL RECOMMENDATION CARD =================

@Composable
private fun AiSoilRecommendationCard(
    recommendation: SoilRecommendation,
    onAskAi: () -> Unit
) {
    val isHighPriority = recommendation.priority.contains("HIGH")
    val bannerBg = if (isHighPriority) FarmAlertRedContainer else FarmSuccessGreenContainer
    val bannerText = if (isHighPriority) FarmAlertRed else FarmSuccessGreen

    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (isHighPriority) FarmAlertRed.copy(alpha = 0.5f) else FarmPrimaryGreen.copy(alpha = 0.5f),
                FarmSathiDesign.shapes.extraLarge
            )
            .testTag("soil_ai_recommendation_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Priority Badge & Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = FarmSathiDesign.shapes.pill,
                    color = bannerBg
                ) {
                    Text(
                        text = recommendation.priority,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = bannerText,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = FarmPrimaryGreen,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. What is wrong?
            RecommendationSectionBlock(
                title = "WHAT IS WRONG? / क्या समस्या है?",
                content = recommendation.whatIsWrong,
                color = if (isHighPriority) FarmAlertRed else FarmHarvestGold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Why?
            RecommendationSectionBlock(
                title = "WHY? / कारण",
                content = recommendation.why,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. What should the farmer do?
            RecommendationSectionBlock(
                title = "WHAT SHOULD THE FARMER DO? / किसान क्या करें?",
                content = recommendation.whatShouldFarmerDo,
                color = FarmPrimaryGreen
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ICAR / Agronomic validation note
            Surface(
                shape = FarmSathiDesign.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚖️ ${recommendation.dosageValidationNote}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ask FarmSathi AI Button
            Button(
                onClick = onAskAi,
                shape = FarmSathiDesign.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = FarmTechBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ask FarmSathi AI for Customized Plan", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RecommendationSectionBlock(
    title: String,
    content: String,
    color: Color
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 17.sp
        )
    }
}

// ================= 4. NUTRIENT METRIC ROW WITH LOW / OPTIMAL / HIGH =================

@Composable
private fun NutrientMetricRow(
    metric: SoilNutrientMetric,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) FarmTechBlueContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) FarmTechBlue else MaterialTheme.colorScheme.outline,
                shape = FarmSathiDesign.shapes.large
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = metric.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = metric.hindiName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${metric.currentValue} ${metric.unit}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    NutrientIndicatorBadge(indicator = metric.indicator)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Benchmark Range & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = metric.statusMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Optimal: ${metric.minOptimal}–${metric.maxOptimal} ${metric.unit}",
                    style = MaterialTheme.typography.labelSmall,
                    color = FarmPrimaryGreen,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun NutrientIndicatorBadge(indicator: NutrientIndicator) {
    val (bgColor, textColor) = when (indicator) {
        NutrientIndicator.LOW -> FarmWarningAmberContainer to FarmWarningAmber
        NutrientIndicator.OPTIMAL -> FarmSuccessGreenContainer to FarmSuccessGreen
        NutrientIndicator.HIGH -> FarmTechBlueContainer to FarmTechBlue
    }

    Surface(
        shape = FarmSathiDesign.shapes.pill,
        color = bgColor
    ) {
        Text(
            text = indicator.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = 9.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// ================= 5. HISTORICAL SOIL CHART CARD =================

@Composable
private fun HistoricalSoilChartCard(
    selectedNutrientId: String,
    historicalData: List<SoilHistoryDataPoint>,
    onSelectNutrient: (String) -> Unit
) {
    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.extraLarge)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HISTORICAL NUTRIENT TREND",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${selectedNutrientId.replaceFirstChar { it.uppercase() }} Season Curve",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = FarmSathiDesign.shapes.pill,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "4 Months",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = FarmPrimaryGreen,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Chart
            SoilTrendLineChart(
                points = historicalData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
        }
    }
}

@Composable
private fun SoilTrendLineChart(
    points: List<SoilHistoryDataPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val min = points.minOf { it.value }
    val max = points.maxOf { it.value }
    val range = if (max - min == 0.0) 1.0 else max - min
    val primaryColor = FarmPrimaryGreen

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val paddingBottom = 20.dp.toPx()
        val chartHeight = h - paddingBottom
        val stepX = w / (points.size - 1).coerceAtLeast(1)

        // Draw horizontal grid lines
        for (i in 0..2) {
            val y = (chartHeight / 2f) * i
            drawLine(
                color = Color.LightGray.copy(alpha = 0.35f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
            )
        }

        val coords = points.mapIndexed { index, pt ->
            val x = index * stepX
            val norm = (pt.value - min) / range
            val y = chartHeight - (norm * (chartHeight * 0.75f)) - (chartHeight * 0.12f)
            Offset(x, y.toFloat())
        }

        // Fill path
        val fillPath = Path().apply {
            moveTo(coords.first().x, chartHeight)
            coords.forEach { lineTo(it.x, it.y) }
            lineTo(coords.last().x, chartHeight)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(FarmPrimaryLight.copy(alpha = 0.35f), FarmPrimaryGreen.copy(alpha = 0.02f)),
                startY = 0f,
                endY = chartHeight
            )
        )

        // Curve stroke
        val strokePath = Path().apply {
            moveTo(coords.first().x, coords.first().y)
            for (i in 1 until coords.size) {
                val prev = coords[i - 1]
                val curr = coords[i]
                val midX = (prev.x + curr.x) / 2f
                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
            }
        }

        drawPath(
            path = strokePath,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx())
        )

        // Point nodes
        coords.forEach { pt ->
            drawCircle(color = Color.White, radius = 4.dp.toPx(), center = pt)
            drawCircle(color = primaryColor, radius = 3.dp.toPx(), center = pt)
        }
    }
}
