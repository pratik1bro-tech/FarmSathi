package com.example.features.buyers

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
import com.example.data.models.BuyerOffer
import com.example.data.repository.FarmRepository
import com.example.shared.components.*
import com.example.ui.theme.*

@Composable
fun BuyersScreen(
    repository: FarmRepository,
    onBack: () -> Unit
) {
    val buyerOffers by repository.getBuyerOffers().collectAsState(initial = emptyList())
    var contactedBuyer by remember { mutableStateOf<BuyerOffer?>(null) }

    Scaffold(
        topBar = {
            FarmSathiTopBar(
                title = "Farmer-to-Buyer Matching",
                subtitle = "Verified Direct Mandi Mills & Food Processors",
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
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SkyWaterBlue, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "All buyers are verified with valid APMC trading licenses and direct UPI escrow settlement.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(buyerOffers) { buyer ->
                BuyerDetailedCard(
                    buyer = buyer,
                    onContact = { contactedBuyer = buyer }
                )
            }
        }
    }

    contactedBuyer?.let { buyer ->
        AlertDialog(
            onDismissRequest = { contactedBuyer = null },
            title = { Text("Connect with ${buyer.buyerName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Company: ${buyer.buyerCompany}", fontWeight = FontWeight.SemiBold)
                    Text("Offered Price: ₹${buyer.offeredPricePerQuintal} / Quintal", color = FarmGreenPrimary, fontWeight = FontWeight.Bold)
                    Text("Requirement: ${buyer.quantityQuintals} Quintals of ${buyer.cropRequired}")
                    Text("Payment: ${buyer.paymentTerms}")
                    Text("Pickup: ${buyer.pickupLocation}")
                }
            },
            confirmButton = {
                Button(
                    onClick = { contactedBuyer = null },
                    colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary)
                ) {
                    Text("Confirm Deal / Call Buyer")
                }
            },
            dismissButton = {
                TextButton(onClick = { contactedBuyer = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun BuyerDetailedCard(
    buyer: BuyerOffer,
    onContact: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(buyer.buyerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (buyer.verified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, contentDescription = "Verified", tint = SkyWaterBlue, modifier = Modifier.size(18.dp))
                        }
                    }
                    Text(buyer.buyerCompany, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    Text("⭐ ${buyer.rating} Rating • ${buyer.distanceKm} km away", style = MaterialTheme.typography.bodySmall, color = HarvestGold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${buyer.offeredPricePerQuintal}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = FarmGreenPrimary
                    )
                    Text("/ Quintal", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Demands: ${buyer.quantityQuintals} Quintals • ${buyer.cropRequired}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text("Payment: ${buyer.paymentTerms}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            Text("Logistics: ${buyer.pickupLocation}", style = MaterialTheme.typography.bodySmall, color = TextMuted)

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onContact,
                colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Accept Offer & Coordinate")
            }
        }
    }
}
