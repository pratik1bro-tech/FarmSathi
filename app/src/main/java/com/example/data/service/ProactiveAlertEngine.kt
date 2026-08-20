package com.example.data.service

import com.example.core.navigation.Screen
import com.example.data.models.AlertCategory
import com.example.data.models.AlertPriority
import com.example.data.models.ProactiveFarmAlert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProactiveAlertEngine {

    private val _alertsFlow = MutableStateFlow(
        listOf(
            // 1. Prompt Example 1: Soil Moisture / Irrigation
            ProactiveFarmAlert(
                id = "alert_01",
                title = "Soil Moisture Depletion Warning",
                description = "Your soil moisture in Field 2 has fallen below the recommended range.",
                category = AlertCategory.IRRIGATION,
                priority = AlertPriority.HIGH,
                timestamp = "10 mins ago",
                recommendedAction = "Execute 25mm evening drip cycle or toggle solar pump.",
                relatedField = "Field 2 (South Cotton)",
                deepLinkRoute = Screen.SmartIrrigation.route,
                isRead = false,
                fcmMessageId = "fcm_msg_soil_01"
            ),

            // 2. Prompt Example 2: Market Opportunity
            ProactiveFarmAlert(
                id = "alert_02",
                title = "Mandi Price Surge Detected",
                description = "Soybean prices increased 3.2% today.",
                category = AlertCategory.MARKET_OPPORTUNITY,
                priority = AlertPriority.MEDIUM,
                timestamp = "32 mins ago",
                recommendedAction = "Compare Sell Now vs Hold targets in Market Intelligence.",
                relatedField = "All Fields (Soybean Main)",
                deepLinkRoute = Screen.Market.route,
                isRead = false,
                fcmMessageId = "fcm_msg_mandi_02"
            ),

            // 3. Prompt Example 3: High Humidity / Disease Risk
            ProactiveFarmAlert(
                id = "alert_03",
                title = "Fungal Infection Microclimate Advisory",
                description = "High humidity is expected tomorrow. Consider inspecting your soybean crop.",
                category = AlertCategory.DISEASE_RISK,
                priority = AlertPriority.HIGH,
                timestamp = "1 hour ago",
                recommendedAction = "Scan leaf samples with AI Crop Doctor or inspect Community Outbreak Radar.",
                relatedField = "Field 1 (Soybean Block A)",
                deepLinkRoute = Screen.DiseaseDetection.route,
                isRead = false,
                fcmMessageId = "fcm_msg_humidity_03"
            ),

            // 4. Critical Weather Risk
            ProactiveFarmAlert(
                id = "alert_04",
                title = "Severe Unseasonal Hail & Wind Alert",
                description = "Heavy squall line forecasted in Sanwer corridor within 18 hours.",
                category = AlertCategory.CRITICAL,
                priority = AlertPriority.CRITICAL,
                timestamp = "2 hours ago",
                recommendedAction = "Secure harvested produce tarpaulins and clear perimeter drainage.",
                relatedField = "All Farm Parcels",
                deepLinkRoute = Screen.WeatherIntelligence.route,
                isRead = false,
                fcmMessageId = "fcm_msg_storm_04"
            ),

            // 5. Soil Health Category
            ProactiveFarmAlert(
                id = "alert_05",
                title = "Root Zone Nitrogen Deficit",
                description = "Nitrogen level in Block C Gram plot fell to 180 kg/ha.",
                category = AlertCategory.SOIL,
                priority = AlertPriority.MEDIUM,
                timestamp = "4 hours ago",
                recommendedAction = "Apply 12kg/acre Urea fertigation during next drip run.",
                relatedField = "Field 3 (Block C Gram)",
                deepLinkRoute = Screen.SoilHealth.route,
                isRead = true,
                fcmMessageId = "fcm_msg_soil_05"
            ),

            // 6. Harvest Readiness
            ProactiveFarmAlert(
                id = "alert_06",
                title = "Optimum Harvest Readiness Window",
                description = "Soybean pod dry-down reached 85% maturity index in Block A.",
                category = AlertCategory.HARVEST,
                priority = AlertPriority.MEDIUM,
                timestamp = "Yesterday",
                recommendedAction = "Check harvest readiness forecast and pre-book combine harvester.",
                relatedField = "Field 1 (Soybean Block A)",
                deepLinkRoute = Screen.DigitalTwin.route,
                isRead = true,
                fcmMessageId = "fcm_msg_harvest_06"
            ),

            // 7. Logistics Category
            ProactiveFarmAlert(
                id = "alert_07",
                title = "Shared Mandi Freight Pool Available",
                description = "Shared LCV truck available for Indore Mandi from Sanwer Corridor.",
                category = AlertCategory.LOGISTICS,
                priority = AlertPriority.LOW,
                timestamp = "Yesterday",
                recommendedAction = "Join shared transport pool to save 42% freight costs.",
                relatedField = "All Farm Parcels",
                deepLinkRoute = Screen.Logistics.route,
                isRead = true,
                fcmMessageId = "fcm_msg_logistics_07"
            )
        )
    )

    fun getAlerts(): Flow<List<ProactiveFarmAlert>> = _alertsFlow.asStateFlow()

    fun markAsRead(alertId: String) {
        _alertsFlow.update { list ->
            list.map { if (it.id == alertId) it.copy(isRead = true) else it }
        }
    }

    fun dismissAlert(alertId: String) {
        _alertsFlow.update { list ->
            list.filterNot { it.id == alertId }
        }
    }

    fun injectIncomingBackendFcmPayload(alert: ProactiveFarmAlert) {
        _alertsFlow.update { listOf(alert) + it }
    }
}
