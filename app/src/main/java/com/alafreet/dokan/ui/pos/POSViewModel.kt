package com.alafreet.dokan.ui.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alafreet.dokan.data.CartLine
import com.alafreet.dokan.data.DokanRepository
import com.alafreet.dokan.data.entities.PaymentMethod
import com.alafreet.dokan.data.entities.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class POSUiState(
    val products: List<Product> = emptyList(),
    val cart: List<CartLine> = emptyList(),
    val searchQuery: String = "",
    val lastSaleSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val subtotal: Double get() = cart.sumOf { it.product.price * it.quantity }
    val discountAmount: Double get() = cart.sumOf { (it.product.price - it.product.finalPrice) * it.quantity }
    val taxAmount: Double get() = (subtotal - discountAmount) * DokanRepository.TAX_RATE
    val total: Double get() = subtotal - discountAmount + taxAmount
}

class POSViewModel(private val repository: DokanRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(POSUiState())
    val uiState: StateFlow<POSUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getActiveProducts().collectLatest { products ->
                _uiState.value = _uiState.value.copy(products = products)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        viewModelScope.launch {
            val results = if (query.isBlank()) {
                repository.getActiveProducts()
            } else {
                repository.searchProducts(query)
            }
            results.collectLatest { products ->
                _uiState.value = _uiState.value.copy(products = products)
            }
        }
    }

    fun addToCart(product: Product) {
        val currentCart = _uiState.value.cart.toMutableList()
        val existingIndex = currentCart.indexOfFirst { it.product.id == product.id }
        val currentQtyInCart = currentCart.getOrNull(existingIndex)?.quantity ?: 0

        if (currentQtyInCart + 1 > product.quantity) {
            _uiState.value = _uiState.value.copy(errorMessage = "الكمية المتاحة غير كافية من \"${product.name}\"")
            return
        }

        if (existingIndex >= 0) {
            currentCart[existingIndex] = currentCart[existingIndex].copy(quantity = currentQtyInCart + 1)
        } else {
            currentCart.add(CartLine(product, 1))
        }
        _uiState.value = _uiState.value.copy(cart = currentCart, errorMessage = null)
    }

    fun decreaseFromCart(product: Product) {
        val currentCart = _uiState.value.cart.toMutableList()
        val index = currentCart.indexOfFirst { it.product.id == product.id }
        if (index < 0) return

        val line = currentCart[index]
        if (line.quantity <= 1) {
            currentCart.removeAt(index)
        } else {
            currentCart[index] = line.copy(quantity = line.quantity - 1)
        }
        _uiState.value = _uiState.value.copy(cart = currentCart)
    }

    fun removeFromCart(product: Product) {
        val currentCart = _uiState.value.cart.filterNot { it.product.id == product.id }
        _uiState.value = _uiState.value.copy(cart = currentCart)
    }

    fun clearCart() {
        _uiState.value = _uiState.value.copy(cart = emptyList())
    }

    fun checkout(paymentMethod: PaymentMethod) {
        val cart = _uiState.value.cart
        if (cart.isEmpty()) return

        viewModelScope.launch {
            runCatching {
                repository.completeSale(cart, paymentMethod)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    cart = emptyList(),
                    lastSaleSuccess = true,
                    errorMessage = null
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = "تعذر إتمام عملية البيع: ${e.message}"
                )
            }
        }
    }

    fun consumeSaleSuccessFlag() {
        _uiState.value = _uiState.value.copy(lastSaleSuccess = false)
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    class Factory(private val repository: DokanRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return POSViewModel(repository) as T
        }
    }
}
