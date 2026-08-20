package com.example.features.digital_twin

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.models.FarmFieldParcel
import com.example.data.models.FieldHealthStatus
import com.example.data.models.WhatIfScenario
import com.example.data.models.WhatIfSimulationResult
import com.example.data.repository.FarmRepository
import com.example.designsystem.theme.FarmSathiDesign
import com.example.shared.components.FarmSathiTopBar
import com.example.ui.theme.*

@Composable
fun DigitalTwinScreen(
    repository: FarmRepository,
    onBack: () -> Unit,
    viewModel: DigitalTwinViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedField = uiState.selectedField
    val simulationResult = uiState.simulationResult

    Scaffold(
        topBar = {
            FarmSathiTopBar(
                title = "Farm Digital Twin & What-If Simulator",
                subtitle = "Multilayer Bio-Physical Field & Physics Model",
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
                .testTag("digital_twin_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Digital Twin Overview Hero Header
            item {
                DigitalTwinOverviewCard(selectedField = selectedField)
            }

            // 2. Interactive Farm Field Parcels Grid (Green = Healthy, Yellow = Warning, Red = Critical)
            item {
                Text(
                    text = "SELECT FARM FIELD PARCEL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.fields) { field ->
                        FieldParcelSelectorCard(
                            field = field,
                            isSelected = field.fieldId == selectedField.fieldId,
                            onSelect = { viewModel.selectField(field) }
                        )
                    }
                }
            }

            // 3. Detailed Telemetry Matrix for Selected Field
            item {
                FieldDetailedTelemetryCard(field = selectedField)
            }

            // 4. WHAT-IF SIMULATOR SECTION HEADER
            item {
                Text(
                    text = "WHAT-IF AGRONOMIC SIMULATOR",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }

            // 5. Scenario Selectors
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WhatIfScenario.values().forEach { scenario ->
                        val isSelected = scenario == uiState.selectedScenario
                        ScenarioOptionTile(
                            scenario = scenario,
                            isSelected = isSelected,
                            onSelect = { viewModel.selectScenario(scenario) }
                        )
                    }
                }
            }

            // 6. AI SIMULATION RESULT CARD (WITH MANDATORY DISCLAIMER)
            simulationResult?.let { result ->
                item {
                    AiSimulationResultCard(result = result)
                }
            }
        }
    }
}

// ================= 1. OVERVIEW HERO CARD =================

@Composable
private fun DigitalTwinOverviewCard(selectedField: FarmFieldParcel) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = FarmPrimaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FARM DIGITAL TWIN MODEL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(
                    shape = FarmSathiDesign.shapes.pill,
                    color = FarmSuccessGreenContainer
                ) {
                    Text(
                        text = "📡 ESP32 Mesh Live",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = FarmPrimaryGreen,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Simulating root zone percolation, canopy NDVI transpiration, and microclimate stress vectors for ${selectedField.fieldName}.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

// ================= 2. FIELD PARCEL SELECTOR CARD =================

@Composable
private fun FieldParcelSelectorCard(
    field: FarmFieldParcel,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val status = field.healthStatus

    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) status.containerColor else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .width(220.dp)
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) status.color else MaterialTheme.colorScheme.outline,
                shape = FarmSathiDesign.shapes.large
            )
            .testTag("field_card_${field.fieldId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = field.fieldName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = FarmSathiDesign.shapes.pill,
                    color = status.color
                ) {
                    Text(
                        text = "${status.iconEmoji} ${status.label}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${field.cropName} • ${field.areaAcres} Acres",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Field Attributes Matrix
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Moisture", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                    Text("${field.soilMoisturePct.toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }

                Column {
                    Text("Health", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                    Text("${field.healthScore}/100", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = status.color)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Risk", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                    Text(field.diseaseRisk, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ================= 3. FIELD DETAILED TELEMETRY CARD =================

@Composable
private fun FieldDetailedTelemetryCard(field: FarmFieldParcel) {
    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.extraLarge)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "INSPECTED FIELD TELEMETRY • ${field.fieldName.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryMetricTile("Crop Type", field.cropName, "🌱")
                TelemetryMetricTile("Growth Stage", field.growthStage, "🌾")
                TelemetryMetricTile("NDVI Index", "${field.ndviIndex}", "🍃")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryMetricTile("Soil Moisture", "${field.soilMoisturePct.toInt()}%", "💧")
                TelemetryMetricTile("Disease Risk", field.diseaseRisk, "🦠")
                TelemetryMetricTile("Sensor Status", field.sensorStatus, "📡")
            }
        }
    }
}

@Composable
private fun TelemetryMetricTile(
    label: String,
    value: String,
    icon: String
) {
    Column(modifier = Modifier.padding(4.dp)) {
        Text("$icon $label", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

// ================= 4. SCENARIO OPTION TILE =================

@Composable
private fun ScenarioOptionTile(
    scenario: WhatIfScenario,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) FarmPrimaryContainer else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) FarmPrimaryGreen else MaterialTheme.colorScheme.outline,
                shape = FarmSathiDesign.shapes.large
            )
            .testTag("scenario_tile_${scenario.name}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) FarmPrimaryGreen else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(scenario.iconEmoji, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scenario.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) FarmPrimaryGreen else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = scenario.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = FarmPrimaryGreen)
            )
        }
    }
}

// ================= 5. AI SIMULATION RESULT CARD =================

@Composable
private fun AiSimulationResultCard(result: WhatIfSimulationResult) {
    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, FarmTechBlue.copy(alpha = 0.5f), FarmSathiDesign.shapes.extraLarge)
            .testTag("ai_simulation_result_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // MANDATORY DISCLAIMER LABEL
            Surface(
                shape = FarmSathiDesign.shapes.pill,
                color = FarmTechBlueContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = FarmTechBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI SIMULATION — NOT A GUARANTEE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = FarmTechBlueText,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "SIMULATION OUTCOME • ${result.scenario.title.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Predicted Moisture & Crop Stress Matrix
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Predicted Moisture
                Surface(
                    shape = FarmSathiDesign.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Predicted Soil Moisture", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${result.predictedSoilMoisturePct.toInt()}%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = FarmTechBlue
                        )
                        Text(
                            text = result.moistureChangeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp
                        )
                    }
                }

                // Estimated Yield Impact
                Surface(
                    shape = FarmSathiDesign.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Estimated Yield Impact", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = result.yieldImpactLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = result.yieldImpactColor
                        )
                        Text(
                            text = result.cropStressLevel,
                            style = MaterialTheme.typography.labelSmall,
                            color = result.cropStressColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Assumptions List
            Text("Simulation Physics Assumptions:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(6.dp))

            result.assumptionsList.forEach { assumption ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("• ", fontWeight = FontWeight.Bold, color = FarmTechBlue, fontSize = 11.sp)
                    Text(
                        text = assumption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Uncertainty Margin
            Surface(
                shape = FarmSathiDesign.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Analytics, contentDescription = null, tint = FarmTechBlue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Uncertainty Margin: ${result.uncertaintyMargin}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI Actionable Advice
            Surface(
                shape = FarmSathiDesign.shapes.medium,
                color = FarmPrimaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 FarmSathi Advisory:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = FarmPrimaryGreen,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.AIAdvice,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
