package com.example.data.models

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

/**
 * Historical time filter horizons for price trends.
 */
enum class MarketTimePeriod(val label: String, val shortLabel: String, val daysCount: Int) {
    TODAY("Today", "24H", 1),
    SEVEN_DAYS("7 Days", "7D", 7),
    THIRTY_DAYS("30 Days", "30D", 30),
    SIX_MONTHS("6 Months", "6M", 180)
}

/**
 * Directional trend indicator.
 */
enum class MarketTrendDirection(val labelEn: String, val labelHi: String) {
    UP("Bullish (+)", "बढ़त (+)") ,
    DOWN("Bearish (-)", "गिरावट (-)"),
    STABLE("Stable (=)", "स्थिर (=)")
}

/**
 * Level of prediction uncertainty.
 */
enum class ForecastUncertaintyLevel(val label: String, val colorTag: String) {
    LOW("Low Volatility (±2%)", "LOW"),
    MODERATE("Moderate Uncertainty (±4-6%)", "MODERATE"),
    ELEVATED("High Volatility / Monsoon Uncertainty (±8-10%)", "HIGH")
}

/**
 * Crop definition in the market catalog.
 */
data class CropMarketItem(
    val id: String,
    val name: String,
    val hindiName: String,
    val variety: String,
    val unit: String = "₹ / Quintal",
    val isPrimaryCrop: Boolean = false
)

/**
 * Historical / Intra-day price coordinate for graphing.
 */
data class MarketHistoricalPoint(
    val label: String,
    val priceInr: Double,
    val volumeQuintals: Double,
    val timestampMillis: Long = 0L
)

/**
 * Summary metrics of the historical time window.
 */
data class PeriodPriceStats(
    val highPrice: Int,
    val lowPrice: Int,
    val avgPrice: Int,
    val changeAmount: Int,
    val changePercent: Double,
    val trend: MarketTrendDirection
)

/**
 * Real-time mandi price quotation for a crop.
 */
data class MandiPriceDetail(
    val cropId: String,
    val cropName: String,
    val mandiId: String,
    val mandiName: String,
    val mandiDistrict: String,
    val distanceKm: Double,
    val currentPricePerQuintal: Int,
    val yesterdayPricePerQuintal: Int,
    val priceChangeAmount: Int,
    val priceChangePercent: Double,
    val trend: MarketTrendDirection,
    val minPrice: Int,
    val maxPrice: Int,
    val modalPrice: Int,
    val todayArrivalsQuintals: Double,
    val lastUpdatedText: String,
    val estimatedTransportCostPerQtl: Int,
    val netEffectivePricePerQtl: Int,
    val isBestNetRealization: Boolean = false
)

/**
 * Nearby APMC mandi comparison point.
 */
data class NearbyMandiQuote(
    val mandiId: String,
    val mandiName: String,
    val district: String,
    val distanceKm: Double,
    val pricePerQuintal: Int,
    val priceDiffVsPrimary: Int,
    val changePercent: Double,
    val trend: MarketTrendDirection,
    val arrivalsQuintals: Double,
    val transportCostPerQtl: Int,
    val netRealizationPerQtl: Int,
    val isBestNetReturn: Boolean,
    val lastUpdated: String
)

/**
 * AI-driven forecast and market intelligence with explicit uncertainty bounds.
 */
data class AiMarketForecastIntelligence(
    val cropName: String,
    val targetHorizonLabel: String = "Next 7–15 Days",
    val expectedPriceRangeMin: Int,
    val expectedPriceRangeMax: Int,
    val projectedMedianPrice: Int,
    val confidencePercent: Int,
    val uncertaintySpreadInr: Int,
    val uncertaintyLevel: ForecastUncertaintyLevel,
    val primaryMarketDrivers: List<String>,
    val riskFactors: List<String>,
    val recommendationTitle: String,
    val recommendationSummary: String,
    val recommendationSummaryHi: String,
    val disclaimerText: String = "⚠️ Probabilistic Market Estimate: APMC prices fluctuate based on daily arrivals, international commodity indices, and local mill demand. Projections are AI-assisted estimates and are NOT guaranteed prices."
)

/**
 * Complete UI State for Market Intelligence.
 */
data class MarketIntelligenceState(
    val availableCrops: List<CropMarketItem> = emptyList(),
    val selectedCrop: CropMarketItem = CropMarketItem("soybean", "Soybean (Yellow JS-2034)", "सोयाबीन", "High Oil Content (JS-2034)", isPrimaryCrop = true),
    
    // Primary market quote
    val currentMandiPrice: MandiPriceDetail = MandiPriceDetail(
        cropId = "soybean",
        cropName = "Soybean (Yellow JS-2034)",
        mandiId = "mandi_indore",
        mandiName = "Indore APMC (Laxmibai Nagar)",
        mandiDistrict = "Indore, MP",
        distanceKm = 14.2,
        currentPricePerQuintal = 4850,
        yesterdayPricePerQuintal = 4620,
        priceChangeAmount = 230,
        priceChangePercent = 4.98,
        trend = MarketTrendDirection.UP,
        minPrice = 4550,
        maxPrice = 5020,
        modalPrice = 4850,
        todayArrivalsQuintals = 12450.0,
        lastUpdatedText = "Today, 11:15 AM • APMC Agmarknet",
        estimatedTransportCostPerQtl = 35,
        netEffectivePricePerQtl = 4815,
        isBestNetRealization = false
    ),
    
    // Historical price points & stats
    val selectedTimePeriod: MarketTimePeriod = MarketTimePeriod.SEVEN_DAYS,
    val historicalPoints: List<MarketHistoricalPoint> = emptyList(),
    val periodStats: PeriodPriceStats = PeriodPriceStats(5020, 4550, 4780, 230, 4.98, MarketTrendDirection.UP),
    
    // Nearby mandi comparisons (Indore, Dewas, Ujjain, Sanwer, Dhar - dynamic from backend)
    val nearbyMandiQuotes: List<NearbyMandiQuote> = emptyList(),
    
    // AI Forecast with uncertainty quantification
    val aiForecast: AiMarketForecastIntelligence = AiMarketForecastIntelligence(
        cropName = "Soybean",
        expectedPriceRangeMin = 4750,
        expectedPriceRangeMax = 5150,
        projectedMedianPrice = 4980,
        confidencePercent = 88,
        uncertaintySpreadInr = 200,
        uncertaintyLevel = ForecastUncertaintyLevel.MODERATE,
        primaryMarketDrivers = listOf(
            "Crush margins for solvent extraction plants in Malwa region are positive (+₹180/Q).",
            "Monsoon downpours delayed fresh arrivals from interior rural yards by 24 hours.",
            "Global soybean meal export parity remains supportive on NCDEX."
        ),
        riskFactors = listOf(
            "Sudden clearing of weather on weekend could trigger heavy Monday arrivals (+30,000 Q).",
            "High moisture content (>12%) in fresh harvest will invite mandi quality deductions of ₹50–100/Q."
        ),
        recommendationTitle = "HOLD & SELL IN TRANCHES",
        recommendationSummary = "Hold 60% of harvested stock for 5–7 days; target partial sales (40%) if Indore spot rate crosses ₹5,000/Q on clear trading days.",
        recommendationSummaryHi = "60% माल 5-7 दिन रोककर रखें। इंदौर मंडी का भाव ₹5,000/क्विंटल पार करने पर 40% माल बेचें।"
    ),
    
    // UI Metadata & Search
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val lastSyncTimestamp: String = "Today, 11:15 AM",
    val errorMessage: String? = null
)
