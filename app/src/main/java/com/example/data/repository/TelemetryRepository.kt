package com.example.data.repository

import com.example.data.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

interface TelemetryRepository {
    fun getTelemetryStream(nodeId: String = "ESP32-NODE-SANWER-01"): Flow<Esp32NodeTelemetry>
    suspend fun getHistoricalTelemetry(sensorId: String, timeRange: TelemetryTimeRange): List<TelemetryChartDataPoint>
    suspend fun refreshTelemetry(): Result<Esp32NodeTelemetry>
    fun setConnectionState(state: DataConnectionState)
}

class DevelopmentTelemetryRepository : TelemetryRepository {

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    private val _telemetryState = MutableStateFlow(
        createControlledFixture(DataConnectionState.LIVE)
    )

    override fun getTelemetryStream(nodeId: String): Flow<Esp32NodeTelemetry> = _telemetryState.asStateFlow()

    override suspend fun refreshTelemetry(): Result<Esp32NodeTelemetry> {
        delay(400) // Simulated backend API roundtrip
        val updated = createControlledFixture(_telemetryState.value.connectionState)
        _telemetryState.value = updated
        return Result.success(updated)
    }

    override fun setConnectionState(state: DataConnectionState) {
        _telemetryState.value = createControlledFixture(state)
    }

    override suspend fun getHistoricalTelemetry(
        sensorId: String,
        timeRange: TelemetryTimeRange
    ): List<TelemetryChartDataPoint> {
        delay(200) // API delay

        return when (sensorId) {
            "soil_moisture" -> when (timeRange) {
                TelemetryTimeRange.HOURS_24 -> listOf(
                    TelemetryChartDataPoint("00:00", 42.0),
                    TelemetryChartDataPoint("04:00", 41.5),
                    TelemetryChartDataPoint("08:00", 40.0),
                    TelemetryChartDataPoint("12:00", 38.5),
                    TelemetryChartDataPoint("16:00", 38.0),
                    TelemetryChartDataPoint("20:00", 39.2)
                )
                TelemetryTimeRange.DAYS_7 -> listOf(
                    TelemetryChartDataPoint("Mon", 48.0),
                    TelemetryChartDataPoint("Tue", 46.0),
                    TelemetryChartDataPoint("Wed", 44.0),
                    TelemetryChartDataPoint("Thu", 41.0),
                    TelemetryChartDataPoint("Fri", 39.5),
                    TelemetryChartDataPoint("Sat", 38.0),
                    TelemetryChartDataPoint("Sun", 38.0)
                )
                TelemetryTimeRange.DAYS_30 -> listOf(
                    TelemetryChartDataPoint("Week 1", 52.0),
                    TelemetryChartDataPoint("Week 2", 47.0),
                    TelemetryChartDataPoint("Week 3", 42.0),
                    TelemetryChartDataPoint("Week 4", 38.0)
                )
            }

            "temperature" -> when (timeRange) {
                TelemetryTimeRange.HOURS_24 -> listOf(
                    TelemetryChartDataPoint("00:00", 22.4),
                    TelemetryChartDataPoint("04:00", 21.0),
                    TelemetryChartDataPoint("08:00", 26.5),
                    TelemetryChartDataPoint("12:00", 31.8),
                    TelemetryChartDataPoint("16:00", 30.5),
                    TelemetryChartDataPoint("20:00", 25.8)
                )
                TelemetryTimeRange.DAYS_7 -> listOf(
                    TelemetryChartDataPoint("Mon", 28.5),
                    TelemetryChartDataPoint("Tue", 29.1),
                    TelemetryChartDataPoint("Wed", 29.8),
                    TelemetryChartDataPoint("Thu", 30.2),
                    TelemetryChartDataPoint("Fri", 29.0),
                    TelemetryChartDataPoint("Sat", 28.8),
                    TelemetryChartDataPoint("Sun", 29.2)
                )
                TelemetryTimeRange.DAYS_30 -> listOf(
                    TelemetryChartDataPoint("Week 1", 27.5),
                    TelemetryChartDataPoint("Week 2", 28.2),
                    TelemetryChartDataPoint("Week 3", 29.0),
                    TelemetryChartDataPoint("Week 4", 29.2)
                )
            }

            "humidity" -> when (timeRange) {
                TelemetryTimeRange.HOURS_24 -> listOf(
                    TelemetryChartDataPoint("00:00", 78.0),
                    TelemetryChartDataPoint("04:00", 84.0),
                    TelemetryChartDataPoint("08:00", 70.0),
                    TelemetryChartDataPoint("12:00", 55.0),
                    TelemetryChartDataPoint("16:00", 58.0),
                    TelemetryChartDataPoint("20:00", 64.0)
                )
                TelemetryTimeRange.DAYS_7 -> listOf(
                    TelemetryChartDataPoint("Mon", 68.0),
                    TelemetryChartDataPoint("Tue", 65.0),
                    TelemetryChartDataPoint("Wed", 62.0),
                    TelemetryChartDataPoint("Thu", 60.0),
                    TelemetryChartDataPoint("Fri", 63.0),
                    TelemetryChartDataPoint("Sat", 66.0),
                    TelemetryChartDataPoint("Sun", 64.0)
                )
                TelemetryTimeRange.DAYS_30 -> listOf(
                    TelemetryChartDataPoint("Week 1", 72.0),
                    TelemetryChartDataPoint("Week 2", 68.0),
                    TelemetryChartDataPoint("Week 3", 65.0),
                    TelemetryChartDataPoint("Week 4", 64.0)
                )
            }

            "soil_ph" -> when (timeRange) {
                TelemetryTimeRange.HOURS_24 -> listOf(
                    TelemetryChartDataPoint("00:00", 6.8),
                    TelemetryChartDataPoint("06:00", 6.8),
                    TelemetryChartDataPoint("12:00", 6.8),
                    TelemetryChartDataPoint("18:00", 6.8)
                )
                TelemetryTimeRange.DAYS_7 -> listOf(
                    TelemetryChartDataPoint("Mon", 6.7),
                    TelemetryChartDataPoint("Tue", 6.7),
                    TelemetryChartDataPoint("Wed", 6.8),
                    TelemetryChartDataPoint("Thu", 6.8),
                    TelemetryChartDataPoint("Fri", 6.8),
                    TelemetryChartDataPoint("Sat", 6.8),
                    TelemetryChartDataPoint("Sun", 6.8)
                )
                TelemetryTimeRange.DAYS_30 -> listOf(
                    TelemetryChartDataPoint("Week 1", 6.6),
                    TelemetryChartDataPoint("Week 2", 6.7),
                    TelemetryChartDataPoint("Week 3", 6.8),
                    TelemetryChartDataPoint("Week 4", 6.8)
                )
            }

            "npk_nitrogen" -> when (timeRange) {
                TelemetryTimeRange.HOURS_24 -> listOf(
                    TelemetryChartDataPoint("00:00", 142.0),
                    TelemetryChartDataPoint("08:00", 142.0),
                    TelemetryChartDataPoint("16:00", 142.0)
                )
                TelemetryTimeRange.DAYS_7 -> listOf(
                    TelemetryChartDataPoint("Mon", 148.0),
                    TelemetryChartDataPoint("Tue", 146.0),
                    TelemetryChartDataPoint("Wed", 145.0),
                    TelemetryChartDataPoint("Thu", 144.0),
                    TelemetryChartDataPoint("Fri", 143.0),
                    TelemetryChartDataPoint("Sat", 142.0),
                    TelemetryChartDataPoint("Sun", 142.0)
                )
                TelemetryTimeRange.DAYS_30 -> listOf(
                    TelemetryChartDataPoint("Week 1", 160.0),
                    TelemetryChartDataPoint("Week 2", 152.0),
                    TelemetryChartDataPoint("Week 3", 146.0),
                    TelemetryChartDataPoint("Week 4", 142.0)
                )
            }

            "npk_phosphorus" -> when (timeRange) {
                TelemetryTimeRange.HOURS_24 -> listOf(
                    TelemetryChartDataPoint("00:00", 28.5),
                    TelemetryChartDataPoint("12:00", 28.5)
                )
                TelemetryTimeRange.DAYS_7 -> listOf(
                    TelemetryChartDataPoint("Mon", 30.0),
                    TelemetryChartDataPoint("Wed", 29.2),
                    TelemetryChartDataPoint("Fri", 28.8),
                    TelemetryChartDataPoint("Sun", 28.5)
                )
                TelemetryTimeRange.DAYS_30 -> listOf(
                    TelemetryChartDataPoint("Week 1", 32.0),
                    TelemetryChartDataPoint("Week 2", 30.5),
                    TelemetryChartDataPoint("Week 3", 29.0),
                    TelemetryChartDataPoint("Week 4", 28.5)
                )
            }

            else -> when (timeRange) {
                TelemetryTimeRange.HOURS_24 -> listOf(
                    TelemetryChartDataPoint("00:00", 210.0),
                    TelemetryChartDataPoint("12:00", 210.0)
                )
                TelemetryTimeRange.DAYS_7 -> listOf(
                    TelemetryChartDataPoint("Mon", 215.0),
                    TelemetryChartDataPoint("Wed", 212.0),
                    TelemetryChartDataPoint("Fri", 210.0),
                    TelemetryChartDataPoint("Sun", 210.0)
                )
                TelemetryTimeRange.DAYS_30 -> listOf(
                    TelemetryChartDataPoint("Week 1", 220.0),
                    TelemetryChartDataPoint("Week 2", 216.0),
                    TelemetryChartDataPoint("Week 3", 212.0),
                    TelemetryChartDataPoint("Week 4", 210.0)
                )
            }
        }
    }

    private fun createControlledFixture(connectionState: DataConnectionState): Esp32NodeTelemetry {
        val nowStr = "Today, 10:30 AM"
        val freshness = when (connectionState) {
            DataConnectionState.LIVE -> "Just now (Live)"
            DataConnectionState.CACHED -> "5 mins ago"
            DataConnectionState.OFFLINE -> "2 hours ago"
            DataConnectionState.UNAVAILABLE -> "Disconnected"
        }

        val sensors = listOf(
            TelemetrySensorMetric(
                id = "soil_moisture",
                name = "Soil Moisture",
                hindiName = "मिट्टी की नमी",
                currentValue = 38.0,
                unit = "%",
                status = if (connectionState == DataConnectionState.UNAVAILABLE) SensorHealthStatus.CRITICAL else SensorHealthStatus.WARNING,
                lastUpdated = nowStr,
                freshness = freshness,
                optimalRange = "45% – 65%",
                iconType = "moisture"
            ),
            TelemetrySensorMetric(
                id = "temperature",
                name = "Temperature",
                hindiName = "तापमान",
                currentValue = 29.2,
                unit = "°C",
                status = SensorHealthStatus.OPTIMAL,
                lastUpdated = nowStr,
                freshness = freshness,
                optimalRange = "22°C – 32°C",
                iconType = "temp"
            ),
            TelemetrySensorMetric(
                id = "humidity",
                name = "Humidity",
                hindiName = "आर्द्रता",
                currentValue = 64.0,
                unit = "%",
                status = SensorHealthStatus.OPTIMAL,
                lastUpdated = nowStr,
                freshness = freshness,
                optimalRange = "50% – 75%",
                iconType = "humidity"
            ),
            TelemetrySensorMetric(
                id = "soil_ph",
                name = "Soil pH",
                hindiName = "मिट्टी पीएच",
                currentValue = 6.8,
                unit = "pH",
                status = SensorHealthStatus.OPTIMAL,
                lastUpdated = nowStr,
                freshness = freshness,
                optimalRange = "6.5 – 7.5",
                iconType = "ph"
            ),
            TelemetrySensorMetric(
                id = "npk_nitrogen",
                name = "Nitrogen (N)",
                hindiName = "नाइट्रोजन (N)",
                currentValue = 142.0,
                unit = "mg/kg",
                status = SensorHealthStatus.OPTIMAL,
                lastUpdated = nowStr,
                freshness = freshness,
                optimalRange = "120 – 180 mg/kg",
                iconType = "npk_n"
            ),
            TelemetrySensorMetric(
                id = "npk_phosphorus",
                name = "Phosphorus (P)",
                hindiName = "फास्फोरस (P)",
                currentValue = 28.5,
                unit = "mg/kg",
                status = SensorHealthStatus.OPTIMAL,
                lastUpdated = nowStr,
                freshness = freshness,
                optimalRange = "20 – 40 mg/kg",
                iconType = "npk_p"
            ),
            TelemetrySensorMetric(
                id = "npk_potassium",
                name = "Potassium (K)",
                hindiName = "पोटेशियम (K)",
                currentValue = 210.0,
                unit = "mg/kg",
                status = SensorHealthStatus.OPTIMAL,
                lastUpdated = nowStr,
                freshness = freshness,
                optimalRange = "180 – 250 mg/kg",
                iconType = "npk_k"
            )
        )

        return Esp32NodeTelemetry(
            nodeId = "ESP32-NODE-SANWER-01",
            fieldName = "Field 2 (South Cotton Block)",
            connectionState = connectionState,
            lastSyncTime = nowStr,
            freshness = freshness,
            batteryPercent = if (connectionState == DataConnectionState.UNAVAILABLE) 0 else 92,
            signalDbm = if (connectionState == DataConnectionState.OFFLINE) -95 else -62,
            soilMoisturePercent = 38.0,
            temperatureCelsius = 29.2,
            humidityPercent = 64.0,
            soilPh = 6.8,
            nitrogenMgKg = 142.0,
            phosphorusMgKg = 28.5,
            potassiumMgKg = 210.0,
            sensors = sensors
        )
    }
}
