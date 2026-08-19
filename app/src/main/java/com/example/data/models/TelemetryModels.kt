package com.example.data.models

enum class DataConnectionState(
    val label: String,
    val hindiLabel: String,
    val description: String
) {
    LIVE("LIVE", "लाइव", "Live stream via backend gateway"),
    CACHED("CACHED", "कैश किया हुआ", "Cached from last successful sync"),
    OFFLINE("OFFLINE", "ऑफलाइन", "ESP32 node unreachable"),
    UNAVAILABLE("UNAVAILABLE", "अनुपलब्ध", "Sensor hardware disconnected")
}

enum class SensorHealthStatus(
    val label: String,
    val hindiLabel: String
) {
    OPTIMAL("Optimal", "सामान्य"),
    WARNING("Advisory", "सतर्कता"),
    CRITICAL("Critical", "गंभीर"),
    CALIBRATING("Calibrating", "कैलिब्रेशन")
}

enum class TelemetryTimeRange(
    val label: String,
    val hindiLabel: String
) {
    HOURS_24("24 Hours", "24 घंटे"),
    DAYS_7("7 Days", "7 दिन"),
    DAYS_30("30 Days", "30 दिन")
}

data class TelemetrySensorMetric(
    val id: String,
    val name: String,
    val hindiName: String,
    val currentValue: Double,
    val unit: String,
    val status: SensorHealthStatus,
    val lastUpdated: String,
    val freshness: String,
    val optimalRange: String,
    val iconType: String
)

data class TelemetryChartDataPoint(
    val timestampLabel: String,
    val value: Double,
    val minThreshold: Double? = null,
    val maxThreshold: Double? = null
)

data class Esp32NodeTelemetry(
    val nodeId: String = "ESP32-NODE-SANWER-01",
    val fieldName: String = "Field 2 (South Cotton Block)",
    val connectionState: DataConnectionState = DataConnectionState.LIVE,
    val lastSyncTime: String = "Today, 10:28 AM",
    val freshness: String = "Just now",
    val batteryPercent: Int = 92,
    val signalDbm: Int = -62, // Wi-Fi / LoRa RSSI
    val soilMoisturePercent: Double = 38.0,
    val temperatureCelsius: Double = 29.2,
    val humidityPercent: Double = 64.0,
    val soilPh: Double = 6.8,
    val nitrogenMgKg: Double = 142.0,
    val phosphorusMgKg: Double = 28.5,
    val potassiumMgKg: Double = 210.0,
    val sensors: List<TelemetrySensorMetric> = emptyList()
)
