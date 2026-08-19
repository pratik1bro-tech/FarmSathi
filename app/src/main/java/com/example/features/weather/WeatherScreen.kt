package com.example.features.weather

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.WeatherForecast
import com.example.data.repository.FarmRepository
import com.example.shared.components.*
import com.example.ui.theme.*

@Composable
fun WeatherScreen(
    repository: FarmRepository,
    onBack: () -> Unit
) {
    val weather by repository.getWeatherForecast().collectAsState(initial = WeatherForecast())

    Scaffold(
        topBar = {
            FarmSathiTopBar(
                title = "Agri Weather Intelligence",
                subtitle = "Microclimate & Spray Advisory Window",
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
            // Main Weather Hero Banner
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = FarmGreenPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(listOf(Color(0xFF00695C), Color(0xFF004D40)))
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Indore Mandi Region", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
                                Text("${weather.currentTempC.toInt()}°C", color = Color.White, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                                Text(weather.condition, color = Color.White, style = MaterialTheme.typography.titleSmall)
                            }
                            Icon(Icons.Default.WbSunny, contentDescription = null, tint = HarvestGold, modifier = Modifier.size(56.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            WeatherParamItem(label = "Humidity", value = "${weather.humidityPercent}%", icon = Icons.Default.WaterDrop)
                            WeatherParamItem(label = "Wind Speed", value = "${weather.windSpeedKmh} km/h", icon = Icons.Default.Air)
                            WeatherParamItem(label = "Rain Chance", value = "${weather.rainfallChancePercent}%", icon = Icons.Default.CloudQueue)
                        }
                    }
                }
            }

            // AI Spray Advisory Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FarmGreenContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sanitizer, contentDescription = null, tint = FarmGreenPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Foliar Spray Advisory Index",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = FarmOnGreenContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = weather.sprayAdvisoryStatus,
                            style = MaterialTheme.typography.bodyMedium,
                            color = FarmOnGreenContainer
                        )
                    }
                }
            }

            // 7-Day Forecast
            item {
                Text(
                    text = "7-Day Farm Weather Forecast",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(weather.dailyForecasts) { day ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(day.day, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
                        Text(day.condition, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = SkyWaterBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${day.rainProb}%", style = MaterialTheme.typography.bodySmall, color = SkyWaterBlue, fontWeight = FontWeight.Bold)
                        }
                        Text("${day.tempMax}° / ${day.tempMin}°", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherParamItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
    }
}
