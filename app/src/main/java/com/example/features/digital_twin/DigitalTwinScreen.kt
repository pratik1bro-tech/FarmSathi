package com.example.features.digital_twin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.models.DigitalTwinLayer
import com.example.data.repository.FarmRepository
import com.example.shared.components.*
import com.example.ui.theme.*

@Composable
fun DigitalTwinScreen(
    repository: FarmRepository,
    onBack: () -> Unit
) {
    val layers by repository.getDigitalTwinLayers().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            FarmSathiTopBar(
                title = "Farm Digital Twin (Block A)",
                subtitle = "Multilayer Bio-Physical Field Model",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF004D40)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Soybean JS-2034 Digital Twin", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            StatusBadge(text = "Synced with ESP32", type = StatusType.AI)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Simulating root zone transpiration, canopy NDVI, and water percolation layers.", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item {
                Text(
                    text = "Stratified Field & Soil Horizons",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(layers) { layer ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(layer.healthColorHex))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(layer.layerName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text(layer.status, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(layer.depthOrHeight, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = FarmGreenPrimary)
                            Text(layer.metricValue, style = MaterialTheme.typography.bodySmall, color = TextDarkSecondary)
                        }
                    }
                }
            }
        }
    }
}
