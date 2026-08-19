package com.example.features.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.BuyerOffer
import com.example.data.models.MandiPriceItem
import com.example.data.repository.FarmRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MarketUiState(
    val mandiPrices: List<MandiPriceItem> = emptyList(),
    val buyerOffers: List<BuyerOffer> = emptyList(),
    val selectedFilter: String = "All Crops",
    val searchMandiQuery: String = ""
)

class MarketViewModel(
    private val repository: FarmRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getMandiPrices(),
                repository.getBuyerOffers()
            ) { prices, buyers ->
                MarketUiState(
                    mandiPrices = prices,
                    buyerOffers = buyers
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }
}
