package com.alafreet.dokan.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alafreet.dokan.data.DokanRepository
import com.alafreet.dokan.data.entities.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class InventoryUiState(
    val allProducts: List<Product> = emptyList(),
    val lowStockProducts: List<Product> = emptyList()
)

class InventoryViewModel(private val repository: DokanRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getActiveProducts().collectLatest { products ->
                _uiState.value = _uiState.value.copy(allProducts = products)
            }
        }
        viewModelScope.launch {
            repository.getLowStockProducts().collectLatest { lowStock ->
                _uiState.value = _uiState.value.copy(lowStockProducts = lowStock)
            }
        }
    }

    fun adjustStock(productId: Long, delta: Int) {
        viewModelScope.launch { repository.adjustStock(productId, delta) }
    }

    class Factory(private val repository: DokanRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = InventoryViewModel(repository) as T
    }
}
