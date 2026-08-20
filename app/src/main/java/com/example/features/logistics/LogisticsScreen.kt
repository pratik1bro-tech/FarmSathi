package com.example.features.logistics

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
import com.example.data.models.LogisticsRouteRequest
import com.example.data.models.PooledFarmerInfo
import com.example.data.models.SharedTransportPool
import com.example.data.models.VehicleType
import com.example.data.repository.FarmRepository
import com.example.designsystem.theme.FarmSathiDesign
import com.example.shared.components.FarmSathiTopBar
import com.example.ui.theme.*

@Composable
fun LogisticsScreen(
    repository: FarmRepository,
    onBack: () -> Unit,
    viewModel: LogisticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activePool = uiState.activePool

    Scaffold(
        topBar = {
            FarmSathiTopBar(
                title = "Smart Logistics & Shared Freight",
                subtitle = "Pool Mandi Transport to Cut Transit Costs",
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
                .testTag("logistics_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Confirmation Banner
            if (uiState.isBookingConfirmed && uiState.confirmedBooking != null) {
                item {
                    BookingConfirmationBanner(
                        booking = uiState.confirmedBooking!!,
                        onDismiss = { viewModel.dismissBookingConfirmationBanner() }
                    )
                }
            }

            // 1. Logistics Trip Input & Overview Card
            item {
                LogisticsRouteOverviewCard(
                    request = uiState.routeRequest,
                    onQuantityChange = { viewModel.updateQuantity(it) },
                    onVehicleChange = { viewModel.updateVehicle(it) },
                    onDestinationChange = { viewModel.updateDestination(it) }
                )
            }

            // 2. Cost Comparison Banner (Normal vs. Shared vs. Savings)
            activePool?.let { pool ->
                item {
                    SharedCostSavingsSummaryCard(
                        pool = pool,
                        onJoinPool = { viewModel.openConfirmationDialog(pool) }
                    )
                }
            }

            // 3. Compatible Regional Farmers Pool Details (Privacy Protected)
            activePool?.let { pool ->
                item {
                    CompatibleFarmersPoolCard(
                        pool = pool,
                        onConfirmJoin = { viewModel.openConfirmationDialog(pool) }
                    )
                }
            }

            // 4. Alternative Shared Freight Routes
            if (uiState.alternativePools.isNotEmpty()) {
                item {
                    Text(
                        text = "ALTERNATIVE SHARED MANDI ROUTES",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                items(uiState.alternativePools) { altPool ->
                    AlternativeRouteCard(
                        pool = altPool,
                        onSelect = { viewModel.openConfirmationDialog(altPool) }
                    )
                }
            }
        }
    }

    // Farmer Confirmation Modal Dialog
    if (uiState.isConfirmationDialogOpen && activePool != null) {
        SharedTransportConfirmationDialog(
            pool = activePool,
            routeRequest = uiState.routeRequest,
            onDismiss = { viewModel.closeConfirmationDialog() },
            onConfirm = { viewModel.confirmSharedTransportBooking() }
        )
    }
}

// ================= 1. ROUTE OVERVIEW CARD =================

@Composable
private fun LogisticsRouteOverviewCard(
    request: LogisticsRouteRequest,
    onQuantityChange: (Double) -> Unit,
    onVehicleChange: (VehicleType) -> Unit,
    onDestinationChange: (String) -> Unit
) {
    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.extraLarge)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "CARGO & PICKUP SPECIFICATIONS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Pickup & Destination Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = FarmPrimaryGreen,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Farm / Pickup Point", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    Text(request.farmPickupAddress, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = FarmTechBlue,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Mandi Destination", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    Text(request.destinationMandi, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Crop & Quantity Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Crop & Harvest", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    Text(request.cropName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = FarmPrimaryGreen)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (request.quantityQuintals > 5.0) onQuantityChange(request.quantityQuintals - 5.0) },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text("-", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Text(
                        text = "${request.quantityQuintals.toInt()} Qtl",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )

                    IconButton(
                        onClick = { onQuantityChange(request.quantityQuintals + 5.0) },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text("+", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Vehicle Selector Chips
            Text("Preferred Transport Vehicle", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(VehicleType.values()) { veh ->
                    val isSelected = veh == request.preferredVehicle
                    FilterChip(
                        selected = isSelected,
                        onClick = { onVehicleChange(veh) },
                        label = {
                            Text("${veh.iconEmoji} ${veh.title}", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FarmPrimaryContainer,
                            selectedLabelColor = FarmPrimaryGreen
                        )
                    )
                }
            }
        }
    }
}

// ================= 2. SHARED COST SAVINGS SUMMARY CARD =================

@Composable
private fun SharedCostSavingsSummaryCard(
    pool: SharedTransportPool,
    onJoinPool: () -> Unit
) {
    Card(
        shape = FarmSathiDesign.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = FarmSuccessGreenContainer),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, FarmPrimaryGreen.copy(alpha = 0.5f), FarmSathiDesign.shapes.extraLarge)
            .testTag("cost_savings_summary_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = FarmSathiDesign.shapes.pill,
                    color = FarmPrimaryGreen
                ) {
                    Text(
                        text = "SAVE ${pool.savingsPercentage}% WITH SHARED POOL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "${pool.vehicle.iconEmoji} ${pool.vehicle.title}",
                    style = MaterialTheme.typography.labelSmall,
                    color = FarmOnContainerGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Way Cost Breakdown Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Normal Solo Cost", style = MaterialTheme.typography.labelSmall, color = FarmOnContainerGreen, fontSize = 10.sp)
                    Text(
                        text = "₹${pool.normalSoloCostInr.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Shared Freight Cost", style = MaterialTheme.typography.labelSmall, color = FarmOnContainerGreen, fontSize = 10.sp)
                    Text(
                        text = "₹${pool.sharedPoolCostInr.toInt()}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = FarmPrimaryGreen
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Estimated Savings", style = MaterialTheme.typography.labelSmall, color = FarmOnContainerGreen, fontSize = 10.sp)
                    Text(
                        text = "₹${pool.estimatedSavingsInr.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FarmPrimaryGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onJoinPool,
                shape = FarmSathiDesign.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = FarmPrimaryGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("join_shared_transport_button")
            ) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Join Shared Transport Pool", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ================= 3. COMPATIBLE FARMERS POOL CARD =================

@Composable
private fun CompatibleFarmersPoolCard(
    pool: SharedTransportPool,
    onConfirmJoin: () -> Unit
) {
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
                Column {
                    Text(
                        text = "MATCHED FARMERS IN YOUR CORRIDOR",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${pool.pickupRegionRadius} • ${pool.dispatchDate}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = FarmSathiDesign.shapes.pill,
                    color = FarmTechBlueContainer
                ) {
                    Text(
                        text = "${pool.currentBookedQuintals.toInt()} / ${pool.totalCapacityQuintals.toInt()} Qtl Filled",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = FarmTechBlueText,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Privacy Notice
            Surface(
                shape = FarmSathiDesign.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = FarmTechBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Privacy Protected: Exact farm location details of co-farmers are kept confidential. Only regional corridors are matched.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Farmers List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // User's own entry
                FarmerPoolRow(
                    name = "You (Your Farm)",
                    crop = pool.cropGroup,
                    quantity = "${pool.userQuantityQuintals.toInt()} Quintals",
                    region = "Sanwer Primary Hub",
                    isUser = true
                )

                // Co-farmers
                pool.pooledFarmers.forEach { farmer ->
                    FarmerPoolRow(
                        name = farmer.anonymousName,
                        crop = farmer.crop,
                        quantity = "${farmer.quantityQuintals.toInt()} Quintals",
                        region = "${farmer.pickupRegionLabel} (~${farmer.approximateDistanceKm} km)",
                        isUser = false
                    )
                }
            }
        }
    }
}

@Composable
private fun FarmerPoolRow(
    name: String,
    crop: String,
    quantity: String,
    region: String,
    isUser: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FarmSathiDesign.shapes.medium)
            .background(if (isUser) FarmPrimaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isUser) FarmPrimaryGreen else FarmTechBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isUser) "👤" else "🌾",
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text("$crop • $region", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            }
        }

        Text(quantity, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (isUser) FarmPrimaryGreen else MaterialTheme.colorScheme.onSurface)
    }
}

// ================= 4. ALTERNATIVE ROUTE CARD =================

@Composable
private fun AlternativeRouteCard(
    pool: SharedTransportPool,
    onSelect: () -> Unit
) {
    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.large)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(pool.destinationName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("${pool.vehicle.iconEmoji} ${pool.vehicle.title} • ${pool.totalDistanceKm.toInt()} km", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                Text("Shared: ₹${pool.sharedPoolCostInr.toInt()} (Save ₹${pool.estimatedSavingsInr.toInt()})", style = MaterialTheme.typography.labelSmall, color = FarmPrimaryGreen, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onSelect,
                shape = FarmSathiDesign.shapes.small,
                colors = ButtonDefaults.buttonColors(containerColor = FarmTechBlue),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Select", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ================= 5. CONFIRMATION BANNER =================

@Composable
private fun BookingConfirmationBanner(
    booking: SharedTransportPool,
    onDismiss: () -> Unit
) {
    Card(
        shape = FarmSathiDesign.shapes.large,
        colors = CardDefaults.cardColors(containerColor = FarmPrimaryGreen),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Shared Freight Reserved!", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Driver ${booking.driverName} (${booking.vehicleNumber}) will contact for pickup window.", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                }
            }

            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
            }
        }
    }
}

// ================= 6. FARMER CONFIRMATION DIALOG =================

@Composable
private fun SharedTransportConfirmationDialog(
    pool: SharedTransportPool,
    routeRequest: LogisticsRouteRequest,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = FarmPrimaryGreen, modifier = Modifier.size(32.dp))
        },
        title = {
            Text(
                text = "Confirm Shared Freight Join",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "You are requesting to join a shared transport route to ${pool.destinationName}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = FarmSathiDesign.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Destination: ${pool.destinationName}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("Vehicle: ${pool.vehicle.title} (${pool.vehicleNumber})", fontSize = 11.sp)
                        Text("Driver: ${pool.driverName} • ${pool.driverPhone}", fontSize = 11.sp)
                        Text("Cargo: ${routeRequest.quantityQuintals.toInt()} Qtl ${routeRequest.cropName}", fontSize = 11.sp)
                        Text("Shared Rate: ₹${pool.sharedPoolCostInr.toInt()} (Normal: ₹${pool.normalSoloCostInr.toInt()})", color = FarmPrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Surface(
                    shape = FarmSathiDesign.shapes.small,
                    color = FarmTechBlueContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = FarmTechBlue, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Confirmation required: No transport booking is finalized without your explicit authorization.",
                            style = MaterialTheme.typography.labelSmall,
                            color = FarmTechBlueText,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = FarmSathiDesign.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = FarmPrimaryGreen)
            ) {
                Text("Confirm Join Pool", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
