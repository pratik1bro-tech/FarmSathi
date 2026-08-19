package com.example.features.logistics

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.LogisticsTrip
import com.example.data.repository.FarmRepository
import com.example.shared.components.*
import com.example.ui.theme.*

@Composable
fun LogisticsScreen(
    repository: FarmRepository,
    onBack: () -> Unit
) {
    val trips by repository.getLogisticsTrips().collectAsState(initial = emptyList())
    var bookedTrip by remember { mutableStateOf<LogisticsTrip?>(null) }

    Scaffold(
        topBar = {
            FarmSathiTopBar(
                title = "Smart Logistics & Shared Trucks",
                subtitle = "Pool Transport to Reduce Mandi Transit Costs",
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
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF512DA8), modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Save up to 45% on Transportation", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF311B92))
                            Text("Pool truck cargo space with neighboring farmers heading to the same Mandi yard.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4527A0))
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Available Mandi Shared Routes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(trips) { trip ->
                LogisticsTripCard(
                    trip = trip,
                    onBook = { bookedTrip = trip }
                )
            }
        }
    }

    bookedTrip?.let { trip ->
        AlertDialog(
            onDismissRequest = { bookedTrip = null },
            title = { Text("Book Shared Freight Slot") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Destination: ${trip.destinationMandi}", fontWeight = FontWeight.Bold)
                    Text("Vehicle: ${trip.truckType}")
                    Text("Driver: ${trip.driverName} (${trip.driverPhone})")
                    Text("Rate: ₹${trip.costPerQuintalInr} / Quintal", color = FarmGreenPrimary, fontWeight = FontWeight.Bold)
                    Text("Pickup Route: ${trip.routeStops.joinToString(" ➔ ")}")
                }
            },
            confirmButton = {
                Button(
                    onClick = { bookedTrip = null },
                    colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary)
                ) {
                    Text("Confirm Cargo Space")
                }
            },
            dismissButton = {
                TextButton(onClick = { bookedTrip = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun LogisticsTripCard(
    trip: LogisticsTrip,
    onBook: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(trip.destinationMandi, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Departs: ${trip.departureTime}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
                StatusBadge(text = "${trip.availableSpaceTons} Tons Free", type = StatusType.SUCCESS)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("🚚 ${trip.truckType} • Driver: ${trip.driverName}", style = MaterialTheme.typography.bodyMedium)
            Text("📍 Stops: ${trip.routeStops.joinToString(" ➔ ")}", style = MaterialTheme.typography.bodySmall, color = TextMuted)

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("₹${trip.costPerQuintalInr} / Quintal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = FarmGreenPrimary)
                    Text("${trip.pooledFarmersCount} farmers joined", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }

                Button(
                    onClick = onBook,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF512DA8)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Reserve Space")
                }
            }
        }
    }
}
