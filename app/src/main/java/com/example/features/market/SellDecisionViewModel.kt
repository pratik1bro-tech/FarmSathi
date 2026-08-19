package com.example.features.market

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.voice.AndroidTextToSpeechService
import com.example.core.voice.VoiceLanguage
import com.example.data.models.*
import com.example.data.repository.FarmRepository
import com.example.data.service.SellDecisionEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class SellDecisionViewModel(
    application: Application,
    private val repository: FarmRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SellDecisionUiState())
    val uiState: StateFlow<SellDecisionUiState> = _uiState.asStateFlow()

    private val ttsService by lazy { AndroidTextToSpeechService(application.applicationContext) }
    private val timeFormat = SimpleDateFormat("h:mm a, d MMM yyyy", Locale.getDefault())

    init {
        recalculateScenario(
            cropId = _uiState.value.selectedCropId,
            qty = _uiState.value.enteredQuantityQuintals,
            holdDays = _uiState.value.customHoldDurationDays
        )
    }

    fun selectCrop(cropId: String) {
        _uiState.update { it.copy(selectedCropId = cropId) }
        recalculateScenario(
            cropId = cropId,
            qty = _uiState.value.enteredQuantityQuintals,
            holdDays = _uiState.value.customHoldDurationDays
        )
    }

    fun updateQuantity(qty: Double) {
        val safeQty = qty.coerceAtLeast(1.0)
        _uiState.update { it.copy(enteredQuantityQuintals = safeQty) }
        recalculateScenario(
            cropId = _uiState.value.selectedCropId,
            qty = safeQty,
            holdDays = _uiState.value.customHoldDurationDays
        )
    }

    fun updateHoldDuration(days: Int) {
        val safeDays = days.coerceIn(3, 90)
        _uiState.update { it.copy(customHoldDurationDays = safeDays) }
        recalculateScenario(
            cropId = _uiState.value.selectedCropId,
            qty = _uiState.value.enteredQuantityQuintals,
            holdDays = safeDays
        )
    }

    fun setActiveTab(tab: Int) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    private fun recalculateScenario(cropId: String, qty: Double, holdDays: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isEvaluating = true) }
            delay(150) // Micro-delay for UI polish
            val scenario = SellDecisionEngine.calculateScenario(cropId, qty, holdDays)
            _uiState.update {
                it.copy(
                    scenario = scenario,
                    isEvaluating = false
                )
            }
        }
    }

    fun askQuestion(question: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAskingAi = true, aiChatResponse = null) }
            delay(400) // Realistic AI synthesis
            val answer = SellDecisionEngine.answerFarmerQuestion(question, _uiState.value.scenario)
            _uiState.update {
                it.copy(
                    isAskingAi = false,
                    aiChatResponse = answer
                )
            }
        }
    }

    /**
     * Prepares trade confirmation intent.
     * STRICT SAFETY RULE: System never auto-sells. Requires explicit modal confirmation with checkbox & button.
     */
    fun initiateTradeIntent(channelType: String) {
        val scenario = _uiState.value.scenario
        val intent = when (channelType) {
            "BUYER" -> {
                val price = scenario.sellNow.bestBuyerOfferPerQtl ?: scenario.sellNow.currentMandiPricePerQtl
                val gross = scenario.quantityQuintals * price
                FarmerTradeExecutionIntent(
                    cropName = scenario.cropName,
                    quantityToSellQuintals = scenario.quantityQuintals,
                    channelType = "VERIFIED_BUYER_PURCHASE",
                    channelTargetName = scenario.sellNow.buyerName ?: "Verified Agro Buyer",
                    agreedPricePerQtl = price,
                    transportArrangedBy = "Buyer Farm-Gate Pickup (Free Freight)",
                    grossPayout = gross,
                    netPayoutToAccount = gross, // Zero transport/mandi fees for farmgate pickup
                    farmerConfirmed = false
                )
            }
            "MANDI" -> {
                val price = scenario.sellNow.currentMandiPricePerQtl
                FarmerTradeExecutionIntent(
                    cropName = scenario.cropName,
                    quantityToSellQuintals = scenario.quantityQuintals,
                    channelType = "MANDI_SPOT_AUCTION",
                    channelTargetName = scenario.sellNow.mandiName,
                    agreedPricePerQtl = price,
                    transportArrangedBy = "Shared Truck Delivery (₹${scenario.sellNow.transportationCostPerQtl}/Q)",
                    grossPayout = scenario.sellNow.grossEstimatedRevenue,
                    netPayoutToAccount = scenario.sellNow.netEstimatedRevenue,
                    farmerConfirmed = false
                )
            }
            else -> {
                // Warehouse Storage Lock
                FarmerTradeExecutionIntent(
                    cropName = scenario.cropName,
                    quantityToSellQuintals = scenario.quantityQuintals,
                    channelType = "WAREHOUSE_STORAGE_RECEIPT",
                    channelTargetName = scenario.wait.storageType,
                    agreedPricePerQtl = scenario.wait.predictedPricePerQtl,
                    transportArrangedBy = "Warehouse Logistics Inward (₹${scenario.wait.futureTransportationCostPerQtl}/Q)",
                    grossPayout = scenario.wait.grossEstimatedRevenue,
                    netPayoutToAccount = scenario.wait.netEstimatedRevenue,
                    farmerConfirmed = false
                )
            }
        }

        _uiState.update {
            it.copy(
                pendingTradeIntent = intent,
                showTradeConfirmationDialog = true,
                tradeSuccessMessage = null
            )
        }
    }

    fun confirmAndExecuteTrade() {
        val intent = _uiState.value.pendingTradeIntent ?: return
        val timestamp = timeFormat.format(Date())
        val confirmedIntent = intent.copy(
            farmerConfirmed = true,
            confirmationTimestamp = timestamp
        )

        _uiState.update {
            it.copy(
                pendingTradeIntent = confirmedIntent,
                showTradeConfirmationDialog = false,
                tradeSuccessMessage = "Trade Order #ORD-${(100000..999999).random()} Authorized & Confirmed by Farmer at $timestamp. Sent to ${intent.channelTargetName}."
            )
        }
    }

    fun dismissTradeDialog() {
        _uiState.update {
            it.copy(
                showTradeConfirmationDialog = false,
                pendingTradeIntent = null
            )
        }
    }

    fun clearTradeSuccessMessage() {
        _uiState.update { it.copy(tradeSuccessMessage = null) }
    }

    fun speakRecommendation(isHindi: Boolean) {
        val scenario = _uiState.value.scenario
        val text = if (isHindi) {
            "फॉर्मसाथी निर्णय सलाह: ${scenario.whyRecommendationTitle}। ${scenario.recommendedTrancheStrategyHi}। कृपया ध्यान दें, सभी मूल्य अनुमान हैं और कीमतें गारंटीकृत नहीं हैं।"
        } else {
            "FarmSathi Decision Advisory: ${scenario.whyRecommendationTitle}. ${scenario.recommendedTrancheStrategy}. Note that all price projections are probabilistic estimates and not guaranteed."
        }

        ttsService.speak(text = text, language = if (isHindi) VoiceLanguage.HINDI else VoiceLanguage.ENGLISH)
    }

    override fun onCleared() {
        super.onCleared()
        ttsService.release()
    }
}
