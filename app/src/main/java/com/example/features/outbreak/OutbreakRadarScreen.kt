package com.example.features.outbreak

import androidx.compose.animation.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.models.AggregatedOutbreakReport
import com.example.data.models.FarmRiskStatus
import com.example.data.models.RegionalRiskLevel
import com.example.data.repository.FarmRepository
import com.example.designsystem.theme.FarmSathiDesign
import com.example.shared.components.FarmSathiTopBar
import com.example.ui.theme.*

@Composable
fun OutbreakRadarScreen(
    repository: FarmRepository,
    onBack: () -> Unit,
    onProtectMyFarm: () -> Unit = {},
    viewModel: OutbreakRadarViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val status = uiState.farmStatus

    Scaffold(
        topBar = {
            FarmSathiTopBar(
                title = "Community Disease Outbreak Radar",
                subtitle = "Crowdsourced & Satellite Early Warning",
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
                .testTag("outbreak_radar_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Dual Risk Hero Section (Your Farm Risk vs. Regional Risk)
            item {
                DualRiskHeroSection(
                    status = status,
                    onProtectMyFarm = onProtectMyFarm
                )
            }

            // 2. Protect My Farm Action Banner
            item {
                ProtectMyFarmActionCard(
                    onProtectMyFarm = onProtectMyFarm
                )
            }

            // 3. Farmer Privacy Protection Banner
            item {
                FarmerPrivacyCard()
            }

            // 4. Aggregated Regional Disease Reports List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REGIONAL OUTBREAK CLUSTERS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )

                    Surface(
                        shape = FarmSathiDesign.shapes.pill,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${uiState.reports.size} Active Clusters",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = FarmPrimaryGreen,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // 5. Outbreak Cluster Items
            items(uiState.reports) { report ->
                AggregatedOutbreakCard(
                    report = report,
                    onScanCrop = onProtectMyFarm
                )
            }
        }
    }
}

// ================= 1. DUAL RISK HERO SECTION =================

@Composable
private fun DualRiskHeroSection(
    status: FarmRiskStatus,
    onProtectMyFarm: () -> Unit
) {
    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.extraLarge)
            .testTag("dual_risk_hero_section")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = status.regionalRisk.color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RADAR SCAN • ${status.radarRadiusKm.toInt()} KM CORRIDOR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "Updated ${status.lastUpdated}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Side-by-side Risk Cards: Your Farm Risk vs Regional Risk
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // YOUR FARM RISK
                SingleRiskCard(
                    title = "YOUR FARM RISK",
                    level = status.yourFarmRisk,
                    reason = status.yourFarmRiskReason,
                    modifier = Modifier.weight(1f)
                )

                // REGIONAL RISK
                SingleRiskCard(
                    title = "REGIONAL CORRIDOR RISK",
                    level = status.regionalRisk,
                    reason = status.regionalRiskReason,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SingleRiskCard(
    title: String,
    level: RegionalRiskLevel,
    reason: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = FarmSathiDesign.shapes.large,
        color = level.containerColor,
        modifier = modifier
            .border(1.dp, level.color.copy(alpha = 0.5f), FarmSathiDesign.shapes.large)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = level.color,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Risk Level Badge (GREEN LOW, YELLOW MODERATE, RED HIGH)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(level.color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${level.colorCode} • ${level.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = level.color,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}

// ================= 2. PROTECT MY FARM ACTION CARD =================

@Composable
private fun ProtectMyFarmActionCard(
    onProtectMyFarm: () -> Unit
) {
    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = FarmPrimaryGreen),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("protect_my_farm_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Protect My Farm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Scan leaf samples now to catch early fungal spores before visible crop damage occurs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onProtectMyFarm,
                shape = FarmSathiDesign.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.testTag("protect_my_farm_button")
            ) {
                Text(
                    text = "Scan Crop",
                    color = FarmPrimaryGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ================= 3. FARMER PRIVACY CARD =================

@Composable
private fun FarmerPrivacyCard() {
    Surface(
        shape = FarmSathiDesign.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = FarmTechBlue,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Privacy Protected: Exact farm GPS plot boundaries and farmer identities are strictly anonymized into regional corridor clusters.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

// ================= 4. AGGREGATED OUTBREAK CARD =================

@Composable
private fun AggregatedOutbreakCard(
    report: AggregatedOutbreakReport,
    onScanCrop: () -> Unit
) {
    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                report.riskLevel.color.copy(alpha = 0.5f),
                FarmSathiDesign.shapes.extraLarge
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Disease Name & Risk Level Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.diseaseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = report.hindiDiseaseName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                RiskLevelBadge(level = report.riskLevel)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Aggregated Info Metadata Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Region Corridor", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    Text("${report.regionName} (${report.distanceKm} km)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Reports Count", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    Text("${report.reportCount} Verified Reports", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = FarmPrimaryGreen)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Spread & Timestamp Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "💨 Vector: ${report.spreadDirection}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )

                Text(
                    text = "Updated: ${report.lastUpdated}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Prevention Guidance Box
            Surface(
                shape = FarmSathiDesign.shapes.medium,
                color = report.riskLevel.containerColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🛡️ Prevention & Control Guidance:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = report.riskLevel.color,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = report.preventionGuidance,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button
            OutlinedButton(
                onClick = onScanCrop,
                shape = FarmSathiDesign.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Crop for ${report.affectedCrop}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun RiskLevelBadge(level: RegionalRiskLevel) {
    Surface(
        shape = FarmSathiDesign.shapes.pill,
        color = level.containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(level.color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${level.colorCode} • ${level.title}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = level.color,
                fontSize = 10.sp
            )
        }
    }
}
