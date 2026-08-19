package com.example.features.outbreak

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.OutbreakAlert
import com.example.data.repository.FarmRepository
import com.example.shared.components.*
import com.example.ui.theme.*

@Composable
fun OutbreakRadarScreen(
    repository: FarmRepository,
    onBack: () -> Unit
) {
    val alerts by repository.getOutbreakAlerts().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            FarmSathiTopBar(
                title = "Community Outbreak Radar",
                subtitle = "Crowdsourced & Satellite Pest Early Warning",
                showBackButton = true,
                onBackClick = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Radar Visual Box
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF263238)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(CircleShape)
                                        .background(AlertRed.copy(alpha = 0.2f))
                                        .border(2.dp, AlertRed, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Radar, contentDescription = null, tint = AlertRed, modifier = Modifier.size(36.dp))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("RADAR RADIUS: 20 KM SCAN ACTIVE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("2 Outbreak Vectors Detected Nearby", color = Color(0xFFFF8A80), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Active Regional Pest & Disease Alerts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(alerts) { alert ->
                OutbreakAlertCard(alert = alert)
            }
        }
    }
}

@Composable
private fun OutbreakAlertCard(alert: OutbreakAlert) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (alert.riskLevel.contains("HIGH")) AlertRed else HarvestGold,
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(alert.diseaseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Crop: ${alert.crop} • Location: ${alert.reportedVillage}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
                StatusBadge(
                    text = "${alert.distanceKm} km away",
                    type = if (alert.riskLevel.contains("HIGH")) StatusType.DANGER else StatusType.WARNING
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBadge(text = alert.riskLevel, type = if (alert.riskLevel.contains("HIGH")) StatusType.DANGER else StatusType.WARNING)
                StatusBadge(text = "${alert.affectedFarmsCount} Farms Affected", type = StatusType.INFO)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("💨 Spread Vector: ${alert.spreadVelocity}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = TextDarkSecondary)

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = AlertRedContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("🛡️ Proactive Farmer Advisory:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = AlertRed)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(alert.recommendedAction, style = MaterialTheme.typography.bodySmall, color = Color(0xFF5C1010))
                }
            }
        }
    }
}
