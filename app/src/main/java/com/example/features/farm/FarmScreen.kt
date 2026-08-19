package com.example.features.farm

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.core.navigation.Screen
import com.example.data.models.CropField
import com.example.shared.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmScreen(
    viewModel: FarmViewModel,
    onNavigate: (Screen) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenLanguageSelector: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            FarmSathiTopBar(
                title = "My Farm",
                subtitle = "${state.totalAcres} Acres • ${state.fields.size} Crop Blocks",
                onNotificationsClick = onOpenNotifications,
                onLanguageClick = onOpenLanguageSelector
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.setAddFieldDialogVisible(true) },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Field") },
                text = { Text("Add Crop Field", fontWeight = FontWeight.Bold) },
                containerColor = CleanMinPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("add_field_fab")
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Farm Overview KPI Card (Clean Minimalism white card with 3 columns)
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanMinCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CleanMinBorder, RoundedCornerShape(28.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TOTAL AREA", style = MaterialTheme.typography.labelSmall, color = CleanMinTextMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${state.totalAcres} Ac", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CleanMinPrimary)
                        }
                        Divider(modifier = Modifier.height(40.dp).width(1.dp), color = CleanMinDivider)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("AVG HEALTH", style = MaterialTheme.typography.labelSmall, color = CleanMinTextMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${state.averageHealth}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CleanMinPrimaryLight)
                        }
                        Divider(modifier = Modifier.height(40.dp).width(1.dp), color = CleanMinDivider)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("IOT SENSORS", style = MaterialTheme.typography.labelSmall, color = CleanMinTextMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("4 Active", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CleanMinBluePrimary)
                        }
                    }
                }
            }

            // Quick Action Row (3D Digital Twin & Leaf Doctor)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onNavigate(Screen.DigitalTwin) },
                        colors = ButtonDefaults.buttonColors(containerColor = CleanMinContainer, contentColor = CleanMinOnContainerDark),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("3D Digital Twin", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onNavigate(Screen.DiseaseDetection) },
                        colors = ButtonDefaults.buttonColors(containerColor = CleanMinPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Leaf Doctor", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Active Crop Blocks
            item {
                Text(
                    text = "Active Crop Blocks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = CleanMinTextDark
                )
            }

            items(state.fields) { field ->
                FieldDetailCard(
                    field = field,
                    onOpenDigitalTwin = { onNavigate(Screen.DigitalTwin) },
                    onOpenSoil = { onNavigate(Screen.SoilHealth) },
                    onOpenIrrigation = { onNavigate(Screen.SmartIrrigation) }
                )
            }
        }
    }

    if (state.showAddFieldDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setAddFieldDialogVisible(false) },
            shape = RoundedCornerShape(28.dp),
            containerColor = CleanMinCard,
            title = { Text("Add New Crop Field", fontWeight = FontWeight.Bold, color = CleanMinTextDark) },
            text = {
                Text("Configure a new farm block, select crop variety (Wheat, Mustard, Gram, Onion), assign IoT sensor node, and start digital monitoring.", color = CleanMinTextSecondary)
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.setAddFieldDialogVisible(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = CleanMinPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Save Field")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setAddFieldDialogVisible(false) }) {
                    Text("Cancel", color = CleanMinTextMuted)
                }
            }
        )
    }
}

@Composable
private fun FieldDetailCard(
    field: CropField,
    onOpenDigitalTwin: () -> Unit,
    onOpenSoil: () -> Unit,
    onOpenIrrigation: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = CleanMinCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CleanMinBorder, RoundedCornerShape(28.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = field.name.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = CleanMinTextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontSize = 10.sp
                    )
                    Text(
                        text = field.cropName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CleanMinTextDark
                    )
                }
                StatusBadge(
                    text = "${field.healthScore}% Health",
                    type = if (field.healthScore >= 90) StatusType.SUCCESS else StatusType.WARNING
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metrics row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CleanMinCardVariant)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("AREA", style = MaterialTheme.typography.labelSmall, color = CleanMinTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("${field.areaAcres} Acres", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = CleanMinTextDark)
                }
                Column {
                    Text("GROWTH STAGE", style = MaterialTheme.typography.labelSmall, color = CleanMinTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(field.growthStage, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = CleanMinTextDark)
                }
                Column {
                    Text("SOIL MOISTURE", style = MaterialTheme.typography.labelSmall, color = CleanMinTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("${field.soilMoisture}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = CleanMinPrimaryLight)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenSoil,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CleanMinBorder)
                ) {
                    Icon(Icons.Default.Spa, contentDescription = null, modifier = Modifier.size(14.dp), tint = CleanMinPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("NPK", fontSize = 11.sp, color = CleanMinTextDark)
                }
                OutlinedButton(
                    onClick = onOpenIrrigation,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CleanMinBorder)
                ) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(14.dp), tint = CleanMinPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Irrigation", fontSize = 11.sp, color = CleanMinTextDark)
                }
                Button(
                    onClick = onOpenDigitalTwin,
                    colors = ButtonDefaults.buttonColors(containerColor = CleanMinContainer, contentColor = CleanMinOnContainerDark),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Twin View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
