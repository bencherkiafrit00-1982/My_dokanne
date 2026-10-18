package com.alafreet.dokan.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alafreet.dokan.data.DokanRepository
import com.alafreet.dokan.data.entities.Category
import com.alafreet.dokan.data.entities.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AdminUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList()
)

class AdminViewModel(private val repository: DokanRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllProducts().collectLatest { products ->
                _uiState.value = _uiState.value.copy(products = products)
            }
        }
        viewModelScope.launch {
            repository.getCategories().collectLatest { categories ->
                _uiState.value = _uiState.value.copy(categories = categories)
            }
        }
    }

    fun saveProduct(product: Product) {
        viewModelScope.launch {
            if (product.id == 0L) repository.addProduct(product)
            else repository.updateProduct(product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch { repository.deleteProduct(product) }
    }

    fun addCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.addCategory(Category(name = name.trim())) }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch { repository.deleteCategory(category) }
    }

    class Factory(private val repository: DokanRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AdminViewModel(repository) as T
    }
}
