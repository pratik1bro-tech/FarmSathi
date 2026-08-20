package com.example.data.models

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class AlertCategory(
    val id: String,
    val title: String,
    val hindiTitle: String,
    val iconEmoji: String
) {
    CRITICAL("CRITICAL", "Critical", "आपातकालीन", "🚨"),
    DISEASE_RISK("DISEASE_RISK", "Disease Risk", "रोग जोखिम", "🦠"),
    WEATHER_RISK("WEATHER_RISK", "Weather Risk", "मौसम जोखिम", "⛈️"),
    MARKET_OPPORTUNITY("MARKET_OPPORTUNITY", "Market Opportunity", "बाजार अवसर", "📈"),
    IRRIGATION("IRRIGATION", "Irrigation", "सिंचाई परामर्श", "💧"),
    SOIL("SOIL", "Soil", "मृदा स्वास्थ्य", "🧪"),
    HARVEST("HARVEST", "Harvest", "फसल कटाई", "🌾"),
    LOGISTICS("LOGISTICS", "Logistics", "परिवहन/लॉजिस्टिक्स", "🚚")
}

enum class AlertPriority(
    val title: String,
    val color: Color,
    val containerColor: Color,
    val levelRank: Int
) {
    CRITICAL("CRITICAL", FarmAlertRed, FarmAlertRedContainer, 4),
    HIGH("HIGH", FarmWarningAmber, FarmWarningAmberContainer, 3),
    MEDIUM("MEDIUM", FarmTechBlue, FarmTechBlueContainer, 2),
    LOW("LOW", FarmSuccessGreen, FarmSuccessGreenContainer, 1)
}

data class ProactiveFarmAlert(
    val id: String,
    val title: String,
    val description: String,
    val category: AlertCategory,
    val priority: AlertPriority,
    val timestamp: String,
    val recommendedAction: String,
    val relatedField: String? = null,
    val deepLinkRoute: String,
    val isRead: Boolean = false,
    val fcmMessageId: String? = null
)
