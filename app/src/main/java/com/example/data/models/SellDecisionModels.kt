package com.example.data.models

/**
 * AI Recommendation Type for crop monetization strategy.
 */
enum class DecisionActionType(val labelEn: String, val labelHi: String, val badgeColorTag: String) {
    SELL_NOW("SELL NOW", "तुरंत बेचें", "GREEN"),
    WAIT("WAIT / HOLD", "रुकें / होल्ड करें", "BLUE"),
    COMPARE("COMPARE TRANCHES", "आंशिक बिक्री करें", "GOLD")
}

/**
 * Sell Now financial breakdown.
 */
data class SellNowBreakdown(
    val mandiName: String = "Indore APMC (Laxmibai Nagar)",
    val currentMandiPricePerQtl: Int = 4850,
    val bestBuyerOfferPerQtl: Int? = 4950,
    val buyerName: String? = "Malwa Agro Solvents Ltd",
    val quantityQuintals: Double = 50.0,
    val transportationCostPerQtl: Int = 35,
    val mandiFeeAndHandlingPerQtl: Int = 15,
    val netRealizedPricePerQtl: Int = 4800, // Mandi: 4850 - 35 - 15 = 4800; or Buyer at Farmgate: 4950
    val grossEstimatedRevenue: Double = 242500.0, // 50 Q * 4850
    val totalDeductions: Double = 2500.0, // (35 + 15) * 50
    val netEstimatedRevenue: Double = 240000.0,
    val settlementSpeed: String = "Instant T+0 settlement via e-NAM / Direct Bank Transfer"
)

/**
 * Wait / Storage holding financial breakdown.
 */
data class WaitHoldingBreakdown(
    val holdDurationDays: Int = 14,
    val targetMandiName: String = "Indore APMC",
    val predictedPricePerQtl: Int = 5180,
    val predictedPriceRangeMin: Int = 4950,
    val predictedPriceRangeMax: Int = 5350,
    val storageType: String = "WDRA Certified Cold Storage / On-Farm Silo",
    val storageCostPerQtlPerMonth: Int = 22,
    val totalStorageCost: Double = 513.0, // (22 * 14 / 30) * 50 Q
    val moistureShrinkageLossPercent: Double = 1.0, // 1% natural weight loss
    val effectiveQuantityAfterLoss: Double = 49.5, // 50 Q - 0.5 Q
    val futureTransportationCostPerQtl: Int = 38,
    val totalFutureTransportCost: Double = 1881.0, // 38 * 49.5
    val capitalHoldingOpportunityCost: Double = 700.0, // Interest on working capital
    val grossEstimatedRevenue: Double = 256410.0, // 49.5 * 5180
    val totalHoldingExpenses: Double = 3094.0, // Storage + Transport + Capital
    val netEstimatedRevenue: Double = 253316.0,
    val netRevenueDeltaVsSellNow: Double = 13316.0, // +₹13,316 extra net profit
    val netGainPercent: Double = 5.55
)

/**
 * Comprehensive Sell vs Wait Decision Scenario.
 */
data class SellDecisionScenario(
    val cropId: String = "soybean",
    val cropName: String = "Soybean (JS-2034)",
    val cropVariety: String = "Yellow Grain High Oil Content",
    val quantityQuintals: Double = 50.0,
    
    // Sell Now vs Wait models
    val sellNow: SellNowBreakdown = SellNowBreakdown(),
    val wait: WaitHoldingBreakdown = WaitHoldingBreakdown(),
    
    // AI Strategic Synthesis
    val recommendation: DecisionActionType = DecisionActionType.WAIT,
    val confidencePercent: Int = 89,
    val whyRecommendationTitle: String = "Hold for 14 Days (Higher Net Return of +₹13,316)",
    val whyRecommendationReasons: List<String> = listOf(
        "Crush margins for solvent extraction plants in Malwa are surging (+₹180/Q).",
        "Recent heavy monsoon rains have curtailed arrivals from rural interiors by 35%.",
        "Storage cost (₹10.3/Q for 14 days) is vastly offset by projected price jump of +₹330/Q.",
        "Moisture shrinkage risk is minimal (1.0%) for mature dried batch."
    ),
    val riskFactors: List<String> = listOf(
        "Sudden bulk arrivals from Vidarbha could soften prices by ₹80–120/Q.",
        "Uncertified on-farm storage may risk pest/dampness; use WDRA registered warehouse."
    ),
    val recommendedTrancheStrategy: String = "Sell 30% (15 Quintals) immediately to lock working cashflow; hold 70% (35 Quintals) for peak auction rates at ₹5,150+ in 14 days.",
    val recommendedTrancheStrategyHi: String = "30% (15 क्विंटल) तुरंत बेचें ताकि नकद खर्च निकल सके; शेष 70% (35 क्विंटल) 14 दिन रोककर ₹5,150+ के भाव पर बेचें।"
)

/**
 * Farmer explicitly chosen trade execution intent.
 */
data class FarmerTradeExecutionIntent(
    val cropName: String,
    val quantityToSellQuintals: Double,
    val channelType: String, // "MANDI_SPOT_AUCTION", "VERIFIED_BUYER_PURCHASE", "WAREHOUSE_RECEIPT_FINANCING"
    val channelTargetName: String,
    val agreedPricePerQtl: Int,
    val transportArrangedBy: String, // "Buyer Farm-Gate Pickup", "Shared Truck Logistics", "Self Delivery"
    val grossPayout: Double,
    val netPayoutToAccount: Double,
    val farmerConfirmed: Boolean = false,
    val confirmationTimestamp: String? = null
)

/**
 * UI State for the Sell Now or Wait Decision feature.
 */
data class SellDecisionUiState(
    val selectedCropId: String = "soybean",
    val enteredQuantityQuintals: Double = 50.0,
    val customHoldDurationDays: Int = 14,
    val scenario: SellDecisionScenario = SellDecisionScenario(),
    val isEvaluating: Boolean = false,
    val activeTab: Int = 0, // 0 = Decision Dashboard, 1 = Deep Breakdown, 2 = AI Chat Advisor
    
    // Explicit Trade Execution Modal State
    val showTradeConfirmationDialog: Boolean = false,
    val pendingTradeIntent: FarmerTradeExecutionIntent? = null,
    val tradeSuccessMessage: String? = null,
    
    // Ask FarmSathi Quick Prompts
    val quickQuestions: List<String> = listOf(
        "Why is waiting better than selling today?",
        "What is the warehouse storage cost in Indore?",
        "Can I sell 40% now and store the rest?",
        "What if mandi arrivals surge next week?"
    ),
    val aiChatResponse: String? = null,
    val isAskingAi: Boolean = false
)
