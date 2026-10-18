package com.alafreet.dokan.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alafreet.dokan.data.DokanRepository
import com.alafreet.dokan.data.dao.BestSellerRow
import com.alafreet.dokan.data.entities.Sale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

data class ReportsUiState(
    val todayRevenue: Double = 0.0,
    val todayTransactionCount: Int = 0,
    val bestSellers: List<BestSellerRow> = emptyList(),
    val recentSales: List<Sale> = emptyList()
)

class ReportsViewModel(private val repository: DokanRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        val (startOfDay, endOfDay) = todayRange()

        viewModelScope.launch {
            repository.getRevenueBetween(startOfDay, endOfDay).collectLatest { revenue ->
                _uiState.value = _uiState.value.copy(todayRevenue = revenue)
            }
        }
        viewModelScope.launch {
            repository.getTransactionCountBetween(startOfDay, endOfDay).collectLatest { count ->
                _uiState.value = _uiState.value.copy(todayTransactionCount = count)
            }
        }
        viewModelScope.launch {
            repository.getBestSellers(startOfDay, endOfDay).collectLatest { sellers ->
                _uiState.value = _uiState.value.copy(bestSellers = sellers)
            }
        }
        viewModelScope.launch {
            repository.getAllSales().collectLatest { sales ->
                _uiState.value = _uiState.value.copy(recentSales = sales.take(20))
            }
        }
    }

    private fun todayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        val end = start + (24 * 60 * 60 * 1000L) - 1
        return start to end
    }

    class Factory(private val repository: DokanRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ReportsViewModel(repository) as T
    }
}
