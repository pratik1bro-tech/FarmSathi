package com.example.features.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FarmPreferences
import com.example.data.repository.FarmRepository
import com.example.designsystem.components.*
import com.example.designsystem.theme.FarmSathiDesign
import com.example.designsystem.theme.FarmSemanticStatus
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmSetupScreen(
    repository: FarmRepository,
    onFarmSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    val preferences = remember { FarmPreferences(context) }

    var farmName by remember { mutableStateOf("Patel Krishi Farm Block A") }
    var farmLocation by remember { mutableStateOf("Indore Mandi Region, Madhya Pradesh") }
    var farmAreaText by remember { mutableStateOf("4.5") }
    var selectedUnit by remember { mutableStateOf("Acres") }
    var selectedSoilType by remember { mutableStateOf("Black Cotton Soil (काली मिट्टी)") }
    var selectedPrimaryCrop by remember { mutableStateOf("Soybean (JS-2034)") }
    var sowingDate by remember { mutableStateOf("20 June 2026") }

    var areaExpanded by remember { mutableStateOf(false) }
    var soilExpanded by remember { mutableStateOf(false) }
    var cropExpanded by remember { mutableStateOf(false) }

    var validationError by remember { mutableStateOf<String?>(null) }

    val unitOptions = listOf("Acres", "Bigha (बीघा)", "Hectares")
    val soilOptions = listOf(
        "Black Cotton Soil (काली मिट्टी)",
        "Alluvial Soil (दोमट मिट्टी)",
        "Red Sandy Loam (लाल बलुई)",
        "Clay Loam (चिकनी दोमट)"
    )
    val cropOptions = listOf(
        "Soybean (JS-2034)",
        "Cotton (Bt Hybrid RCH-659)",
        "Wheat (Lokwan / Sharbati)",
        "Tomato (Hybrid Abhinav)",
        "Maize (Kharif Pioneer)",
        "Mustard (Pusa Bold)"
    )

    fun handleCompleteSetup() {
        if (farmName.trim().isBlank()) {
            validationError = "Please enter your farm name."
            return
        }
        if (farmLocation.trim().isBlank()) {
            validationError = "Please enter your farm village or district location."
            return
        }
        val areaVal = farmAreaText.toDoubleOrNull()
        if (areaVal == null || areaVal <= 0.0) {
            validationError = "Please enter a valid numeric farm area (e.g. 4.5)."
            return
        }

        validationError = null
        preferences.setFarmSetupCompleted(true)
        onFarmSetupComplete()
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("2/2", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Farm & Field Setup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Step 2: Land, Soil & Primary Crop",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = FarmSathiDesign.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.extraLarge)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(FarmSathiDesign.shapes.small)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Agriculture,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Farm Identity & Location",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Used for microclimate weather & IoT node pairing",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Farm Name
                        Text(
                            text = "FARM / PLOT NAME *",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = farmName,
                            onValueChange = { farmName = it },
                            placeholder = { Text("e.g. Patel Krishi Farm Block A") },
                            leadingIcon = { Icon(Icons.Default.Landscape, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            singleLine = true,
                            shape = FarmSathiDesign.shapes.large,
                            modifier = Modifier.fillMaxWidth().testTag("setup_farm_name_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Farm Location
                        Text(
                            text = "LOCATION (VILLAGE / MANDI REGION) *",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = farmLocation,
                            onValueChange = { farmLocation = it },
                            placeholder = { Text("e.g. Indore Mandi Region, MP") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            singleLine = true,
                            shape = FarmSathiDesign.shapes.large,
                            modifier = Modifier.fillMaxWidth().testTag("setup_farm_location_input")
                        )
                    }
                }
            }

            // Farm Land & Soil Specs
            item {
                Card(
                    shape = FarmSathiDesign.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.extraLarge)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(FarmSathiDesign.shapes.small)
                                    .background(FarmHarvestGoldContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SquareFoot,
                                    contentDescription = null,
                                    tint = FarmHarvestGold,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Area & Soil Parameters",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Powers precise NPK dosing & water calculations",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Area + Unit Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(
                                    text = "TOTAL AREA *",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = farmAreaText,
                                    onValueChange = { farmAreaText = it },
                                    placeholder = { Text("4.5") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = FarmSathiDesign.shapes.large,
                                    modifier = Modifier.fillMaxWidth().testTag("setup_farm_area_input")
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "UNIT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                ExposedDropdownMenuBox(
                                    expanded = areaExpanded,
                                    onExpandedChange = { areaExpanded = !areaExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = selectedUnit,
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = areaExpanded) },
                                        shape = FarmSathiDesign.shapes.large,
                                        modifier = Modifier.menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = areaExpanded,
                                        onDismissRequest = { areaExpanded = false }
                                    ) {
                                        unitOptions.forEach { unit ->
                                            DropdownMenuItem(
                                                text = { Text(unit) },
                                                onClick = {
                                                    selectedUnit = unit
                                                    areaExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Soil Type Dropdown
                        Text(
                            text = "SOIL TYPE / मिट्टी का प्रकार *",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        ExposedDropdownMenuBox(
                            expanded = soilExpanded,
                            onExpandedChange = { soilExpanded = !soilExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedSoilType,
                                onValueChange = {},
                                readOnly = true,
                                leadingIcon = { Icon(Icons.Default.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = soilExpanded) },
                                shape = FarmSathiDesign.shapes.large,
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = soilExpanded,
                                onDismissRequest = { soilExpanded = false }
                            ) {
                                soilOptions.forEach { soil ->
                                    DropdownMenuItem(
                                        text = { Text(soil) },
                                        onClick = {
                                            selectedSoilType = soil
                                            soilExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Primary Crop & Sowing Date
            item {
                Card(
                    shape = FarmSathiDesign.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, FarmSathiDesign.shapes.extraLarge)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(FarmSathiDesign.shapes.small)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Active Crop & Season",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Determines yield forecast & mandi advisory",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Primary Crop Dropdown
                        Text(
                            text = "PRIMARY CROP / मुख्य फसल *",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        ExposedDropdownMenuBox(
                            expanded = cropExpanded,
                            onExpandedChange = { cropExpanded = !cropExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedPrimaryCrop,
                                onValueChange = {},
                                readOnly = true,
                                leadingIcon = { Icon(Icons.Default.Grass, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cropExpanded) },
                                shape = FarmSathiDesign.shapes.large,
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = cropExpanded,
                                onDismissRequest = { cropExpanded = false }
                            ) {
                                cropOptions.forEach { crop ->
                                    DropdownMenuItem(
                                        text = { Text(crop) },
                                        onClick = {
                                            selectedPrimaryCrop = crop
                                            cropExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Sowing Date
                        Text(
                            text = "SOWING DATE (बुवाई की तारीख)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = sowingDate,
                            onValueChange = { sowingDate = it },
                            leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            singleLine = true,
                            shape = FarmSathiDesign.shapes.large,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (validationError != null) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = validationError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Complete Farm Setup Button
            item {
                Spacer(modifier = Modifier.height(10.dp))
                FarmPrimaryButton(
                    text = "Complete Setup & Enter FarmSathi",
                    trailingIcon = Icons.Default.CheckCircle,
                    onClick = { handleCompleteSetup() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("complete_farm_setup_button")
                )
            }
        }
    }
}
